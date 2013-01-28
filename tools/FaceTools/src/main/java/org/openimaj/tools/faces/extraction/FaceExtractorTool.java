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
package org.openimaj.tools.faces.extraction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.video.processing.shotdetector.ShotBoundary;
import org.openimaj.video.processing.shotdetector.HistogramVideoShotDetector;
import org.openimaj.video.processing.timefinder.ObjectTimeFinder;
import org.openimaj.video.processing.timefinder.ObjectTimeFinder.TimeFinderListener;
import org.openimaj.video.processing.tracking.BasicMBFImageObjectTracker;
import org.openimaj.video.timecode.VideoTimecode;
import org.openimaj.video.xuggle.XuggleVideo;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 	A tool that provides a means of extracting faces from videos and images.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 7 Nov 2011
 */
public class FaceExtractorTool
{
	/** The options for this tool instance */
	private FaceExtractorToolOptions options = null;
	
	/** The video from which to extract faces */
	private XuggleVideo video = null;

	/** The output directory where we'll write the extracted faces */
	private File outputDir = null;
	
	/** Used in the object tracking to store the frame in which the best face was found */
	private MBFImage bestFaceFrame = null;

	/** Used in the object tracking to store the best face timecode for each face */
	private VideoTimecode bestFaceTimecode = null;
	
	/** Used in the object tracking to store the best face bounding box */
	private Rectangle bestFaceBoundingBox = null;

	/**
	 * 	Default constructor
	 *  @param o the options 
	 */
	public FaceExtractorTool( FaceExtractorToolOptions o )
    {
		this.options = o;
		
		this.outputDir  = new File( o.outputFile );
		
		// If we have a video file, read in the video
		if( options.videoFile != null )
		{
			// Create the video reader for reading the video
			this.video = new XuggleVideo( new File( options.videoFile ) );
		}
		
		// Create the output directory if it doesn't exist.
		if( !this.outputDir.exists() )
			this.outputDir.mkdirs();
		
		// Process the video
		this.processVideo();
    }

	/**
	 * 	Process the video to extract faces.
	 */
	private void processVideo()
	{
		if( this.options.verbose )
		{
			System.out.println( this.options.videoFile );
			System.out.println( "    - Size: "+video.getWidth()+"x"+video.getHeight() );
			System.out.println( "    - Frame Rate: "+video.getFPS() );
			System.out.println( "Detecting shots in video..." );
		}
		
		// This is the video shot detector we'll use to find the shots in
		// the incoming video. These shots will provide hard limits for the
		// face tracking.
		HistogramVideoShotDetector vsd = new HistogramVideoShotDetector( this.video );
		vsd.setThreshold( this.options.threshold );
		vsd.setFindKeyframes( true );
		vsd.setStoreAllDifferentials( false );
		vsd.process();
		
		// Retrieve the shots from the shot detector
		List<ShotBoundary<MBFImage>> shots = vsd.getShotBoundaries();
		
		if( this.options.verbose )
			System.out.println( "Found "+shots.size()+" shots.");
		
		// We'll use the HaarCascadeFaceDetector for detecting faces.
		HaarCascadeDetector fd = HaarCascadeDetector.BuiltInCascade.frontalface_alt2.load();
		fd.setMinSize( this.options.faceSize );
		
		// Now we'll go through the video looking for faces every x seconds.
		this.video.reset();
		
		// For each shot boundary...
		ShotBoundary<MBFImage> prev = shots.get(0);
		for( int i = 1; i < shots.size(); i++ ) 
		{
			ShotBoundary<MBFImage> thiz = shots.get(i);
			
			// Get the timecodes of the shot. Remember the timecode gives the
			// start of the shot, so the shot is between the previous timecode
			// and one frame before this timecode.
			long pframe = prev.getTimecode().getFrameNumber();
			long tframe = thiz.getTimecode().getFrameNumber()-1;
			
			if( this.options.verbose )
				System.out.println( "Shot: "+prev+" ("+pframe+") -> "+thiz+" ("+tframe+")" );
			
			// This will be the frame we'll store for a given face
			MBFImage faceFrame = null;
			
			// Now loop around looking for faces in the shot
			List<DetectedFace> faces = null;
			boolean doneSearching = false;
			while( !doneSearching )
			{
				// If we're supposed to use just the centre frame, then we'll work
				// out where that centre frame is and grab the frame.
				if( this.options.useCentre )
				{
					long mframe = pframe + ((tframe - pframe) / 2);
					video.setCurrentFrameIndex(mframe);
					faceFrame = video.getCurrentFrame();
					doneSearching = true;
				}
				// If we're searching for a face every x-seconds then we'll skip frames
				// forward by x-seconds.
				else
				{
					// Push the video forward by x frames
					pframe += options.seconds * video.getFPS();
					
					// Check if we're still within the shot
					if( pframe >= tframe )
					{
						doneSearching = true;
						pframe = tframe;
					}
					
					// Push the video forward
					video.setCurrentFrameIndex( pframe );
					faceFrame = video.getCurrentFrame();
				}

				if( this.options.verbose )
					System.out.println( "    - Using frame "+
						video.getCurrentTimecode()+" ("+video.getTimeStamp()+")" );
				
				// Detect faces in the frame
				faces = fd.detectFaces( faceFrame.flatten() );
				
				if( this.options.verbose )
					System.out.println( "        + Found "+faces.size()+" faces in frame.");
				
				if( faces.size() > 0 )
					doneSearching = true;

				// For each of the detected faces (if there are any) track
				// back and forth in the video to find the times at which the
				// face is best. As a consequence, we will also end up with the
				// timecodes at which the face appears in the video (at any size)
				for( DetectedFace f : faces ) 
				{
					if( options.verbose )
						System.out.println( "        - Tracking face..." );
					
					bestFaceTimecode = null;
					bestFaceFrame = null;
					bestFaceBoundingBox = null;
					
					// We'll use this ObjectTimeFinder to track the faces once they're
					// extracted from the video.
					ObjectTimeFinder otf = new ObjectTimeFinder();					
					IndependentPair<VideoTimecode, VideoTimecode> timecodes = 
						otf.trackObject( new BasicMBFImageObjectTracker(), video, 
							video.getCurrentTimecode(), f.getBounds(), 
							new TimeFinderListener<Rectangle,MBFImage>()
							{
								double maxArea = 0;
							
								@Override
                                public void objectTracked(
                                        List<Rectangle> objects,
                                        VideoTimecode time,
                                        MBFImage image )
                                {
									if( objects.size() > 0 && 
										objects.get(0).calculateArea() > maxArea )
									{
										maxArea = objects.get(0).calculateArea();
										bestFaceTimecode  = time;
										bestFaceFrame = image.clone();
										bestFaceBoundingBox = objects.get(0);
									}
                                }
							} );

					if( options.verbose )
						System.out.println( "        - Face tracked between "+timecodes );
					
					if( bestFaceBoundingBox != null ) try
                    {
	                    saveFace( bestFaceFrame, 
	                    		bestFaceTimecode.getFrameNumber(), timecodes,
	                    		bestFaceBoundingBox );
                    }
                    catch( IOException e )
                    {
	                    e.printStackTrace();
                    }
				}
			}

			prev = thiz;
		}
	}
	
	/**
	 * 	Writes a face image to an appropriate file in the output directory
	 * 	named as per the input file but suffixed with the frame number.
	 * 
	 * 	@param frame The video frame
	 *  @param mframe The frame number
	 * 	@param timecodes The timecodes of the face 
	 * 	@param bounds The bounding box of the face 
	 *  @throws IOException If the write cannot be completed
	 */
	private void saveFace( MBFImage frame, long mframe, 
			IndependentPair<VideoTimecode, VideoTimecode> timecodes, 
			Rectangle bounds ) throws IOException 
	{
		File base = new File( this.options.outputFile );
		
		if( options.writeFaceImage )
		{
			File img = new File( base, new File( this.options.videoFile ).getName() 
					+ "#" + mframe + ".face.png");
			ImageUtilities.write( frame.extractROI( bounds ), img);
		}
		
		if( options.writeFrameImage )
		{	
			File img = new File( base, new File( this.options.videoFile ).getName() 
					+ "#" + mframe + ".frame.png");
			ImageUtilities.write(frame, img);
		}
		
		if( options.writeXML )
		{
			File xml = new File( base, new File( this.options.videoFile ).getName() 
					+ "#" + mframe + ".xml");
			
			try
			{
				DocumentBuilderFactory documentBuilderFactory = 
					DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder = 
					documentBuilderFactory.newDocumentBuilder();
				Document document = documentBuilder.newDocument();
				Element rootElement = document.createElement( "face" );
				document.appendChild( rootElement );
				
				Element em = document.createElement( "boundingBox" );
				em.appendChild( document.createTextNode( bounds.toString() ) );
				rootElement.appendChild(em);
				
				em = document.createElement( "appearanceTimecode" );
				em.appendChild( document.createTextNode( timecodes.firstObject().toString() ) );
				rootElement.appendChild(em);

				em = document.createElement( "disappearanceTimecode" );
				em.appendChild( document.createTextNode( timecodes.secondObject().toString() ) );
				rootElement.appendChild(em);

				em = document.createElement( "appearanceFrame" );
				em.appendChild( document.createTextNode( ""+timecodes.firstObject().getFrameNumber() ) );
				rootElement.appendChild(em);

				em = document.createElement( "disappearanceFrame" );
				em.appendChild( document.createTextNode( ""+timecodes.secondObject().getFrameNumber() ) );
				rootElement.appendChild(em);

				em = document.createElement( "appearanceTime" );
				em.appendChild( document.createTextNode( ""+timecodes.firstObject().getTimecodeInMilliseconds() ) );
				rootElement.appendChild(em);

				em = document.createElement( "disappearanceTime" );
				em.appendChild( document.createTextNode( ""+timecodes.secondObject().getTimecodeInMilliseconds() ) );
				rootElement.appendChild(em);

				try
				{
					TransformerFactory transformerFactory = 
						TransformerFactory.newInstance();
					Transformer transformer = transformerFactory.newTransformer();
					DOMSource source = new DOMSource( document );
					StreamResult result = new StreamResult( new FileWriter( xml ) );
					transformer.transform( source, result );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
			catch( DOMException e )
			{
				e.printStackTrace();
			}
			catch( ParserConfigurationException e )
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 	Parses the command line arguments.
	 * 
	 *  @param args The arguments to parse
	 *  @return The tool options class
	 */
	private static FaceExtractorToolOptions parseArgs( String[] args )
	{
		FaceExtractorToolOptions fdto = new FaceExtractorToolOptions();
        CmdLineParser parser = new CmdLineParser( fdto );

        try
        {
	        parser.parseArgument( args );
        }
        catch( CmdLineException e )
        {
        	System.err.println( e.getMessage());
        	System.err.println( "java FaceExtractorTool [options...]");
	        parser.printUsage( System.err );
	        System.exit(1);
        }
        
        return fdto;
	}
	
	/**
	 * 	Default main
	 *  @param args
	 */
	public static void main( String[] args )
    {
		FaceExtractorToolOptions options = parseArgs( args );		
	    new FaceExtractorTool( options );
    }
}
