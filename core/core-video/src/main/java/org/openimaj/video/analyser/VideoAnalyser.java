/**
 * 
 */
package org.openimaj.video.analyser;

import org.openimaj.image.Image;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.video.Video;
import org.openimaj.video.processor.VideoProcessor;

/**
 *	This class is analagous to the {@link ImageAnalyser} class for analysing
 *	image. It should be used to make analysis and measurements on video frames
 *	without altering the frames themselves.
 *	<p>
 *	This class overrides {@link VideoProcessor} to inherit many of its
 *	abilities (chainability etc.) but the processFrame method will always
 *	return the input frame having called analyseFrame. The analyseFrame method
 *	should not alter the input frame. 
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 1 Mar 2012
 *	@version $Author$, $Revision$, $Date$
 * 	@param <T> The type of the video frame to be analysed 
 */
public abstract class VideoAnalyser<T extends Image<?,T>> 
	extends VideoProcessor<T>
{
	/**
	 * 	Construct a stand-alone video analyser.
	 */
	public VideoAnalyser()
	{
	}
	
	/**
	 * 	Construct a chainable video analyser.
	 *	@param v The video to chain to
	 */
	public VideoAnalyser( Video<T> v )
	{
		super( v );
	}
	
	/**
	 * 	Analyse the given frame and make no changes to the frame.
	 *	@param frame The video frame to analyse
	 */
	public abstract void analyseFrame( T frame );
	
	/**
	 * 	This method will return the input frame after calling 
	 * 	{@link #analyseFrame(Image)} on the input image.
	 * 
	 * 	@see org.openimaj.video.processor.VideoProcessor#processFrame(org.openimaj.image.Image)
	 */
	final public T processFrame( T frame )
	{
		analyseFrame( frame );
		return frame;
	}
}
