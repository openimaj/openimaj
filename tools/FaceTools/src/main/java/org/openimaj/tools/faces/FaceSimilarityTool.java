/**
 * 
 */
package org.openimaj.tools.faces;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.parts.FacePipeline;
import org.openimaj.image.processing.face.parts.FacialDescriptor;

import corejava.PrintfFormat;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 30 May 2011
 */
public class FaceSimilarityTool
{	
	/**
	 * 	Calculates the distance between all the faces in the first image with
	 * 	all the faces in the given images.
	 * 
	 * 	Faces are identified by their image file and the index into the file,
	 * 	so if an image does not contain any faces, it will return null
	 * 	from the map for that filename.
	 * 
	 * 	@param first The query image
	 *  @param others The list of files to compare against
	 *  @return A Map giving the distance of every face with every other.
	 */
	public Map<String,Map<String,Double>> getDistances( File first, List<File> others )
	{
		List<File> x = new ArrayList<File>();
		x.add( first );
		x.addAll( others );
		
		return this.getDistances( x, true );
	}
	
	/**
	 * 	Calculates the distance between faces in the given images.
	 * 	Faces are identified by their image file and the index into the file,
	 * 	so if an image does not contain any faces, it will return null
	 * 	from the map for that filename.
	 * 
	 *  @param inputFiles The list of files to process
	 *  @return A Map giving the distance of every face with every other.
	 */
	public Map<String,Map<String,Double>> getDistances( List<File> inputFiles )
    {
		return getDistances( inputFiles, false );
    }
	
	/**
	 * 	Calculates the distance between faces in the given images.
	 * 	Faces are identified by their image file and the index into the file,
	 * 	so if an image does not contain any faces, it will return null
	 * 	from the map for that filename.
	 * 
	 *  @param inputFiles The list of files to process
	 *  @param withFirst if TRUE, the first image in the list will be matched
	 *  	against all others, otherwise all images are matches against each other.
	 *  @return A Map giving the distance of every face with every other.
	 */
	public Map<String,Map<String,Double>> getDistances( List<File> inputFiles, boolean withFirst )
	{
		Map<String,Map<String,Double>> m = new HashMap<String, Map<String,Double>>();

		// This is the face analyser we'll use to find faces in the images.
		FacePipeline fp = new FacePipeline();
		
		int xx = 0;
		if( withFirst )
				xx = 1;
		else	xx = inputFiles.size();
		
		for( int i = 0; i < xx; i++ )
		{
			try
            {
				// Read the first image and extract the faces.
	            FImage f1 = ImageUtilities.readF( inputFiles.get(i) );
	            LocalFeatureList<FacialDescriptor> f1faces = fp.extractFaces( f1 );
	            
	            // Now loop through all the other images.
	            for( int j = withFirst?1:0; j < inputFiles.size(); j++ )
	            {
	            	try
	                {
	            		// Read the other image and extract the faces.
	            		FImage f2 = null;
	            		LocalFeatureList<FacialDescriptor> f2faces = null;
	            		if( i != j )
	            		{
		                    f2 = ImageUtilities.readF( inputFiles.get(j) );
		                    f2faces = fp.extractFaces( f2 );
	            		}
	            		else
	            		{
	            			f2 = f1;
	            			f2faces = f1faces;
	            		}
	                    
	                    // Now compare all the faces in the first image
	                    // with all the faces in the second image.
	                    for( int ii = 0; ii < f1faces.size(); ii++ )
	                    {
	                    	FacialDescriptor f1f = f1faces.get(ii);
	                    	String face1id = inputFiles.get(i)+":"+ii;
	                    	
		                    // NOTE that the distance matrix will be symmetrical
		                    // so we only have to do half the comparisons.
	                    	for( int jj = 0; jj < f2faces.size(); jj++ )
	                    	{
	                    		double d = 0;
	                    		String face2id = null;
	                    		if( i == j && ii == jj )
	                    		{
	                    			d = 0;
	                    			face2id = face1id;
	                    		}
	                    		else
	                    		{
		                    		FacialDescriptor f2f = f2faces.get(jj);
		                    		face2id = inputFiles.get(j).
		                    			getPath()+":"+jj;
		                    		
		                    		FloatFV f1fv = f1f.getFeatureVector();
		                    		FloatFV f2fv = f2f.getFeatureVector();
		                    		
		                    		d = f1fv.compare( f2fv, 
		                    				FloatFVComparison.EUCLIDEAN );
	                    		}
	                    		
	                    		Map<String,Double> mm = m.get( face1id );
	                    		if( mm == null )
	                    			m.put( face1id, 
	                    				mm = new HashMap<String,Double>() );
	                    		mm.put( face2id, d );
	                    	}
	                    }
	                }
	                catch( IOException e )
	                {
	                	System.err.println( "While reading "+inputFiles.get(j));
	                    e.printStackTrace();
	                }
	            }
            }
            catch( IOException e )
            {
            	System.err.println( "While reading "+inputFiles.get(i));
	            e.printStackTrace();
            }
		}

		return m;
    }
	
	/**
	 * 	Parses the command line arguments.
	 * 
	 *  @param args The arguments to parse
	 *  @return The tool options class
	 */
	private static FaceSimilarityToolOptions parseArgs( String[] args )
	{
		FaceSimilarityToolOptions fdto = new FaceSimilarityToolOptions();
        CmdLineParser parser = new CmdLineParser( fdto );

        try
        {
	        parser.parseArgument( args );
        }
        catch( CmdLineException e )
        {
	        e.printStackTrace();
	        parser.printUsage( System.err );
        }
        
        return fdto;
	}

	/**
	 * 	
	 *  @param args
	 */
	public static void main( String[] args )
	{
		FaceSimilarityToolOptions o = parseArgs( args );
		Map<String, Map<String, Double>> m = 
			new FaceSimilarityTool().getDistances( o.inputFiles, o.withFirst );
		//System.out.println( "Map:" +m);
		
		// Pretty print the matrix
		Set<String> xx = null;
		if( o.withFirst )
				xx = m.get( m.keySet().iterator().next() ).keySet();
		else	xx = m.keySet();
		
		int maxLen = 0;
		for( String f : xx ) 
			if( f.length() > maxLen ) maxLen = f.length();
		
		System.out.print( new PrintfFormat( "%+"+maxLen+"s" ).sprintf("") );
		for( String f : m.keySet() ) 
			System.out.print( new PrintfFormat( "%8s" ).sprintf(
				f.substring( f.lastIndexOf( ":" ) ) ) );
		System.out.println();
		
		for( String f : xx )
		{
			String s = f;
			if( f.length() < maxLen )
				s = new PrintfFormat( "%+"+maxLen+"s" ).sprintf( f );
			System.out.print( s+":" );

			Set<String> zz = m.keySet();
				
			// Iterate over the outer map so that we get the same ordering
			// of the inner map
			boolean first = true;
			for( String ff : zz )
			{
				Double d = null;
				if( m.get(f) != null )
					d = m.get(f).get(ff);
				
				if( d == null )
					if( m.get(ff) != null )
						d = m.get(ff).get(f);
				
				if( d == null )
					System.out.print("        ");
				else
				{
					if( !first ) System.out.print(",");
					System.out.print( new PrintfFormat("%7.2f").sprintf( d ) );
					first = false;
				}
			}

			System.out.println();
		}
	}
}
