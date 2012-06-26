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

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;

/**
 * Basic class for displaying videos. 
 * 
 * {@link VideoDisplayListener}s can be added to be informed when the display
 * is about to be updated or has just been updated.
 * 
 * The video can be played, paused and stopped. The difference is that during
 * pause mode, the video display events are still fired to the listeners with
 * the paused frame, whereas during stopped mode they are not. The default is
 * that when the video comes to its end, the display is automatically set to
 * stop.
 * 
 * The VideoDisplay constructor takes an {@link ImageComponent} which is used
 * to draw the video to. This allows video displays to be integrated in
 * an Swing UI. Use the {@link #createVideoDisplay(Video)} to create a basic 
 * frame displaying the video.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> the image type of the frames in the video
 */
public class VideoDisplay<T extends Image<?,T>> implements Runnable 
{
	/**
	 *	Enumerator to represent the state of the player.
	 * 
	 * 	@author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 	@author David Dupplaw (dpd@ecs.soton.ac.uk)
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

	/** The default mode is to play the player */
	private Mode mode = Mode.PLAY;

	/** The screen to show the player in */
	private ImageComponent screen;

	/** The video being displayed */
	private Video<T> video;

	/** The list of video display listeners */
	private List<VideoDisplayListener<T>> videoDisplayListeners;

	/** List of state listeners */
	private List<VideoDisplayStateListener> stateListeners;

	/** Whether to display the screen */
	private boolean displayMode = true;

	/** 
	 * Whether the video display will switch to STOP mode at the
	 * end of the video play (video.getNextFrame() returns null).
	 * Otherwise the video will be set to PAUSE.
	 */
	private boolean stopAtEndOfVideo = true;

	private long videoPlayerStartTime = -1;

	private long firstFrameTimestamp;

	/**
	 * If we are in seek mode, this value is used to seek
	 */
	private double seekTimestamp;


	/**
	 * Construct a video display with the given video and frame.
	 * @param v the video
	 * @param screen the frame to draw into.
	 */
	public VideoDisplay( Video<T> v, ImageComponent screen ) 
	{
		this.video = v;
		this.screen = screen;
		videoDisplayListeners = new ArrayList<VideoDisplayListener<T>>();
		stateListeners = new ArrayList<VideoDisplayStateListener>();
	}

	@Override
	public void run() 
	{
		BufferedImage bimg = null;
		T toDraw = null;
		
		while (true) 
		{
			T currentFrame = null;
			T nextFrame;
		
			if (this.mode == Mode.CLOSED)
			{
				this.video.close();
				return;
			}
			
			if(this.mode == Mode.SEEK){
//				System.out.println("Seeking video to: " + seekTimestamp);
				this.video.seek(seekTimestamp);
				this.videoPlayerStartTime = -1;
				this.mode = Mode.PLAY;
				
			}
			
			if(this.mode == Mode.PLAY) {
				nextFrame = video.getNextFrame();
			} else {
				nextFrame = video.getCurrentFrame();
			}

			// If the getNextFrame() returns null then the end of the
			// video may have been reached, so we pause the video.
			if( nextFrame == null ) {
				if( this.stopAtEndOfVideo ) {
					setMode( Mode.STOP );
				} else {
//					setMode( Mode.PAUSE );
					this.seek(0);
				}
			} else {
				currentFrame = nextFrame;
			}

			// If we have a frame to draw, then draw it.
			if( currentFrame != null && this.mode != Mode.STOP ) 
			{
				if( videoPlayerStartTime == -1 && this.mode == Mode.PLAY )
				{					
//					System.out.println("Resseting internal times");
					firstFrameTimestamp = video.getTimeStamp();
					videoPlayerStartTime = System.currentTimeMillis();
//					System.out.println("First time stamp: " + firstFrameTimestamp);
				}
				else
				{
					// This is based on the Xuggler demo code:
					// http://xuggle.googlecode.com/svn/trunk/java/xuggle-xuggler/src/com/xuggle/xuggler/demos/DecodeAndPlayVideo.java
					final long systemDelta = System.currentTimeMillis() - videoPlayerStartTime;
					final long currentFrameTimestamp = video.getTimeStamp();
					final long videoDelta = currentFrameTimestamp - firstFrameTimestamp;
					final long tolerance = 20;
					final long sleepTime = videoDelta - tolerance - systemDelta;
					
					if( sleepTime > 0 )
					{
						try {
							Thread.sleep( sleepTime );
						} catch (InterruptedException e) {
							return;
						}
					}
				}
			}
			final boolean fireUpdates = this.videoDisplayListeners.size() != 0;
			if (toDraw == null) {
				toDraw = currentFrame.clone();
			}
			else{
				if(currentFrame!=null)
					toDraw.internalCopy(currentFrame);
			}
			if (fireUpdates) {
				fireBeforeUpdate(toDraw);
			}
			
			if( displayMode )
			{
				screen.setImage( bimg = ImageUtilities.createBufferedImageForDisplay( toDraw, bimg ) );
//					renderImage(toDraw);
			}
			
			if (fireUpdates) {
				fireVideoUpdate();
			}
		}
	}
	
	/**
	 * Close the video display. Causes playback to stop,
	 * and further events are ignored. 
	 */
	public synchronized void close() {
		this.mode = Mode.CLOSED;
	}

	/**
	 * 	Set whether this player is playing, paused or stopped.
	 *	@param m The new mode
	 */
	public void setMode( Mode m )
	{
		if (this.mode == Mode.CLOSED)
			return;
		
		this.mode = m;
		
		if( this.mode == Mode.PAUSE || this.mode == Mode.STOP )
			videoPlayerStartTime = -1;
		
		fireStateChanged();
	}

	/**
	 * 	Fire the event to the video listeners that a frame is about to be
	 * 	displayed on the video.
	 * 
	 *  @param currentFrame The frame that is about to be displayed
	 */
	protected void fireBeforeUpdate(T currentFrame) {
		synchronized(this.videoDisplayListeners){
			for(VideoDisplayListener<T> vdl : videoDisplayListeners){
				vdl.beforeUpdate(currentFrame);
			}
		}
	}

	/**
	 * 	Fire the event to the video listeners that a frame has been put on
	 * 	the display
	 */
	protected void fireVideoUpdate() {
		synchronized(this.videoDisplayListeners){
			for(VideoDisplayListener<T> vdl : videoDisplayListeners){
				vdl.afterUpdate(this);
			}
		}
	}

	/**
	 * Get the frame the video is being drawn to
	 * @return the frame
	 */
	public ImageComponent getScreen() {
		return screen;
	}

	/**
	 * Get the video
	 * @return the video
	 */
	public Video<T> getVideo() {
		return video;
	}

	/**
	 * Add a listener that will get fired as every
	 * frame is displayed.
	 * @param dsl the listener
	 */
	public void addVideoListener(VideoDisplayListener<T> dsl) {
		synchronized(this.videoDisplayListeners){
			this.videoDisplayListeners.add(dsl);
		}
		
	}

	/**
	 * 	Add a listener for the state of this player.
	 *	@param vdsl The listener to add
	 */
	public void addVideoDisplayStateListener( VideoDisplayStateListener vdsl )
	{
		this.stateListeners.add( vdsl );
	}

	/**
	 * 	Remove a listener from the state of this player
	 *	@param vdsl The listener
	 */
	public void removeVideoDisplayStateListener( VideoDisplayStateListener vdsl )
	{
		this.stateListeners.remove( vdsl );
	}

	/**
	 * 	Fire the state changed event
	 */
	protected void fireStateChanged()
	{
		for( VideoDisplayStateListener s : stateListeners )
		{
			s.videoStateChanged( mode, this );
			switch( mode )
			{
			case PAUSE: s.videoPaused( this ); break;
			case PLAY:  s.videoPlaying( this ); break;
			case STOP:  s.videoStopped( this ); break;
			}
		}
	}

	/**
	 * 	Pause or resume the display. This will only have an affect if the
	 * 	video is not in STOP mode.
	 */
	public void togglePause() {
		if (this.mode == Mode.CLOSED)
			return;
		
		if( this.mode == Mode.PLAY )
			setMode( Mode.PAUSE );
		else
			if( this.mode == Mode.PAUSE )
				setMode( Mode.PLAY );
	}

	/**
	 * Is the video paused?
	 * @return true if paused; false otherwise.
	 */
	public boolean isPaused() {
		return mode == Mode.PAUSE;
	}

	/**
	 * 	Returns whether the video is stopped or not.
	 *  @return TRUE if stopped; FALSE otherwise.
	 */
	public boolean isStopped()
	{
		return mode == Mode.STOP;
	}

	/**
	 * 	Whether to stop the video at the end (when {@link Video#getNextFrame()}
	 * 	returns null). If FALSE, the display will PAUSE the video; otherwise
	 * 	the video will be STOPPED.
	 * 
	 *  @param stopOnVideoEnd Whether to stop the video at the end.
	 */
	public void setStopOnVideoEnd( boolean stopOnVideoEnd )
	{
		this.stopAtEndOfVideo = stopOnVideoEnd;
	}

	/**
	 * Convenience function to create a VideoDisplay from an array of images
	 * @param images the images
	 * @return a VideoDisplay
	 */
	public static VideoDisplay<FImage> createVideoDisplay( FImage[] images ) 
	{
		return createVideoDisplay( new ArrayBackedVideo<FImage>(images,30) );
	}

	/**
	 * Convenience function to create a VideoDisplay from a video
	 * in a new window. 
	 * @param <T> the image type of the video frames 
	 * @param video the video
	 * @return a VideoDisplay
	 */
	public static<T extends Image<?,T>> VideoDisplay<T> createVideoDisplay(Video<T> video ) 
	{
		final JFrame screen = DisplayUtilities.makeFrame("Video");
		return createVideoDisplay(video,screen);
	}

	/**
	 * Convenience function to create a VideoDisplay from a video
	 * in a new window. 
	 * @param <T> the image type of the video frames 
	 * @param video The video
	 * @param screen The window to draw into
	 * @return a VideoDisplay
	 */
	public static<T extends Image<?,T>> VideoDisplay<T> createVideoDisplay(Video<T> video, JFrame screen) {
		
		ImageComponent ic = new ImageComponent();
		ic.setSize( video.getWidth(), video.getHeight() );
		ic.setPreferredSize( new Dimension( video.getWidth(), video.getHeight() ) );
		screen.getContentPane().add( ic );

		screen.pack();
		screen.setVisible( true );

		VideoDisplay<T> dv = new VideoDisplay<T>( video, ic );

		new Thread(dv ).start();
		return dv ;

	}
	
	/**
	 * Convenience function to create a VideoDisplay from a video
	 * in a new window. 
	 * @param <T> the image type of the video frames 
	 * @param video The video
	 * @param ic The {@link ImageComponent} to draw into
	 * @return a VideoDisplay
	 */
	public static<T extends Image<?,T>> VideoDisplay<T> createVideoDisplay(Video<T> video, ImageComponent ic) {
		VideoDisplay<T> dv = new VideoDisplay<T>( video, ic );

		new Thread(dv ).start();
		return dv ;

	}
	
	/**
	 * Convenience function to create a VideoDisplay from a video
	 * in a new window. 
	 * @param <T> the image type of the video frames 
	 * @param video the video
	 * @return a VideoDisplay
	 */
	public static<T extends Image<?,T>> VideoDisplay<T> createOffscreenVideoDisplay(Video<T> video) {
		
		VideoDisplay<T> dv = new VideoDisplay<T>( video, null);
		dv.displayMode = false;
		new Thread(dv).start();
		return dv ;

	}

	/**
	 * Convenience function to create a VideoDisplay from a video
	 * in an existing component. 
	 * @param <T> the image type of the video frames 
	 * @param video The video
	 * @param comp The {@link JComponent} to draw into
	 * @return a VideoDisplay
	 */
	public static<T extends Image<?,T>> VideoDisplay<T> createVideoDisplay(Video<T> video, JComponent comp) {
		ImageComponent ic = new ImageComponent();
		ic.setSize( video.getWidth(), video.getHeight() );
		ic.setPreferredSize( new Dimension( video.getWidth(), video.getHeight() ) );
		comp.add( ic );

		VideoDisplay<T> dv = new VideoDisplay<T>( video, ic );

		new Thread(dv ).start();
		return dv ;

	}

	/**
	 * Set whether to draw onscreen or not
	 * @param b if true then video is drawn to the screen, otherwise it is not
	 */
	public void displayMode( boolean b ) 
	{
		this.displayMode  = b;
	}
	
	/**
	 * Seek to a given timestamp in millis.
	 * @param toSeek timestamp to seek to in millis.
	 */
	public void seek(double toSeek) {
		this.seekTimestamp = toSeek;
		this.mode = Mode.SEEK;
	}
}
