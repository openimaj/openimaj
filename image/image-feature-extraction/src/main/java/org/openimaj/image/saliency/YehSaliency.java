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

import gnu.trove.map.hash.TObjectFloatHashMap;

import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.segmentation.FelzenszwalbHuttenlocherSegmenter;

/**
 * Implementation of the region-based saliency algorithm described in:
 * 
 * Che-Hua Yeh, Yuan-Chen Ho, Brian A. Barsky, Ming Ouhyoung.
 * Personalized photograph ranking and selection system.
 * In Proceedings of ACM Multimedia'2010. pp.211~220
 * 
 * This algorithm is used to create a Rule-of-Thirds feature for images.
 * 
 * The algorithm uses the {@link AchantaSaliency} approach to get the saliency
 * values for individual pixels. Regions are segmented from the image 
 * using a {@link FelzenszwalbHuttenlocherSegmenter}. Saliency values are
 * generated for each region by averaging the saliency values of the
 * pixels within the region.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Che-Hua Yeh, Yuan-Chen Ho, Brian A. Barsky, Ming Ouhyoung" },
		title = "Personalized Photograph Ranking and Selection System",
		year = "2010",
		booktitle = "Proceedings of ACM Multimedia",
		pages = { "211", "220" },
		month = "October",
		customData = { "location", "Florence, Italy" }
	)
public class YehSaliency implements SaliencyMapGenerator<MBFImage> {
	AchantaSaliency saliencyGenerator;
	FelzenszwalbHuttenlocherSegmenter<MBFImage> segmenter;
	protected FImage map;
	protected TObjectFloatHashMap<ConnectedComponent> componentMap;
	
	/**
	 * Construct with default settings for the {@link AchantaSaliency} 
	 * and {@link FelzenszwalbHuttenlocherSegmenter}.
	 */
	public YehSaliency() {
		saliencyGenerator = new AchantaSaliency();
		segmenter = new FelzenszwalbHuttenlocherSegmenter<MBFImage>();
	}
	
	/**
	 * Construct with custom parameters.
	 * @param saliencySigma smoothing for the {@link AchantaSaliency} class
	 * @param segmenterSigma smoothing for {@link FelzenszwalbHuttenlocherSegmenter}.
	 * @param k k value for {@link FelzenszwalbHuttenlocherSegmenter}.
	 * @param minSize minimum region size for {@link FelzenszwalbHuttenlocherSegmenter}.
	 */
	public YehSaliency(float saliencySigma, float segmenterSigma, float k, int minSize) {
		saliencyGenerator = new AchantaSaliency(saliencySigma);
		segmenter = new FelzenszwalbHuttenlocherSegmenter<MBFImage>(segmenterSigma, k, minSize);
	}

	/* (non-Javadoc)
	 * @see org.openimaj.image.analyser.ImageAnalyser#analyseImage(org.openimaj.image.Image)
	 */
	@Override
	public void analyseImage(MBFImage image) {
		List<ConnectedComponent> ccs = segmenter.segment(image);
		
		image.analyseWith(saliencyGenerator);
		map = saliencyGenerator.getSaliencyMap();
		componentMap = new TObjectFloatHashMap<ConnectedComponent>();
		
		for (ConnectedComponent cc : ccs) {
			float mean = 0;
			
			for (Pixel p : cc.pixels) {
				mean += map.pixels[p.y][p.x];
			}
			
			mean /= cc.pixels.size();
			
			for (Pixel p : cc.pixels) {
				map.pixels[p.y][p.x] = mean;
			}
			
			componentMap.put(cc, mean);
		}
	}

	@Override
	public FImage getSaliencyMap() {
		return map;
	}
	
	/**
	 * Get a map of component->saliency for all the components in
	 * the image
	 * @return component->saliency map
	 */
	public TObjectFloatHashMap<ConnectedComponent> getSaliencyComponents() {
		return componentMap;
	}
}
