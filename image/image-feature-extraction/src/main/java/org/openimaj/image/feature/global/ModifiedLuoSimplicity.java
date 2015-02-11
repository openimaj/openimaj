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
import org.openimaj.citation.annotation.References;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.statistics.MaskingHistogramModel;
import org.openimaj.image.processor.connectedcomponent.render.BoundingBoxRenderer;
import org.openimaj.image.saliency.AchantaSaliency;
import org.openimaj.image.saliency.YehSaliency;
import org.openimaj.image.segmentation.FelzenszwalbHuttenlocherSegmenter;
import org.openimaj.math.statistics.distribution.MultidimensionalHistogram;
import org.openimaj.util.array.ArrayUtils;

/**
 * Estimate the simplicity of an image by looking at the colour distribution of
 * the background.
 * <p>
 * Algorithm based on that proposed by Yiwen Luo and Xiaoou Tang, but modified
 * to use the foreground detection approach suggested in Che-Hua Yeh et al.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@References(references = {
		@Reference(
				type = ReferenceType.Inproceedings,
				author = { "Luo, Yiwen", "Tang, Xiaoou" },
				title = "Photo and Video Quality Evaluation: Focusing on the Subject",
				year = "2008",
				booktitle = "Proceedings of the 10th European Conference on Computer Vision: Part III",
				pages = { "386", "399" },
				url = "http://dx.doi.org/10.1007/978-3-540-88690-7_29",
				publisher = "Springer-Verlag",
				series = "ECCV '08",
				customData = { "isbn", "978-3-540-88689-1", "location", "Marseille, France", "numpages", "14", "doi",
						"10.1007/978-3-540-88690-7_29", "acmid", "1478204", "address", "Berlin, Heidelberg" }),
						@Reference(
								type = ReferenceType.Inproceedings,
								author = { "Che-Hua Yeh", "Yuan-Chen Ho", "Brian A. Barsky", "Ming Ouhyoung" },
								title = "Personalized Photograph Ranking and Selection System",
								year = "2010",
								booktitle = "Proceedings of ACM Multimedia",
								pages = { "211", "220" },
								month = "October",
								customData = { "location", "Florence, Italy" }) })
public class ModifiedLuoSimplicity implements ImageAnalyser<MBFImage>, FeatureVectorProvider<DoubleFV> {
	protected YehSaliency extractor;
	protected float alpha = 0.67f;

	protected int binsPerBand = 16;
	protected float gamma = 0.01f;
	protected boolean boxMode = true;
	protected double simplicity;

	/**
	 * Construct with the default values
	 */
	public ModifiedLuoSimplicity() {
		extractor = new YehSaliency();
	}

	/**
	 * Construct with the given values
	 *
	 * @param binsPerBand
	 *            the number of histogram bins per colour band
	 * @param gamma
	 *            the gamma value for determining the threshold
	 * @param boxMode
	 *            whether to extract rectangular boxes for the foreground
	 *            regions (true) or to just use the pixels (false)
	 * @param alpha
	 *            the alpha value for determining the foreground/background
	 *            threshold
	 * @param saliencySigma
	 *            smoothing for the {@link AchantaSaliency} class
	 * @param segmenterSigma
	 *            smoothing for {@link FelzenszwalbHuttenlocherSegmenter}.
	 * @param k
	 *            k value for {@link FelzenszwalbHuttenlocherSegmenter}.
	 * @param minSize
	 *            minimum region size for
	 *            {@link FelzenszwalbHuttenlocherSegmenter}.
	 */
	public ModifiedLuoSimplicity(int binsPerBand, float gamma, boolean boxMode, float alpha, float saliencySigma,
			float segmenterSigma, float k, int minSize)
	{
		extractor = new YehSaliency(saliencySigma, segmenterSigma, k, minSize);
		this.binsPerBand = binsPerBand;
		this.gamma = gamma;
		this.boxMode = boxMode;
		this.alpha = alpha;
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
		image.analyseWith(extractor);

		FImage mask;
		if (boxMode) {
			final TObjectFloatHashMap<ConnectedComponent> componentMap = extractor.getSaliencyComponents();

			final float max = ArrayUtils.maxValue(componentMap.values());

			mask = new FImage(image.getWidth(), image.getHeight());
			final float thresh = max * alpha;
			final BoundingBoxRenderer<Float> renderer = new BoundingBoxRenderer<Float>(mask, 1F, true);

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
		} else {
			mask = extractor.getSaliencyMap();
			final float maskthresh = mask.max() * alpha;
			mask = mask.threshold(maskthresh);
		}

		mask = mask.inverse();

		final MaskingHistogramModel hm = new MaskingHistogramModel(mask, binsPerBand, binsPerBand, binsPerBand);
		hm.estimateModel(image);

		final MultidimensionalHistogram fv = hm.getFeatureVector();
		final double thresh = gamma * fv.max();
		int count = 0;
		for (final double f : fv.values) {
			if (f >= thresh)
				count++;
		}

		simplicity = (double) count / (double) fv.values.length;
	}

	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double[] { simplicity });
	}
}
