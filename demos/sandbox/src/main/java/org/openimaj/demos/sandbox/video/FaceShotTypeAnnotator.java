/**
 * 
 */
package org.openimaj.demos.sandbox.video;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.tracking.clm.CLMFaceTracker;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker.TrackedFace;

/**
 *	Uses a tracker ({@link CLMFaceTracker})
 *	to track the face in the frame and determine whether the face stays within
 *	certain size boundaries to determine shot type.
 *	<p>
 *	The shot types detected are:
 *	<ul>
 *	<li><b>Close Up:</b> The face fills the frame - that is, it takes between
 *		12% or more of the frame.</li>
 *	<li><b>Medium Close Up:</b> The person is filmed head-and-shoulders - that
 *		is, the face takes up between 3% and 12% of the frame.</li>
 *	<li><b>Mid-Shot:</b> The person is filmed half-body or less - that is,
 *		the face takes up between 0% and 3% of the image.</li>
 *	</ul>
 *	<p>
 *	If the tracker loses track of any of the faces, then the face is removed
 *	from the set that is used to produce the annotations.
 *	<p>
 *	The cumulative moving average is used to store the size of each face during
 *	the tracking of the faces.
 *	<p>
 *	The {@link CLMFaceTracker} is also able to provide pose information about
 *	the subject. From this we are able to determine some extra annotations
 *	about the subject - such as whether they appear to be talking to an off-screen
 *	person (interviewee or interviewer), or talking to the camera (presenter
 *	or anchor). 
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 22 Jan 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class FaceShotTypeAnnotator extends VideoAnnotator<MBFImage, String>
{
	/** The ontology URI */
	private static final String ONTO = "http://onto.dupplaw.me.uk/video/";

	/** The face tracker we're going to use to track faces */
	private CLMFaceTracker faceTracker = null;
	
	/** The percentage that each face covers the full frame */
	private HashMap<TrackedFace,Double> faceSizes = null;
	
	/** The number of frames we've processed in calculating this shot */
	private int frameCount = 0;

	/**
	 * 	Constructor
	 */
	public FaceShotTypeAnnotator()
	{
		this.faceTracker = new CLMFaceTracker();
		this.faceSizes = new HashMap<MultiTracker.TrackedFace, Double>();
	}
	
	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.processor.VideoProcessor#processFrame(org.openimaj.image.Image)
	 */
	@Override
	public MBFImage processFrame( final MBFImage frame )
	{
		// Update the frame counter.
		this.frameCount++;
		
		// Track the face in the image.
		this.faceTracker.track( frame );
		
		// Get the tracked faces
		// The assumption is that the tracker will return the same TrackedFace
		// object for the same face in the image as the tracking continues.
		final List<TrackedFace> trackedFaces = this.faceTracker.getTrackedFaces();
		
		// Calculate the size of the frame
		final double frameSize = frame.getWidth() * frame.getHeight();
		
		// Remove any faces which no longer exist
		final HashSet<TrackedFace> missingFaces = 
				new HashSet<TrackedFace>( this.faceSizes.keySet() );
		for( final TrackedFace face : trackedFaces )
			missingFaces.remove( face );
		for( final TrackedFace face : missingFaces )
			this.faceSizes.remove( face );
		
		// Loop over the tracked faces and update the map
		for( final TrackedFace face : trackedFaces )
		{
			// Calculate the size of the face in percent of the frame size
			final double faceSizePc = (face.lastMatchBounds.width *
					face.lastMatchBounds.height) / frameSize;
			
			// If it's a new face, we make the cumulative average to be the
			// current size of the face.
			if( this.faceSizes.get( face ) == null )
				this.faceSizes.put( face, faceSizePc );
			// If the face exists, we update the size
			else
			{
				// Get the current running face average
				final double ca = this.faceSizes.get( face );
				
				// Update the face average
				this.faceSizes.put( face, (faceSizePc + ca*(this.frameCount-1)) 
						/ this.frameCount );
			}
		}
		
		// Returns the original frame untouched.
		return frame;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.demos.sandbox.video.VideoAnnotator#updateAnnotations()
	 */
	@Override
	protected void updateAnnotations()
	{
		this.addShotTypeAnnotations();
	}
	
	/**
	 * 	Determines the shot type annotations to add based
	 * 	on the face stats that has been captured. 
	 */
	private void addShotTypeAnnotations()
	{
		// Check if we found any faces
		if( this.faceSizes.keySet().size() == 0 )
			return;
		
		// If we found more than one face, then it's some sort of
		// group shot.
		if( this.faceSizes.keySet().size() > 1 )
		{
			// Group shot.
			if( this.faceSizes.keySet().size() == 2 )
					this.annotations.add( FaceShotTypeAnnotator.ONTO+"TwoShot" );
			else	this.annotations.add( FaceShotTypeAnnotator.ONTO+"GroupShot" );
		}
		// There is one face in the video:
		else
		{
			// If there's only one face, we'll retrieve it.
			final TrackedFace onlyFace = this.faceSizes.keySet().iterator().next();
			
			// Retrieve it's average size:
			final double size = this.faceSizes.get( onlyFace );
			
			// Mid-Shot
			if( size <= 0.03 )
				this.annotations.add( FaceShotTypeAnnotator.ONTO+"MidShot" );
			else
			// Medium Close Up
			if( size <= 0.12 )
				this.annotations.add( FaceShotTypeAnnotator.ONTO+"MediumCloseUp" );
			// Close up shot
			else
				this.annotations.add( FaceShotTypeAnnotator.ONTO+"CloseUp" );
		}
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.demos.sandbox.video.VideoAnnotator#resetAnnotator()
	 */
	@Override
	protected void resetAnnotator()
	{
		this.faceTracker.reset();
	}
}
