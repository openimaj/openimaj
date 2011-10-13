/**
 * 
 */
package org.openimaj.image.processing.face.tracking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.video.tracking.klt.FeatureList;
import org.openimaj.video.tracking.klt.KLTTracker;
import org.openimaj.video.tracking.klt.TrackingContext;

/**
 * 	A face tracker that uses the {@link HaarCascadeDetector} to detect faces
 * 	in the image and then tracks them using the {@link KLTTracker}.
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 13 Oct 2011
 */
public class KLTHaarFaceTracker implements FaceTracker<FImage>
{
	/**
	 * 	A tracker that will track a single face.
	 * 
	 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
	 *	@version $Author$, $Revision$, $Date$
	 *	@created 13 Oct 2011
	 */
	protected class KLTFaceTracker
	{		
		/** The tracking context for the KLTTracker */
		private TrackingContext trackingContext = new TrackingContext();
		
		/** The feature list used for the tracking */
		private FeatureList featureList = new FeatureList(50);
		
		/** The number of features found during the initialisation stage of the tracking */
		private int featuresFound = -1;
		
		/** The tracker used to track the faces */
		private KLTTracker tracker = new KLTTracker( trackingContext, featureList );
		
		public KLTFaceTracker()
        {
        }
		
		/**
		 * 	Reset this tracker using the given image
		 * 	@return TRUE if the tracking continued ok; FALSE otherwise
		 */
		public boolean track( FImage img )
		{
			try
            {
	            tracker.trackFeatures( previousFrame, img );
	            
	            // If we're losing features left-right and centre, we'll find
	            // some more features around the area which we can track
	            if( featureList.countRemainingFeatures() <= featuresFound  * 0.5 )
	            	return false;
	            
	            return true;
            }
            catch( IOException e )
            {
	            e.printStackTrace();
	            return false;
            }
		}

		/**
		 * 	Initialise this face tracker with a particular face on a particular
		 * 	image.
		 * 
		 *  @param face The face to track
		 *  @param img The image
		 */
		public void initialise( DetectedFace face, FImage img )
        {
			try
            {
	            // Set the tracking area to be the face found
	            trackingContext.setTargetArea( face.getBounds() );
	            
	            // Select the good features from the area
	            tracker.selectGoodFeatures( img );

	            // Remember how many features we found, so that if we
	            // start to lose them, we can re-initialise the tracking
	            featuresFound = featureList.countRemainingFeatures();
            }
            catch( IOException e )
            {
	            e.printStackTrace();
            }
        }
	}
	
	/** The face detector used to detect the faces */
	private HaarCascadeDetector faceDetector = new HaarCascadeDetector();
	
	/** A list of trackers that are tracking faces within the image */
	private List<KLTFaceTracker> trackers = new ArrayList<KLTFaceTracker>();
	
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
	 *  @inheritDoc
	 *  @see org.openimaj.image.processing.face.tracking.FaceTracker#trackFace(org.openimaj.image.Image)
	 */
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
            	KLTFaceTracker faceTracker = new KLTFaceTracker();
            	faceTracker.initialise( face, img );
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
    		Iterator<KLTFaceTracker> i = trackers.iterator();
	    	while( i.hasNext() )
	    	{
	    		KLTFaceTracker tracker = i.next();
	    		if( !tracker.track( img ) )
	    			i.remove();
	    		else
				{
					// Store the bounding box of the tracked features as the face
					detectedFaces.add( new DetectedFace( tracker.featureList.getBounds(), null ) ); 
					
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
