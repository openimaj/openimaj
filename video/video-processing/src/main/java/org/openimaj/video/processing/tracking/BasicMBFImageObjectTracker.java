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
package org.openimaj.video.processing.tracking;

import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.tracking.klt.FeatureList;

/**
 * 	A tracker that will track one rectangular region using the KLTTracker from
 * 	MBFImages. It simply uses the {@link BasicObjectTracker} and flattens the
 * 	incoming MBFImages to FImages and then tracks.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 14 Oct 2011
 */
public class BasicMBFImageObjectTracker implements ObjectTracker<Rectangle,MBFImage>
{
	private BasicObjectTracker objectTracker = new BasicObjectTracker();
	
	/**
	 * 	Returns the list of features that the tracker has been tracking.
	 *	@return the {@link FeatureList}
	 */
	public FeatureList getFeatureList()
	{
		return objectTracker.getFeatureList();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.processing.tracking.ObjectTracker#initialiseTracking(org.openimaj.math.geometry.shape.Rectangle, java.lang.Object)
	 */
	@Override
	public List<Rectangle> initialiseTracking( Rectangle bounds, MBFImage image )
	{
		return objectTracker.initialiseTracking( bounds, image.flatten() );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.processing.tracking.ObjectTracker#trackObject(java.lang.Object)
	 */
	@Override
	public List<Rectangle> trackObject( MBFImage image )
	{
		return objectTracker.trackObject( image.flatten() );
	}
}
