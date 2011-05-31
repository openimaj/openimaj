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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * 	A command-line tool that displays the bounding boxes of detected
 * 	faces in the input image(s).
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 30 May 2011
 */
public class FaceDetectorTool
{
	/**
	 * 	Takes a set of image files and returns a map that maps the
	 * 	image filename to the list of detected images.
	 * 
	 *  @param images The input images
	 *  @param minSize The minimum detectable face
	 *  @return A map of images
	 */
	public Map<String,List<Rectangle>> detectFaces( List<File> images, int minSize )
	{
		return detectFaces( images, minSize, false );
	}
	
	/**
	 * 	Takes a set of image files and returns a map that maps the
	 * 	image filename to the list of detected images.
	 * 
	 *  @param images The input images
	 *  @param minSize The minimum detectable face
	 *  @param displayResults Displays windows showing the detection results.
	 *  
	 *  @return A map of images
	 */
	public Map<String,List<Rectangle>> detectFaces( List<File> images, 
			int minSize, boolean displayResults )
	{
		// The output will be each input mapped to the rectangles
		Map<String,List<Rectangle>> output = 
			new HashMap<String, List<Rectangle>>();
		
		try
        {
	        HaarCascadeDetector hcd = new HaarCascadeDetector("haarcascade_frontalface_alt.xml");
	        hcd.setMinSize( minSize );
	        
	        // Loop through the given images and extract the faces.
	        for( File image : images )
	        {
	        	try
	            {
	                FImage f = ImageUtilities.readF( image );
	                List<Rectangle> faces = hcd.detectObjects( f );
	                if( faces.size() > 0 )
	                {
	                	List<Rectangle> b = new ArrayList<Rectangle>();
	                	output.put( image.getPath(), b );
	                	for( Rectangle fd : faces )
	                		b.add( fd );
	                }
	                
	                if( displayResults )
	                {
	                	MBFImage m = new MBFImage( ColourSpace.RGB, f,f,f );
	                	for( Rectangle r : faces )
	                		m.drawPolygon( r.asPolygon(), RGBColour.RED );
	                	DisplayUtilities.display( m );
	                }
	            }
	            catch( IOException e )
	            {
	            	System.err.println( "While reading image "+image );
	                e.printStackTrace();
	            }
	        }
        }
        catch( Exception e )
        {
        	System.err.println( "Could not load HAAR Cascade." );
	        e.printStackTrace();
        }
		
		return output;
	}
	
	/**
	 * 	Parses the command line arguments.
	 * 
	 *  @param args The arguments to parse
	 *  @return The tool options class
	 */
	private static FaceDetectorToolOptions parseArgs( String[] args )
	{
		FaceDetectorToolOptions fdto = new FaceDetectorToolOptions();
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
	 *  @param args The input images to detect faces within.
	 */
	public static void main( String[] args )
	{
		FaceDetectorToolOptions o = parseArgs( args );
		Map<String, List<Rectangle>> x = 
			new FaceDetectorTool().detectFaces( o.inputFiles, 
					o.minSize, o.displayResults );
		for( String img : x.keySet() )
		{
			List<Rectangle> r = x.get(img);
			for( Rectangle rect : r )
				System.out.println( img+":"+rect.x+","+rect.y+","
						+rect.width+","+rect.height );
		}
	}
}
