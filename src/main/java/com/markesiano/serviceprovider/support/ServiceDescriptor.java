package com.markesiano.serviceprovider.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.markesiano.serviceprovider.constants.ServiceLifetime;
import com.markesiano.serviceprovider.interfaces.ServiceLifetimeStrategy;
import com.markesiano.serviceprovider.interfaces.ServiceProvider;
import com.markesiano.serviceprovider.interfaces.SupplierWithProvider;

public class ServiceDescriptor {

    private static final Logger logger = Logger.getLogger(ServiceDescriptor.class.getName());

    private final Class<?> implementationType;
    private final SupplierWithProvider<?> factory;
    private final ServiceLifetimeStrategy lifetime;

    // Constructor cached
    private final Constructor<?> constructor;



    public ServiceDescriptor(Class<?> implementationType, ServiceLifetimeStrategy lifetime) {
        this.implementationType = implementationType;
        this.factory = null;
        this.lifetime = lifetime;

        if (implementationType != null) {
            Constructor<?>[] constructors = implementationType.getDeclaredConstructors();
            if (constructors.length == 0) {
                throw new IllegalArgumentException("No constructors found for " + implementationType.getName());
            }
            if (constructors.length > 1) {
                logger.log(Level.WARNING, "Multiple constructors found for {0}. Using the first one.", implementationType.getName());
            }
            this.constructor = constructors[0];
            this.constructor.setAccessible(true);
        } else {
            this.constructor = null;
        }
    }

    public ServiceDescriptor(SupplierWithProvider<?> factory, ServiceLifetime lifetime) {
        this.implementationType = null;
        this.factory = factory;
        this.lifetime = lifetime;
        this.constructor = null;
    }

    
    public Object getInstance(ServiceProvider provider) {
        if (factory != null) {
            try {
                return factory.get(provider);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error creating instance using factory: {0}", e.getMessage());
                throw new RuntimeException("Error creating instance using factory", e);
            }
        } else {
            try {
                Parameter[] parameters = constructor.getParameters();
                Object[] args = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    args[i] = provider.getService("", parameters[i].getType());
                }
                return constructor.newInstance(args);
            } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException e) {
                logger.log(Level.SEVERE, "Error creating instance of {0}: {1}", new Object[]{implementationType.getName(), e.getMessage()});
                throw new RuntimeException("Error creating instance of " + implementationType.getName(), e);
            }
        }
    }



    public Object getRawInstance(ServiceProvider provider) {
        if (factory != null) {
            try {
                return factory.get(provider);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error invoking factory for service: " + e.getMessage(), e);
                throw new RuntimeException("Error invoking factory for service", e);
            }
        } else {
            try {
                Parameter[] parameters = constructor.getParameters();
                Object[] args = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    args[i] = provider.getService("", parameters[i].getType());
                }
                return constructor.newInstance(args);
            } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException e) {
                logger.log(Level.SEVERE, 
                    "Error creating instance of " + implementationType.getName() + ": " + e.getMessage(), 
                    e);
                throw new RuntimeException("Error creating instance of " + implementationType.getName(), e);
            }
        }
    }

    public Object getInstance(ServiceProvider provider, String key) {
        return lifetime.getInstance(provider, this, key);
    }

    public ServiceLifetimeStrategy getLifetime() {
        return lifetime;
    }

    public Class<?> getImplementationType() {
        return implementationType;
    }
    @Override
    public String toString() {
        if (factory != null) {
            return "Factory<" + factory.getClass().getName() + ">";
        } else {
            return "Class<" + implementationType.getName() + ">";
        }
    }
}
