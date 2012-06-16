package com.jmatio.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Tiny class that represents MAT-file TAG 
 * It simplifies writing data. Automates writing padding for instance.
 */
class OSArrayTag extends MatTag
{
    private ByteBuffer data;
    private int padding;
    /**
     * Creates TAG and stets its <code>size</code> as size of byte array
     * 
     * @param type
     * @param data
     */
    public OSArrayTag(int type, byte[] data )
    {
        this ( type, ByteBuffer.wrap( data ) );
    }
    /**
     * Creates TAG and stets its <code>size</code> as size of byte array
     * 
     * @param type
     * @param data
     */
    public OSArrayTag(int type, ByteBuffer data )
    {
        super( type, data.limit() );
        this.data = data;
        data.rewind();
        this.padding = getPadding(data.limit(), false);
    }

    
    /**
     * Writes tag and data to <code>DataOutputStream</code>. Wites padding if neccesary.
     * 
     * @param os
     * @throws IOException
     */
    public void writeTo(DataOutputStream os) throws IOException
    {
        os.writeInt(type);
        os.writeInt(size);
        
        int maxBuffSize = 1024;
        int writeBuffSize = data.remaining() < maxBuffSize ? data.remaining() : maxBuffSize;
        byte[] tmp = new byte[writeBuffSize]; 
        while ( data.remaining() > 0 )
        {
            int length = data.remaining() > tmp.length ? tmp.length : data.remaining();
            data.get( tmp, 0, length);
            os.write(tmp, 0, length);
        }
        
        if ( padding > 0 )
        {
            os.write( new byte[padding] );
        }
    }
}