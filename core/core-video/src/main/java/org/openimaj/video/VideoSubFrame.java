package org.openimaj.video;

import org.openimaj.image.Image;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.timecode.VideoTimecode;

/**
 * A {@link VideoFrame} with a subwindow defined
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class VideoSubFrame<T extends Image<?,T>> extends VideoFrame<T> {

	public Rectangle roi;
	private VideoFrame<T> cachedSubFrame;

	/**Constructor
	 * @param frame The frame
	 * @param timecode The timecode
	 * @param rectangle The subwindow into the frame
	 */
	public VideoSubFrame(T frame, VideoTimecode timecode,Rectangle rectangle) {
		super(frame, timecode);
		this.roi = rectangle;
	}

	/**
	 * @return the subframe as a {@link VideoFrame}
	 */
	public VideoFrame<T> extract(){
		if(cachedSubFrame == null){
			cachedSubFrame = new VideoFrame<T>(frame.extractROI(roi), timecode);
		}
		return cachedSubFrame;
	}
}
