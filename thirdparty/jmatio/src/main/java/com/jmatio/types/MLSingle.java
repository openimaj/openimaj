package com.jmatio.types;

import java.nio.ByteBuffer;

public class MLSingle extends MLNumericArray<Float>
{
    
    public MLSingle(String name, Float[] vals, int m)
    {
        super(name, MLArray.mxSINGLE_CLASS, vals, m);
    }

    public MLSingle(String name, int[] dims, int type, int attributes)
    {
        super(name, dims, type, attributes);
    }

    public Float[] createArray(int m, int n)
    {
        return new Float[m*n];
    }

    public Float buldFromBytes(byte[] bytes)
    {
        if ( bytes.length != getBytesAllocated() )
        {
            throw new IllegalArgumentException( 
                        "To build from byte array I need array of size: " 
                                + getBytesAllocated() );
        }
        return ByteBuffer.wrap( bytes ).getFloat();
    }

    public byte[] getByteArray(Float value)
    {
        int byteAllocated = getBytesAllocated();
        ByteBuffer buff = ByteBuffer.allocate( byteAllocated );
        buff.putFloat( value );
        return buff.array();
    }

    public int getBytesAllocated()
    {
        return Float.SIZE >> 3;
    }

    public Class<?> getStorageClazz()
    {
        return Float.class;
    }

}
