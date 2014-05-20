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

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.util.function.Operation;
import org.openimaj.util.pair.IndependentPair;

/**
 * The neighbourhood border/contour tracing algorithm described in Appendix 1 of
 * the Suzuki contour detection algorithm
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Suzuki, S.", "Abe, K." },
		title = "Topological Structural Analysis of Digitized Binary Image by Border Following",
		year = "1985",
		journal = "Computer Vision, Graphics and Image Processing",
		pages = { "32", "46" },
		month = "January",
		number = "1",
		volume = "30")
public class SuzukiNeighborStrategy extends ContourFollowingStrategy {
	@Override
	public void contour(FImage image, Pixel start, Pixel from, final Operation<Pixel> operation) {
		directedContour(image, start, from, new Operation<IndependentPair<Pixel, boolean[]>>() {

			@Override
			public void perform(IndependentPair<Pixel, boolean[]> object) {
				operation.perform(object.firstObject());
			}
		});
	}

	/**
	 * Directed contour following.
	 * 
	 * @param image
	 *            the image
	 * @param ij
	 *            the first pixel
	 * @param i2j2
	 *            the second pixel
	 * @param operation
	 *            the operation to perform
	 */
	public void
			directedContour(FImage image, Pixel ij, Pixel i2j2, Operation<IndependentPair<Pixel, boolean[]>> operation)
	{
		Direction dir = Direction.fromTo(ij, i2j2);
		Direction trace = dir.clockwise();
		// find i1j1 (3.1)
		Pixel i1j1 = null;
		while (trace != dir) {
			final Pixel activePixel = trace.active(image, ij);
			if (activePixel != null) {
				i1j1 = activePixel;
				break;
			}
			trace = trace.clockwise();
		}
		if (i1j1 == null)
			return; // operation never called, signals the starting pixel is
					// alone! (3.1)

		i2j2 = i1j1;
		Pixel i3j3 = ij; // (3.2)
		final boolean[] checked = new boolean[] {
				/*
				 * N , NE ,E ,SE ,S ,SW ,W ,NW
				 */
				false, false, false, false, false, false, false, false
		};
		while (true) {
			dir = Direction.fromTo(i3j3, i2j2);
			trace = dir.counterClockwise();
			Pixel i4j4 = null;
			resetChecked(checked);
			while (true) {
				i4j4 = trace.active(image, i3j3); // 3.3
				if (i4j4 != null)
					break;
				checked[trace.ordinal()] = true;
				trace = trace.counterClockwise();
			}
			operation.perform(IndependentPair.pair(i3j3, checked));
			if (i4j4.equals(ij) && i3j3.equals(i1j1))
				break; // 3.5
			i2j2 = i3j3; // 3.5
			i3j3 = i4j4; // 3.5
		}
	}

	private void resetChecked(boolean[] checked) {
		for (int i = 0; i < checked.length; i++) {
			checked[i] = false;
		}
	}

}
