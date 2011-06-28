/**
 * 
 */
package org.openimaj.tools.faces;

import java.io.File;
import java.io.IOException;
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
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;

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
	public Map<String,List<DetectedFace>> detectFaces( List<File> images, int minSize )
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
	public Map<String,List<DetectedFace>> detectFaces( List<File> images, 
			int minSize, boolean displayResults )
	{
		// The output will be each input mapped to the rectangles
		Map<String,List<DetectedFace>> output = 
			new HashMap<String, List<DetectedFace>>();
		
		for( File f : images )
		{
			try
            {
	            List<DetectedFace> r = detectFaces( 
	            		ImageUtilities.readF( f ), minSize, displayResults );	            
            	output.put( f.getPath(), r );
            }
            catch( IOException e )
            {
	            e.printStackTrace();
            }
			
		}
		
		return output;
	}
	
	/**
	 * 	Takes a single image and detects faces, returning a map that maps
	 * 	a number (the face number) to the rectangle of the detected face.
	 * 
	 *  @param img The image to detect faces within
	 *  @param minSize The minimum size a face is allowed to be
	 *  @param displayResults Whether to display the result of detection
	 *  @return A list of rectangles delineating the faces
	 */
	public List<DetectedFace> detectFaces( FImage img,
			int minSize, boolean displayResults )
	{
		try
        {
	        HaarCascadeDetector hcd = new HaarCascadeDetector("haarcascade_frontalface_alt.xml");
	        hcd.setMinSize( minSize );
	        
            List<DetectedFace> faces = hcd.detectFaces( img );
            if( displayResults )
            {
            	MBFImage m = new MBFImage( ColourSpace.RGB, img,img,img );
            	for( DetectedFace df : faces )
            		m.drawShape( df.getBounds(), RGBColour.RED );
            	DisplayUtilities.display( m );
            }
            
            return faces;
        }
        catch( Exception e )
        {
        	System.err.println( "Could not load HAAR Cascade." );
	        e.printStackTrace();
        }
		
		return null;
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
	        System.err.println( e.getMessage() );
	        System.err.println( "java FaceDetectorTool [options...] IMAGE-FILES");
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
		Map<String, List<DetectedFace>> x = 
			new FaceDetectorTool().detectFaces( o.inputFiles, 
					o.minSize, o.displayResults );
		for( String img : x.keySet() )
		{
			List<DetectedFace> dfs = x.get(img);
			for( DetectedFace df : dfs )
				System.out.println( img+":"+df.getBounds().x+","+df.getBounds().y+","
						+df.getBounds().width+","+df.getBounds().height );
		}
	}
}
