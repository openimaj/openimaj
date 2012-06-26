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
package org.openimaj.video.processing.tracking;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.tracking.klt.FeatureList;
import org.openimaj.video.tracking.klt.KLTTracker;
import org.openimaj.video.tracking.klt.TrackingContext;

/**
 * 	A tracker that will track one rectangular region using the KLTTracker.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 13 Oct 2011
 */
public class BasicObjectTracker implements ObjectTracker<Rectangle,FImage>
{		
	/** The tracking context for the KLTTracker */
	private TrackingContext trackingContext = new TrackingContext();
	
	/** The feature list used for the tracking */
	private FeatureList featureList = null;
	
	/** The number of features found during the initialisation stage of the tracking */
	private int featuresFound = -1;
	
	/** The tracker used to track the faces */
	private KLTTracker tracker = null;

	/** The accuracy to use when tracking, between 0 and 1 */
	private double accuracy = 0.5;

	/** The previous frame */
	private FImage previousFrame;
	
	/**
	 * 	Default constructor that will use 50 features and an accuracy of 0.5.
	 */
	public BasicObjectTracker()
    {
		this( 50, 0.5 );
    }
	
	/**
	 * 	Default constructor that takes the number of features to be used.
	 * 	Will use an accuracy of 0.5.
	 * 
	 *	@param nFeatures The number of features to use.
	 */
	public BasicObjectTracker( int nFeatures )
	{
		this( nFeatures, 0.5 );
	}
	
	/**
	 * 	Constructor that takes the accuracy to use for tracking. Will use 50
	 * 	features.
	 * 
	 *	@param accuracy The accuracy to use.
	 */
	public BasicObjectTracker( double accuracy )
	{
		this( 50, accuracy );
	}
	
	/**
	 * 	Constructor that takes the number of features to use and the accuracy
	 * 	for tracking.
	 * 
	 *	@param nFeatures The number of features to use.
	 *	@param accuracy The accuracy to use
	 */
	public BasicObjectTracker( int nFeatures, double accuracy )
	{
		this.featureList = new FeatureList( nFeatures );
		this.accuracy  = accuracy;
		tracker = new KLTTracker( trackingContext, featureList );
	}
	
	/**
	 * 	Reset this tracker using the given image
	 * 	@return TRUE if the tracking continued ok; FALSE otherwise
	 */
	@Override
	public List<Rectangle> trackObject( FImage img )
	{
		List<Rectangle> trackedObjects = new ArrayList<Rectangle>();
		
		tracker.trackFeatures( previousFrame, img );
		
		// If we're losing features left-right and centre then we say
		// we've lost the object we're tracking
		if( featureList.countRemainingFeatures() <= featuresFound * accuracy )
			return trackedObjects;
		
		trackedObjects.add( featureList.getBounds() );
		
		previousFrame = img;
		
		return trackedObjects;
	}

	/**
	 * 	Initialise this tracker with a particular area on a particular
	 * 	image.
	 * 
	 *  @param bounds The area to track
	 *  @param img The image
	 */
	@Override
	public List<Rectangle> initialiseTracking( Rectangle bounds, FImage img )
    {
		List<Rectangle> initialObjects = new ArrayList<Rectangle>();
		
		// Set the tracking area to be the face found
		trackingContext.setTargetArea( bounds );
		
		// Select the good features from the area
		tracker.selectGoodFeatures( img );

		// Remember how many features we found, so that if we
		// start to lose them, we can re-initialise the tracking
		featuresFound = featureList.countRemainingFeatures();

		// Add the initial bounds as the found object
		initialObjects.add( bounds );
		
		previousFrame = img;
        
        return initialObjects;
    }
	
	/**
	 * 	Returns the list of features that the tracker has been tracking.
	 *	@return the {@link FeatureList}
	 */
	public FeatureList getFeatureList()
	{
		return featureList;
	}
}
