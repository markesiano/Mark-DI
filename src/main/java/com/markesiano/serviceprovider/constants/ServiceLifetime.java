package com.markesiano.serviceprovider.constants;

import java.util.HashMap;
import java.util.Map;

import com.markesiano.serviceprovider.interfaces.ServiceLifetimeStrategy;
import com.markesiano.serviceprovider.interfaces.ServiceProvider;
import com.markesiano.serviceprovider.support.ServiceDescriptor;

public enum ServiceLifetime implements ServiceLifetimeStrategy {
    TRANSIENT {
        @Override
        @SuppressWarnings("unchecked")
        public <T> T getInstance(ServiceProvider provider, ServiceDescriptor descriptor, String key) {
            return (T) descriptor.getInstance(provider);
        }
    },
    SINGLETON {
        private final Map<String, Object> singletons = new HashMap<>();
        @Override
        @SuppressWarnings({"unchecked", "DoubleCheckedLocking"})
        
        public <T> T getInstance(ServiceProvider provider, ServiceDescriptor descriptor, String key) {
            Object instance = singletons.get(key);
            if (instance == null) {
                synchronized (singletons) {
                    instance = singletons.get(key);
                    if (instance == null) {
                        instance = descriptor.getRawInstance(provider);
                        singletons.put(key, instance);
                    }
                }
            }
            return (T) instance;
        }

    };
}
