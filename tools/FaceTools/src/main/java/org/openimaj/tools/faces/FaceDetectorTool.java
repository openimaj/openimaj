/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
import org.openimaj.image.renderer.MBFImageRenderer;

/**
 * 	A command-line tool that displays the bounding boxes of detected
 * 	faces in the input image(s).
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
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
            	MBFImageRenderer renderer = m.createRenderer();
            	
            	for( DetectedFace df : faces )
            		renderer.drawShape( df.getBounds(), RGBColour.RED );
            	
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
	        System.exit(1);
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
