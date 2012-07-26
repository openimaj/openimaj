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
package org.openimaj.tools.localfeature;

import java.io.IOException;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.local.affine.AffineSimulationKeypoint;
import org.openimaj.image.feature.local.affine.BasicASIFT;
import org.openimaj.image.feature.local.affine.ColourASIFT;
import org.openimaj.image.feature.local.engine.DoGColourSIFTEngine;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.engine.MinMaxDoGSIFTEngine;
import org.openimaj.image.feature.local.engine.asift.ASIFTEngine;
import org.openimaj.image.feature.local.engine.asift.ColourASIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.MinMaxKeypoint;
import org.openimaj.tools.localfeature.ColourMode.ColourModeOp;
import org.openimaj.tools.localfeature.ImageTransform.ImageTransformOp;

/**
 * Types of local feature
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public enum LocalFeatureMode implements CmdLineOptionsProvider {
	/**
	 * Difference-of-Gaussian SIFT
	 */
	SIFT {
		@Override
		public LocalFeatureModeOp getOptions() {
			return new SiftMode();
		}
	},
	/**
	 * Min/Max Difference-of-Gaussian SIFT
	 */
	MIN_MAX_SIFT {
		@Override
		public LocalFeatureModeOp getOptions() {
			return new MinMaxSiftMode();
		}
	},
	/**
	 * Affine simulated Difference-of-Gaussian SIFT (ASIFT). Outputs
	 * x, y, scale, ori + feature
	 */
	ASIFT {
		@Override
		public LocalFeatureModeOp getOptions() {
			return new AsiftMode();
		}
	},
	/**
	 * Enhanced output affine simulated Difference-of-Gaussian SIFT (ASIFT).
	 * Outputs x, y, scale, ori , tilt, theta, simulation index
	 */
	ASIFTENRICHED {
		@Override
		public LocalFeatureModeOp getOptions() {
			return new AsiftEnrichedMode();
		}
	}
	;

	@Override
	public abstract LocalFeatureModeOp getOptions();

	/**
	 * Associated options for each {@link LocalFeatureMode}.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static abstract class LocalFeatureModeOp {
		@Option(name="--colour-mode", aliases="-cm", required=false, usage="Optionally perform sift using the colour of the image in some mode", handler=ProxyOptionHandler.class)
		protected ColourMode cm = ColourMode.INTENSITY;
		protected ColourModeOp cmOp = (ColourModeOp) ColourMode.INTENSITY.getOptions();

		@Option(name="--image-transform", aliases="-it", required=false, usage="Optionally perform a image transform before keypoint calculation", handler=ProxyOptionHandler.class)
		protected ImageTransform it = ImageTransform.NOTHING;
		protected ImageTransformOp itOp = (ImageTransformOp) ImageTransform.NOTHING.getOptions();

		@Option(name="--no-double-size", aliases="-nds", required=false, usage="Double the image sizes for the first iteration")
		protected boolean noDoubleImageSize = false;

		/**
		 * Extract features based on the options.
		 * @param image the image
		 * @return the features
		 * @throws IOException
		 */
		public abstract LocalFeatureList<? extends LocalFeature<?>> extract(byte[] image) throws IOException ;
	}

	private static class SiftMode extends LocalFeatureModeOp {
		@Override
		public LocalFeatureList<Keypoint> extract(byte[] img) throws IOException {
			LocalFeatureList<Keypoint> keys  = null;
			switch(this.cm){
			case SINGLE_COLOUR:
			case INTENSITY:
			{
				DoGSIFTEngine engine = new DoGSIFTEngine();
				engine.getOptions().setDoubleInitialImage(!noDoubleImageSize);
				Image<?,?> image = cmOp.process(img);
				image = itOp.transform(image);

				keys = engine.findFeatures((FImage)image);
				break;
			}
			case INTENSITY_COLOUR:
			{
				DoGColourSIFTEngine engine = new DoGColourSIFTEngine();
				engine.getOptions().setDoubleInitialImage(!noDoubleImageSize);
				MBFImage image = (MBFImage) cmOp.process(img);
				image = (MBFImage) itOp.transform(image);

				keys = engine.findFeatures(image);
				break;
			}
			}
			return keys;
		}
	}

	private static class MinMaxSiftMode extends LocalFeatureModeOp {
		@Override
		public LocalFeatureList<? extends Keypoint> extract(byte[] img) throws IOException {
			MinMaxDoGSIFTEngine engine = new MinMaxDoGSIFTEngine();
			LocalFeatureList<MinMaxKeypoint> keys  = null;
			switch(this.cm) {
			case SINGLE_COLOUR:
			case INTENSITY:
				keys = engine.findFeatures((FImage) cmOp.process(img));
				break;
			case INTENSITY_COLOUR:
				throw new UnsupportedOperationException();
			}
			return keys;
		}
	}

	private static class AsiftMode extends LocalFeatureModeOp {
		@Option(name="--n-tilts", aliases="-nt", required=false, usage="The number of tilts for the affine simulation")
		public int ntilts = 5;

		@Override
		public LocalFeatureList<Keypoint> extract(byte[] image) throws IOException {

			LocalFeatureList<Keypoint> keys  = null;

			switch(this.cm) {
			case SINGLE_COLOUR:
			case INTENSITY:
				BasicASIFT basic = new BasicASIFT(!noDoubleImageSize);
				basic.process((FImage) itOp.transform(cmOp.process(image)),ntilts);
				keys = basic.getKeypoints();
				break;
			case INTENSITY_COLOUR:
				ColourASIFT colour = new ColourASIFT(!noDoubleImageSize);
				colour.process((MBFImage)itOp.transform(cmOp.process(image)), ntilts);
			}
			return keys;
		}
	}

	private static class AsiftEnrichedMode extends LocalFeatureModeOp {
		@Option(name="--n-tilts", aliases="-nt", required=false, usage="The number of tilts for the affine simulation")
		public int ntilts = 5;

		@Override
		public LocalFeatureList<AffineSimulationKeypoint> extract(byte[] image) throws IOException {
			ASIFTEngine engine = new ASIFTEngine(!noDoubleImageSize ,ntilts);
			LocalFeatureList<AffineSimulationKeypoint> keys  = null;
			switch(this.cm){
			case SINGLE_COLOUR:
			case INTENSITY:
				FImage img = (FImage) cmOp.process(image);
				img = (FImage) itOp.transform(img);
				keys = engine.findSimulationKeypoints(img);
				break;
			case INTENSITY_COLOUR:
				ColourASIFTEngine colourengine = new ColourASIFTEngine(!noDoubleImageSize ,ntilts);
				MBFImage colourimg = (MBFImage) cmOp.process(image);
				colourimg = (MBFImage) itOp.transform(colourimg);
				keys = colourengine.findSimulationKeypoints(colourimg);
			}
			return keys;
		}
	}
}
