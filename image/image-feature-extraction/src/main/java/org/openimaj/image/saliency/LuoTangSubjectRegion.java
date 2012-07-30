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
import org.openimaj.image.analysis.algorithm.HorizontalProjection;
import org.openimaj.image.analysis.algorithm.VerticalProjection;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Extract the subject region of an image based on the
 * part that is least blurred (most in-focus).
 * <p>
 * Algorithm based on:
 * Yiwen Luo and Xiaoou Tang. 2008. 
 * Photo and Video Quality Evaluation: Focusing on the Subject. 
 * In Proceedings of the 10th European Conference on Computer Vision: 
 * Part III (ECCV '08), David Forsyth, Philip Torr, and Andrew Zisserman (Eds.). 
 * Springer-Verlag, Berlin, Heidelberg, 386-399. DOI=10.1007/978-3-540-88690-7_29 
 * http://dx.doi.org/10.1007/978-3-540-88690-7_29
 * <p>
 * Note that this is not scale invariant - you will get different results with
 * different sized images...
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Luo, Yiwen", "Tang, Xiaoou" },
		title = "Photo and Video Quality Evaluation: Focusing on the Subject",
		year = "2008",
		booktitle = "Proceedings of the 10th European Conference on Computer Vision: Part III",
		pages = { "386", "", "399" },
		url = "http://dx.doi.org/10.1007/978-3-540-88690-7_29",
		publisher = "Springer-Verlag",
		series = "ECCV '08",
		customData = { 
				"isbn", "978-3-540-88689-1", 
				"location", "Marseille, France", 
				"numpages", "14", 
				"doi", "10.1007/978-3-540-88690-7_29", 
				"acmid", "1478204", 
				"address", "Berlin, Heidelberg" 
		}
	)
public class LuoTangSubjectRegion implements SaliencyMapGenerator<FImage> {
	DepthOfFieldEstimator dofEstimator;
	
	Rectangle roi;
	private FImage dofMap;
	private float alpha = 0.9f;
	
	/**
	 * Construct with default values for the {@link DepthOfFieldEstimator} 
	 * and an alpha parameter of 0.9.
	 */
	public LuoTangSubjectRegion() {
		dofEstimator = new DepthOfFieldEstimator();
	}
	
	/**
	 * Construct with the given parameters.
	 * @param alpha the alpha value.
	 * @param maxKernelSize Maximum kernel size for the {@link DepthOfFieldEstimator}.
	 * @param kernelSizeStep Kernel step size for the {@link DepthOfFieldEstimator}.
	 * @param nbins Number of bins for the {@link DepthOfFieldEstimator}.
	 * @param windowSize window size for the {@link DepthOfFieldEstimator}.
	 */
	public LuoTangSubjectRegion(float alpha, int maxKernelSize, int kernelSizeStep, int nbins, int windowSize) {
		this.dofEstimator = new DepthOfFieldEstimator(maxKernelSize, kernelSizeStep, nbins, windowSize);
		this.alpha = alpha;
	}
	
	/* (non-Javadoc)
	 * @see org.openimaj.image.analyser.ImageAnalyser#analyseImage(org.openimaj.image.Image)
	 */
	@Override
	public void analyseImage(FImage image) {
		image.analyseWith(dofEstimator);
		dofMap = dofEstimator.getSaliencyMap();
		
		for (int y=0; y<dofMap.height; y++) {
			for (int x=0; x<dofMap.width; x++) {
				if (dofMap.pixels[y][x] == 0) 
					dofMap.pixels[y][x] = 1;
				else 
					dofMap.pixels[y][x] = 0;
			}
		}
	}
	
	/**
	 * @return the estimated rectangular region of interest
	 */
	public Rectangle calculateROI() {		
		float [] pUx = HorizontalProjection.project(dofMap);
		float [] pUy = VerticalProjection.project(dofMap);
		
		float energy = 0;
		for (float f : pUx) energy += f;
		float thresh = energy * ((1 - alpha) / 2);
		
		int x1 = 0;
		float tmp = pUx[x1];
		while (tmp < thresh) {
			x1++;
			tmp += pUx[x1];
		}
		
		int y1 = 0;
		tmp = pUy[y1];
		while (tmp < thresh) {
			y1++;
			tmp += pUy[y1];
		}
		
		int x2 = pUx.length - 1;
		tmp = pUx[x2];
		while (tmp < thresh) {
			x2--;
			tmp += pUx[x2];
		}
		
		int y2 = pUy.length - 1;
		tmp = pUy[y2];
		while (tmp < thresh) {
			y2--;
			tmp += pUy[y2];
		}
		
		return new Rectangle(x1, y1, x2-x1, y2-y1);
	}

	@Override
	public FImage getSaliencyMap() {
		return dofMap;
	}
	
	/**
	 * @return a mask image showing the region of interest.
	 */
	public FImage getROIMap() {
		FImage image = new FImage(dofMap.width, dofMap.height);
		image.drawShapeFilled(calculateROI(), 1f);
		return image;
	}
}
