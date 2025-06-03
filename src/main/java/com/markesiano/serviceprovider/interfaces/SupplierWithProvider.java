package com.markesiano.serviceprovider.interfaces;

@FunctionalInterface
public interface SupplierWithProvider<T> {
    T get(ServiceProvider provider);
}
