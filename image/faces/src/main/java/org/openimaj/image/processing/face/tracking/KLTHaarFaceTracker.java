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
package org.openimaj.image.processing.face.tracking;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.video.processing.tracking.BasicObjectTracker;
import org.openimaj.video.tracking.klt.KLTTracker;

/**
 * 	A face tracker that uses the {@link HaarCascadeDetector} to detect faces
 * 	in the image and then tracks them using the {@link KLTTracker}.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 13 Oct 2011
 */
public class KLTHaarFaceTracker implements FaceTracker<FImage>
{	
	/** The face detector used to detect the faces */
	private HaarCascadeDetector faceDetector = new HaarCascadeDetector();
	
	/** A list of trackers that are tracking faces within the image */
	private List<BasicObjectTracker> trackers = new ArrayList<BasicObjectTracker>();
	
	/** The previous frame */
	private FImage previousFrame = null;
	
	/** When all faces are lost, the frame is retried */
	private boolean retryFrame = false;

	/**
	 * 	Default constructor that takes the minimum size (in pixels) of detections
	 * 	that should be considered faces.
	 * 
	 *  @param minSize The minimum size of face boxes
	 */
	public KLTHaarFaceTracker( int minSize )
    {
		faceDetector.setMinSize( minSize );
    }
	
	/**
	 * 	Used to detect faces when there is no current state.
	 *  @return The list of detected faces
	 */
	private List<DetectedFace> detectFaces( FImage img )
	{
		return faceDetector.detectFaces( img );
	}
	
	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.processing.face.tracking.FaceTracker#trackFace(org.openimaj.image.Image)
	 */
    @Override
	public List<DetectedFace> trackFace( FImage img )
    {
    	List<DetectedFace> detectedFaces = new ArrayList<DetectedFace>();

    	// If we're just starting tracking, find some features and start
    	// tracking them.
		if( previousFrame == null || trackers.size() == 0 )
		{
			System.out.println( "Detecting faces..." );
            
            // Detect the faces in the image.
            List<DetectedFace> faces = detectFaces( img );
            
            System.out.println( "Found "+faces.size()+" faces ");
            
            // Create trackers for each face found
            for( DetectedFace face : faces )
            {
            	// Create a new tracker for this face
            	BasicObjectTracker faceTracker = new BasicObjectTracker();
            	faceTracker.initialiseTracking( face.getBounds(), img );
            	trackers.add( faceTracker );

            	// Store the last frame
            	this.previousFrame = img;
            }
            
            detectedFaces = faces;
		}
		else
		// If we have a previous frame, attempt to track the frame
    	if( previousFrame != null )
    	{
			// Update all the trackers
    		Iterator<BasicObjectTracker> i = trackers.iterator();
	    	while( i.hasNext() )
	    	{
	    		BasicObjectTracker tracker = i.next();
	    		if( tracker.trackObject( img ).size() == 0 )
	    			i.remove();
	    		else
				{
					// Store the bounding box of the tracked features as the face
					detectedFaces.add( new DetectedFace( 
							tracker.getFeatureList().getBounds(), null ) ); 
					
					// Store the last frame
					this.previousFrame = img;
				}
    		}
	    	
	    	if( trackers.size() == 0 && this.retryFrame == false )
	    	{
	    		this.retryFrame = true;
	    		detectedFaces = trackFace( img );
	    	}
    	}
		
		this.retryFrame = false;
	    return detectedFaces;
    }
}
