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
