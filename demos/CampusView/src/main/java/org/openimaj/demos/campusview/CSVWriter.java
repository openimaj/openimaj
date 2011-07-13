/**
 * 
 */
package org.openimaj.demos.campusview;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 13 Jul 2011
 */
public class CSVWriter
{
	/**
	 * 	Writes the given data directly onto the end of the given file.
	 * 	If the file doesn't exist it will be created. All data will be
	 * 	encapsulated in double quotes. Quotes inside the string will be
	 * 	escaped with a single backslash.
	 * 
	 *  @param csvFile The file to add data to
	 *  @param data The data to add to the file
	 */
	public static void writeLine( File csvFile, String[] data )
	{
		try
        {
	        FileWriter fw = new FileWriter( csvFile, true );
	        BufferedWriter bw = new BufferedWriter( fw );
	        
	        boolean first = true;
	        for( String s : data )
	        {
	        	if( !first ) bw.write( "," );
	        	bw.write( "\""+s.replace( "\"", "\\\"" )+"\"" );
	        	first = false;
	        }
	        bw.write( "\r\n" );
	        bw.close();
        }
        catch( IOException e )
        {
	        e.printStackTrace();
        }
	}
}
