package com.jmatio.io;

import java.util.HashSet;
import java.util.Set;

/**
 * File filter.
 * 
 * This class is used to tell <code>MatFileReader</code> which matrices
 * should be processed. This is useful when operating on big MAT-files,
 * when there's no need to load all arrays into memory.
 * 
 * Usage:
 * <pre></code>
 * //create new filter instance
 * MatFileFilter filter = new MatFileFilter();
 * //add a needle
 * filter.addArrayName( "your_array_name" );
 * 
 * //read array form file (haystack) looking _only_ for pecified array (needle)
 * MatFileReader mfr = new MatFileReader( fileName, filter );
 * </code></pre>
 * 
 * @see com.jmatio.io.MatFileReader
 * @author Wojciech Gradkowski (<a href="mailto:wgradkowski@gmail.com">wgradkowski@gmail.com</a>)
 */
public class MatFileFilter
{
    private Set<String> filter;
    
    /**
     * Creates empty filter intance.
     * 
     * <i>Note: empty filter acceps all results.</i>
     */
    public MatFileFilter()
    {
        filter = new HashSet<String>();
    }
    /**
     * Create filter intance and add array names.
     * 
     * @param names - array of names (needles)
     */
    public MatFileFilter( String[] names )
    {
        this();
        
        for ( String name : names )
        {
            addArrayName( name );
        }
    }
    /**
     * Add array name to the filter. This array will be processed
     * while crawling thourg the MAT-file
     * 
     * @param name - array name (needle)
     */
    public void addArrayName( String name )
    {
        filter.add( name );
    }
    /**
     * Test if given name matches the filter.
     * 
     * @param name - array name to be tested
     * @return - <code>true</code> if array (matrix) of this name should be processed
     */
    public boolean matches( String name )
    {
        if ( filter.size() == 0 )
        {
            return true;
        }
        return filter.contains( name );
    }
}
