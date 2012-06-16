package com.jmatio.types;

import java.nio.ByteBuffer;

/**
 * Class represents UInt8 (byte) array (matrix)
 * 
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MLUInt8 extends MLNumericArray<Byte>
{

    /**
     * Normally this constructor is used only by MatFileReader and MatFileWriter
     * 
     * @param name - array name
     * @param dims - array dimensions
     * @param type - array type: here <code>mxDOUBLE_CLASS</code>
     * @param attributes - array flags
     */
    public MLUInt8( String name, int[] dims, int type, int attributes )
    {
        super( name, dims, type, attributes );
    }
    /**
     * Create a <code>{@link MLUInt8}</code> array with given name,
     * and dimensions.
     * 
     * @param name - array name
     * @param dims - array dimensions
     */
    public MLUInt8(String name, int[] dims)
    {
        super(name, dims, MLArray.mxUINT8_CLASS, 0);
    }
    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a 2D real matrix from a one-dimensional packed array
     * 
     * @param name - array name
     * @param vals - One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param m - Number of rows
     */
    public MLUInt8(String name, Byte[] vals, int m )
    {
        super(name, MLArray.mxUINT8_CLASS, vals, m );
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
    public MLUInt8( String name, byte[][] vals )
    {
        this( name, byte2DToByte(vals), vals.length );
    }
    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a matrix from a one-dimensional packed array
     * 
     * @param name - array name
     * @param vals - One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param m - Number of rows
     */
    public MLUInt8(String name, byte[] vals, int m)
    {
        this(name, castToByte( vals ), m );
    }
    /* (non-Javadoc)
     * @see com.jmatio.types.GenericArrayCreator#createArray(int, int)
     */
    public Byte[] createArray(int m, int n)
    {
        return new Byte[m*n];
    }
    /**
     * Gets two-dimensional real array.
     * 
     * @return - 2D real array
     */
    public byte[][] getArray()
    {
        byte[][] result = new byte[getM()][];
        
        for ( int m = 0; m < getM(); m++ )
        {
           result[m] = new byte[ getN() ];

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
     * @param - source <code>Byte[]</code>
     * @return - result <code>byte[]</code>
     */
    private static Byte[] castToByte( byte[] d )
    {
        Byte[] dest = new Byte[d.length];
        for ( int i = 0; i < d.length; i++ )
        {
            dest[i] = (byte)d[i];
        }
        return dest;
    }
    /**
     * Converts byte[][] to Byte[]
     * 
     * @param dd
     * @return
     */
    private static Byte[] byte2DToByte ( byte[][] dd )
    {
        Byte[] d = new Byte[ dd.length*dd[0].length ];
        for ( int n = 0; n < dd[0].length; n++ )
        {
            for ( int m = 0; m < dd.length; m++ )
            {
                d[ m+n*dd.length ] = dd[m][n]; 
            }
        }
        return d;
    }
    public Byte buldFromBytes(byte[] bytes)
    {
        if ( bytes.length != getBytesAllocated() )
        {
            throw new IllegalArgumentException( 
                        "To build from byte array I need array of size: " 
                                + getBytesAllocated() );
        }
        return bytes[0];
    }
    public byte[] getByteArray(Byte value)
    {
        return new byte[] { value };
    }
    public int getBytesAllocated()
    {
        return Byte.SIZE >> 3;
    }
    
    public Class<Byte> getStorageClazz()
    {
        return Byte.class;
    }
    
    /**
     * Override to accelerate the performance
     * 
     * @see com.jmatio.types.MLNumericArray#_get(java.nio.ByteBuffer, int)
     */
    @Override
    protected Byte _get( ByteBuffer buffer, int index )
    {
        return buffer.get( index );
    }

}
