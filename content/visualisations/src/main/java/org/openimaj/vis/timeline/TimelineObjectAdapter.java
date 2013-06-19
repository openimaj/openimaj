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

import org.openimaj.vis.Visualisation;

/**
 *	A timeline object is a temporal object and is able to drawn sections
 *	of temporal data into a given viewport.  The {@link #setViewSize(Dimension, long, long)}
 *	is called to tell the object the viewport size and the start and duration
 *	of the data to draw. As this class also extends {@link Visualisation}, the
 *	implementation of {@link Visualisation#update()} should use those dimensions
 *	and time series to draw into the <code>visImage</code> member.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 3 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 * 	@param <T> The type of the data the timeline is displaying
 */
public abstract class TimelineObjectAdapter<T> extends Visualisation<T>
	implements TimelineObject
{
	/** */
	private static final long serialVersionUID = 1L;

	/** The viewport dimensions */
	private Dimension viewport = null;

	/** The start time */
	private long startTime = 0;

	/** The end time */
	private long endTime = 0;

	/**
	 * 	Set the part of this timeline that is visible, allowing this timeline
	 * 	object to draw only the part that is visible. The dimension should be
	 * 	in pixels and give the viewport area. The start time gives the time
	 * 	in milliseconds at which 0 coordinate is positioned in time.
	 *
	 *	@param d The dimension of the viewport.
	 * @param startTimeMilliseconds Start time
	 */
	@Override
	public void setViewSize( final Dimension d, final long startTimeMilliseconds, final long endTimeMilliseconds )
	{
		this.viewport = d;
		this.startTime = startTimeMilliseconds;
		this.endTime = endTimeMilliseconds;
	}

	/**
	 * 	Returns the viewport dimensions.
	 *	@return the viewport The viewport dimensions
	 */
	public Dimension getViewSize()
	{
		return this.viewport;
	}

	/**
	 * 	Returns the start time position in milliseconds
	 *	@return the start time position.
	 */
	public long getStartTime()
	{
		return this.startTime;
	}

	/**
	 * 	Returns the end time position in milliseconds
	 *	@return The end time
	 */
	public long getEndTime()
	{
		return this.endTime;
	}
}
