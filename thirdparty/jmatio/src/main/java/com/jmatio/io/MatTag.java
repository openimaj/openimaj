package com.jmatio.io;

import com.jmatio.common.MatDataTypes;

/**
 * 
 * @author Wojciech Gradkowski (<a href="mailto:wgradkowski@gmail.com">wgradkowski@gmail.com</a>)
 */
class MatTag
{
    protected int type;
    protected int size;
    
    /**
     * @param type
     * @param size
     * @param compressed
     */
    public MatTag(int type, int size)
    {
        this.type = type;
        this.size = size;
    }

    /**
     * Calculate padding
     */
    protected int getPadding(int size, boolean compressed)
    {
        int padding;
        //data not packed in the tag
        if ( !compressed )
        {    
            int b;
            padding = ( b = ( ((size/sizeOf())%(8/sizeOf()))*sizeOf() ) ) !=0   ? 8-b : 0;
        }
        else //data _packed_ in the tag (compressed)
        {
            int b;
            padding = ( b = ( ((size/sizeOf())%(4/sizeOf()))*sizeOf() ) ) !=0   ? 4-b : 0;
        }
        return padding;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        String s;
        
        s = "[tag: " + MatDataTypes.typeToString(type) + " size: " + size + "]";
        
        return s;
    }
    /**
     * Get size of single data in this tag.
     * 
     * @return - number of bytes for single data
     */
    public int sizeOf()
    {
        return MatDataTypes.sizeOf(type);
    }
    
}
