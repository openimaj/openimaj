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

import java.io.File;
import java.io.IOException;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.EnumFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processor.PixelProcessor;


/**
 * Implementation of Hasler and Susstruck's Colorfulness metric
 * http://infoscience.epfl.ch/record/33994/files/HaslerS03.pdf?version=1
 * 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class Colorfulness implements PixelProcessor<Float[]>, FeatureVectorProvider<DoubleFV> {
	int n = 0;
	double mean_rg = 0;
	double mean_yb = 0;
	double m2_rg = 0;
	double m2_yb = 0;
	
	
	
	@Override
	public Float[] processPixel(Float[] pixel, Number[]... otherpixels) {
		float r = pixel[0];
		float g =pixel[1];
		float b = pixel[2];
		
		float rg = r - g;
		float yb = 0.5f * (r+g) - b;
		
		n++;
		double delta_rg = rg - mean_rg;
		double delta_yb = yb - mean_yb;
		mean_rg += delta_rg / n;
		mean_yb += delta_yb / n;
		
		m2_rg += delta_rg * (rg - mean_rg);
		m2_yb += delta_yb * (yb - mean_yb);
		
		return pixel;
	}

	public enum ColorfulnessAttr implements FeatureVectorProvider<EnumFV<ColorfulnessAttr>> {
		NOT(0.0),
		SLIGHTLY(15.0 / 255.0),
		MODERATELY(33.0 / 255.0),
		AVERAGELY(45.0 / 255.0),
		QUITE(59.0 / 255.0),
		HIGHLY(82.0 / 255.0),
		EXTREMELY(109.0 / 255.0);
		
		private double threshold;

		private ColorfulnessAttr(double val) {
			this.threshold = val;
		}
		
		public static ColorfulnessAttr getAttr(double val) {
			ColorfulnessAttr [] attrs = values();
			for (int i=attrs.length-1; i>=0; i--) {
				if (val >= attrs[i].threshold)
					return attrs[i];
			}
			return null;
		}

		@Override
		public EnumFV<ColorfulnessAttr> getFeatureVector() {
			return new EnumFV<ColorfulnessAttr>(this);
		}
	}
	
	public ColorfulnessAttr getColorfulnessAttribute() {
		return ColorfulnessAttr.getAttr(getColorfulness());
	}
	
	public double getColorfulness() {
		double var_rg = m2_rg / n;
		double var_yb = m2_yb / n;
		
		double stddev = Math.sqrt(var_rg + var_yb);
		double mean = Math.sqrt(mean_rg*mean_rg + mean_yb*mean_yb);
		
		return stddev + 0.3*mean;
	}
	
	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double[]{ getColorfulness() });
	}
	
	public void reset() {
		n = 0;
		mean_rg = 0;
		mean_yb = 0;
		m2_rg = 0;
		m2_yb = 0;
	}
	
	public static void main(String [] args) throws IOException {
//		MBFImage image = ImageUtilities.readMBF(new File("/Users/jsh2/Desktop/test.jpg"));
//		MBFImage image = ImageUtilities.readMBF(new File("/Users/jsh2/Desktop/testsep.jpg"));
		MBFImage image = ImageUtilities.readMBF(new File("/Users/jsh2/Pictures/08-earth_shuttle1.jpg"));
//		MBFImage image = ImageUtilities.readMBF(new File("/Users/jsh2/Pictures/la-v3-l-1280.jpg"));
//		MBFImage image = ImageUtilities.readMBF(new URL("http://farm4.static.flickr.com/3067/2612399892_7df428d482.jpg"));
//		MBFImage image = ImageUtilities.readMBF(new File("/Users/jsh2/Pictures/mandolux-ca-l-1280.jpg"));
		Colorfulness cf = new Colorfulness();
		image.process(cf);
		
		System.out.println(cf.getFeatureVector());
		System.out.println(cf.getColorfulnessAttribute());
	}
}
