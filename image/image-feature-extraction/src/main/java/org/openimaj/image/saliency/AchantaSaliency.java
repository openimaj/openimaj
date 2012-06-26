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
package org.openimaj.image.saliency;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.processing.convolution.FGaussianConvolve;

/**
 * Implementation of the saliency map algorithm described in:
 * 
 * R. Achanta, S. Hemami, F. Estrada and S. Susstrunk, Frequency-tuned Salient 
 * Region Detection, IEEE International Conference on Computer Vision and 
 * Pattern Recognition (CVPR), 2009.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Achanta, Radhakrishna", "Hemami, Sheila", "Estrada, Francisco", "S{\"u}sstrunk, Sabine" },
		title = "Frequency-tuned {S}alient {R}egion {D}etection",
		year = "2009",
		booktitle = "{IEEE} {I}nternational {C}onference on {C}omputer {V}ision and {P}attern {R}ecognition ({CVPR})",
		url = "http://infoscience.epfl.ch/record/135217/files/1708.pdf",
		customData = { "Affiliation", "EPFL", "Details", "http://infoscience.epfl.ch/record/135217", "Keywords", "IVRG; NCCR-MICS; NCCR-MICS/CL4; K-Space; PHAROS; Saliency; Segmentation; Frequency-domain analysis", "Location", "Miami Beach, Florida" }
	)
public class AchantaSaliency implements SaliencyMapGenerator<MBFImage> {
	protected float sigma;
	protected FImage map;
	
	/**
	 * Construct with the given amount of smoothing.
	 * @param sigma standard deviation of Gaussian kernel smoothing
	 */
	public AchantaSaliency(float sigma) {
		this.sigma = sigma;
	}
	
	/**
	 * Construct with a smoothing of 1 pixel standard deviation.
	 */
	public AchantaSaliency() {
		this.sigma = 1;
	}
	
	/* (non-Javadoc)
	 * @see org.openimaj.image.analyser.ImageAnalyser#analyseImage(org.openimaj.image.Image)
	 */
	@Override
	public void analyseImage(MBFImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		MBFImage lab = ColourSpace.convert(image, ColourSpace.CIE_Lab);
		
		float[][] Lb = lab.getBand(0).pixels;
		float[][] ab = lab.getBand(1).pixels;
		float[][] bb = lab.getBand(2).pixels;
		float mL = 0, ma = 0, mb = 0;
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				mL += Lb[y][x];
				ma += ab[y][x];
				mb += bb[y][x];
			}
		}
		
		mL /= (height*width);
		ma /= (height*width);
		mb /= (height*width);
		
		//blur
		MBFImage blur = lab.process(new FGaussianConvolve(sigma));
		Lb = blur.getBand(0).pixels;
		ab = blur.getBand(1).pixels;
		bb = blur.getBand(2).pixels;
		
		//create map
		map = new FImage(width, height);
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				float dL = (Lb[y][x]-mL);
				float da = (ab[y][x]-ma);
				float db = (bb[y][x]-mb);
				
				map.pixels[y][x] = dL*dL + da*da + db*db;
			}
		}
		map.normalise();
	}
	
	@Override
	public FImage getSaliencyMap() {
		return map;
	}
}
