/**
 * 
 */
package org.openimaj.demos.sandbox.video;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.tracking.clm.CLMFaceTracker;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker;
import org.openimaj.image.processing.face.tracking.clm.MultiTracker.TrackedFace;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.util.RunningStat;

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
	private HashMap<TrackedFace,RunningStat> faceSizes = null;
	
	/** The average pose of each face */
	private HashMap<TrackedFace,RunningStat[]> facePoses = null;
	
	/** The size of the frame stored for any visual outputs */
	private Dimension frameSize = null;
	
	/** The last frame processed - for visualisations */
	private MBFImage lastFrame = null;

	/**
	 * 	Constructor
	 */
	public FaceShotTypeAnnotator()
	{
		this.faceTracker = new CLMFaceTracker();
		this.faceSizes = new HashMap<MultiTracker.TrackedFace, RunningStat>();
		this.facePoses = new HashMap<MultiTracker.TrackedFace, RunningStat[]>();
	}
	
	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.processor.VideoProcessor#processFrame(org.openimaj.image.Image)
	 */
	@Override
	public MBFImage processFrame( final MBFImage frame )
	{
		// Store the size of the frame.
		this.frameSize = new Dimension( frame.getWidth(), frame.getHeight() );
		
		// Track the face in the image.
		this.faceTracker.track( frame );
		
		// Get the tracked faces
		// The assumption is that the tracker will return the same TrackedFace
		// object for the same face in the image as the tracking continues.
		// FIXME: If the tracker is set to auto-redetect, it returns different objects for the same faces
		final List<TrackedFace> trackedFaces = this.faceTracker.getTrackedFaces();
		
		// Calculate the size of the frame
		final double frameSize = frame.getWidth() * frame.getHeight();
		
		// Remove any faces which no longer exist
		final HashSet<TrackedFace> missingFaces = 
				new HashSet<TrackedFace>( this.faceSizes.keySet() );
		for( final TrackedFace face : trackedFaces )
			missingFaces.remove( face );
		for( final TrackedFace face : missingFaces )
		{
			this.faceSizes.remove( face );
			this.facePoses.remove( face );
		}
		
		// Loop over the tracked faces and update the map
		for( final TrackedFace face : trackedFaces )
		{
			// ---------- SIZE -----------
			// Calculate the size of the face in percent of the frame size
			final double faceSizePc = (face.lastMatchBounds.width *
					face.lastMatchBounds.height) / frameSize;
			
			// If it's a new face, we make the cumulative average to be the
			// current size of the face.
			if( this.faceSizes.get( face ) == null )
				this.faceSizes.put( face, new RunningStat(faceSizePc) );
			// If the face exists, we update the size
			else
			{
				// Get the current running face average
				final RunningStat ca = this.faceSizes.get( face );
				
				// Update the face average
				ca.push( faceSizePc );
			}

			// ---------- POSE -----------
			// Get the pose information for this face
			final int nParams = face.clm._pglobl.getRowDimension();
			final double[] poseInfo = new double[nParams];
			for( int i = 0; i < nParams; i++ )
				poseInfo[i] = face.clm._pglobl.get(i,0);
			
			// If it's a new face, store the current pose into the map
			RunningStat[] stats = this.facePoses.get(face);
			if( stats == null )
				this.facePoses.put( face, stats = new RunningStat[nParams] );

			// Update the average pose information
			for( int i = 0; i < nParams; i++ )
			{
				if( stats[i] == null ) stats[i] = new RunningStat();
				stats[i].push( poseInfo[i] ); 
			}
		}
		
		// Store the last frame for visualisation purposes.
		this.lastFrame = frame;
		
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
			
			// ==============================================================
			// Determine the shot type
			// ==============================================================
			// Retrieve it's average size:
			final double size = this.faceSizes.get( onlyFace ).mean();
			
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

		this.getAveragePoses( this.lastFrame );
		
		// Of the global parameters, i=2 is the y-rotation (head shake)7
		// i=1 is the x-rotation (head nod) and i=3 is the z-rotation (headstand)
		
	}
	
	/**
	 * 	Returns an image containing the current average poses of all the
	 * 	faces in the video shot so far.
	 * 	@param frame (optional) Frame to place in the backround of the visualisation 
	 *	@return an MBFImage
	 */
	public MBFImage getAveragePoses( final MBFImage frame )
	{
		// We'll generate a video frame the size of the frame that was
		// last processed (the assumption is that video frame size doesn't change)
		final MBFImage image = new MBFImage( 
					(int)this.frameSize.getWidth(), 
					(int)this.frameSize.getHeight(), 3 );
		
		// If we have a frame to put in the background...
		if( frame != null )
			image.addInplace( frame.multiply( 0.5f ) );
		
		// Create a tracked face that we'll morph into the various average
		// parameters to draw to the image
		final TrackedFace avgFace = new TrackedFace( 
			new Rectangle(50, -50, 500, 500), this.faceTracker.getInitialVars() );
		
		// We need the arrays of triangles and connections to draw the faces
		final int[][] connections = this.faceTracker.connections;
		final int[][] triangles = this.faceTracker.triangles;
		
		// Loop through each of the faces setting the pose and drawing to an image
		for( final TrackedFace face : this.facePoses.keySet() )
		{
			// Get the average pose for the tracked face
			final RunningStat[] poseInfo = this.facePoses.get( face );
			
			// Set the model face to this pose
			for( int i = 0; i < poseInfo.length; i++ )
				avgFace.clm._pglobl.set( i, 0, poseInfo[i].mean() );
			
			// Recalculate the face shape
			avgFace.clm._pdm.calcShape2D( avgFace.shape,
					avgFace.clm._plocal, avgFace.clm._pglobl );

			// Draw the model to the image.
			CLMFaceTracker.drawFaceModel( image, avgFace, true, true,
					true, true, true, triangles, connections, 1, RGBColour.WHITE,
					RGBColour.WHITE, RGBColour.YELLOW, RGBColour.RED );
		}
		
		DisplayUtilities.display( image );
		
		return image;
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
