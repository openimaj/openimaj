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

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.algorithm.FourierTransform;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_3D;

public class VideoSpectrogram {
	public static void main(String[] args) throws IOException {
		final FImage[] sequence = new FImage[20];
		for (int i = 0; i < sequence.length; i++) {
			sequence[i] = ImageUtilities.readF(new File("/Users/jon/pendulum+circle+notexture/frame_" + i + ".png"));
		}

		final int width = sequence[0].width;
		final int height = sequence[0].height;

		final int windowSize = 10;
		final float[][][][] spectrogram = new float[sequence.length - windowSize][][][];

		for (int w = 0; w < spectrogram.length; w++) {
			final FloatFFT_3D fft = new FloatFFT_3D(windowSize, height, width);
			final float[][][] data = new float[10][][];
			for (int i = 0; i < windowSize; i++)
				data[i] = FourierTransform.prepareData(sequence[i], height, width, false);
			fft.complexForward(data);

			spectrogram[w] = data;
		}

		for (int i = 1; i < spectrogram.length - 2; i++) {
			for (int j = 1; j < spectrogram[0].length - 2; j++) {
				for (int k = 1; k < spectrogram[0][0].length - 2; k++) {
					for (int l = 1; l < (spectrogram[0][0][0].length / 2) - 2; l++) {
						final float centre = (float) Math.sqrt(spectrogram[i][j][k][l * 2] * spectrogram[i][j][k][l * 2]
								+ spectrogram[i][j][k][l * 2 + 1] * spectrogram[i][j][k][l * 2 + 1]);
						boolean max = true;

						for (int ii = -1; ii <= 1; ii++) {
							for (int jj = -1; jj <= 1; jj++) {
								for (int kk = -1; kk <= 1; kk++) {
									for (int ll = -1; ll <= 1; ll++) {
										if (i != 0 && j != 0 && k != 0 && l != 0) {
											final float curr = (float) Math
													.sqrt(spectrogram[i + ii][j + jj][k + kk][(l + ll) * 2]
															* spectrogram[i + ii][j + jj][k + kk][(l + ll) * 2]
															+ spectrogram[i + ii][j + jj][k + kk][(l + ll) * 2 + 1]
															* spectrogram[i + ii][j + jj][k + kk][(l + ll) * 2 + 1]);

											if (curr > centre) {
												max = false;
											}
										}
									}
								}
							}
						}

						if (max)
							System.out.println("max " + i + " " + j + " " + k + " " + l);
					}
				}
			}
		}
	}
}
