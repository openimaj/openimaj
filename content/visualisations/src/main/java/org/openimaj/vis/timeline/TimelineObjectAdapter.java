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

import org.openimaj.vis.DataUnitsTransformer;
import org.openimaj.vis.VisualisationImpl;

/**
 *	A timeline object is a temporal object and is able to drawn sections
 *	of temporal data into a given viewport.  As this class also extends {@link VisualisationImpl}, the
 *	implementation of {@link VisualisationImpl#update()} should use those dimensions
 *	and time series to draw into the <code>visImage</code> member.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 3 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 * 	@param <T> The type of the data the timeline is displaying
 */
public abstract class TimelineObjectAdapter<T> extends VisualisationImpl<T>
	implements TimelineObject
{
	/** */
	private static final long serialVersionUID = 1L;

	/** The start time */
	protected long startTime = 0;

	/** The end time */
	protected long endTime = 0;

	/** The pixel transformer to use */
	protected DataUnitsTransformer<Float[], double[], int[]> pixelTransformer = null;

	/**
	 * 	Default constructor. Note that it generates a visualisation image
	 * 	for visualisation of the timeline object as a stand-alone visualisation.
	 * 	The size of this image is fixed to 1000x300.
	 */
	public TimelineObjectAdapter()
	{
		super( 1000, 300 );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.timeline.TimelineObject#getStartTimeMilliseconds()
	 */
	@Override
	public long getStartTimeMilliseconds()
	{
		return this.startTime;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.timeline.TimelineObject#setStartTimeMilliseconds(long)
	 */
	@Override
	public void setStartTimeMilliseconds( final long l )
	{
		this.startTime = l;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.timeline.TimelineObject#getEndTimeMilliseconds()
	 */
	@Override
	public long getEndTimeMilliseconds()
	{
		return this.endTime;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.timeline.TimelineObject#setDataPixelTransformer(org.openimaj.vis.DataUnitsTransformer)
	 */
	@Override
	public void setDataPixelTransformer( final DataUnitsTransformer<Float[],double[],int[]> dpt )
	{
		this.pixelTransformer = dpt;
	}
}
