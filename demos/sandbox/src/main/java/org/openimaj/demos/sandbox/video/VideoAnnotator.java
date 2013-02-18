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
