package com.jmatio.types;


public interface ByteStorageSupport<T extends Number>
{
    int getBytesAllocated();
    T buldFromBytes( byte[] bytes );
    byte[] getByteArray ( T value );
    Class<?> getStorageClazz();

}
