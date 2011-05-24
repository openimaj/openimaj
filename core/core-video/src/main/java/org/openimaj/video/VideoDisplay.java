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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;

/**
 * Basic class for displaying videos.
 * 
 * @author Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 * @param <T> the image type of the frames in the video
 */
public class VideoDisplay<T extends Image<?,T>> implements Runnable {
	enum Mode{
		PLAY,PAUSE
	}
	private Mode mode = Mode.PLAY;
	private JFrame screen;
	private Video<T> video;
	private List<VideoDisplayListener<T>> videoDisplayListeners;
	private boolean displayMode = true;
	
	
	/**
	 * Construct a video display with the given video and frame.
	 * @param v the video
	 * @param screen the frame to draw into.
	 */
	public VideoDisplay(Video<T> v, JFrame screen) {
		this.video = v;
		this.screen = screen;
		videoDisplayListeners = new ArrayList<VideoDisplayListener<T>>();
	}
	
	@Override
	public void run() {
		while(true){
			T currentFrame;
			if(this.mode == Mode.PLAY)
				currentFrame = video.getNextFrame();
			else
				currentFrame = video.getCurrentFrame();
			
			T toDraw = currentFrame.clone();
			fireBeforeUpdate(toDraw);
			if(displayMode)
				DisplayUtilities.display(toDraw,screen);
			fireVideoUpdate();
			try {
				Thread.sleep(video.getMilliPerFrame());
			} catch (InterruptedException e) {
				return;
			}
		}
	}
	
	private void fireBeforeUpdate(T currentFrame) {
		for(VideoDisplayListener<T> vdl : videoDisplayListeners){
			vdl.beforeUpdate(currentFrame);
		}
	}

	private void fireVideoUpdate() {
		for(VideoDisplayListener<T> vdl : videoDisplayListeners){
			vdl.afterUpdate(this);
		}
	}

	/**
	 * Get the frame the video is being drawn to
	 * @return the frame
	 */
	public JFrame getScreen() {
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
		this.videoDisplayListeners.add(dsl);
	}

	/**
	 * Pause or resume the display
	 */
	public void togglePause() {
		if(this.mode == Mode.PLAY){
			this.mode = Mode.PAUSE;
		}
		else{
			this.mode = Mode.PLAY;
		}
	}
	
	/**
	 * Is the video paused?
	 * @return true if paused; false otherwise.
	 */
	public boolean isPaused() {
		return mode == Mode.PAUSE;
	}
	
	/**
	 * Convenience function to create a VideoDisplay from an array of images
	 * @param images the images
	 * @return a VideoDisplay
	 */
	public static VideoDisplay<FImage> createVideoDisplay(FImage[] images) {
		return createVideoDisplay(new ArrayBackedVideo<FImage>(images,30));
	}
	
	/**
	 * Convenience function to create a VideoDisplay from a video
	 * in a new window. 
	 * @param <T> the image type of the video frames 
	 * @param video the video
	 * @return a VideoDisplay
	 */
	public static<T extends Image<?,T>> VideoDisplay<T> createVideoDisplay(Video<T> video) {
		final JFrame screen = DisplayUtilities.makeFrame("Video");
		VideoDisplay<T> dv = new VideoDisplay<T>(video,screen);
		new Thread(dv ).start();
		return dv ;
	}

	public void displayMode(boolean b) {
		this.displayMode  = b;
	}
}
