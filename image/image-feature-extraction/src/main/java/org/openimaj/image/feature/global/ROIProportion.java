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

import gnu.trove.map.hash.TObjectFloatHashMap;
import gnu.trove.procedure.TObjectFloatProcedure;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processor.connectedcomponent.render.BoundingBoxRenderer;
import org.openimaj.image.saliency.AchantaSaliency;
import org.openimaj.image.saliency.YehSaliency;
import org.openimaj.image.segmentation.FelzenszwalbHuttenlocherSegmenter;
import org.openimaj.util.array.ArrayUtils;

/**
 * Implementation of the region of interest based image simplicity measure
 * described by Yeh et al.
 * <p>
 * Basically returns the proportion of the image that can be considered
 * interesting.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Che-Hua Yeh", "Yuan-Chen Ho", "Brian A. Barsky", "Ming Ouhyoung" },
		title = "Personalized Photograph Ranking and Selection System",
		year = "2010",
		booktitle = "Proceedings of ACM Multimedia",
		pages = { "211", "220" },
		month = "October",
		customData = { "location", "Florence, Italy" })
public class ROIProportion implements ImageAnalyser<MBFImage>, FeatureVectorProvider<DoubleFV> {
	protected YehSaliency saliencyGenerator;
	protected float alpha = 0.67f;

	protected double roiProportion;

	/**
	 * Construct with the default values
	 */
	public ROIProportion() {
		saliencyGenerator = new YehSaliency();
	}

	/**
	 * Construct with the given alpha value, but use the defaults for the
	 * {@link YehSaliency} estimator.
	 *
	 * @param alpha
	 *            the alpha value for determining the threshold
	 */
	public ROIProportion(float alpha) {
		this();
		this.alpha = alpha;
	}

	/**
	 * Construct with the given parameters.
	 *
	 * @param saliencySigma
	 *            smoothing for the {@link AchantaSaliency} class
	 * @param segmenterSigma
	 *            smoothing for {@link FelzenszwalbHuttenlocherSegmenter}.
	 * @param k
	 *            k value for {@link FelzenszwalbHuttenlocherSegmenter}.
	 * @param minSize
	 *            minimum region size for
	 *            {@link FelzenszwalbHuttenlocherSegmenter}.
	 * @param alpha
	 *            the alpha value for determining the threshold
	 */
	public ROIProportion(float saliencySigma, float segmenterSigma, float k, int minSize, float alpha) {
		saliencyGenerator = new YehSaliency(saliencySigma, segmenterSigma, k, minSize);
		this.alpha = alpha;
	}

	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double[] { roiProportion });
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.openimaj.image.analyser.ImageAnalyser#analyseImage(org.openimaj.image
	 * .Image)
	 */
	@Override
	public void analyseImage(MBFImage image) {
		image.analyseWith(saliencyGenerator);
		final TObjectFloatHashMap<ConnectedComponent> componentMap = saliencyGenerator.getSaliencyComponents();

		final float max = ArrayUtils.maxValue(componentMap.values());

		final FImage map = new FImage(image.getWidth(), image.getHeight());
		final float thresh = max * alpha;
		final BoundingBoxRenderer<Float> renderer = new BoundingBoxRenderer<Float>(map, 1F, true);

		componentMap.forEachEntry(new TObjectFloatProcedure<ConnectedComponent>() {
			@Override
			public boolean execute(ConnectedComponent cc, float sal) {
				if (sal >= thresh) { // note that this is reversed from the
					// paper, which doesn't seem to make
					// sense.
					renderer.process(cc);
				}

				return true;
			}
		});

		roiProportion = 0;
		for (int y = 0; y < map.height; y++)
			for (int x = 0; x < map.width; x++)
				roiProportion += map.pixels[y][x];

		roiProportion /= (map.width * map.height); // smaller simplicity means
		// smaller ROI
	}
}
