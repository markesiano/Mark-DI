package com.markesiano.serviceprovider.support;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeReference<T> {
    private final Type type;
    
    protected TypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType parameterizedType) {
            this.type = parameterizedType.getActualTypeArguments()[0];
        } else {
            throw new IllegalArgumentException("TypeReference must be parameterized");
        }

    }
    public Type getType() {
        return type;
    }
    @SuppressWarnings("unchecked")
    public T cast(Object obj) {
        return (T) obj;
    }

}
