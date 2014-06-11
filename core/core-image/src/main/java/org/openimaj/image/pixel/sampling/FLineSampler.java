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
package org.openimaj.image.pixel.sampling;

import org.openimaj.image.FImage;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;

/**
 * Methods for sampling along a line in an {@link FImage}. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public enum FLineSampler implements LineSampler<FImage, float[]> {
	/**
	 * Sample at 1-pixel intervals about the centre of the
	 * line using nearest-neighbour interpolation. The 
	 * overall length of the line is ignored. Values
	 * outside the image are considered to be 0.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	PIXELSTEP_NEAREST_NEIGHBOUR {
		@Override
		public float[] extractSamples(Line2d line, FImage image, int numSamples) {
			Line2d scaled = getSampleLine(line, image, numSamples);
			
			return NEAREST_NEIGHBOUR.extractSamples(scaled, image, numSamples);
		}
		
		@Override
		public Line2d getSampleLine(Line2d line, FImage image, int numSamples) {
			Line2d scaled = line.clone();
			scaled.scaleCentroid((float) (numSamples / line.calculateLength()));
			return scaled;
		}
	},
	/**
	 * Sample the pixel values at regular intervals along the full length
	 * of the line taking the sample value from the nearest pixel. Pixels 
	 * outside the image have a value of 0.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	NEAREST_NEIGHBOUR {
		@Override
		public float[] extractSamples(Line2d line, FImage image, int numSamples) {
			float[] samples = new float[numSamples];
			
			final Point2d p1 = line.getBeginPoint();
			final Point2d p2 = line.getEndPoint();
			float x = p1.getX();
			float y = p1.getY();
			final float dxStep = (p2.getX() - x) / (numSamples-1);
			final float dyStep = (p2.getY() - y) / (numSamples-1);
			
			final int width = image.width;
			final int height = image.height;
			
			for (int i=0; i<numSamples; i++) {
				int ix = Math.round(x);
				int iy = Math.round(y);
				
				if (ix < 0 || ix >= width || iy < 0 || iy >= height)
					samples[i] = 0;
				else
					samples[i] = image.getPixelNative(ix, iy);
				
				x += dxStep;
				y += dyStep;
			}
			
			return samples;
		}
	},
	/**
	 * Sample at 1-pixel intervals about the centre of the
	 * line using bilinear interpolation to estimate sub-pixel
	 * values. The overall length of the line is ignored. Values
	 * outside the image are considered to be 0.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	PIXELSTEP_INTERPOLATED {
		@Override
		public float[] extractSamples(Line2d line, FImage image, int numSamples) {
			Line2d scaled = getSampleLine(line, image, numSamples);
			
			return INTERPOLATED.extractSamples(scaled, image, numSamples);
		}
		
		@Override
		public Line2d getSampleLine(Line2d line, FImage image, int numSamples) {
			Line2d scaled = line.clone();
			scaled.scaleCentroid((float) (numSamples / line.calculateLength()));
			return scaled;
		}
	},
	/**
	 * Sample the pixel values at regular intervals along the full
	 * length of the line using bilinear interpolation to get 
	 * sub-pixel values.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	INTERPOLATED {
		@Override
		public float[] extractSamples(Line2d line, FImage image, int numSamples) {
			float[] samples = new float[numSamples];
			
			Point2d p1 = line.getBeginPoint();
			Point2d p2 = line.getEndPoint();
			float x = p1.getX();
			float y = p1.getY();
			float dxStep = (p2.getX() - x) / (numSamples-1);
			float dyStep = (p2.getY() - y) / (numSamples-1);
			
			for (int i=0; i<numSamples; i++) {
				samples[i] = image.getPixelInterpNative(x, y, 0);
				
				x += dxStep;
				y += dyStep;
			}
			
			return samples;
		}
	},
	/**
	 * Sample at 1-pixel intervals about the centre of the
	 * line using nearest neighbour interpolation to estimate values
	 * and then compute the derivative using [1 0 -1] filter. The number of 
	 * regular samples is two bigger than the requested number so 
	 * the number of derivative samples is as requested. 
	 * The overall length of the line is ignored. Values
	 * outside the image are considered to be 0.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	PIXELSTEP_NEAREST_NEIGHBOUR_DERIVATIVE {
		@Override
		public float[] extractSamples(Line2d line, FImage image, int numSamples) {
			Line2d scaled = getSampleLine(line, image, numSamples+2);
			
			return NEAREST_NEIGHBOUR_DERIVATIVE.extractSamples(scaled, image, numSamples);
		}
		
		@Override
		public Line2d getSampleLine(Line2d line, FImage image, int numSamples) {
			Line2d scaled = line.clone();
			scaled.scaleCentroid((float) (numSamples / line.calculateLength()));
			return scaled;
		}
	},
	/**
	 * Sample the pixel values at regular intervals along the full
	 * length of the line using nearest-neighbour pixels and then 
	 * compute the derivative using [1 0 -1] filter. The number of 
	 * regular samples is two bigger than the requested number so 
	 * the number of derivative samples is as requested.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	NEAREST_NEIGHBOUR_DERIVATIVE {
		@Override
		public float[] extractSamples(Line2d line, FImage image, int numSamples) {
			float[] samples = NEAREST_NEIGHBOUR.extractSamples(line, image, numSamples+2);
			return computeDerivative(samples);
		}
	},
	/**
	 * Sample at 1-pixel intervals about the centre of the
	 * line using bilinear interpolation to estimate sub-pixel values
	 * and then compute the derivative using [1 0 -1] filter. The number of 
	 * regular samples is two bigger than the requested number so 
	 * the number of derivative samples is as requested. 
	 * The overall length of the line is ignored. Values
	 * outside the image are considered to be 0.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	PIXELSTEP_INTERPOLATED_DERIVATIVE {
		@Override
		public float[] extractSamples(Line2d line, FImage image, int numSamples) {
			Line2d scaled = getSampleLine(line, image, numSamples+2);
			
			return NEAREST_NEIGHBOUR_DERIVATIVE.extractSamples(scaled, image, numSamples);
		}
		
		@Override
		public Line2d getSampleLine(Line2d line, FImage image, int numSamples) {
			Line2d scaled = line.clone();
			scaled.scaleCentroid((float) (numSamples / line.calculateLength()));
			return scaled;
		}
	},
	/**
	 * Sample the pixel values at regular intervals along the full
	 * length of the line using bilinear interpolation and then 
	 * compute the derivative using [1 0 -1] filter. The number 
	 * of regular samples is two bigger than the requested number 
	 * so the number of derivative samples is as requested.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	INTERPOLATED_DERIVATIVE {
		@Override
		public float[] extractSamples(Line2d line, FImage image, int numSamples) {
			float[] samples = INTERPOLATED.extractSamples(line, image, numSamples+2);
			return computeDerivative(samples);
		}
	}
	;
	
	/**
	 * Compute the derivative with a [1 0 -1] filter. The returned
	 * derivatives will have a length of samples.length-2.
	 * @param samples the samples to differentiate
	 * @return differentiated samples
	 */
	private static float[] computeDerivative(float[] samples) {
		final int numSamples = samples.length - 2;
		final float[] dsamples = new float[numSamples];
		
		for (int i=0; i<numSamples; i++) {
			dsamples[i] = samples[i] - samples[i+2];
		}
		
		return dsamples;
	}
	
	
	@Override
	public Line2d getSampleLine(Line2d line, FImage image, int numSamples) {
		//most implementations sample the full length of the line
		return line;
	}
	
	@Override
	public abstract float[] extractSamples(Line2d line, FImage image, int numSamples);
}