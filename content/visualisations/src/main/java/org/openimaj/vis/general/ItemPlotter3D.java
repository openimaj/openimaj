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
package org.openimaj.vis.general;

import javax.media.opengl.GLAutoDrawable;

import org.openimaj.vis.general.XYZVisualisation3D.LocatedObject3D;

/**
 *	An interface for classes that are able to plot items into a 3D visualisation
 *	using an {@link AxesRenderer3D} to determine the position.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 11 Jul 2013
 *	@version $Author$, $Revision$, $Date$
 * 	@param <O>
 */
public interface ItemPlotter3D<O>
{
	/**
	 * 	Called just before a render of the visualisation is about to start.
	 * 	This can be used to prepare context objects for the plotObject method.
	 */
	public abstract void renderRestarting();

	/**
	 * 	Plots a specific object to the 3D visualisation using the {@link AxesRenderer3D} to
	 * 	provide the position of the object. Should side affect the given image.
	 *
	 * 	@param drawable The 3D world
	 *	@param object The object
	 *	@param renderer The axes renderer
	 */
	public abstract void plotObject( GLAutoDrawable drawable,
			LocatedObject3D<O> object, AxesRenderer3D renderer );
}
