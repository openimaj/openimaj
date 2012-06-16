package com.jmatio.types;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Abstract class for numeric arrays.
 * 
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 *
 * @param <T>
 */
/**
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 *
 * @param <T>
 */
public abstract class MLNumericArray<T extends Number> extends MLArray 
                                                       implements GenericArrayCreator<T>,
                                                                  ByteStorageSupport<T>
{
    private ByteBuffer real;
    private ByteBuffer imaginary;
    /** The buffer for creating Number from bytes */
    private byte[] bytes;
    
    /**
     * Normally this constructor is used only by MatFileReader and MatFileWriter
     * 
     * @param name - array name
     * @param dims - array dimensions
     * @param type - array type
     * @param attributes - array flags
     */
    public MLNumericArray(String name, int[] dims, int type, int attributes)
    {
        super(name, dims, type, attributes);
        allocate();
        
    }
    
    protected void allocate( )
    {
        real = ByteBuffer.allocate( getSize()*getBytesAllocated());
        if ( isComplex() )
        {
            imaginary = ByteBuffer.allocate( getSize()*getBytesAllocated());
        }
        bytes = new byte[ getBytesAllocated() ];
    }
    
    
    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a 2D real matrix from a one-dimensional packed array
     * 
     * @param name - array name
     * @param type - array type
     * @param vals - One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param m - Number of rows
     */
    public MLNumericArray(String name, int type, T[] vals, int m )
    {
        this(name, new int[] {  m, vals.length/m }, type, 0);
        //fill the array
        for ( int i = 0; i < vals.length; i++ )
        {
            set( vals[i], i );
        }
    }
    /**
     * Gets single real array element of A(m,n).
     * 
     * @param m - row index
     * @param n - column index
     * @return - array element
     */
    public T getReal(int m, int n)
    {
        return getReal( getIndex(m,n) );
    }
    
    /**
     * @param index
     * @return
     */
    public T getReal( int index )
    {
        return _get(real, index);
    }
    
    /**
     * Sets single real array element.
     * 
     * @param value - element value
     * @param m - row index
     * @param n - column index
     */
    public void setReal(T value, int m, int n)
    {
        setReal( value, getIndex(m,n) );
    }
    /**
     * Sets single real array element.
     * 
     * @param value - element value
     * @param index - column-packed vector index
     */
    public void setReal(T value, int index)
    {
        _set( real, value, index );
    }
    /**
     * Sets real part of matrix
     * 
     * @param vector - column-packed vector of elements
     */
    public void setReal( T[] vector )
    {
        if ( vector.length != getSize() )
        {
            throw new IllegalArgumentException("Matrix dimensions do not match. " + getSize() + " not " + vector.length);
        }
        System.arraycopy(vector, 0, real, 0, vector.length);
    }
    /**
     * Sets single imaginary array element.
     * 
     * @param value - element value
     * @param m - row index
     * @param n - column index
     */
    public void setImaginary(T value, int m, int n)
    {
        setImaginary( value, getIndex(m,n) );
    }
    /**
     * Sets single real array element.
     * 
     * @param value - element value
     * @param index - column-packed vector index
     */
    public void setImaginary(T value, int index)
    {
        if ( isComplex() )
        {
            _set(imaginary, value, index);
        }
    }
    /**
     * Gets single imaginary array element of A(m,n).
     * 
     * @param m - row index
     * @param n - column index
     * @return - array element
     */
    public T getImaginary(int m, int n)
    {
        return getImaginary( getIndex(m, n) );
    }
    /**
     * @param index
     * @return
     */
    public T getImaginary( int index )
    {
        return _get( imaginary, index );
    }
    
    /**
     * Exports column-packed vector of real elements
     * 
     * @return - column-packed vector of real elements
     */
//    public T[] exportReal()
//    {
//        return real.clone();
//    }
    /**
     * Exports column-packed vector of imaginary elements
     * 
     * @return - column-packed vector of imaginary elements
     */
//    public T[] exportImaginary()
//    {
//        return imaginary.clone();
//    }
    /**
     * Does the same as <code>setReal</code>.
     * 
     * @param value - element value
     * @param m - row index
     * @param n - column index
     */
    public void set(T value, int m, int n)
    {
        if ( isComplex() )
        {
            throw new IllegalStateException("Cannot use this method for Complex matrices");
        }
        setReal(value, m, n);
    }
    /**
     * Does the same as <code>setReal</code>.
     * 
     * @param value - element value
     * @param index - column-packed vector index
     */
    public void set(T value, int index)
    {
        if ( isComplex() )
        {
            throw new IllegalStateException("Cannot use this method for Complex matrices");
        }
        setReal(value, index);
    }
    /**
     * Does the same as <code>getReal</code>.
     * 
     * @param m - row index
     * @param n - column index
     * @return - array element
     */
    public T get( int m, int n )
    {
        if ( isComplex() )
        {
            throw new IllegalStateException("Cannot use this method for Complex matrices");
        }
        return getReal(m, n);
    }
    /**
     * @param index
     * @return
     */
    public T get ( int index )
    {
        if ( isComplex() )
        {
            throw new IllegalStateException("Cannot use this method for Complex matrices");
        }
        return _get( real, index );
    }
    /**
     * @param vector
     */
    public void set(T[] vector)
    {
        if ( isComplex() )
        {
            throw new IllegalStateException("Cannot use this method for Complex matrices");
        }
        setReal(vector);
    }
    private int getByteOffset( int index )
    {
        return index*getBytesAllocated();
    }
    
    protected T _get( ByteBuffer buffer, int index )
    {
        buffer.position( getByteOffset(index) );
        buffer.get( bytes, 0, bytes.length );
        return buldFromBytes( bytes );
    }
    
    protected void _set( ByteBuffer buffer, T value, int index )
    {
        buffer.position( getByteOffset(index) );
        buffer.put( getByteArray( value ) );
    }
    
    public void putImaginaryByteBuffer( ByteBuffer buff )
    {
        if ( !isComplex() )
        {
            throw new RuntimeException("Array is not complex");
        }
        imaginary.rewind();
        imaginary.put( buff );
    }
    
    public ByteBuffer getImaginaryByteBuffer()
    {
        return imaginary;
    }
    
    public void putRealByteBuffer( ByteBuffer buff )
    {
        real.rewind();
        real.put( buff );
    }
    
    public ByteBuffer getRealByteBuffer()
    {
        return real;
    }
    
    /* (non-Javadoc)
     * @see com.jmatio.types.MLArray#contentToString()
     */
    public String contentToString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(name + " = \n");
        
        if ( getSize() > 1000 )
        {
            sb.append("Cannot display variables with more than 1000 elements.");
            return sb.toString();
        }
        for ( int m = 0; m < getM(); m++ )
        {
           sb.append("\t");
           for ( int n = 0; n < getN(); n++ )
           {
               sb.append( getReal(m,n) );
               if ( isComplex() )
               {
                   sb.append("+" + getImaginary(m,n) );
               }
               sb.append("\t");
           }
           sb.append("\n");
        }
        return sb.toString();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o)
    {
        if ( o instanceof  MLNumericArray )
        {
            boolean result = directByteBufferEquals(real, ((MLNumericArray<?>)o).real )
                                   && Arrays.equals( dims, ((MLNumericArray<?>)o).dims );
            if ( isComplex() && result )
            {
                result &= directByteBufferEquals(imaginary, ((MLNumericArray<?>)o).imaginary );
            }
            return result;
        }
        return super.equals( o );
    }
    
    /**
     * Equals implementation for direct <code>ByteBuffer</code>
     * 
     * @param buffa the source buffer to be compared
     * @param buffb the destination buffer to be compared
     * @return <code>true</code> if buffers are equal in terms of content
     */
    private static boolean directByteBufferEquals(ByteBuffer buffa, ByteBuffer buffb)
    {
        if ( buffa == buffb )
        {
            return true;
        }
        
        if ( buffa ==null || buffb == null )
        {
            return false;
        }
        
        buffa.rewind();
        buffb.rewind();
        
        int length = buffa.remaining();
        
        if ( buffb.remaining() != length )
        {
            return false;
        }
        
        for ( int i = 0; i < length; i++ )
        {
            if ( buffa.get() != buffb.get() )
            {
                return false;
            }
        }

        return true;
    }
    
    public void dispose()
    {
        if ( real != null )
        {
            real.clear();
        }
        if ( imaginary != null )
        {
            real.clear();
        }
        
    }
    
}
