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
package org.openimaj.video;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.openimaj.audio.AudioPlayer;
import org.openimaj.audio.AudioStream;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.time.TimeKeeper;
import org.openimaj.time.Timecode;
import org.openimaj.video.timecode.HrsMinSecFrameTimecode;

/**
 * Basic class for displaying videos.
 * <p>
 * {@link VideoDisplayListener}s can be added to be informed when the display is
 * about to be updated or has just been updated.
 * {@link VideoDisplayStateListener}s can be added to be informed about when the
 * playback state of the display changes (e.g. when it entered play or pause
 * mode). {@link VideoPositionListener}s can be added to be informed when the
 * video hits the start or end frame.
 * <p>
 * The video can be played, paused and stopped. Pause and stop have slightly
 * different semantics. After pause mode, the playback will continue from the
 * point of pause; whereas after stop mode, the playback will continue from the
 * start. Also, when in pause mode, frames are still sent to any listeners at
 * roughly the frame-rate of the video; compare this to stop mode where no video
 * events are fired. The default is that when the video comes to its end, the
 * display is automatically set to stop mode. The action at the end of the video
 * can be altered with {@link #setEndAction(EndAction)}.
 * <p>
 * The VideoDisplay constructor takes an {@link ImageComponent} which is used to
 * draw the video to. This allows video displays to be integrated into a Swing
 * UI. Use the {@link #createVideoDisplay(Video)} to have the video display
 * create an appropriate image component and a basic frame into which to display
 * the video. There is a {@link #createOffscreenVideoDisplay(Video)} method
 * which will not display the resulting component.
 * <p>
 * The player uses a separate object for controlling the speed of playback. The
 * {@link TimeKeeper} class is used to generate timestamps which the video
 * display will do its best to synchronise with. A basic time keeper is
 * encapsulated in this class ({@link BasicVideoTimeKeeper}) which is used for
 * video without audio. The timekeeper can be set using
 * {@link #setTimeKeeper(TimeKeeper)}. As video is read from the video stream,
 * each frame's timestamp is compared with the current time of the timekeeper.
 * If the frame should have been shown in the past the video display will
 * attempt to read video frames until the frame's timestamp is in the future.
 * Once its in the future it will wait until the frame's timestamp becomes
 * current (or in the past by a small amount). The frame is then displayed. Note
 * that in the case of live video, the display does not check to see if the
 * frame was in the past - it always assumes that {@link Video#getNextFrame()}
 * will return the latest frame to be displayed.
 * <p>
 * The VideoDisplay class can also accept an {@link AudioStream} as input. If
 * this is supplied, an {@link AudioPlayer} will be instantiated to playback the
 * audio and this audio player will be designated the {@link TimeKeeper} for the
 * video playback. That means the audio will control the speed of playback for
 * the video. An example of playing back a video with sound might look like
 * this:
 * <p>
 * 
 * <pre>
 * <code>
 * 		XuggleVideo xv = new XuggleVideo( videoFile );
 * 		XuggleAudio xa = new XuggleAudio( videoFile );
 * 		VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay( xv, xa );
 * </code>
 * </pre>
 * <p>
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            the image type of the frames in the video
 */
public class VideoDisplay<T extends Image<?, T>> implements Runnable
{
	/**
	 * Enumerator to represent the state of the player.
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 */
	public enum Mode
	{
		/** The video is playing */
		PLAY,

		/** The video is paused */
		PAUSE,

		/** The video is stopped */
		STOP,

		/** The video is seeking */
		SEEK,

		/** The video is closed */
		CLOSED;
	}

	/**
	 * An enumerator for what to do when the video reaches the end.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 14 Aug 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	public enum EndAction
	{
		/** The video will be switched to STOP mode at the end */
		STOP_AT_END,

		/** The video will be switched to PAUSE mode at the end */
		PAUSE_AT_END,

		/** The video will be looped */
		LOOP,

		/** The player and timekeeper will be CLOSED at the end */
		CLOSE_AT_END,
	}

	/**
	 * A timekeeper for videos without audio - uses the system time to keep
	 * track of where in a video a video should be. Also used for live videos
	 * that are to be displayed at a given rate.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 14 Aug 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	public class BasicVideoTimeKeeper implements TimeKeeper<Timecode>
	{
		/** The current time we'll return */
		private long currentTime = 0;

		/** The last time the timer was started */
		private long lastStarted = 0;

		/** The time the timer was paused */
		private long pausedAt = -1;

		/** The amount of time to offset the timer */
		private long timeOffset = 0;

		/** Whether the timer is running */
		private boolean isRunning = false;

		/** The timecode object we'll update */
		private HrsMinSecFrameTimecode timecode = null;

		/** Whether the timekeeper is for live video or not */
		private boolean liveVideo = false;

		/**
		 * Default constructor
		 * 
		 * @param liveVideo
		 *            Whether the timekeeper is for a live video or for a video
		 *            that supports pausing
		 */
		public BasicVideoTimeKeeper(final boolean liveVideo)
		{
			this.timecode = new HrsMinSecFrameTimecode(0,
					VideoDisplay.this.video.getFPS());
			this.liveVideo = liveVideo;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.openimaj.time.TimeKeeper#run()
		 */
		@Override
		public void run()
		{
			if (this.lastStarted == 0)
				this.lastStarted = System.currentTimeMillis();
			else if (this.supportsPause())
				this.timeOffset += System.currentTimeMillis() - this.pausedAt;

			this.isRunning = true;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.openimaj.time.TimeKeeper#stop()
		 */
		@Override
		public void stop()
		{
			this.isRunning = false;
			this.currentTime = 0;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.openimaj.time.TimeKeeper#getTime()
		 */
		@Override
		public Timecode getTime()
		{
			if (this.isRunning)
			{
				// Update the current time.
				this.currentTime = (System.currentTimeMillis() -
						this.lastStarted - this.timeOffset);
				this.timecode.setTimecodeInMilliseconds(this.currentTime);
			}

			return this.timecode;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.openimaj.time.TimeKeeper#supportsPause()
		 */
		@Override
		public boolean supportsPause()
		{
			return !this.liveVideo;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.openimaj.time.TimeKeeper#supportsSeek()
		 */
		@Override
		public boolean supportsSeek()
		{
			return !this.liveVideo;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.openimaj.time.TimeKeeper#seek(long)
		 */
		@Override
		public void seek(final long timestamp)
		{
			if (!this.liveVideo)
				this.lastStarted = System.currentTimeMillis() - timestamp;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.openimaj.time.TimeKeeper#reset()
		 */
		@Override
		public void reset()
		{
			this.lastStarted = 0;
			this.pausedAt = -1;
			this.run();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.openimaj.time.TimeKeeper#pause()
		 */
		@Override
		public void pause()
		{
			if (!this.liveVideo)
			{
				this.isRunning = false;
				this.pausedAt = System.currentTimeMillis();
			}
		}

		/**
		 * Set the time offset to use in the current time calculation. Can be
		 * used to force the time keeper to start at a different point in time.
		 * 
		 * @param timeOffset
		 *            the new time offset.
		 */
		public void setTimeOffset(final long timeOffset)
		{
			this.timeOffset = timeOffset;
		}
	}

	/** The default mode is to play the player */
	private Mode mode = Mode.PLAY;

	/** The screen to show the player in */
	private final ImageComponent screen;

	/** The video being displayed */
	private Video<T> video;

	/** The list of video display listeners */
	private final List<VideoDisplayListener<T>> videoDisplayListeners;

	/** List of state listeners */
	private final List<VideoDisplayStateListener> stateListeners;

	/** List of position listeners */
	private final List<VideoPositionListener> positionListeners;

	/** Whether to display the screen */
	private boolean displayMode = true;

	/** What to do at the end of the video */
	private EndAction endAction = EndAction.STOP_AT_END;

	/** If audio comes with the video, then we play it with the player */
	private AudioPlayer audioPlayer = null;

	/** The time keeper to use to synch the video */
	private TimeKeeper<? extends Timecode> timeKeeper = null;

	/** This is the calculated FPS that the video player is playing at */
	private double calculatedFPS = 0;

	/** Whether to fire video updates or not */
	private final boolean fireUpdates = true;

	/** The timestamp of the frame currently being displayed */
	private long currentFrameTimestamp = 0;

	/** The current frame being displayed */
	private T currentFrame = null;

	/** A count of the number of frames that have been dropped while playing */
	private int droppedFrameCount = 0;

	/** Whether to calculate frames per second at each frame */
	private boolean calculateFPS = true;

	/**
	 * Construct a video display with the given video and frame.
	 * 
	 * @param v
	 *            the video
	 * @param screen
	 *            the frame to draw into.
	 */
	public VideoDisplay(final Video<T> v, final ImageComponent screen)
	{
		this(v, null, screen);
	}

	/**
	 * Construct a video display with the given video and audio
	 * 
	 * @param v
	 *            The video
	 * @param a
	 *            The audio
	 * @param screen
	 *            The frame to draw into.
	 */
	public VideoDisplay(final Video<T> v, final AudioStream a, final ImageComponent screen)
	{
		this.video = v;

		// If we're given audio, we create an audio player that will also
		// act as our synchronisation time keeper.
		if (a != null)
		{
			this.audioPlayer = new AudioPlayer(a);
			this.timeKeeper = this.audioPlayer;
		}
		// If no audio is provided, we'll use a basic time keeper
		else
			this.timeKeeper = new BasicVideoTimeKeeper(this.video.countFrames() == -1);

		this.screen = screen;
		this.videoDisplayListeners = new ArrayList<VideoDisplayListener<T>>();
		this.stateListeners = new ArrayList<VideoDisplayStateListener>();
		this.positionListeners = new ArrayList<VideoPositionListener>();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void run()
	{
		BufferedImage bimg = null;

		// Current frame
		this.currentFrame = this.video.getCurrentFrame();
		// this.currentFrameTimestamp = this.video.getTimeStamp();

		// We'll estimate each iteration how long we should wait before
		// trying again.
		long roughSleepTime = 10;

		// Tolerance is an estimate (it only need be rough) of the time it takes
		// to get a frame from the video and display it.
		final long tolerance = 10;

		// Used to calculate the FPS the video's playing at
		long lastTimestamp = 0, currentTimestamp = 0;

		// Just about the start the video
		this.fireVideoStartEvent();

		// Start the timekeeper (if we have audio, this will start the
		// audio playing)
		new Thread(this.timeKeeper).start();

		// Keep going until the mode becomes closed
		while (this.mode != Mode.CLOSED)
		{
			// System.out.println( "[Main loop ping: "+this.mode+"]" );

			// If we're on stop we don't update at all
			if (this.mode == Mode.PLAY || this.mode == Mode.PAUSE)
			{
				// Calculate the display's FPS
				if (this.calculateFPS)
				{
					currentTimestamp = System.currentTimeMillis();
					this.calculatedFPS = 1000d / (currentTimestamp - lastTimestamp);
					lastTimestamp = currentTimestamp;
				}

				// We initially set up with the last frame
				T nextFrame = this.currentFrame;
				long nextFrameTimestamp = this.currentFrameTimestamp;

				if (this.mode == Mode.PLAY)
				{
					// We may need to catch up if we're behind in display frames
					// rather than ahead. In which case, we keep skipping frames
					// until we find one that's in the future.
					// We only do this if we're not working on live video. If
					// we're working on live video, then getNextFrame() will
					// always
					// deliver the latest video frame, so we never have to catch
					// up.
					if (this.video.countFrames() != -1 && this.currentFrame != null)
					{
						final long t = this.timeKeeper.getTime().getTimecodeInMilliseconds();
						// System.out.println( "Should be at "+t );
						int droppedThisRound = -1;
						while (nextFrameTimestamp <= t && nextFrame != null)
						{
							// Get the next frame to determine if it's in the
							// future
							nextFrame = this.video.getNextFrame();
							nextFrameTimestamp = this.video.getTimeStamp();
							// System.out.println("Frame is "+nextFrameTimestamp
							// );
							droppedThisRound++;
						}
						this.droppedFrameCount += droppedThisRound;
						// System.out.println(
						// "Dropped "+this.droppedFrameCount+" frames.");
					}
					else
					{
						nextFrame = this.video.getNextFrame();
						nextFrameTimestamp = this.video.getTimeStamp();
						if (this.currentFrame == null && (this.timeKeeper instanceof VideoDisplay.BasicVideoTimeKeeper))
							((VideoDisplay.BasicVideoTimeKeeper) this.timeKeeper).setTimeOffset(-nextFrameTimestamp);
					}

					// We've got to the end of the video. What should we do?
					if (nextFrame == null)
					{
						// System.out.println( "Video ended" );
						this.processEndAction(this.endAction);
						continue;
					}
				}

				// We process the current frame before we draw it to the screen
				if (this.fireUpdates)
				{
					// nextFrame = this.currentFrame.clone();
					this.fireBeforeUpdate(this.currentFrame);

				}

				// Draw the image into the display
				if (this.displayMode && this.currentFrame != null)
				{
					// System.out.println( "Drawing frame");
					this.screen.setImage(bimg = ImageUtilities.
							createBufferedImageForDisplay(this.currentFrame, bimg));
				}

				// Fire that we've put a frame to the screen
				if (this.fireUpdates)
					this.fireVideoUpdate();

				// Estimate the sleep time for next time
				roughSleepTime = (long) (1000 / this.video.getFPS()) - tolerance;

				if (this.mode == Mode.PLAY)
				{
					// System.out.println("Next frame:   "+nextFrameTimestamp );
					// System.out.println("Current time: "+this.timeKeeper.getTime().getTimecodeInMilliseconds()
					// );

					// Wait until the timekeeper says we should be displaying
					// the next frame
					// We also check to see we're still in play mode, as it's
					// in this wait that the state is most likely to get the
					// time
					// to change, so we need to drop out of this loop if it
					// does.
					while (this.timeKeeper.getTime().getTimecodeInMilliseconds() <
							nextFrameTimestamp && this.mode == Mode.PLAY)
					{
						// System.out.println( "Sleep "+roughSleepTime );
						try {
							Thread.sleep(Math.max(0, roughSleepTime));
						} catch (final InterruptedException e) {
						}
					}

					// The current frame will become what was our next frame
					this.currentFrame = nextFrame;
					this.currentFrameTimestamp = nextFrameTimestamp;
				}
				else
				{
					// We keep delivering frames at roughly the frame rate
					// when in pause mode.
					try {
						Thread.sleep(Math.max(0, roughSleepTime));
					} catch (final InterruptedException e) {
					}
				}
			}
			else
			{
				// In STOP mode, we patiently wait to be played again
				try {
					Thread.sleep(500);
				} catch (final InterruptedException e) {
				}
			}
		}

		/*
		 * This is the old code, for posterity while( true ) { T currentFrame =
		 * null; T nextFrame;
		 * 
		 * if (this.mode == Mode.CLOSED) { this.video.close(); return; }
		 * 
		 * if( this.mode == Mode.SEEK ) { this.video.seek( this.seekTimestamp );
		 * this.videoPlayerStartTime = -1; this.mode = Mode.PLAY;
		 * 
		 * }
		 * 
		 * if(this.mode == Mode.PLAY) { nextFrame = this.video.getNextFrame(); }
		 * else { nextFrame = this.video.getCurrentFrame(); }
		 * 
		 * // If the getNextFrame() returns null then the end of the // video
		 * may have been reached, so we pause the video. if( nextFrame == null )
		 * { switch( this.endAction ) { case STOP_AT_END: this.setMode(
		 * Mode.STOP ); break; case PAUSE_AT_END: this.setMode( Mode.PAUSE );
		 * break; case LOOP: this.seek( 0 ); break; } } else { currentFrame =
		 * nextFrame; }
		 * 
		 * // If we have a frame to draw, then draw it. if( currentFrame != null
		 * && this.mode != Mode.STOP ) { if( this.videoPlayerStartTime == -1 &&
		 * this.mode == Mode.PLAY ) { //
		 * System.out.println("Resseting internal times");
		 * this.firstFrameTimestamp = this.video.getTimeStamp();
		 * this.videoPlayerStartTime = System.currentTimeMillis(); //
		 * System.out.println("First time stamp: " + firstFrameTimestamp); }
		 * else { // This is based on the Xuggler demo code: //
		 * http://xuggle.googlecode
		 * .com/svn/trunk/java/xuggle-xuggler/src/com/xuggle
		 * /xuggler/demos/DecodeAndPlayVideo.java final long systemDelta =
		 * System.currentTimeMillis() - this.videoPlayerStartTime; final long
		 * currentFrameTimestamp = this.video.getTimeStamp(); final long
		 * videoDelta = currentFrameTimestamp - this.firstFrameTimestamp; final
		 * long tolerance = 20; final long sleepTime = videoDelta - tolerance -
		 * systemDelta;
		 * 
		 * if( sleepTime > 0 ) { try { Thread.sleep( sleepTime ); } catch (final
		 * InterruptedException e) { return; } } } } final boolean fireUpdates =
		 * this.videoDisplayListeners.size() != 0; if (toDraw == null) { toDraw
		 * = currentFrame.clone(); } else{ if(currentFrame!=null)
		 * toDraw.internalCopy(currentFrame); } if (fireUpdates) {
		 * this.fireBeforeUpdate(toDraw); }
		 * 
		 * if( this.displayMode ) { this.screen.setImage( bimg =
		 * ImageUtilities.createBufferedImageForDisplay( toDraw, bimg ) ); }
		 * 
		 * if (fireUpdates) { this.fireVideoUpdate(); } }
		 */
	}

	/**
	 * Process the end of the video action.
	 * 
	 * @param e
	 *            The end action to process
	 */
	protected void processEndAction(final EndAction e)
	{
		this.fireVideoEndEvent();

		switch (e)
		{
		// The video needs to loop, so we reset the video, any audio player,
		// the timekeeper back to zero. We also have to zero the current frame
		// timestamp so that the main loop will read a new frame.
		case LOOP:
			this.video.reset();
			if (this.audioPlayer != null)
				this.audioPlayer.reset();
			this.timeKeeper.reset();
			this.currentFrameTimestamp = 0;
			this.fireVideoStartEvent();
			break;

		// Pause the video player
		case PAUSE_AT_END:
			this.setMode(Mode.PAUSE);
			break;

		// Stop the video player
		case STOP_AT_END:
			this.setMode(Mode.STOP);
			break;

		// Close the video player
		case CLOSE_AT_END:
			this.setMode(Mode.CLOSED);
			break;
		}
	}

	/**
	 * Close the video display. Causes playback to stop, and further events are
	 * ignored.
	 */
	public synchronized void close()
	{
		this.setMode(Mode.CLOSED);
	}

	/**
	 * Set whether this player is playing, paused or stopped. This method will
	 * also control the state of the timekeeper by calling its run, stop or
	 * reset method.
	 * 
	 * @param m
	 *            The new mode
	 */
	synchronized public void setMode(final Mode m)
	{
		// System.out.println( "Mode is: "+this.mode+"; setting to "+m );

		// If we're already closed - stop allowing mode changes
		if (this.mode == Mode.CLOSED)
			return;

		// No change in the mode? Just return
		if (m == this.mode)
			return;

		switch (m)
		{
		// -------------------------------------------------
		case PLAY:
			if (this.mode == Mode.STOP)
				this.fireVideoStartEvent();

			// Restart the timekeeper
			new Thread(this.timeKeeper).start();

			// Seed the player with the next frame
			this.currentFrame = this.video.getCurrentFrame();
			this.currentFrameTimestamp = this.video.getTimeStamp();

			break;
		// -------------------------------------------------
		case STOP:
			this.timeKeeper.stop();
			this.timeKeeper.reset();
			if (this.audioPlayer != null)
			{
				this.audioPlayer.stop();
				this.audioPlayer.reset();
			}
			this.video.reset();
			this.currentFrameTimestamp = 0;
			break;
		// -------------------------------------------------
		case PAUSE:
			// If we can pause the timekeeper, that's what
			// we'll do. If we can't, then it will have to keep
			// running while we pause the video (the video will still get
			// paused).
			System.out.println("Does timekeeper support pause? " + this.timeKeeper.supportsPause());
			if (this.timeKeeper.supportsPause())
				this.timeKeeper.pause();
			break;
		// -------------------------------------------------
		case CLOSED:
			// Kill everything (same as stop)
			this.timeKeeper.stop();
			this.video.close();
			break;
		// -------------------------------------------------
		default:
			break;
		}

		// Update the mode
		this.mode = m;

		// Let the listeners know we've changed mode
		this.fireStateChanged();
	}

	/**
	 * Returns the current state of the video display.
	 * 
	 * @return The current state as a {@link Mode}
	 */
	protected Mode getMode()
	{
		return this.mode;
	}

	/**
	 * Fire the event to the video listeners that a frame is about to be
	 * displayed on the video.
	 * 
	 * @param currentFrame
	 *            The frame that is about to be displayed
	 */
	protected void fireBeforeUpdate(final T currentFrame) {
		synchronized (this.videoDisplayListeners) {
			for (final VideoDisplayListener<T> vdl : this.videoDisplayListeners) {
				vdl.beforeUpdate(currentFrame);
			}
		}
	}

	/**
	 * Fire the event to the video listeners that a frame has been put on the
	 * display
	 */
	protected void fireVideoUpdate() {
		synchronized (this.videoDisplayListeners) {
			for (final VideoDisplayListener<T> vdl : this.videoDisplayListeners) {
				vdl.afterUpdate(this);
			}
		}
	}

	/**
	 * Get the frame the video is being drawn to
	 * 
	 * @return the frame
	 */
	public ImageComponent getScreen() {
		return this.screen;
	}

	/**
	 * Get the video
	 * 
	 * @return the video
	 */
	public Video<T> getVideo() {
		return this.video;
	}

	/**
	 * Change the video that is being displayed by this video display.
	 * 
	 * @param newVideo
	 *            The new video to display.
	 */
	public void changeVideo(final Video<T> newVideo)
	{
		this.video = newVideo;
		this.timeKeeper = new BasicVideoTimeKeeper(newVideo.countFrames() == -1);
	}

	/**
	 * Add a listener that will get fired as every frame is displayed.
	 * 
	 * @param dsl
	 *            the listener
	 */
	public void addVideoListener(final VideoDisplayListener<T> dsl) {
		synchronized (this.videoDisplayListeners) {
			this.videoDisplayListeners.add(dsl);
		}

	}

	/**
	 * Add a listener for the state of this player.
	 * 
	 * @param vdsl
	 *            The listener to add
	 */
	public void addVideoDisplayStateListener(final VideoDisplayStateListener vdsl)
	{
		this.stateListeners.add(vdsl);
	}

	/**
	 * Remove a listener from the state of this player
	 * 
	 * @param vdsl
	 *            The listener
	 */
	public void removeVideoDisplayStateListener(final VideoDisplayStateListener vdsl)
	{
		this.stateListeners.remove(vdsl);
	}

	/**
	 * Fire the state changed event
	 */
	protected void fireStateChanged()
	{
		for (final VideoDisplayStateListener s : this.stateListeners)
		{
			s.videoStateChanged(this.mode, this);
			switch (this.mode)
			{
			case PAUSE:
				s.videoPaused(this);
				break;
			case PLAY:
				s.videoPlaying(this);
				break;
			case STOP:
				s.videoStopped(this);
				break;
			case CLOSED:
				break; // TODO: Need to add more states to video state listener
			case SEEK:
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Add a video position listener to this display
	 * 
	 * @param vpl
	 *            The video position listener
	 */
	public void addVideoPositionListener(final VideoPositionListener vpl)
	{
		this.positionListeners.add(vpl);
	}

	/**
	 * Remove visible panty lines... or video position listeners.
	 * 
	 * @param vpl
	 *            The video position listener
	 */
	public void removeVideoPositionListener(final VideoPositionListener vpl)
	{
		this.positionListeners.remove(vpl);
	}

	/**
	 * Fire the event that says the video is at the start.
	 */
	protected void fireVideoStartEvent()
	{
		for (final VideoPositionListener vpl : this.positionListeners)
			vpl.videoAtStart(this);
	}

	/**
	 * Fire the event that says the video is at the end.
	 */
	protected void fireVideoEndEvent()
	{
		for (final VideoPositionListener vpl : this.positionListeners)
			vpl.videoAtEnd(this);
	}

	/**
	 * Pause or resume the display. This will only have an affect if the video
	 * is not in STOP mode.
	 */
	public void togglePause() {
		if (this.mode == Mode.CLOSED)
			return;

		if (this.mode == Mode.PLAY)
			this.setMode(Mode.PAUSE);
		else if (this.mode == Mode.PAUSE)
			this.setMode(Mode.PLAY);
	}

	/**
	 * Is the video paused?
	 * 
	 * @return true if paused; false otherwise.
	 */
	public boolean isPaused() {
		return this.mode == Mode.PAUSE;
	}

	/**
	 * Returns whether the video is stopped or not.
	 * 
	 * @return TRUE if stopped; FALSE otherwise.
	 */
	public boolean isStopped()
	{
		return this.mode == Mode.STOP;
	}

	/**
	 * Set the action to occur when the video reaches its end. Possible values
	 * are given in the {@link EndAction} enumeration.
	 * 
	 * @param action
	 *            The {@link EndAction} action to occur.
	 */
	public void setEndAction(final EndAction action)
	{
		this.endAction = action;
	}

	/**
	 * Convenience function to create a VideoDisplay from an array of images
	 * 
	 * @param images
	 *            the images
	 * @return a VideoDisplay
	 */
	public static VideoDisplay<FImage> createVideoDisplay(final FImage[] images)
	{
		return VideoDisplay.createVideoDisplay(new ArrayBackedVideo<FImage>(images, 30));
	}

	/**
	 * Convenience function to create a VideoDisplay from a video in a new
	 * window.
	 * 
	 * @param <T>
	 *            the image type of the video frames
	 * @param video
	 *            the video
	 * @return a VideoDisplay
	 */
	public static <T extends Image<?, T>> VideoDisplay<T> createVideoDisplay(final Video<T> video)
	{
		final JFrame screen = DisplayUtilities.makeFrame("Video");
		return VideoDisplay.createVideoDisplay(video, screen);
	}

	/**
	 * Convenience function to create a VideoDisplay from a video in a new
	 * window.
	 * 
	 * @param <T>
	 *            the image type of the video frames
	 * @param video
	 *            the video
	 * @param audio
	 *            the audio stream
	 * @return a VideoDisplay
	 */
	public static <T extends Image<?, T>> VideoDisplay<T> createVideoDisplay(
			final Video<T> video, final AudioStream audio)
	{
		final JFrame screen = DisplayUtilities.makeFrame("Video");
		return VideoDisplay.createVideoDisplay(video, audio, screen);
	}

	/**
	 * Convenience function to create a VideoDisplay from a video in a new
	 * window.
	 * 
	 * @param <T>
	 *            the image type of the video frames
	 * @param video
	 *            The video
	 * @param screen
	 *            The window to draw into
	 * @return a VideoDisplay
	 */
	public static <T extends Image<?, T>> VideoDisplay<T> createVideoDisplay(
			final Video<T> video, final JFrame screen)
	{
		return VideoDisplay.createVideoDisplay(video, null, screen);
	}

	/**
	 * Convenience function to create a VideoDisplay from a video in a new
	 * window.
	 * 
	 * @param <T>
	 *            the image type of the video frames
	 * @param video
	 *            The video
	 * @param as The audio
	 * @param screen
	 *            The window to draw into
	 * @return a VideoDisplay
	 */
	public static <T extends Image<?, T>> VideoDisplay<T> createVideoDisplay(
			final Video<T> video, final AudioStream as, final JFrame screen)
	{
		final ImageComponent ic = new ImageComponent();
		ic.setSize(video.getWidth(), video.getHeight());
		ic.setPreferredSize(new Dimension(video.getWidth(), video.getHeight()));
		ic.setAllowZoom(false);
		ic.setAllowPanning(false);
		ic.setTransparencyGrid(false);
		ic.setShowPixelColours(false);
		ic.setShowXYPosition(false);
		screen.getContentPane().add(ic);

		screen.pack();
		screen.setVisible(true);

		final VideoDisplay<T> dv = new VideoDisplay<T>(video, as, ic);

		new Thread(dv).start();
		return dv;

	}

	/**
	 * Convenience function to create a VideoDisplay from a video in a new
	 * window.
	 * 
	 * @param <T>
	 *            the image type of the video frames
	 * @param video
	 *            The video
	 * @param ic
	 *            The {@link ImageComponent} to draw into
	 * @return a VideoDisplay
	 */
	public static <T extends Image<?, T>>
			VideoDisplay<T>
			createVideoDisplay(final Video<T> video, final ImageComponent ic)
	{
		final VideoDisplay<T> dv = new VideoDisplay<T>(video, ic);

		new Thread(dv).start();
		return dv;

	}

	/**
	 * Convenience function to create a VideoDisplay from a video in a new
	 * window.
	 * 
	 * @param <T>
	 *            the image type of the video frames
	 * @param video
	 *            the video
	 * @return a VideoDisplay
	 */
	public static <T extends Image<?, T>> VideoDisplay<T> createOffscreenVideoDisplay(final Video<T> video) {

		final VideoDisplay<T> dv = new VideoDisplay<T>(video, null);
		dv.displayMode = false;
		new Thread(dv).start();
		return dv;

	}

	/**
	 * Convenience function to create a VideoDisplay from a video in an existing
	 * component.
	 * 
	 * @param <T>
	 *            the image type of the video frames
	 * @param video
	 *            The video
	 * @param comp
	 *            The {@link JComponent} to draw into
	 * @return a VideoDisplay
	 */
	public static <T extends Image<?, T>> VideoDisplay<T> createVideoDisplay(final Video<T> video, final JComponent comp)
	{
		final ImageComponent ic = new ImageComponent();
		ic.setSize(video.getWidth(), video.getHeight());
		ic.setPreferredSize(new Dimension(video.getWidth(), video.getHeight()));
		ic.setAllowZoom(false);
		ic.setAllowPanning(false);
		ic.setTransparencyGrid(false);
		ic.setShowPixelColours(false);
		ic.setShowXYPosition(false);
		comp.add(ic);

		final VideoDisplay<T> dv = new VideoDisplay<T>(video, ic);

		new Thread(dv).start();
		return dv;
	}

	/**
	 * Convenience function to create a VideoDisplay from a video in an existing
	 * component.
	 * 
	 * @param <T>
	 *            the image type of the video frames
	 * @param video
	 *            The video
	 * @param audio
	 *            The audio
	 * @param comp
	 *            The {@link JComponent} to draw into
	 * @return a VideoDisplay
	 */
	public static <T extends Image<?, T>> VideoDisplay<T> createVideoDisplay(final Video<T> video, AudioStream audio,
			final JComponent comp)
	{
		final ImageComponent ic;
		if (video.getWidth() > comp.getPreferredSize().width || video.getHeight() > comp.getPreferredSize().height) {
			ic = new DisplayUtilities.ScalingImageComponent();
			ic.setSize(comp.getSize());
			ic.setPreferredSize(comp.getPreferredSize());
		} else {
			ic = new ImageComponent();
			ic.setSize(video.getWidth(), video.getHeight());
			ic.setPreferredSize(new Dimension(video.getWidth(), video.getHeight()));
		}
		ic.setAllowZoom(false);
		ic.setAllowPanning(false);
		ic.setTransparencyGrid(false);
		ic.setShowPixelColours(false);
		ic.setShowXYPosition(false);
		comp.add(ic);

		final VideoDisplay<T> dv = new VideoDisplay<T>(video, audio, ic);

		new Thread(dv).start();
		return dv;
	}

	/**
	 * Set whether to draw onscreen or not
	 * 
	 * @param b
	 *            if true then video is drawn to the screen, otherwise it is not
	 */
	public void displayMode(final boolean b)
	{
		this.displayMode = b;
	}

	/**
	 * Seek to a given timestamp in millis.
	 * 
	 * @param toSeek
	 *            timestamp to seek to in millis.
	 */
	public void seek(final long toSeek)
	{
		// this.mode = Mode.SEEK;
		if (this.timeKeeper.supportsSeek())
		{
			this.timeKeeper.seek(toSeek);
			this.video.seek(toSeek);
		}
		else
		{
			System.out.println("WARNING: Time keeper does not support seek. " +
					"Not seeking");
		}
	}

	/**
	 * Returns the position of the play head in this video as a percentage of
	 * the length of the video. IF the video is a live video, this method will
	 * always return 0;
	 * 
	 * @return The percentage through the video.
	 */
	public double getPosition()
	{
		final long nFrames = this.video.countFrames();
		if (nFrames == -1)
			return 0;
		return this.video.getCurrentFrameIndex() * 100d / nFrames;
	}

	/**
	 * Set the position of the play head to the given percentage. If the video
	 * is a live video this method will have no effect.
	 * 
	 * @param pc
	 *            The percentage to set the play head to.
	 */
	public void setPosition(final double pc)
	{
		if (pc > 100 || pc < 0)
			throw new IllegalArgumentException("Percentage must be less than " +
					"or equals to 100 and greater than or equal 0. Given " + pc);

		// If it's a live video we cannot do anything
		if (this.video.countFrames() == -1)
			return;

		// We have to seek to a millisecond position, so we find out the length
		// of the video in ms and then multiply by the percentage
		final double nMillis = this.video.countFrames() * this.video.getFPS();
		final long msPos = (long) (nMillis * pc / 100d);
		System.out.println("msPOs = " + msPos + " (" + pc + "%)");
		this.seek(msPos);
	}

	/**
	 * Returns the speed at which the display is being updated.
	 * 
	 * @return The number of frames per second
	 */
	public double getDisplayFPS()
	{
		return this.calculatedFPS;
	}

	/**
	 * Set the timekeeper to use for this video.
	 * 
	 * @param t
	 *            The timekeeper.
	 */
	public void setTimeKeeper(final TimeKeeper<? extends Timecode> t)
	{
		this.timeKeeper = t;
	}

	/**
	 * Returns the number of frames that have been dropped while playing the
	 * video.
	 * 
	 * @return The number of dropped frames
	 */
	public int getDroppedFrameCount()
	{
		return this.droppedFrameCount;
	}

	/**
	 * Reset the dropped frame count to zero.
	 */
	public void resetDroppedFrameCount()
	{
		this.droppedFrameCount = 0;
	}

	/**
	 * Returns whether the frames per second are being calculated at every
	 * frame. If this returns false, then {@link #getDisplayFPS()} will not
	 * return a valid value.
	 * 
	 * @return whether the FPS is being calculated
	 */
	public boolean isCalculateFPS()
	{
		return this.calculateFPS;
	}

	/**
	 * Set whether the frames per second display rate will be calculated at
	 * every frame.
	 * 
	 * @param calculateFPS
	 *            TRUE to calculate the FPS; FALSE otherwise.
	 */
	public void setCalculateFPS(final boolean calculateFPS)
	{
		this.calculateFPS = calculateFPS;
	}
}
