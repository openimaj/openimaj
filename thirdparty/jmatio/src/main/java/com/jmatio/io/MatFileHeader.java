package com.jmatio.io;

import java.util.Date;

/**
 * MAT-file header
 * 
 * Level 5 MAT-files begin with a 128-byte header made up of a 124 byte text field
 * and two, 16-bit flag fields
 * 
 * @author Wojciech Gradkowski (<a href="mailto:wgradkowski@gmail.com">wgradkowski@gmail.com</a>)
 */
public class MatFileHeader
{
    private static String DEFAULT_DESCRIPTIVE_TEXT = "MATLAB 5.0 MAT-file, Platform: " 
                                                   + System.getProperty("os.name")
                                                   + ", CREATED on: ";
    private static int DEFAULT_VERSION = 0x0100;
    private static byte[] DEFAULT_ENDIAN_INDICATOR = new byte[] {(byte)'M', (byte)'I'};
    
    private int version;
    private String description;
    private byte[] endianIndicator;
    
    /**
     * New MAT-file header
     * 
     * @param description - descriptive text (no longer than 116 characters)
     * @param version - by default is set to 0x0100
     * @param endianIndicator - byte array size of 2 indicating byte-swapping requirement
     */
    public MatFileHeader(String description, int version, byte[] endianIndicator)
    {
        this.description = description;
        this.version = version;
        this.endianIndicator = endianIndicator;
    }
    
    /**
     * Gets descriptive text
     * 
     * @return
     */
    public String getDescription()
    {
        return description;
    }
    /**
     * Gets endian indicator. Bytes written as "MI" suggest that byte-swapping operation is required
     * in order to interpret data correctly. If value is set to "IM" byte-swapping is not needed.
     * 
     * @return - a byte array size of 2
     */
    public byte[] getEndianIndicator()
    {
        return endianIndicator;
    }
    /**
     * When creating a MAT-file, set version to 0x0100
     * 
     * @return
     */
    public int getVersion()
    {
        return version;
    }
    
    //@facotry
    /**
     * A factory. Creates new <code>MatFileHeader</code> instance with default header values:
     * <ul>
     *  <li>MAT-file is 5.0 version</li>
     *  <li>version is set to 0x0100</li>
     *  <li>no byte-swapping ("IM")</li>
     * </ul>
     * 
     * @return - new <code>MatFileHeader</code> instance
     */
    public static MatFileHeader createHeader()
    {
        return new MatFileHeader( DEFAULT_DESCRIPTIVE_TEXT + (new Date()).toString(), 
                                    DEFAULT_VERSION, 
                                    DEFAULT_ENDIAN_INDICATOR);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append("desriptive text: " + description);
        sb.append(", version: " + version);
        sb.append(", endianIndicator: " + new String(endianIndicator) );
        sb.append("]");
        
        return sb.toString();
    }
    
}
