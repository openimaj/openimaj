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
package org.openimaj.image.contour;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.util.function.Operation;
import org.openimaj.util.pair.IndependentPair;

/**
 * The Moore Neighborhood border tracing strategy as described by
 * http://www.imageprocessingplace.com/downloads_V3/root_downloads/tutorials/
 * contour_tracing_Abeer_George_Ghuneim/moore.html
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class MooreNeighborStrategy extends BorderFollowingStrategy {

	public void border(FImage image, Pixel start, Pixel from, final Operation<Pixel> operation) {
		directedBorder(image, start, from, new Operation<IndependentPair<Pixel, DIRECTION>>() {

			@Override
			public void perform(IndependentPair<Pixel, DIRECTION> object) {
				operation.perform(object.firstObject());
			}
		});
	}
	
	/**
	 * 
	 * @param image
	 * @param start
	 * @param from
	 * @param operation
	 */
	public void directedBorder(FImage image, Pixel start, Pixel from,
			Operation<IndependentPair<Pixel, DIRECTION>> operation)
	{
		Pixel p = start;
		if (image.pixels[start.y][start.x] == 0)
			return;
		DIRECTION cdirStart = DIRECTION.fromTo(p, from);
		operation.perform(IndependentPair.pair(start, cdirStart));
		final DIRECTION firstCdir = cdirStart;
		DIRECTION cdir = cdirStart.clockwise();
		int startCount = 0;
		while (cdir != cdirStart) {
			Pixel c = cdir.active(image, p);
			if (c != null) {
				cdirStart = cdir.clockwiseEntryDirection();
				if (c.equals(start)) {
					startCount++;
					if (startCount >= 2 || firstCdir == cdirStart) {
						return;
					}

				}
				operation.perform(IndependentPair.pair(c, cdirStart));
				p = c;
				cdir = cdirStart.clockwise();
			}
			else {
				cdir = cdir.clockwise();
			}
		}
	}

}
