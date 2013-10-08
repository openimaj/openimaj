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
package org.openimaj.vis;

import org.openimaj.image.MBFImage;
import org.openimaj.video.AnimatedVideo;
import org.openimaj.video.VideoDisplay;
import org.openimaj.vis.general.DotPlotVisualisation;
import org.openimaj.vis.general.DotPlotVisualisation.ColouredDot;
import org.openimaj.vis.world.WorldMap;


/**
 *	This class provides a means for generating a video representation of a
 *	visualisationImpl. This has various advantages in that the visualisationImpl
 *	can be rendered easily to a video file or the {@link VideoDisplay}
 *	tools can be used to display the visualisationImpl.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 12 Jun 2013
 */
public class VideoVisualisation extends AnimatedVideo<MBFImage>
{
	private final Visualisation<?> visualisationImpl;

	/**
	 *	Construct a video visualisationImpl using the given visualisationImpl.
	 *	@param v The visualisationImpl to make into a video.
	 */
	public VideoVisualisation( final Visualisation<?> v )
	{
		super( v.getVisualisationImage() );
		this.visualisationImpl = v;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.AnimatedVideo#updateNextFrame(org.openimaj.image.Image)
	 */
	@Override
	protected void updateNextFrame( final MBFImage frame )
	{
		this.visualisationImpl.updateVis();
		frame.internalAssign( this.visualisationImpl.getVisualisationImage() );
	}

	/**
	 * 	Main method
	 *	@param args command-line args (unused)
	 */
	public static void main( final String[] args )
	{
		// Create the visualisationImpl
		final DotPlotVisualisation dpv = new DotPlotVisualisation();
		final WorldMap<ColouredDot> wm = new WorldMap<ColouredDot>( 1280, 720, dpv );

		// Create the video and the video display
		final VideoVisualisation vv = new VideoVisualisation( wm );
		final VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay( vv );

		// Now run the sequence
		final String[] countries = new String[] {"us","cn","gb","za","au","cl","ru"};
		for( int i = 0; i < countries.length; i++ )
		{
			wm.addHighlightCountry( countries[i] );
			try
			{
				Thread.sleep( 1000 );
			}
			catch( final InterruptedException e )
			{
				e.printStackTrace();
			}
			wm.removeHighlightCountry( countries[i] );
		}

		vd.close();
	}
}
