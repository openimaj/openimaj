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
package org.openimaj.image.processing.convolution;

import odk.lang.FastMath;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;

/**
 * Image processor for calculating gradients and orientations
 * using finite-differences.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FImageGradients implements ImageAnalyser<FImage> {
	private final static float TWO_PI_FLOAT = (float) (Math.PI * 2);
	
	/**
	 * The gradient magnitudes
	 */
	public FImage magnitudes;
	/**
	 * The gradient orientations
	 */
	public FImage orientations;
	
	/* (non-Javadoc)
	 * @see org.openimaj.image.analyser.ImageAnalyser#analyseImage(org.openimaj.image.Image)
	 */
	@Override
	public void analyseImage(FImage image) {
		if (magnitudes == null || 
				magnitudes.height != image.height || 
				magnitudes.width != image.width) {
			magnitudes = new FImage(image.width, image.height);
			orientations = new FImage(image.width, image.height);
		}

		gradientMagnitudesAndOrientations(image, magnitudes, orientations);
	}

	/**
	 * Static function calling {@link #gradientMagnitudesAndOrientations} with an image.
	 * @param image the image 
	 * @return a FImageGradients for the image
	 */
	public static FImageGradients getGradientMagnitudesAndOrientations(FImage image) {
		FImageGradients go = new FImageGradients();

		go.magnitudes = new FImage(image.width, image.height);
		go.orientations = new FImage(image.width, image.height);

		gradientMagnitudesAndOrientations(image, go.magnitudes, go.orientations);

		return go;
	}

	/**
	 * Estimate gradients magnitudes and orientations by calculating pixel 
	 * differences. Edges get special treatment. The resultant gradients 
	 * and orientations are returned though the gradients and orientations 
	 * parameters  respectively. The images represented by the gradients 
	 * and orientations parameters are assumed to be initialized to the 
	 * same size as the input image. 
	 * 
	 * @param image
	 * @param magnitudes
	 * @param orientations
	 */
	public static void gradientMagnitudesAndOrientations(FImage image, FImage magnitudes, FImage orientations)
	{
		//Note: unrolling this loop to remove the if's doesn't
		//actually seem to make it faster!
		for (int r=0; r<image.height; r++) {
			for (int c=0; c<image.width; c++) {
				float xgrad, ygrad; 

				if( c == 0 )
					xgrad = 2.0f * (image.pixels[r][c + 1] - image.pixels[r][c]);
				else if( c == image.width - 1 )
					xgrad = 2.0f * (image.pixels[r][c] - image.pixels[r][c - 1]);
				else
					xgrad = image.pixels[r][c + 1] - image.pixels[r][c - 1];
				if( r == 0 )
					ygrad = 2.0f * (image.pixels[r][c] - image.pixels[r + 1][c]);
				else if( r == image.height - 1 )
					ygrad = 2.0f * (image.pixels[r - 1][c] - image.pixels[r][c]);
				else
					ygrad = image.pixels[r - 1][c] - image.pixels[r + 1][c];
				
				//magnitudes.pixels[r][c] = (float) Math.sqrt( xgrad * xgrad + ygrad * ygrad );
				//orientations.pixels[r][c] = (float) Math.atan2( ygrad, xgrad );
				
				//JH - my benchmarking shows that (at least on OSX) Math.atan2 is really
				//slow... FastMath provides an alternative that is much faster
				magnitudes.pixels[r][c] = (float) Math.sqrt( xgrad * xgrad + ygrad * ygrad );
				orientations.pixels[r][c] = (float) FastMath.atan2( ygrad, xgrad );
			}
		}
	}
	
	/**
	 * Estimate gradients magnitudes and orientations by calculating pixel 
	 * differences. Edges get special treatment. 
	 * 
	 * The orientations are quantised into magnitudes.length bins and
	 * the magnitudes are spread to the adjacent bin through linear
	 * interpolation. The magnitudes parameter must be fully allocated
	 * as an array of num orientation bin images, each of the same size
	 * as the input image. 
	 * 
	 * @param image
	 * @param magnitudes
	 */
	public static void gradientMagnitudesAndQuantisedOrientations(FImage image, FImage [] magnitudes)
	{
		final int numOriBins = magnitudes.length;
		
		//Note: unrolling this loop to remove the if's doesn't
		//actually seem to make it faster!
		for (int r=0; r<image.height; r++) {
			for (int c=0; c<image.width; c++) {
				float xgrad, ygrad; 

				if( c == 0 )
					xgrad = 2.0f * (image.pixels[r][c + 1] - image.pixels[r][c]);
				else if( c == image.width - 1 )
					xgrad = 2.0f * (image.pixels[r][c] - image.pixels[r][c - 1]);
				else
					xgrad = image.pixels[r][c + 1] - image.pixels[r][c - 1];
				if( r == 0 )
					ygrad = 2.0f * (image.pixels[r][c] - image.pixels[r + 1][c]);
				else if( r == image.height - 1 )
					ygrad = 2.0f * (image.pixels[r - 1][c] - image.pixels[r][c]);
				else
					ygrad = image.pixels[r - 1][c] - image.pixels[r + 1][c];
				
				//JH - my benchmarking shows that (at least on OSX) Math.atan2 is really
				//slow... FastMath provides an alternative that is much faster
				final float mag = (float) Math.sqrt( xgrad * xgrad + ygrad * ygrad );
				final float ori = (float) FastMath.atan2( ygrad, xgrad );

				final float po = numOriBins * ori / TWO_PI_FLOAT; //po is now 0<=po<oriSize
				final int oi = (int) Math.floor(po);
				final float of = po - oi;

				magnitudes[ oi   % numOriBins].pixels[r][c] = (1f - of) * mag;
				magnitudes[(oi+1)% numOriBins].pixels[r][c] = of * mag;
			}
		}
	}
}
