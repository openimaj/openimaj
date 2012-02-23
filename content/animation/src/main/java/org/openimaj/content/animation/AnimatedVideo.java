package org.openimaj.content.animation;

import org.openimaj.image.Image;
import org.openimaj.video.Video;

/**
 * A basic abstract implementation of a video that displays an image and provides
 * double-buffering
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <I>
 */
public abstract class AnimatedVideo<I extends Image<?, I>> extends Video<I> 
{
	private I currentFrame;
	private I nextFrame;
	private double fps;
	
	/**
	 * Default video constructor with a rate of 30 fps
	 * using the given image as a basis.
	 * @param blankFrame blank video frame to pass to the update method
	 */
	public AnimatedVideo(I blankFrame) {
		this(blankFrame, 30);
	}
	
	/**
	 * Default video constructor with the given rate 
	 * using the given image as a basis.
	 * @param blankFrame blank video frame to pass to the update method
	 * @param fps the frame rate
	 */
	public AnimatedVideo(I blankFrame, double fps) {
		currentFrame = blankFrame;
		nextFrame = blankFrame.clone();
		this.fps = fps;
		
		init();
	}
	
	protected abstract void updateNextFrame(I frame);
	
	@Override
	public I getNextFrame() {
		updateNextFrame(nextFrame);
		I tmp = currentFrame;
		currentFrame = nextFrame;
		nextFrame = tmp;
		
		super.currentFrame++;
		
		return currentFrame;
	}

	@Override
	public I getCurrentFrame() {
		return currentFrame;
	}

	@Override
	public int getWidth() {
		return currentFrame.getWidth();
	}

	@Override
	public int getHeight() {
		return currentFrame.getHeight();
	}

	@Override
	public long getTimeStamp() {
		return (long)(super.currentFrame * 1000 / this.fps);
	}

	@Override
	public double getFPS() {
		return fps;
	}

	@Override
	public boolean hasNextFrame() {
		return true;
	}

	@Override
	public long countFrames() {
		return -1;
	}

	@Override
	public void reset() {
		//do nothing by default, but might be overridden to do things with animators
	}

	/**
	 * Called by the constructor. Does nothing by default.
	 */
	protected void init() {
		//do nothing by default, but might be overridden to do things with animators
	}
}
