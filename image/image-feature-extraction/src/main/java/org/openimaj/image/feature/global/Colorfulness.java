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

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.EnumFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.analyser.PixelAnalyser;

/**
 * Implementation of Hasler and Susstruck's Colorfulness metric
 * http://infoscience.epfl.ch/record/33994/files/HaslerS03.pdf?version=1
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Hasler, David", "S\\\"{u}sstrunk, Sabine" },
		title = "Measuring {C}olourfulness in {N}atural {I}mages",
		year = "2003",
		booktitle = "Proc. {IS}&{T}/{SPIE} {E}lectronic {I}maging 2003: {H}uman {V}ision and {E}lectronic {I}maging {VIII}",
		pages = { "87", "", "95" },
		volume = "5007",
		url = "http://infoscience.epfl.ch/record/33994/files/HaslerS03.pdf?version=1",
		customData = {
				"details", "http://infoscience.epfl.ch/record/33994",
				"documenturl", "http://infoscience.epfl.ch/record/33994/files/HaslerS03.pdf",
				"doi", "10.1117/12.477378",
				"keywords", "IVRG; colorfulness; image quality; image attributes; colourfulness",
				"location", "San Jose, CA",
				"oai-id", "oai:infoscience.epfl.ch:33994",
				"oai-set", "conf; fulltext; fulltext-public",
				"review", "NON-REVIEWED",
				"status", "PUBLISHED",
				"unit", "LCAV IVRG"
		})
public class Colorfulness implements PixelAnalyser<Float[]>, FeatureVectorProvider<DoubleFV> {
	SummaryStatistics rgStats = new SummaryStatistics();
	SummaryStatistics ybStats = new SummaryStatistics();

	@Override
	public void analysePixel(Float[] pixel) {
		final float r = pixel[0];
		final float g = pixel[1];
		final float b = pixel[2];

		final float rg = r - g;
		final float yb = 0.5f * (r + g) - b;

		rgStats.addValue(rg);
		ybStats.addValue(yb);
	}

	/**
	 * Classes of colourfulness
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	@Reference(
			type = ReferenceType.Inproceedings,
			author = { "Hasler, David", "S\\\"{u}sstrunk, Sabine" },
			title = "Measuring {C}olourfulness in {N}atural {I}mages",
			year = "2003",
			booktitle = "Proc. {IS}&{T}/{SPIE} {E}lectronic {I}maging 2003: {H}uman {V}ision and {E}lectronic {I}maging {VIII}",
			pages = { "87", "", "95" },
			volume = "5007",
			url = "http://infoscience.epfl.ch/record/33994/files/HaslerS03.pdf?version=1",
			customData = {
					"details", "http://infoscience.epfl.ch/record/33994",
					"documenturl", "http://infoscience.epfl.ch/record/33994/files/HaslerS03.pdf",
					"doi", "10.1117/12.477378",
					"keywords", "IVRG; colorfulness; image quality; image attributes; colourfulness",
					"location", "San Jose, CA",
					"oai-id", "oai:infoscience.epfl.ch:33994",
					"oai-set", "conf; fulltext; fulltext-public",
					"review", "NON-REVIEWED",
					"status", "PUBLISHED",
					"unit", "LCAV IVRG"
			})
	public enum ColorfulnessAttr implements FeatureVectorProvider<EnumFV<ColorfulnessAttr>> {
		/**
		 * Not colourful
		 */
		NOT(0.0),
		/**
		 * Slightly colourful
		 */
		SLIGHTLY(15.0 / 255.0),
		/**
		 * Moderately colourful
		 */
		MODERATELY(33.0 / 255.0),
		/**
		 * Averagely colourful
		 */
		AVERAGELY(45.0 / 255.0),
		/**
		 * Quite colourful
		 */
		QUITE(59.0 / 255.0),
		/**
		 * Highly colourful
		 */
		HIGHLY(82.0 / 255.0),
		/**
		 * Extremely colourful
		 */
		EXTREMELY(109.0 / 255.0);

		private double threshold;

		private ColorfulnessAttr(double val) {
			this.threshold = val;
		}

		/**
		 * Get the colourfulness class for a given colourfulness value.
		 * 
		 * @param val
		 *            the colourfulness value
		 * @return the colourfulness class
		 */
		public static ColorfulnessAttr getAttr(double val) {
			final ColorfulnessAttr[] attrs = values();
			for (int i = attrs.length - 1; i >= 0; i--) {
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

	/**
	 * @return the colourfulness class from the last image analysed
	 */
	public ColorfulnessAttr getColorfulnessAttribute() {
		return ColorfulnessAttr.getAttr(getColorfulness());
	}

	/**
	 * @return the colourfulness value from the last image analysed
	 */
	public double getColorfulness() {
		final double var_rg = rgStats.getVariance();
		final double var_yb = ybStats.getVariance();
		final double mean_rg = rgStats.getMean();
		final double mean_yb = ybStats.getMean();

		final double stddev = Math.sqrt(var_rg + var_yb);
		final double mean = Math.sqrt(mean_rg * mean_rg + mean_yb * mean_yb);

		return stddev + 0.3 * mean;
	}

	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double[] { getColorfulness() });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openimaj.image.analyser.PixelAnalyser#reset()
	 */
	@Override
	public void reset() {
		rgStats.clear();
		ybStats.clear();
	}
}
