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
package org.openimaj.image.processing.algorithm;

import org.openimaj.image.FImage;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

/**
 * Perform forward and inverse Fast Fourier Transforms on image data.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FourierTransform {
	private FImage phase;
	private FImage magnitude;
	private boolean centre;

	/**
	 * Construct Fourier Transform by performing a forward transform
	 * on the given image. If the centre option is set, the FFT will 
	 * be re-ordered so that the DC component is in the centre.
	 * @param image the image to transform
	 * @param centre should the FFT be reordered so the centre is DC component
	 */
	public FourierTransform(FImage image, boolean centre) {
		this.centre = centre;

		process(image);
	}
	
	/**
	 * Construct Fourier Transform object from the given magnitude and
	 * phase images in the frequency domain. The resultant object can
	 * then be used to construct the image using the {@link #inverse()}
	 * method.
	 * 
	 * @param magnitude the magnitude image
	 * @param phase the phase image
	 * @param centre is the DC component in the image centre?
	 */
	public FourierTransform(FImage magnitude, FImage phase, boolean centre) {
		this.centre = centre;
		this.magnitude = magnitude;
		this.phase = phase;
	}
	
	/**
	 * Prepare data for a input to the FFT, padding if necessary.
	 * 
	 * @param input input data
	 * @param rs desired number of rows
	 * @param cs desired number of columns
	 * @param centre if true, then the data will be prepared so that the DC component is centred.
	 * @return prepared data
	 */
	public static float[][] prepareData(FImage input, int rs, int cs, boolean centre) {
		return prepareData(input.pixels, rs, cs, centre);
	}
	
	/**
	 * Prepare data for a input to the FFT, padding if necessary. The data is
	 * prepared as a packed 1D array.
	 * 
	 * @param input input data
	 * @param rs desired number of rows
	 * @param cs desired number of columns
	 * @param centre if true, then the data will be prepared so that the DC component is centred.
	 * @return prepared data
	 */
	public static float[] prepareData1d(FImage input, int rs, int cs, boolean centre) {
		return prepareData1d(input.pixels, rs, cs, centre);
	}
	
	/**
	 * Prepare data for a input to the FFT, padding if necessary.
	 * 
	 * @param input input data
	 * @param rs desired number of rows
	 * @param cs desired number of columns
	 * @param centre if true, then the data will be prepared so that the DC component is centered.
	 * @return prepared data
	 */
	public static float[][] prepareData(float[][] input, int rs, int cs, boolean centre) {
		float[][] prepared = new float[rs][cs*2];

		if (centre) {
			for(int r = 0; r < Math.min(rs, input.length) ; r++) {
				for(int c = 0; c < Math.min(cs, input[0].length); c++) {
					prepared[r][c*2] = input[r][c] * (1 - 2 * ((r+c)%2) );
				}
			}
		} else {
			for(int r = 0; r < Math.min(rs, input.length) ; r++) {
				for(int c = 0; c < Math.min(cs, input[0].length); c++) {
					prepared[r][c*2] = input[r][c];
				}
			}
		}

		return prepared;
	}
	
	/**
	 * Prepare data for a input to the FFT, padding if necessary. The data is
	 * prepared as a packed 1D array.
	 * 
	 * @param input input data
	 * @param rs desired number of rows
	 * @param cs desired number of columns
	 * @param centre if true, then the data will be prepared so that the DC component is centered.
	 * @return prepared data
	 */
	public static float[] prepareData1d(float[][] input, int rs, int cs, boolean centre) {
		float[] prepared = new float[rs * cs * 2];
		
		if (centre) {
			for(int r = 0; r < Math.min(rs, input.length) ; r++) {
				for(int c = 0; c < Math.min(cs, input[0].length); c++) {
					prepared[r * 2 * cs + 2*c] = input[r][c] * (1 - 2 * ((r+c)%2) );
				}
			}
		} else {
			for(int r = 0; r < Math.min(rs, input.length) ; r++) {
				for(int c = 0; c < Math.min(cs, input[0].length); c++) {
					prepared[r * 2 * cs + 2*c] = input[r][c];
				}
			}
		}

		return prepared;
	}

	/**
	 * Extract the actual data from prepared data. The output image
	 * must have the same number of rows as the prepared data, and
	 * half the number of columns.
	 * 
	 * @param prepared the prepared data
	 * @param output the output 
	 * @param centre if true, then the data will be prepared so that the DC component is centered.
	 */
	public static void unprepareData(float[][] prepared, FImage output, boolean centre) {
		unprepareData(prepared, output.pixels, centre);
	}
	
	/**
	 * Extract the actual data from prepared data. The output image
	 * must have the same number of rows as the prepared data, and
	 * half the number of columns.
	 * 
	 * @param prepared the prepared data
	 * @param output the output 
	 * @param centre if true, then the data will be prepared so that the DC component is centered.
	 */
	public static void unprepareData(float[] prepared, FImage output, boolean centre) {
		unprepareData(prepared, output.pixels, centre);
	}
	
	/**
	 * Extract the actual data from prepared data. The output array
	 * must have the same number of rows as the prepared data, and
	 * half the number of columns.
	 * 
	 * @param prepared the prepared data
	 * @param output the output 
	 * @param centre if true, then the data will be prepared so that the DC component is centered.
	 */
	public static void unprepareData(float[][] prepared, float[][] output, boolean centre) {
		int rs = output.length;
		int cs = output[0].length;
		
		if (centre) {
			for(int r = 0; r < rs ; r++) {
				for(int c = 0; c < cs; c++) {
					output[r][c] = prepared[r][c*2] * (1 - 2 * ((r+c)%2) );
				}
			}
		} else {
			for(int r = 0; r < rs ; r++) {
				for(int c = 0; c < cs; c++) {
					output[r][c] = prepared[r][c*2];
				}
			}
		}
	}
	
	/**
	 * Extract the actual data from prepared data. The output array
	 * must have the same number of rows as the prepared data, and
	 * half the number of columns.
	 * 
	 * @param prepared the prepared data
	 * @param output the output 
	 * @param centre if true, then the data will be prepared so that the DC component is centered.
	 */
	public static void unprepareData(float[] prepared, float[][] output, boolean centre) {
		int rs = output.length;
		int cs = output[0].length;
		
		if (centre) {
			for(int r = 0; r < rs ; r++) {
				for(int c = 0; c < cs; c++) {
					output[r][c] = prepared[r * 2 * cs + 2*c] * (1 - 2 * ((r+c)%2) );
				}
			}
		} else {
			for(int r = 0; r < rs ; r++) {
				for(int c = 0; c < cs; c++) {
					output[r][c] = prepared[r * 2 * cs + 2*c];
				}
			}
		}
	}
	
	private void process(FImage image) {
		int cs = image.getCols();
		int rs = image.getRows();
		
		phase = new FImage(cs, rs);
		magnitude = new FImage(cs, rs);
		
		FloatFFT_2D fft = new FloatFFT_2D(rs,cs);
		float[][] prepared = prepareData(image.pixels, rs, cs, centre);
	
		fft.complexForward(prepared);
		
		for(int y = 0; y < rs; y++){
			for(int x = 0; x < cs; x++){
				float re = prepared[y][x*2];
				float im = prepared[y][1 + x*2];
				
				phase.pixels[y][x] = (float) Math.atan2(im, re);
				magnitude.pixels[y][x] = (float) Math.sqrt(re*re + im*im);
			}
		}
	}
	
	/**
	 * Perform the inverse FFT using the underlying magnitude
	 * and phase images. The resultant reconstructed image
	 * may need normalisation.
	 * 
	 * @return the reconstructed image
	 */
	public FImage inverse() {
		int cs = magnitude.getCols();
		int rs = magnitude.getRows();
		
		FloatFFT_2D fft = new FloatFFT_2D(rs,cs);
		float[][] prepared = new float[rs][cs*2];
		for(int y = 0; y < rs; y++) {
			for(int x = 0; x < cs; x++) {
				float p = phase.pixels[y][x];
				float m = magnitude.pixels[y][x];

				float re = (float) (m*Math.cos(p));
				float im = (float) (m*Math.sin(p));
				
				prepared[y][x*2] = re;
				prepared[y][1 + x*2] = im;				
			}
		}
		
		fft.complexInverse(prepared, true);
		
		FImage image = new FImage(cs, rs);
		unprepareData(prepared, image, centre);
		
		return image;
	}
	
	/**
	 * Get a log-normalised copy of the magnitude image suitable
	 * for displaying.
	 * @return a log-normalised copy of the magnitude image 
	 */
	public FImage getLogNormalisedMagnitude() {
		FImage im = magnitude.clone();
		
		for (int y=0; y<im.height; y++) {
			for (int x=0; x<im.width; x++) {
				im.pixels[y][x] = (float) Math.log(im.pixels[y][x] + 1);
			}
		}
		
		return im.normalise();
	}
	
	/**
	 * @return the phase image
	 */
	public FImage getPhase() {
		return phase;
	}

	/**
	 * @return the magnitude image
	 */
	public FImage getMagnitude() {
		return magnitude;
	}

	/**
	 * @return true if the DC component is in the centre; false otherwise
	 */
	public boolean isCentre() {
		return centre;
	}
}
