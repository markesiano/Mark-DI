package com.markesiano.serviceprovider;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.markesiano.serviceprovider.constants.ServiceLifetime;
import com.markesiano.serviceprovider.interfaces.ServiceProvider;
import com.markesiano.serviceprovider.interfaces.SupplierWithProvider;
import com.markesiano.serviceprovider.support.ServiceDescriptor;
import com.markesiano.serviceprovider.support.TypeReference;

import java.lang.reflect.Type;


public class Provider implements ServiceProvider{
    private final static Logger logger = Logger.getLogger(Provider.class.getName());

    private final Map<String, ServiceDescriptor> services = new ConcurrentHashMap<>();
    private final ThreadLocal<Set<String>> resolutionStack = ThreadLocal.withInitial(HashSet::new);
    private final ThreadLocal<Boolean> isConfiguring = ThreadLocal.withInitial(() -> false);

    @Override
    public void configure(Runnable configuration) {
        if (isConfiguring.get()) {
            logger.log(Level.SEVERE, "Reentrant configuration detected");
            throw new IllegalStateException("Cannot configure services recursively");
        }
        isConfiguring.set(true);
        try {
            configuration.run();
        } finally {
            isConfiguring.set(false);
        }
    }


    private String generateKey(String name, Class<?> interfaceType) {
        String baseKey = interfaceType.getSimpleName();
        return name.isEmpty() ? baseKey : baseKey + "#" + name;

    }


    // Basic implementation without name
    @Override
    public <TInterface, TImplementation extends TInterface> void register(
            Class<TInterface> interfaceType, 
            Class<TImplementation> implementationType,
            ServiceLifetime serviceLifetime) {
        register("",interfaceType, implementationType, serviceLifetime);
    }
    
    // Multiples implementations of the same interface
    @Override
    public <TInterface, TImplementation extends TInterface> void register(
            String name,
            Class<TInterface> interfaceType, 
            Class<TImplementation> implementationType, 
            ServiceLifetime serviceLifetime) {
        if (isConfiguring.get()) {
            String key = generateKey(name, interfaceType);
            if (services.containsKey(key)) {
                logger.log(Level.WARNING, "Duplicate registration for key: {0}. Overwriting existing service.", key);
            }
            if (!interfaceType.isAssignableFrom(implementationType)) {
                throw new IllegalArgumentException(
                        "Implementation " + implementationType.getName() + " does not implement interface " + interfaceType.getName());
            }
            services.put(key, new ServiceDescriptor(implementationType, serviceLifetime));
        } else {
            logger.log(Level.SEVERE, "Registration outside of configuration block is not allowed");
            throw new IllegalStateException("Registration must occur within a configuration block");
        }
    }



    // Registering with a factory
    @Override
    public <TInterface> void register(
            Class<TInterface> interfaceType, 
            SupplierWithProvider<TInterface> factory, 
            ServiceLifetime lifetime) {
        register("", interfaceType, factory, lifetime);
    }
    @Override
    public <TInterface> void register(
            String name, 
            Class<TInterface> interfaceType, 
            SupplierWithProvider<TInterface> factory, 
            ServiceLifetime lifetime) {
        if (isConfiguring.get()) {
            String key = generateKey(name, interfaceType);
            if (services.containsKey(key)) {
                logger.log(Level.WARNING, "Duplicate registration for key: {0}. Overwriting existing service.", key);
            }
            if (factory == null) {
                logger.log(Level.WARNING, "Factory cannot be null for key: {0}", key);
                throw new IllegalArgumentException("Factory cannot be null");
            }
            services.put(key, new ServiceDescriptor(factory, lifetime));
        } else {
            logger.log(Level.SEVERE, "Registration outside of configuration block is not allowed");
            throw new IllegalStateException("Registration must occur within a configuration block");
        }
    }


    @Override
    public <TInterface> TInterface getService(Class<TInterface> iface) {
        return getService("", iface);
    }
    @Override
    public <TInterface> TInterface getService(String name, Class<TInterface> iface) {
        String key = generateKey(name, iface);
        Set<String> stack = resolutionStack.get();
        if (stack.contains(key)) {
            logger.log(Level.SEVERE, "Cyclic dependency detected for key: {0}", key);
            throw new RuntimeException("Cyclic dependency detected: " + key);
        }
        stack.add(key);
        try {
            ServiceDescriptor descriptor = services.get(key);
            if (descriptor == null) {
                logger.log(Level.WARNING, "Service not found: {0}", key);
                throw new RuntimeException("Service not found: " + key);
            }
            Object instance = descriptor.getInstance(this, key);
            if (instance == null) {
                logger.log(Level.WARNING, "Null instance returned for key: {0}", key);
                throw new RuntimeException("Null instance returned for: " + key);
            }
            return iface.cast(instance);
        } finally {
            stack.remove(key);
        }
    }

    @Override
    public <T> T getService(TypeReference<T> typeRef) {
        return getService("", typeRef);
    }    
    @Override
    public <T> T getService(String name, TypeReference<T> typeRef) {
        // Extract the raw type from the TypeReference
        Type type = typeRef.getType();
        if (type == null) {
            throw new IllegalArgumentException("TypeReference must represent a Class or ParameterizedType");
        }
        // Check if the type is a ParameterizedType or Class
        Class<?> rawType;
        if (type instanceof ParameterizedType parameterizedType) {
            rawType = (Class<?>) parameterizedType.getRawType();
        } else if (type instanceof Class) {
            rawType = (Class<?>) type;
        } else {
            throw new IllegalArgumentException("TypeReference must represent a Class or ParameterizedType");
        }

        // Use the raw type to look up the service
        String key = generateKey(name, rawType);
        ServiceDescriptor descriptor = services.get(key);
        if (descriptor == null) {
            logger.log(Level.WARNING, "Service not found: {0}", key);
            throw new RuntimeException("Service not found: " + key);
        }
        Object instance = descriptor.getInstance(this, key);
        return typeRef.cast(instance);

    }


    @Override
    public Map<String, ServiceDescriptor> getServices() {
        return services;
    }
}
