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
package org.openimaj.demos.sandbox.video;

import java.io.File;

import org.openimaj.video.xuggle.XuggleVideo;
import org.openimaj.vis.DataUnitsTransformer;
import org.openimaj.vis.video.ShotBoundaryVideoBarVisualisation;

/**
 *
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 3 Jul 2012
 * @version $Author$, $Revision$, $Date$
 */
public class ShotBoundaryVis
{
	/**
	 *
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final XuggleVideo video = new XuggleVideo(new File("heads2.mpeg"));

		System.out.println("HERRE");
		final ShotBoundaryVideoBarVisualisation sbvbv =
				new ShotBoundaryVideoBarVisualisation(video);
		sbvbv.setDataPixelTransformer( new DataUnitsTransformer<Float[],double[],int[]>()
		{
			private double videoLength = 0;

			@Override
			public void precalc()
			{
				this.videoLength = 1000d * video.countFrames() / video.getFPS();
			}

			@Override
			public double[] calculateUnitsAt( final int[] xy )
			{
				return new double[]
				{  this.videoLength / sbvbv.getVisualisationImage().getWidth() * xy[0], xy[1] };
			}

			@Override
			public int[] calculatePosition( final double xy[] )
			{
				return new int[] { (int)(this.videoLength / xy[0] *
						sbvbv.getVisualisationImage().getWidth()), (int)xy[1] };
			}

			@Override
			public int[] scaleDimension( final double[] dimension )
			{
				return null;
			}
		} );
		sbvbv.showWindow( "Shot Boundaries" );
		sbvbv.processVideo();
	}

}
