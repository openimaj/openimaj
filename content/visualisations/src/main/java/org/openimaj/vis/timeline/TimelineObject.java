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
package org.openimaj.vis.timeline;

import java.awt.Dimension;

import org.openimaj.vis.DataUnitsTransformer;
import org.openimaj.vis.VisualisationImageProvider;

/**
 *	An object that can be drawn into a timeline needs to be able to provide
 *	the start and end time of its data, and also be able to accept a size
 *	at which it should draw itself. The object should draw itself in the
 *	given image size with the start and end times fitting in the image exactly.
 *	This allows the {@link Timeline} visualisation to scale the object as necessary
 *	and allow it to align with the time ruler.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 19 Jun 2013
 *	@version $Author$, $Revision$, $Date$
 */
public interface TimelineObject extends VisualisationImageProvider
{
	/**
	 * 	The timeline object will need to know how to convert between
	 * 	time and pixels, and the {@link DataUnitsTransformer} given here
	 * 	should be able to do that. Use it to paint the image.
	 *	@param dpt
	 */
	public void setDataPixelTransformer( DataUnitsTransformer<Float[],double[],int[]> dpt );

	/**
	 * 	Returns the start time of the timeline object in the timeline.
	 *	@return The start time
	 */
	public long getStartTimeMilliseconds();

	/**
	 * 	Set the start time of this object
	 * 	@param l The start time in milliseconds
	 */
	public void setStartTimeMilliseconds( long l );

	/**
	 * 	Returns the end time of the timeline object in the timeline.
	 *	@return The end time.
	 */
	public long getEndTimeMilliseconds();

	/**
	 * 	The timeline object needs to be able to suggest a size, if it
	 * 	needs a particular amount. It's not guaranteed it will get this size,
	 * 	but it may ask for it by implementing this method.
	 *	@return A {@link Dimension}
	 */
	public Dimension getPreferredSize();

	/**
	 * 	Returns the size that this object wishes to be.
	 *	@return The size the object wishes to be.
	 */
	public Dimension getRequiredSize();
}
