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
package org.openimaj.workinprogress.accel;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.convolution.FSobelMagnitude;
import org.openimaj.image.processor.Processor;

public class MovingEdges {
	FImage[] E;
	FImage[] totalHeat;
	FImage[] heatOut;

	public MovingEdges(FImage[] sequence, Processor<FImage> proc, int niters, float kappa) {
		E = new FImage[sequence.length];
		heatOut = new FImage[sequence.length - 2];
		totalHeat = new FImage[sequence.length - 2];

		for (int i = 0; i < sequence.length; i++) {
			E[i] = sequence[i].process(proc);
		}

		for (int i = 0; i < sequence.length - 2; i++) {
			totalHeat[i] = new FImage(E[0].width, E[0].height);
			heatOut[i] = new FImage(E[0].width, E[0].height);
		}

		evolve(sequence, niters, kappa);
	}

	private void evolve(FImage[] sequence, int niters, float kappa) {
		final int width = E[0].width;
		final int height = E[0].height;

		for (int i = 0; i < niters; i++) {
			FImage last = E[0].clone();

			for (int f = 1; f < sequence.length - 1; f++) {
				final FImage delta = delta(last, E[f], E[f + 1]);
				last = E[f].clone();

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						final float d = delta.pixels[y][x];
						E[f].pixels[y][x] += kappa * d;

						totalHeat[f - 1].pixels[y][x] += Math.abs(kappa * d);
						if (d < 0)
							heatOut[f - 1].pixels[y][x] += Math.abs(kappa * d);
					}
				}
			}
		}
	}

	private FImage delta(FImage enm, FImage en, FImage enp) {
		final FImage delta = new FImage(en.width, en.height);
		for (int y = 0; y < delta.height; y++) {
			for (int x = 0; x < delta.width; x++) {
				delta.pixels[y][x] = enp.pixels[y][x] + enm.pixels[y][x] - 2 * en.pixels[y][x];
			}
		}

		return delta;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		final FImage[] sequence = new FImage[10];
		for (int i = 0; i < sequence.length; i++) {
			sequence[i] = ImageUtilities.readF(new File("/Users/jon/pendulum+circle+notexture/frame_" + i + ".png"));
		}

		final MovingEdges me = new MovingEdges(sequence, new FSobelMagnitude(), 10, 0.45f);

		for (int i = 0; i < sequence.length - 2; i++) {
			DisplayUtilities.display(me.heatOut[i].clone().normalise());
			// DisplayUtilities.display(me.totalHeat[i].clone().normalise());
		}
		// for (int i = 0; i < sequence.length; i++) {
		// DisplayUtilities.display(me.E[i].clone().normalise());
		// }
	}
}
