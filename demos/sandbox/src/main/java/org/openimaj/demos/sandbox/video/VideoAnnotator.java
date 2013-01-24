/**
 * 
 */
package org.openimaj.demos.sandbox.video;

import java.util.HashSet;
import java.util.Set;

import org.openimaj.image.Image;
import org.openimaj.video.processor.VideoProcessor;

/**
 *	A {@link VideoProcessor} that is able to also provide annotations for
 *	the video it is processing. The type of the annotation that it provides is
 *	given in the generic arguments of the class.
 *	<p>
 *	As a video is being processed, the annotator may be asked to reset itself -
 *	to start the annotation process anew. The {@link #reset()} method should be
 *	called to do this, which will in turn call the {@link #resetAnnotator()}
 *	method which may be overridden in subclass implementations.	
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 22 Jan 2013
 *	@version $Author$, $Revision$, $Date$
 * 	@param <T> The image type 
 * 	@param <ANNOTATION> The annotation type
 */
@SuppressWarnings( "javadoc" )
public abstract class VideoAnnotator<T extends Image<?,T>,ANNOTATION> 
	extends VideoProcessor<T>
{
	/** The list of annotations generates for this video since the last reset */
	protected Set<ANNOTATION> annotations = new HashSet<ANNOTATION>();
	
	/**
	 * 	Returns the list of annotations generated for this annotator.
	 *	@return The list of annotations generated since the last reset
	 */
	public final Set<ANNOTATION> getAnnotations()
	{
		this.updateAnnotations();
		return this.annotations;
	}
	
	/**
	 * 	Update the annotations list. The <code>annotations</code> member
	 * 	is a {@link Set}, so you should be able to add annotations without
	 * 	being concerned about duplicates, as long as the ANNOTATION type
	 * 	is {@link Comparable}. 
	 */
	protected void updateAnnotations()
	{
		// No implementation. Override for your implementation.
	}
	
	/**
	 * 	Reset the annotator.
	 */
	protected void resetAnnotator()
	{
		// No implementation. Override for your implementation.
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.processor.VideoProcessor#reset()
	 */
	@Override
	public final void reset()
	{
		this.annotations = new HashSet<ANNOTATION>();
		this.resetAnnotator();
	}
}
