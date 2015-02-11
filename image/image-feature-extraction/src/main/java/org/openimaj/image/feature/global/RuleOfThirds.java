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
import org.openimaj.image.MBFImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.PixelSet;
import org.openimaj.image.saliency.AchantaSaliency;
import org.openimaj.image.saliency.YehSaliency;
import org.openimaj.image.segmentation.FelzenszwalbHuttenlocherSegmenter;
import org.openimaj.math.geometry.point.Point2dImpl;

/**
 * Implementation of the rule-of-thirds algorithm described by Yeh et al.
 * <p>
 * I've assumed that the distances to the power-points should be normalized with
 * respect to the image size - this isn't explicit in the paper, but given that
 * the sigma of the gaussian is fixed, it seems likely...
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
public class RuleOfThirds implements ImageAnalyser<MBFImage>, FeatureVectorProvider<DoubleFV> {
	private static final double SIGMA = 0.17;
	private static final Point2dImpl[] powerPoints = getPowerPoints();

	YehSaliency saliencyGenerator;
	private double asSum;
	private double aseSum;

	/**
	 * Construct a new {@link RuleOfThirds} with the default settings for the
	 * {@link YehSaliency} algorithm.
	 */
	public RuleOfThirds() {
		saliencyGenerator = new YehSaliency();
	}

	/**
	 * Construct a new {@link RuleOfThirds} with the given values for the
	 * {@link YehSaliency} algorithm.
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
	 */
	public RuleOfThirds(float saliencySigma, float segmenterSigma, float k, int minSize) {
		saliencyGenerator = new YehSaliency(saliencySigma, segmenterSigma, k, minSize);
	}

	@Override
	public DoubleFV getFeatureVector() {
		if (asSum == 0)
			new DoubleFV(new double[] { 0 });
		return new DoubleFV(new double[] { aseSum / asSum });
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

		image.analyseWith(saliencyGenerator);
		final TObjectFloatHashMap<ConnectedComponent> componentMap = saliencyGenerator.getSaliencyComponents();

		asSum = 0;
		aseSum = 0;
		componentMap.forEachEntry(new TObjectFloatProcedure<ConnectedComponent>() {
			@Override
			public boolean execute(ConnectedComponent c, float s) {
				final double as = c.calculateArea() * s;

				final double D = closestDistance(c, width, height);

				asSum += as;
				aseSum += as * Math.exp(-(D * D) / (2 * SIGMA));

				return true;
			}
		});
	}

	private double closestDistance(PixelSet cc, int width, int height) {
		final double centroid[] = cc.calculateCentroid();
		double minDistance = Double.MAX_VALUE;

		for (final Point2dImpl pt : powerPoints) {
			final double dx = (centroid[0] / width) - pt.x;
			final double dy = (centroid[1] / width) - pt.y;
			final double d = dx * dx + dy * dy;

			if (d < minDistance)
				minDistance = d;
		}

		return Math.sqrt(minDistance);
	}

	private static Point2dImpl[] getPowerPoints() {
		return new Point2dImpl[] {
				new Point2dImpl(1 / 3f, 1 / 3f),
				new Point2dImpl(2 / 3f, 1 / 3f),
				new Point2dImpl(1 / 3f, 2 / 3f),
				new Point2dImpl(2 / 3f, 2 / 3f) };
	}
}
