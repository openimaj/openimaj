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
package org.openimaj.tools.globalfeature;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.openimaj.image.analysis.algorithm.EdgeDirectionCoherenceVector;
import org.openimaj.image.feature.global.AvgBrightness;
import org.openimaj.image.feature.global.Colorfulness;
import org.openimaj.image.feature.global.ColourContrast;
import org.openimaj.image.feature.global.HorizontalIntensityDistribution;
import org.openimaj.image.feature.global.HueStats;
import org.openimaj.image.feature.global.LRIntensityBalance;
import org.openimaj.image.feature.global.LuoSimplicity;
import org.openimaj.image.feature.global.ModifiedLuoSimplicity;
import org.openimaj.image.feature.global.Naturalness;
import org.openimaj.image.feature.global.ROIProportion;
import org.openimaj.image.feature.global.RuleOfThirds;
import org.openimaj.image.feature.global.SharpPixelProportion;
import org.openimaj.image.feature.global.Sharpness;
import org.openimaj.image.feature.global.WeberContrast;
import org.openimaj.image.pixel.statistics.BlockHistogramModel;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.SandeepFaceDetector;
import org.openimaj.tools.globalfeature.type.AverageBrightnessExtractor;
import org.openimaj.tools.globalfeature.type.ColourContrastExtractor;
import org.openimaj.tools.globalfeature.type.ColourFacesExtractor;
import org.openimaj.tools.globalfeature.type.ColourfulnessExtractor;
import org.openimaj.tools.globalfeature.type.EDCHExtractor;
import org.openimaj.tools.globalfeature.type.HaarFacesExtractor;
import org.openimaj.tools.globalfeature.type.HistogramExtractor;
import org.openimaj.tools.globalfeature.type.HorizontalIntensityDistributionExtractor;
import org.openimaj.tools.globalfeature.type.HueStatsExtractor;
import org.openimaj.tools.globalfeature.type.LocalHistogramExtractor;
import org.openimaj.tools.globalfeature.type.LrIntensityBalanceExtractor;
import org.openimaj.tools.globalfeature.type.LuoSimplicityExtractor;
import org.openimaj.tools.globalfeature.type.MaxHistogramExtractor;
import org.openimaj.tools.globalfeature.type.ModifiedLuoSimplicityExtractor;
import org.openimaj.tools.globalfeature.type.NaturalnessExtractor;
import org.openimaj.tools.globalfeature.type.RoiProportionExtractor;
import org.openimaj.tools.globalfeature.type.RuleOfThirdsExtractor;
import org.openimaj.tools.globalfeature.type.SharpPixelProportionExtractor;
import org.openimaj.tools.globalfeature.type.SharpnessExtractor;
import org.openimaj.tools.globalfeature.type.WeberContrastExtractor;

/**
 * Different types of global image feature.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public enum GlobalFeatureType implements CmdLineOptionsProvider {
	/**
	 * Pixel histograms
	 *
	 * @see HistogramModel
	 */
	HISTOGRAM {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new HistogramExtractor();
		}
	},
	/**
	 * Using a pixel histogram (see {@link GlobalFeatureType#HISTOGRAM}) find
	 * the maximum bin. This can be interpreted as the image's dominant colour
	 */
	MAX_HISTOGRAM {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new MaxHistogramExtractor();
		}
	},
	/**
	 * Local (block-based) pixel histograms
	 *
	 * @see BlockHistogramModel
	 */
	LOCAL_HISTOGRAM {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new LocalHistogramExtractor();
		}
	},
	/**
	 * EDCH
	 *
	 * @see EdgeDirectionCoherenceVector
	 */
	EDGE_DIRECTION_COHERENCE_HISTOGRAM {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new EDCHExtractor();
		}
	},
	/**
	 * Average brightness
	 *
	 * @see AvgBrightness
	 */
	AVERAGE_BRIGHTNESS {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new AverageBrightnessExtractor();
		}
	},
	/**
	 * Sharpness
	 *
	 * @see Sharpness
	 */
	SHARPNESS {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new SharpnessExtractor();
		}
	},
	/**
	 * Colorfulness
	 *
	 * @see Colorfulness
	 */
	COLORFULNESS {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new ColourfulnessExtractor();
		}
	},
	/**
	 * Hue stats
	 *
	 * @see HueStats
	 */
	HUE_STATISTICS {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new HueStatsExtractor();
		}
	},
	/**
	 * Naturalness
	 *
	 * @see Naturalness
	 */
	NATURALNESS {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new NaturalnessExtractor();
		}
	},
	/**
	 * Sandeep faces
	 *
	 * @see SandeepFaceDetector
	 */
	COLOR_FACES {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new ColourFacesExtractor();
		}
	},
	/**
	 * Haar cascades
	 *
	 * @see HaarCascadeDetector
	 */
	HAAR_FACES {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new HaarFacesExtractor();
		}
	},
	/**
	 * Colour contrast
	 *
	 * @see ColourContrast
	 */
	COLOUR_CONTRAST {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new ColourContrastExtractor();
		}
	},
	/**
	 * Weber constrast
	 *
	 * @see WeberContrast
	 */
	WEBER_CONTRAST {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new WeberContrastExtractor();
		}
	},
	/**
	 * Left-right intensity balance
	 *
	 * @see LRIntensityBalance
	 */
	LR_INTENSITY_BALANCE {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new LrIntensityBalanceExtractor();
		}
	},
	/**
	 * Rule of thirds feature
	 *
	 * @see RuleOfThirds
	 */
	RULE_OF_THIRDS {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new RuleOfThirdsExtractor();
		}
	},
	/**
	 * ROI proportion
	 *
	 * @see ROIProportion
	 */
	ROI_PROPORTION {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new RoiProportionExtractor();
		}
	},
	/**
	 * Horizontal intensity distribution
	 *
	 * @see HorizontalIntensityDistribution
	 */
	HORIZONTAL_INTENSITY_DISTRIBUTION {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new HorizontalIntensityDistributionExtractor();
		}
	},
	/**
	 * Sharp pixel proportion
	 *
	 * @see SharpPixelProportion
	 */
	SHARP_PIXEL_PROPORTION {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new SharpPixelProportionExtractor();
		}
	},
	/**
	 * Luo's simplicity feature
	 *
	 * @see LuoSimplicity
	 */
	LUO_SIMPLICITY {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new LuoSimplicityExtractor();
		}
	},
	/**
	 * Modified version of Luo's simplicity feature
	 *
	 * @see ModifiedLuoSimplicity
	 */
	MODIFIED_LUO_SIMPLICITY {
		@Override
		public GlobalFeatureExtractor getOptions() {
			return new ModifiedLuoSimplicityExtractor();
		}
	};

	@Override
	public abstract GlobalFeatureExtractor getOptions();
}
