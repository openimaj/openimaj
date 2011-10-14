/**
 * 
 */
package org.openimaj.video.processing.timefinder;

import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.video.Video;
import org.openimaj.video.VideoCache;
import org.openimaj.video.processing.tracking.ObjectTracker;
import org.openimaj.video.timecode.HrsMinSecFrameTimecode;
import org.openimaj.video.timecode.VideoTimecode;

/**
 * 	This class is a general class for finding the time range in which a specific
 * 	object is found within a video.  Given a seed frame in which the object appears,
 * 	this class will track forwards and backwards in the video stream to find when
 * 	the object disappears. It is then able to provide the timecodes at which the
 * 	object can be found within the video.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 14 Oct 2011
 */
public class ObjectTimeFinder
{
	/**
	 * 	Given a video, a keyframe (timecode) and a region of the image, 
	 * 	this method will attempt to track the the contents of the rectangle
	 * 	from the given frame, back and forth to find the place
	 * 	at which the object appears and disappears from the video.
	 *
	 *	@param <I> The type of image returned from the video
	 *	@param <O> The type of object being tracked
	 *	@param objectTracker The object tracker that will track the object for us
	 *	@param video The video 
	 *	@param keyframeTime The keyframe timecode to start at
	 *	@param bounds The bounding box of the object you wish to track
	 *	@return An {@link IndependentPair} of timecodes delineating the start
	 *		and end of the video which contains the object
	 */
	public <I extends Image<?,I>,O> 
		IndependentPair<VideoTimecode,VideoTimecode> trackObject(
				ObjectTracker<O,I> objectTracker, Video<I> video, 
				VideoTimecode keyframeTime, Rectangle bounds )
	{
		// Set up the initial start and end timecodes. We'll update these
		// as we scan through the video
		HrsMinSecFrameTimecode startTime = new HrsMinSecFrameTimecode( 
				keyframeTime.getFrameNumber(), video.getFPS() );
		HrsMinSecFrameTimecode endTime = new HrsMinSecFrameTimecode( 
				keyframeTime.getFrameNumber(), video.getFPS() );
		
		// Now set the video to the start frame
		video.setCurrentFrameIndex( startTime.getFrameNumber() );
		I image = video.getCurrentFrame();
		I keyframeImage = image.clone();
		
		// Initialise the tracking with the start frame
		objectTracker.initialiseTracking( bounds, image );
		
		// Now we'll scan forwards in the video to find out when the object
		// disappears from view
		boolean foundObject = true;
		while( foundObject && image != null )
		{
			// Track the object
			List<O> objects = objectTracker.trackObject( image );
			
			// If we've found no objects in this frame, then we've lost
			// sight of the object and must stop
			if( objects.size() == 0 )
				foundObject = false;
			else
			{
				// Move on to the next frame
				image = video.getNextFrame();
				
				// Update the end time
				endTime.setFrameNumber( video.getCurrentFrameIndex() );
			}
		}
		
		// Reinitialise the object tracker to our start point
		objectTracker.initialiseTracking( bounds, keyframeImage );
		foundObject = true;

		// Now we're going to scan backwards through the video. However, scanning
		// backwards can be really slow, so what we're going to do is scan back
		// by a big chunk and then cache that chunk, before we track back through
		// it frame by frame. This reduces the number of times we need to do the
		// expensive backwards track.
		int nFramesToJumpBack = (int)video.getFPS()*10;	// We'll go 10 seconds at a time

		// currentTimecode will give the start of the chunk
		HrsMinSecFrameTimecode currentTimecode = new HrsMinSecFrameTimecode( 
				Math.max( startTime.getFrameNumber() - nFramesToJumpBack, 0 ), 
				video.getFPS() );

		// Now keep looping until we either lose the object or 
		// we hit the start of the video
		while( foundObject && currentTimecode.getFrameNumber() >= 0 )
		{
			System.out.println( "Caching "+currentTimecode+" to "+startTime );
			
			// Slightly confusingly here, the startTime is the end of the chunk
			VideoCache<I> vc = VideoCache.cacheVideo( video, currentTimecode, startTime );
			
			// Now track back through the cached video
			for( int n = 1; n <= vc.getNumberOfFrames(); n++ )
			{
				// Track backwards through the frames
				image = vc.getFrame( vc.getNumberOfFrames()-n );
				
				// Track the object
				List<O> objects = objectTracker.trackObject( image );
				
				// If we've found no objects in this frame, then we've lost
				// sight of the object and must stop
				if( objects.size() == 0 )
				{
					foundObject = false;
					break;
				}
				else
				{
					// Update the start time
					startTime.setFrameNumber( startTime.getFrameNumber()-1 );
				}			
			}

			// If we're already at frame zero, we should end this loop
			if( currentTimecode.getFrameNumber() == 0 )
				foundObject = false;
			
			// Update the chunk timecode if we need to keep going
			currentTimecode.setFrameNumber( 
					Math.max( startTime.getFrameNumber() - nFramesToJumpBack, 0 ) );
		}
	
		// Return the videos
		return new IndependentPair<VideoTimecode,VideoTimecode>( startTime, endTime );
	}
}
