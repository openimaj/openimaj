package com.jmatio.types;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents Matlab's Structure object (structure array).
 * 
 * Note: array of structures can contain only structures of the same type
 * , that means structures that have the same field names.
 * 
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MLStructure extends MLArray
{
    /**
     * A Set that keeps structure field names
     */
    private Set<String> keys;
    /**
     * Array of structures
     */
    private List< Map<String,MLArray> > mlStructArray;
    /**
     * Current structure pointer for bulk insert 
     */
    private int currentIndex = 0;
    
    public MLStructure(String name, int[] dims)
    {
        this(name, dims, MLArray.mxSTRUCT_CLASS, 0 );
    }
    
    public MLStructure(String name, int[] dims, int type, int attributes)
    {
        super(name, dims, type, attributes);
        
        mlStructArray = new ArrayList< Map<String,MLArray> >( dims[0]*dims[1] );
        keys = new LinkedHashSet<String>();
    }
    /**
     * Sets field for current structure
     * 
     * @param name - name of the field
     * @param value - <code>MLArray</code> field value
     */
    public void setField(String name, MLArray value)
    {
        //fields.put(name, value);
        setField(name, value, currentIndex);
    }
    /**
     * Sets field for (m,n)'th structure in struct array
     * 
     * @param name - name of the field
     * @param value - <code>MLArray</code> field value
     * @param m
     * @param n
     */
    public void setField(String name, MLArray value, int m, int n)
    {
        setField(name, value, getIndex(m,n) );
    }
    /**
     * Sets filed for structure described by index in struct array
     * 
     * @param name - name of the field
     * @param value - <code>MLArray</code> field value
     * @param index
     */
    public void setField(String name, MLArray value, int index)
    {
        keys.add(name);
        currentIndex = index;
        
        if ( mlStructArray.isEmpty() || mlStructArray.size() <= index )
        {
            mlStructArray.add(index, new LinkedHashMap<String, MLArray>() );
        }
        mlStructArray.get(index).put(name, value);
    }
    
    /**
     * Gets the maximum length of field descriptor
     * 
     * @return
     */
    public int getMaxFieldLenth()
    {
        //get max field name
        int maxLen = 0;
        for ( String s : keys )
        {
            maxLen = s.length() > maxLen ? s.length() : maxLen;
        }
        return maxLen+1;
        
    }
    
    /**
     * Dumps field names to byte array. Field names are written as Zero End Strings
     * 
     * @return
     */
    public byte[] getKeySetToByteArray() 
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        char[] buffer = new char[getMaxFieldLenth()];
        
        try
        {
            for ( String s : keys )
            {
                Arrays.fill(buffer, (char)0);
                System.arraycopy( s.toCharArray(), 0, buffer, 0, s.length() );
                dos.writeBytes( new String(buffer) );
            }
        }
        catch  (IOException e)
        {
            System.err.println("Could not write Structure key set to byte array: " + e );
            return new byte[0];
        }
        return baos.toByteArray();
        
    }
    /**
     * Gets all field from sruct array as flat list of fields.
     * 
     * @return
     */
    public Collection<MLArray> getAllFields()
    {
        ArrayList<MLArray> fields = new ArrayList<MLArray>();
        
        for ( Map<String, MLArray> struct : mlStructArray )
        {
            fields.addAll( struct.values() );
        }
        return fields;
    }
    /**
     * Returns the {@link Collection} of keys for this structure.
     * @return the {@link Collection} of keys for this structure
     */
    public Collection<String> getFieldNames()
    {
        Set<String> fieldNames = new LinkedHashSet<String> ();
        
        fieldNames.addAll( keys );
        
        return fieldNames;
        
    }
    /**
     * Gets a value of the field described by name from current structe
     * in struc array.
     * 
     * @param name
     * @return
     */
    public MLArray getField(String name)
    {
        return getField(name, currentIndex);
    }
    /**
     * Gets a value of the field described by name from (m,n)'th structe
     * in struc array.
     * 
     * @param name
     * @param m
     * @param n
     * @return
     */
    public MLArray getField(String name, int m, int n)
    {
        return getField(name, getIndex(m,n) );
    }
    /**
     * Gets a value of the field described by name from index'th structe
     * in struc array.
     * 
     * @param name
     * @param index
     * @return
     */
    public MLArray getField(String name, int index)
    {
        return mlStructArray.get(index).get(name);
    }
    /* (non-Javadoc)
     * @see com.paradigmdesigner.matlab.types.MLArray#contentToString()
     */
    public String contentToString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(name + " = \n");
        
        if ( getM()*getN() == 1 )
        {
            for ( String key : keys )
            {
                sb.append("\t" + key + " : " + getField(key) + "\n" );
            }
        }
        else
        {
            sb.append("\n");
            sb.append(getM() + "x" + getN() );
            sb.append(" struct array with fields: \n");
            for ( String key : keys)
            {
                sb.append("\t" + key + "\n");
            }
        }
        return sb.toString();
    }

}
