package com.jmatio.types;

import java.nio.ByteBuffer;

/**
 * Class represents Int64 (long) array (matrix)
 * 
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MLInt64 extends MLNumericArray<Long>
{

    /**
     * Normally this constructor is used only by MatFileReader and MatFileWriter
     * 
     * @param name - array name
     * @param dims - array dimensions
     * @param type - array type: here <code>mxDOUBLE_CLASS</code>
     * @param attributes - array flags
     */
    public MLInt64( String name, int[] dims, int type, int attributes )
    {
        super( name, dims, type, attributes );
    }
    /**
     * Create a <code>{@link MLInt64}</code> array with given name,
     * and dimensions.
     * 
     * @param name - array name
     * @param dims - array dimensions
     */
    public MLInt64(String name, int[] dims)
    {
        super(name, dims, MLArray.mxINT64_CLASS, 0);
    }
    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a 2D real matrix from a one-dimensional packed array
     * 
     * @param name - array name
     * @param vals - One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param m - Number of rows
     */
    public MLInt64(String name, Long[] vals, int m )
    {
        super(name, MLArray.mxINT64_CLASS, vals, m );
    }
    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a 2D real matrix from <code>byte[][]</code>
     * 
     * Note: array is converted to Byte[]
     * 
     * @param name - array name
     * @param vals - two-dimensional array of values
     */
    public MLInt64( String name, long[][] vals )
    {
        this( name, long2DToLong(vals), vals.length );
    }
    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a matrix from a one-dimensional packed array
     * 
     * @param name - array name
     * @param vals - One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param m - Number of rows
     */
    public MLInt64(String name, long[] vals, int m)
    {
        this(name, castToLong( vals ), m );
    }
    /* (non-Javadoc)
     * @see com.jmatio.types.GenericArrayCreator#createArray(int, int)
     */
    public Long[] createArray(int m, int n)
    {
        return new Long[m*n];
    }
    /**
     * Gets two-dimensional real array.
     * 
     * @return - 2D real array
     */
    public long[][] getArray()
    {
        long[][] result = new long[getM()][];
        
        for ( int m = 0; m < getM(); m++ )
        {
           result[m] = new long[ getN() ];

           for ( int n = 0; n < getN(); n++ )
           {               
               result[m][n] = getReal(m,n);
           }
        }
        return result;
    }
    /**
     * Casts <code>Double[]</code> to <code>byte[]</code>
     * 
     * @param - source <code>Long[]</code>
     * @return - result <code>long[]</code>
     */
    private static Long[] castToLong( long[] d )
    {
        Long[] dest = new Long[d.length];
        for ( int i = 0; i < d.length; i++ )
        {
            dest[i] = (long)d[i];
        }
        return dest;
    }
    /**
     * Converts byte[][] to Long[]
     * 
     * @param dd
     * @return
     */
    private static Long[] long2DToLong ( long[][] dd )
    {
        Long[] d = new Long[ dd.length*dd[0].length ];
        for ( int n = 0; n < dd[0].length; n++ )
        {
            for ( int m = 0; m < dd.length; m++ )
            {
                d[ m+n*dd.length ] = dd[m][n]; 
            }
        }
        return d;
    }
    public Long buldFromBytes(byte[] bytes)
    {
        if ( bytes.length != getBytesAllocated() )
        {
            throw new IllegalArgumentException( 
                        "To build from byte array I need array of size: " 
                                + getBytesAllocated() );
        }
        return ByteBuffer.wrap( bytes ).getLong();
    }
    public int getBytesAllocated()
    {
        return Long.SIZE >> 3;
    }
    
    public Class<Long> getStorageClazz()
    {
        return Long.class;
    }
    public byte[] getByteArray(Long value)
    {
        int byteAllocated = getBytesAllocated();
        ByteBuffer buff = ByteBuffer.allocate( byteAllocated );
        buff.putLong( value );
        return buff.array();
    }
    

}
