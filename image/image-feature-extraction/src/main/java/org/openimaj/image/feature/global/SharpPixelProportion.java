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
package org.openimaj.image.feature.global;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.algorithm.FourierTransform;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processor.ImageProcessor;

/**
 * Implementation of the blur estimation feature described in:
 * 
 * Y. Ke, X. Tang, and F. Jing. The Design of High-Level Features 
 * for Photo Quality Assessment.  Computer Vision and Pattern 
 * Recognition, 2006
 * 
 * and
 * 
 * Che-Hua Yeh, Yuan-Chen Ho, Brian A. Barsky, Ming Ouhyoung.
 * Personalized photograph ranking and selection system.
 * In Proceedings of ACM Multimedia'2010. pp.211~220
 * 
 * Basically, this technique estimates the proportion of blurred
 * pixels by thresholding the power-spectrum (magnitude) of the 
 * FFT of the image. Results are in the range 0-1. A higher number
 * implies a sharper image.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class SharpPixelProportion implements ImageProcessor<FImage>, FeatureVectorProvider<DoubleFV> {
	double bpp = 0;
	private float threshold = 2f;
	
	public SharpPixelProportion() {}
	
	public SharpPixelProportion(float threshold) {
		this.threshold = threshold;
	}
	
	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double[] { bpp });
	}

	@Override
	public void processImage(FImage image, Image<?, ?>... otherimages) {
		FourierTransform ft = new FourierTransform(image, false);
		FImage mag = ft.getMagnitude();
		
		int count = 0;
		for(int y = 0; y < mag.height ; y++) {
			for(int x = 0; x < mag.width; x++) {
				if ( Math.abs(mag.pixels[y][x]) > threshold) count++; 
			}
		}
		bpp = (double)count / (double)(mag.height * mag.width);
		
		DisplayUtilities.display(image, ""+bpp);
	}

	public static void main(String [] args) throws MalformedURLException, IOException {
		SharpPixelProportion s = new SharpPixelProportion();
		FImage image = ImageUtilities.readF(new URL("http://farm1.static.flickr.com/8/9190606_8024996ff7.jpg"));
//		FImage image = ImageUtilities.readF(new URL("http://upload.wikimedia.org/wikipedia/commons/8/8a/Josefina_with_Bokeh.jpg"));
//		FImage image = ImageUtilities.readF(new URL("http://upload.wikimedia.org/wikipedia/commons/4/4a/Thumbs_up_for_bokeh.JPG"));
		
		FImage foo = image;
		for (int i=0; i<10; i++) {
			float sigma = 0.6f + (0.1f * i);
			
			DisplayUtilities.display(foo, ""+sigma);
			
			foo.process(s);
			
			System.out.println(sigma + " " + s.getFeatureVector());
			
			sigma = 0.6f + (0.1f * (i+1));
			foo = image.process(new FGaussianConvolve(sigma));
		}
	}

	public double getBlurredPixelProportion() {
		return bpp;
	}
}
