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

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.colour.ColourSpace;

/**
 * Implementation of the RGB RMS contrast feature.
 * <p>
 * See the referenced paper for a description. The original python looked like:
 * <code><pre>
 * cdata = image.getdata()
 * center = [0.0,0.0,0.0]
 * 
 * for v in cdata:
 *  center[0]+=v[0]
 * 	center[1]+=v[1]
 * 	center[2]+=v[2]
 * 
 * for i in xrange(len(center)):
 *  center[i]/=len(cdata) #mean vector of the RGB values
 * 	
 * contrast2 = 0.0
 * for v in cdata:
 *  contrast2 += ((v[0]-center[0])**2) + ((v[1]-center[1])**2) + ((v[2]-center[2])**2) #norm of differences wrt the mean
 * contrast2 /= len(cdata)
 * </pre></code>
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Jose San Pedro", "Stefan Siersdorfer" },
		title = "Ranking and Classifying Attractiveness of Photos in Folksonomies",
		year = "2009",
		booktitle = "18th International World Wide Web Conference",
		pages = { "771", "", "771" },
		url = "http://www2009.eprints.org/78/",
		month = "April")
public class RGBRMSContrast implements ImageAnalyser<MBFImage>, FeatureVectorProvider<DoubleFV> {
	double contrast;

	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double[] { contrast });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj
	 * .image.Image)
	 */
	@Override
	public void analyseImage(MBFImage image) {
		final int width = image.getWidth();
		final int height = image.getHeight();

		image = ColourSpace.convert(image, ColourSpace.RGB);

		final FImage r = image.getBand(0);
		final FImage g = image.getBand(1);
		final FImage b = image.getBand(2);

		double avgr = 0;
		double avgg = 0;
		double avgb = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				avgr += r.pixels[y][x];
				avgg += g.pixels[y][x];
				avgb += b.pixels[y][x];
			}
		}
		avgr /= (width * height);
		avgg /= (width * height);
		avgb /= (width * height);

		contrast = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final double deltar = r.pixels[y][x] - avgr;
				final double deltag = g.pixels[y][x] - avgg;
				final double deltab = b.pixels[y][x] - avgb;

				contrast += (deltar * deltar) + (deltag * deltag) + (deltab * deltab);
			}
		}

		contrast /= (height * width);
	}

	/**
	 * Get the contrast of the last image analysed with
	 * {@link #analyseImage(MBFImage)}
	 * 
	 * @return the contrast
	 */
	public double getContrast() {
		return contrast;
	}
}
