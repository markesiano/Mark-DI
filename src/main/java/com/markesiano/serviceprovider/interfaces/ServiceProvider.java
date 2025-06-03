package com.markesiano.serviceprovider.interfaces;

import java.util.Map;

import com.markesiano.serviceprovider.constants.ServiceLifetime;
import com.markesiano.serviceprovider.support.ServiceDescriptor;
import com.markesiano.serviceprovider.support.TypeReference;


/**
 * A dependency injection container that manages service registration and resolution.
 * Supports both transient and singleton lifetimes, multiple implementations of the same interface,
 * and type-safe resolution of generic types.
 */

public interface ServiceProvider {

    // Add configure method
    void configure(Runnable configuration);

    /**
     * Registers a service implementation with a specific name.
     *
     * @param name            The name to identify this implementation (use empty string for default).
     * @param interfaceType   The interface type to register.
     * @param implementationType The concrete implementation type.
     * @param lifetime        The lifetime of the service (TRANSIENT or SINGLETON).
     * @param <TInterface>    The interface type.
     * @param <TImplementation> The implementation type, must extend TInterface.
     */
    <TInterface, TImplementation extends TInterface> void register(
        String name, Class<TInterface> interfaceType, Class<TImplementation> implementationType, ServiceLifetime lifetime);

    /**
     * Registers a service implementation with the default name (empty string).
     *
     * @param interfaceType   The interface type to register.
     * @param implementationType The concrete implementation type.
     * @param lifetime        The lifetime of the service (TRANSIENT or SINGLETON).
     * @param <TInterface>    The interface type.
     * @param <TImplementation> The implementation type, must extend TInterface.
     */
    <TInterface, TImplementation extends TInterface> void register(
        Class<TInterface> interfaceType, Class<TImplementation> implementationType, ServiceLifetime lifetime);

    /**
     * Registers a service using a factory with the default name (empty string).
     *
     * @param interfaceType The interface type to register.
     * @param factory       A factory to create instances of the service.
     * @param lifetime      The lifetime of the service (TRANSIENT or SINGLETON).
     * @param <TInterface>  The interface type.
     */
    <TInterface> void register(
        Class<TInterface> interfaceType, SupplierWithProvider<TInterface> factory, ServiceLifetime lifetime);

    /**
     * Registers a service using a factory with a specific name.
     *
     * @param name          The name to identify this implementation (use empty string for default).
     * @param interfaceType The interface type to register.
     * @param factory       A factory to create instances of the service.
     * @param lifetime      The lifetime of the service (TRANSIENT or SINGLETON).
     * @param <TInterface>  The interface type.
     */
    <TInterface> void register(
        String name, Class<TInterface> interfaceType, SupplierWithProvider<TInterface> factory, ServiceLifetime lifetime);

    /**
     * Resolves a service by its interface type and name.
     *
     * @param name         The name of the implementation (use empty string for default).
     * @param iface        The interface type to resolve.
     * @param <TInterface> The interface type.
     * @return An instance of the service.
     * @throws RuntimeException if the service is not found.
     */
    <TInterface> TInterface getService(String name, Class<TInterface> iface);

    /**
     * Resolves a service by its interface type with the default name (empty string).
     *
     * @param iface        The interface type to resolve.
     * @param <TInterface> The interface type.
     * @return An instance of the service.
     * @throws RuntimeException if the service is not found.
     */
    <TInterface> TInterface getService(Class<TInterface> iface);

    /**
     * Resolves a service by its type and name in a type-safe manner.
     *
     * @param name    The name of the implementation (use empty string for default).
     * @param typeRef A TypeReference capturing the full generic type.
     * @param <T>     The type to resolve.
     * @return An instance of the service.
     * @throws RuntimeException if the service is not found.
     */   
    <T> T getService(String name, TypeReference<T> typeRef);

    /**
     * Resolves a service by its type with the default name (empty string) in a type-safe manner.
     *
     * @param typeRef A TypeReference capturing the full generic type.
     * @param <T>     The type to resolve.
     * @return An instance of the service.
     * @throws RuntimeException if the service is not found.
     */
    <T> T getService(TypeReference<T> typeRef);


    /**
     * Returns a map of all registered services for debugging purposes.
     *
     * @return A map of service keys to their descriptors.
     */
    Map<String, ServiceDescriptor> getServices();

    /*  
     * Convenience methods for registering services with default names and lifetimes.
     * These methods allow you to register services without specifying a name or lifetime.
     * You can use these methods to register services with the default name (empty string)
     */

    default <TInterface, TImplementation extends TInterface> void registerTransient(
        Class<TInterface> interfaceType, Class<TImplementation> implementationType) {
        register(interfaceType, implementationType, ServiceLifetime.TRANSIENT);
    }

    default <TInterface, TImplementation extends TInterface> void registerSingleton(
        Class<TInterface> interfaceType, Class<TImplementation> implementationType) {
        register(interfaceType, implementationType, ServiceLifetime.SINGLETON);
    }

    default <TInterface> void registerTransient(
        Class<TInterface> interfaceType, SupplierWithProvider<TInterface> factory) {
        register(interfaceType, factory, ServiceLifetime.TRANSIENT);
    }

    default <TInterface> void registerSingleton(
        Class<TInterface> interfaceType, SupplierWithProvider<TInterface> factory) {
        register(interfaceType, factory, ServiceLifetime.SINGLETON);
    }

        default <TInterface, TImplementation extends TInterface> void registerTransient(
        String name, Class<TInterface> interfaceType, Class<TImplementation> implementationType) {
        register(name, interfaceType, implementationType, ServiceLifetime.TRANSIENT);
    }

    default <TInterface, TImplementation extends TInterface> void registerSingleton(
        String name, Class<TInterface> interfaceType, Class<TImplementation> implementationType) {
        register(name, interfaceType, implementationType, ServiceLifetime.SINGLETON);
    }

    default <TInterface> void registerTransient(
        String name, Class<TInterface> interfaceType, SupplierWithProvider<TInterface> factory) {
        register(name, interfaceType, factory, ServiceLifetime.TRANSIENT);
    }

    default <TInterface> void registerSingleton(
        String name, Class<TInterface> interfaceType, SupplierWithProvider<TInterface> factory) {
        register(name, interfaceType, factory, ServiceLifetime.SINGLETON);
    }
}

