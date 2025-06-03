package com.markesiano.serviceprovider.interfaces;

import com.markesiano.serviceprovider.support.ServiceDescriptor;

public interface ServiceLifetimeStrategy {
    <T> T getInstance(ServiceProvider provider, ServiceDescriptor descriptor, String key);
}