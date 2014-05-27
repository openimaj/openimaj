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

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.util.function.Operation;

/**
 * A contour Following strategy implements a Contour Tracing algorithm that
 * extracts a boundary from an image
 * <p>
 * Many examples can be found in <a href=
 * "http://www.imageprocessingplace.com/downloads_V3/root_downloads/tutorials/contour_tracing_Abeer_George_Ghuneim/index.html"
 * >this tutorial</a>.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public abstract class ContourFollowingStrategy {
	/**
	 * Follow the contour, adding each pixel to a list. The first pixel in the
	 * list is guaranteed to the be equal to start
	 * 
	 * @param image
	 *            the image
	 * @param start
	 *            the starting point on the contour
	 * @param from
	 *            the pixel that was not a contour
	 * @return a list of contour pixels in the image starting from the start
	 *         pixel
	 */
	public List<Pixel> contour(FImage image, Pixel start, Pixel from) {
		final List<Pixel> ret = new ArrayList<Pixel>();
		contour(image, start, from, new Operation<Pixel>() {
			@Override
			public void perform(Pixel object) {
				ret.add(object);
			}
		});
		return ret;
	}

	/**
	 * 
	 * Given some starting pixel in an image on a contour and the direction of a
	 * non starting image, return each pixel on a contour from the start pixel
	 * in the image. The first pixel returned must be the start pixel
	 * 
	 * @param image
	 *            the image
	 * @param start
	 *            the first point on the contour
	 * @param from
	 *            the pixel that was not a contour
	 * @param operation
	 *            the thing to do for each contour pixel found
	 */
	public abstract void contour(FImage image, Pixel start, Pixel from, final Operation<Pixel> operation);

}
