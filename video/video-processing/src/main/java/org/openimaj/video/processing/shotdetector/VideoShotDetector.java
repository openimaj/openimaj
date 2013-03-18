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
package org.openimaj.video.processing.shotdetector;

import gnu.trove.list.array.TDoubleArrayList;

import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.DoubleFV;
import org.openimaj.image.Image;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplay.EndAction;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.processor.VideoProcessor;
import org.openimaj.video.timecode.HrsMinSecFrameTimecode;
import org.openimaj.video.timecode.VideoTimecode;

/**
 * 	Video shot detector class implemented as a video display listener. This
 * 	means that shots can be detected as the video plays. The class also
 * 	supports direct processing of a video file (with no display).
 * 	<p>
 * 	The default threshold boundary should be set by implementing methods
 * 	as the distances returned by those implementations will only sensibly understand
 * 	where the threshold should be.
 * 	<p>
 * 	Only the last keyframe is stored during processing, so if you want to store
 * 	a list of keyframes you must store this list yourself by listening to the
 * 	ShotDetected event which provides a VideoKeyframe which has a timecode
 * 	and an image. Each event will receive the same VideoKeyframe instance
 * 	containing different information. Use VideoKeyframe#clone() to make a copy.
 *
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@param <I> The type of image
 *	@created 1 Jun 2011
 */
public abstract class VideoShotDetector<I extends Image<?,I>>
	extends VideoProcessor<I>
	implements VideoDisplayListener<I>
{
	/** The current keyframe */
	private VideoKeyframe<I> currentKeyframe = null;

	/** The list of shot boundaries */
	private final List<ShotBoundary<I>> shotBoundaries =
		new ArrayList<ShotBoundary<I>>();

	/** Differences between consecutive frames */
	private final TDoubleArrayList differentials = new TDoubleArrayList();

	/** The frame we're at within the video */
	private int frameCounter = 0;

	/** The video being processed */
	private Video<I> video = null;

	/** Whether to find keyframes */
	private boolean findKeyframes = true;

	/** Whether to store all frame differentials */
	private boolean storeAllDiffs = false;

	/** Whether an event is required to be fired next time */
	private boolean needFire = false;

	/** Whether the last processed frame was a boundary */
	protected boolean lastFrameWasBoundary = false;

	/** A list of the listeners that want to know about new shots */
	private final List<ShotDetectedListener<I>> listeners = new ArrayList<ShotDetectedListener<I>>();

	/** The number of frames per second of the source material */
	private double fps = 25;

	/** Whether the first frame is being processed */
	private boolean firstFrame = true;

	/** Whether to generate a shot boundary for the first frame of a video */
	private final boolean generateStartShot = true;

	/** The threshold to use to determine a shot boundary - this default is arbitrary */
	protected double threshold = 100;

	/**
	 * 	This constructor assumes that you will set the number of
	 * 	frames per second your video uses (using {@link #setFPS(double)})
	 * 	when you know what that will be, otherwise your timecodes will
	 * 	all be messed up.
	 */
	public VideoShotDetector()
	{
	}

	/**
	 * 	Default constructor that allows the processor to be used ad-hoc
	 * 	on frames from any source. The number of FPS is required so that
	 * 	timecodes can be generated for the shot boundaries. Be aware that if
	 * 	your source material does not have a specific number of frames per
	 * 	second then the timecodes will not have any meaning in the detected
	 * 	shot boundaries.
	 *
	 * 	@param fps The number of frames per second of the source material
	 */
	public VideoShotDetector( final double fps )
	{
		this.fps = fps;
	}

	/**
	 * 	Constructor that takes the video file to process.
	 *
	 *  @param video The video to process.
	 */
	public VideoShotDetector( final Video<I> video )
	{
		this( video, false );
	}

	/**
	 * 	Default constructor that takes the video file to process and
	 * 	whether or not to display the video as it's being processed.
	 *
	 *  @param video The video to process
	 *  @param display Whether to display the video during processing.
	 */
	public VideoShotDetector( final Video<I> video, final boolean display )
    {
		this.video = video;
		this.fps = video.getFPS();
		if( display )
		{
			try
	        {
		        final VideoDisplay<I> vd = VideoDisplay.createVideoDisplay( video );
				vd.addVideoListener( this );
				vd.setEndAction( EndAction.STOP_AT_END );
	        }
	        catch( final HeadlessException e )
	        {
		        e.printStackTrace();
	        }
		}
    }

	/**
	 * 	Returns whether the last processed frame was a shot boundary - that is
	 * 	the last processed frame marks a new scene.
	 *	@return Whether the last frame was a boundary.
	 */
	public boolean wasLastFrameBoundary()
	{
		return this.lastFrameWasBoundary;
	}

	/**
	 * 	Process the video.
	 */
	@Override
	public void process()
	{
		super.process( this.video );
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.video.VideoDisplayListener#afterUpdate(org.openimaj.video.VideoDisplay)
	 */
	@Override
	public void afterUpdate( final VideoDisplay<I> display )
    {
    }

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.video.VideoDisplayListener#beforeUpdate(org.openimaj.image.Image)
	 */
	@Override
	public void beforeUpdate( final I frame )
    {
		this.checkForShotBoundary( frame );
    }

	/**
	 * 	Add the given shot detected listener to the list of listeners in this
	 * 	object
	 *
	 *  @param sdl The shot detected listener to add
	 */
	public void addShotDetectedListener( final ShotDetectedListener<I> sdl )
	{
		this.listeners.add( sdl );
	}

	/**
	 * 	Remove the given shot detected listener from this object.
	 *
	 *  @param sdl The shot detected listener to remove
	 */
	public void removeShotDetectedListener( final ShotDetectedListener<I> sdl )
	{
		this.listeners.remove( sdl );
	}

	/**
	 * 	Return the last shot boundary in the list.
	 *	@return The last shot boundary in the list.
	 */
	public ShotBoundary<I> getLastShotBoundary()
	{
		if( this.shotBoundaries.size() == 0 )
			return null;
		return this.shotBoundaries.get( this.shotBoundaries.size()-1 );
	}

	/**
	 * 	Returns the last video keyframe that was generated.
	 *	@return The last video keyframe that was generated.
	 */
	public VideoKeyframe<I> getLastKeyframe()
	{
		return this.currentKeyframe;
	}

	/**
	 * 	Checks whether a shot boundary occurred between the given frame
	 * 	and the previous frame, and if so, it will add a shot boundary
	 * 	to the shot boundary list.
	 *
	 *  @param frame The new frame to process.
	 */
	private void checkForShotBoundary( final I frame )
	{
		this.lastFrameWasBoundary = false;
		final double dist = this.getInterframeDistance( frame );

		if( this.storeAllDiffs )
		{
			this.differentials.add( dist );
			this.fireDifferentialCalculated( new HrsMinSecFrameTimecode(
					this.frameCounter, this.video.getFPS() ), dist, frame );
		}

//		System.out.println( "is "+dist+" > "+this.threshold+"? "+(dist>this.threshold) );

		// We generate a shot boundary if the threshold is exceeded or we're
		// at the very start of the video.
		if( dist > this.threshold || (this.generateStartShot && this.firstFrame) )
		{
			this.needFire = true;

			// The timecode of this frame
			final VideoTimecode tc = new HrsMinSecFrameTimecode(
					this.frameCounter, this.fps );

			// The last shot boundary we created
			final ShotBoundary<I> sb = this.getLastShotBoundary();

			// If this frame is sequential to the last
			if( sb != null &&
				tc.getFrameNumber() - sb.getTimecode().getFrameNumber() < 4  )
			{
				// If the shot boundary is a fade, we simply change the end
				// timecode, otherwise we replace the given shot boundary
				// with a new one.
				if( sb instanceof FadeShotBoundary )
						((FadeShotBoundary<I>)sb).setEndTimecode( tc );
				else
				{
					// Remove the old one.
					this.shotBoundaries.remove( sb );

					// Change it to a fade.
					final FadeShotBoundary<I> fsb = new FadeShotBoundary<I>( sb );
					fsb.setEndTimecode( tc );

					this.lastFrameWasBoundary = true;

					if( this.findKeyframes )
					{
						if( this.currentKeyframe == null )
							this.currentKeyframe = new VideoKeyframe<I>( tc, frame );
						else
						{
							this.currentKeyframe.timecode = tc;
							this.currentKeyframe.imageAtBoundary = frame.clone();
						}
						fsb.keyframe = this.currentKeyframe.clone();
					}

					this.shotBoundaries.add( fsb );
				}
			}
			else
			{
				// Create a new shot boundary
				final ShotBoundary<I> sb2 = new ShotBoundary<I>( tc );

				if( this.findKeyframes )
				{
					if( this.currentKeyframe == null )
						this.currentKeyframe = new VideoKeyframe<I>( tc, frame );
					else
					{
						this.currentKeyframe.timecode = tc;
						this.currentKeyframe.imageAtBoundary = frame;
					}
					sb2.keyframe = this.currentKeyframe.clone();
				}

				this.lastFrameWasBoundary = true;
				this.shotBoundaries.add( sb2 );
				this.fireShotDetected( sb2, this.currentKeyframe );
			}
		}
		else
		{
			// The frame matches with the last (no boundary) but we'll check whether
			// the last thing added to the shot boundaries was a fade and its
			// end time was the timecode before this one. If so, we can fire a
			// shot detected event.
			if( this.frameCounter > 0 && this.needFire )
			{
				this.needFire = false;

				final VideoTimecode tc = new HrsMinSecFrameTimecode(
						this.frameCounter-1, this.fps );

				final ShotBoundary<I> lastShot = this.getLastShotBoundary();

				if( lastShot != null && lastShot instanceof FadeShotBoundary )
					if( ((FadeShotBoundary<I>)lastShot).getEndTimecode().equals( tc ) )
						this.fireShotDetected( lastShot, this.getLastKeyframe() );
			}
		}

		this.frameCounter++;
		this.firstFrame = false;
    }

	/**
	 * 	Returns the inter-frame distance between this frame and the last.
	 *	@return The inter-frame distance
	 */
	protected abstract double getInterframeDistance( I thisFrame );

	/**
	 * 	Get the list of shot boundaries that have been extracted so far.
	 *  @return The list of shot boundaries.
	 */
	public List<ShotBoundary<I>> getShotBoundaries()
	{
		return this.shotBoundaries;
	}

	/**
	 * 	Set the threshold that will determine a shot boundary.
	 *
	 *  @param threshold The new threshold.
	 */
	public void setThreshold( final double threshold )
	{
		this.threshold = threshold;
	}

	/**
	 * 	Returns the current threshold value.
	 *	@return The current threshold
	 */
	public double getThreshold()
	{
		return this.threshold;
	}

	/**
	 * 	Set whether to store keyframes of boundaries when they
	 * 	have been found.
	 *
	 *	@param k TRUE to store keyframes; FALSE otherwise
	 */
	public void setFindKeyframes( final boolean k )
	{
		this.findKeyframes = k;
	}

	/**
	 * 	Set whether to store differentials during the processing
	 * 	stage.
	 *
	 *	@param d TRUE to store all differentials; FALSE otherwise
	 */
	public void setStoreAllDifferentials( final boolean d )
	{
		this.storeAllDiffs = d;
	}

	/**
	 * 	Get the differentials between frames (if storeAllDiff is true).
	 *	@return The differentials between frames as a List of Double.
	 */
	public DoubleFV getDifferentials()
	{
		return new DoubleFV( this.differentials.toArray() );
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.video.processor.VideoProcessor#processFrame(org.openimaj.image.Image)
	 */
	@Override
    public I processFrame( final I frame )
    {
		if( frame == null ) return null;
		this.checkForShotBoundary( frame );
		return frame;
    }

	/**
	 * 	Fire the event to the listeners that a new shot has been detected.
	 *  @param sb The shot boundary defintion
	 *  @param vk The video keyframe
	 */
	protected void fireShotDetected( final ShotBoundary<I> sb, final VideoKeyframe<I> vk )
	{
		for( final ShotDetectedListener<I> sdl : this.listeners )
			sdl.shotDetected( sb, vk );
	}

	/**
	 * 	Fired each time a differential is calculated between frames.
	 *	@param vt The timecode of the differential
	 *	@param d The differential value
	 *	@param frame The different frame
	 */
	protected void fireDifferentialCalculated( final VideoTimecode vt, final double d, final I frame )
	{
		for( final ShotDetectedListener<I> sdl : this.listeners )
			sdl.differentialCalculated( vt, d, frame );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.processor.VideoProcessor#reset()
	 */
	@Override
	public void reset()
	{
	}

	/**
	 * 	Set the frames per second value for the video being processed.
	 *	@param fps The number of frames per second.
	 */
	public void setFPS( final double fps )
	{
		this.fps = fps;
	}
}
