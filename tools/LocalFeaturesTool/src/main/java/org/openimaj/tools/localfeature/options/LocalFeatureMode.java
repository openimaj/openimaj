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
package org.openimaj.tools.localfeature.options;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.LocalFeatureExtractor;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.dense.gradient.dsift.ApproximateDenseSIFT;
import org.openimaj.image.feature.dense.gradient.dsift.ByteDSIFTKeypoint;
import org.openimaj.image.feature.dense.gradient.dsift.ColourDenseSIFT;
import org.openimaj.image.feature.dense.gradient.dsift.DenseSIFT;
import org.openimaj.image.feature.dense.gradient.dsift.FloatDSIFTKeypoint;
import org.openimaj.image.feature.dense.gradient.dsift.PyramidDenseSIFT;
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
import org.openimaj.tools.localfeature.options.ColourMode.ColourModeOp;
import org.openimaj.tools.localfeature.options.ImageTransform.ImageTransformOp;

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
		public AbstractDoGSIFTModeOp getOptions() {
			return new SiftMode(SIFT);
		}
	},
	/**
	 * Min/Max Difference-of-Gaussian SIFT
	 */
	MIN_MAX_SIFT {
		@Override
		public LocalFeatureModeOp getOptions() {
			return new MinMaxSiftMode(MIN_MAX_SIFT);
		}
	},
	/**
	 * Affine simulated Difference-of-Gaussian SIFT (ASIFT). Outputs x, y,
	 * scale, ori + feature
	 */
	ASIFT {
		@Override
		public LocalFeatureModeOp getOptions() {
			return new AsiftMode(ASIFT);
		}
	},
	/**
	 * Enhanced output affine simulated Difference-of-Gaussian SIFT (ASIFT).
	 * Outputs x, y, scale, ori , tilt, theta, simulation index
	 */
	ASIFTENRICHED {
		@Override
		public LocalFeatureModeOp getOptions() {
			return new AsiftEnrichedMode(ASIFTENRICHED);
		}
	},
	/**
	 * Dense SIFT
	 */
	DENSE_SIFT {
		@Override
		public LocalFeatureModeOp getOptions() {
			return new DenseSiftMode(DENSE_SIFT);
		}
	},
	/**
	 * Colour Dense SIFT
	 */
	COLOUR_DENSE_SIFT {
		@Override
		public LocalFeatureModeOp getOptions() {
			return new ColourDenseSiftMode(COLOUR_DENSE_SIFT);
		}
	},
	/**
	 * Dense SIFT in a pyramid
	 */
	PYRAMID_DENSE_SIFT {
		@Override
		public LocalFeatureModeOp getOptions() {
			return new PyramidDenseSiftMode(DENSE_SIFT);
		}
	},
	/**
	 * Dense colour SIFT in a pyramid
	 */
	PYRAMID_COLOUR_DENSE_SIFT {
		@Override
		public LocalFeatureModeOp getOptions() {
			return new PyramidColourDenseSiftMode(COLOUR_DENSE_SIFT);
		}
	};

	@Override
	public abstract LocalFeatureModeOp getOptions();

	/**
	 * Associated options for each {@link LocalFeatureMode}.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static abstract class LocalFeatureModeOp
			implements
			LocalFeatureExtractor<LocalFeature<?, ?>, MBFImage>
	{
		private LocalFeatureMode mode;

		@Option(
				name = "--image-transform",
				aliases = "-it",
				required = false,
				usage = "Optionally perform a image transform before keypoint calculation",
				handler = ProxyOptionHandler.class)
		protected ImageTransform it = ImageTransform.NOTHING;
		protected ImageTransformOp itOp = ImageTransform.NOTHING.getOptions();

		/**
		 * Extract features based on the options.
		 * 
		 * @param image
		 *            the image
		 * @return the features
		 * @throws IOException
		 */
		public abstract LocalFeatureList<? extends LocalFeature<?, ?>> extract(byte[] image) throws IOException;

		private LocalFeatureModeOp(LocalFeatureMode mode) {
			this.mode = mode;
		}

		/**
		 * @return the name of the mode
		 */
		public String name() {
			return mode.name();
		}

		/**
		 * @return the mode
		 */
		public LocalFeatureMode getMode() {
			return mode;
		}
	}

	/**
	 * Associated options for things built on a {@link DoGSIFTEngine}.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static abstract class AbstractDoGSIFTModeOp extends LocalFeatureModeOp {
		@Option(
				name = "--colour-mode",
				aliases = "-cm",
				required = false,
				usage = "Optionally perform sift using the colour of the image in some mode",
				handler = ProxyOptionHandler.class)
		protected ColourMode cm = ColourMode.INTENSITY;
		protected ColourModeOp cmOp = (ColourModeOp) ColourMode.INTENSITY.getOptions();

		@Option(
				name = "--no-double-size",
				aliases = "-nds",
				required = false,
				usage = "Double the image sizes for the first iteration")
		protected boolean noDoubleImageSize = false;

		protected AbstractDoGSIFTModeOp(LocalFeatureMode mode) {
			super(mode);
		}
	}

	private static class SiftMode extends AbstractDoGSIFTModeOp {
		private SiftMode(LocalFeatureMode mode) {
			super(mode);
		}

		@Override
		public LocalFeatureList<Keypoint> extract(byte[] img) throws IOException {
			return extract(cmOp.process(img));
		}

		@Override
		public Class<? extends LocalFeature<?, ?>> getFeatureClass() {
			return Keypoint.class;
		}

		@Override
		public LocalFeatureList<Keypoint> extractFeature(MBFImage img) {
			return extract(cmOp.process(img));
		}

		private LocalFeatureList<Keypoint> extract(Image<?, ?> image) {
			LocalFeatureList<Keypoint> keys = null;
			switch (this.cm) {
			case SINGLE_COLOUR:
			case INTENSITY: {
				final DoGSIFTEngine engine = new DoGSIFTEngine();
				engine.getOptions().setDoubleInitialImage(!noDoubleImageSize);
				image = itOp.transform(image);

				keys = engine.findFeatures((FImage) image);
				break;
			}
			case INTENSITY_COLOUR: {
				final DoGColourSIFTEngine engine = new DoGColourSIFTEngine();
				engine.getOptions().setDoubleInitialImage(!noDoubleImageSize);
				image = itOp.transform(image);

				keys = engine.findFeatures((MBFImage) image);
				break;
			}
			}
			return keys;
		}
	}

	private static class MinMaxSiftMode extends AbstractDoGSIFTModeOp {
		private MinMaxSiftMode(LocalFeatureMode mode) {
			super(mode);
		}

		@Override
		public LocalFeatureList<? extends Keypoint> extract(byte[] img) throws IOException {
			final MinMaxDoGSIFTEngine engine = new MinMaxDoGSIFTEngine();
			LocalFeatureList<MinMaxKeypoint> keys = null;
			switch (this.cm) {
			case SINGLE_COLOUR:
			case INTENSITY:
				keys = engine.findFeatures((FImage) cmOp.process(img));
				break;
			case INTENSITY_COLOUR:
				throw new UnsupportedOperationException();
			}
			return keys;
		}

		@Override
		public LocalFeatureList<? extends Keypoint> extractFeature(MBFImage img) {
			final MinMaxDoGSIFTEngine engine = new MinMaxDoGSIFTEngine();
			LocalFeatureList<MinMaxKeypoint> keys = null;
			switch (this.cm) {
			case SINGLE_COLOUR:
			case INTENSITY:
				keys = engine.findFeatures((FImage) cmOp.process(img));
				break;
			case INTENSITY_COLOUR:
				throw new UnsupportedOperationException();
			}
			return keys;
		}

		@Override
		public Class<? extends LocalFeature<?, ?>> getFeatureClass() {
			return MinMaxKeypoint.class;
		}
	}

	private static class AsiftMode extends AbstractDoGSIFTModeOp {
		private AsiftMode(LocalFeatureMode mode) {
			super(mode);
		}

		@Option(
				name = "--n-tilts",
				required = false,
				usage = "The number of tilts for the affine simulation")
		public int ntilts = 5;

		@Override
		public LocalFeatureList<Keypoint> extract(byte[] image) throws IOException {
			LocalFeatureList<Keypoint> keys = null;

			switch (this.cm) {
			case SINGLE_COLOUR:
			case INTENSITY:
				final BasicASIFT basic = new BasicASIFT(!noDoubleImageSize);
				basic.detectFeatures((FImage) itOp.transform(cmOp.process(image)), ntilts);
				keys = basic.getFeatures();
				break;
			case INTENSITY_COLOUR:
				final ColourASIFT colour = new ColourASIFT(!noDoubleImageSize);
				colour.detectFeatures((MBFImage) itOp.transform(cmOp.process(image)), ntilts);
			}
			return keys;
		}

		@Override
		public LocalFeatureList<Keypoint> extractFeature(MBFImage image) {
			LocalFeatureList<Keypoint> keys = null;

			switch (this.cm) {
			case SINGLE_COLOUR:
			case INTENSITY:
				final BasicASIFT basic = new BasicASIFT(!noDoubleImageSize);
				basic.detectFeatures((FImage) itOp.transform(cmOp.process(image)), ntilts);
				keys = basic.getFeatures();
				break;
			case INTENSITY_COLOUR:
				final ColourASIFT colour = new ColourASIFT(!noDoubleImageSize);
				colour.detectFeatures((MBFImage) itOp.transform(cmOp.process(image)), ntilts);
			}
			return keys;
		}

		@Override
		public Class<? extends LocalFeature<?, ?>> getFeatureClass() {
			return Keypoint.class;
		}
	}

	private static class AsiftEnrichedMode extends AbstractDoGSIFTModeOp {
		private AsiftEnrichedMode(LocalFeatureMode mode) {
			super(mode);
		}

		@Option(
				name = "--n-tilts",
				required = false,
				usage = "The number of tilts for the affine simulation")
		public int ntilts = 5;

		@Override
		public LocalFeatureList<AffineSimulationKeypoint> extract(byte[] image) throws IOException {
			final ASIFTEngine engine = new ASIFTEngine(!noDoubleImageSize, ntilts);
			LocalFeatureList<AffineSimulationKeypoint> keys = null;
			switch (this.cm) {
			case SINGLE_COLOUR:
			case INTENSITY:
				FImage img = (FImage) cmOp.process(image);
				img = (FImage) itOp.transform(img);
				keys = engine.findFeatures(img);
				break;
			case INTENSITY_COLOUR:
				final ColourASIFTEngine colourengine = new ColourASIFTEngine(!noDoubleImageSize, ntilts);
				MBFImage colourimg = (MBFImage) cmOp.process(image);
				colourimg = (MBFImage) itOp.transform(colourimg);
				keys = colourengine.findFeatures(colourimg);
			}
			return keys;
		}

		@Override
		public LocalFeatureList<AffineSimulationKeypoint> extractFeature(MBFImage image) {
			final ASIFTEngine engine = new ASIFTEngine(!noDoubleImageSize, ntilts);
			LocalFeatureList<AffineSimulationKeypoint> keys = null;
			switch (this.cm) {
			case SINGLE_COLOUR:
			case INTENSITY:
				FImage img = (FImage) cmOp.process(image);
				img = (FImage) itOp.transform(img);
				keys = engine.findFeatures(img);
				break;
			case INTENSITY_COLOUR:
				final ColourASIFTEngine colourengine = new ColourASIFTEngine(!noDoubleImageSize, ntilts);
				MBFImage colourimg = (MBFImage) cmOp.process(image);
				colourimg = (MBFImage) itOp.transform(colourimg);
				keys = colourengine.findFeatures(colourimg);
			}
			return keys;
		}

		@Override
		public Class<? extends LocalFeature<?, ?>> getFeatureClass() {
			return AffineSimulationKeypoint.class;
		}
	}

	private static abstract class AbstractDenseSiftMode extends LocalFeatureModeOp {
		@Option(
				name = "--approximate",
				aliases = "-ap",
				required = false,
				usage = "Enable approximate mode (much faster)")
		boolean approximate;

		@Option(
				name = "--step-x",
				aliases = "-sx",
				required = false,
				usage = "Step size of sampling window in x-direction (in pixels)")
		protected int stepX = 5;

		@Option(
				name = "--step-y",
				aliases = "-sy",
				required = false,
				usage = "Step size of sampling window in y-direction (in pixels)")
		protected int stepY = 5;

		@Option(
				name = "--num-bins-x",
				aliases = "-nx",
				required = false,
				usage = "Number of spatial bins in the X direction")
		protected int numBinsX = 4;

		@Option(
				name = "--num-bins-y",
				aliases = "-ny",
				required = false,
				usage = "Number of spatial bins in the Y direction")
		protected int numBinsY = 4;

		@Option(name = "--num-ori-bins", aliases = "-no", required = false, usage = "The number of orientation bins")
		protected int numOriBins = 8;

		@Option(
				name = "--gaussian-window-size",
				aliases = "-gws",
				required = false,
				usage = "Size of the Gaussian window (in relative to of the size of a bin)")
		protected float gaussianWindowSize = 2f;

		@Option(name = "--clipping-threshold", required = false, usage = "Threshold for clipping the SIFT features")
		protected float valueThreshold = 0.2f;

		@Option(
				name = "--contrast-threshold",
				required = false,
				usage = "Threshold on the contrast of the returned features (-ve values disable this)")
		protected float contrastThreshold = -1;

		@Option(
				name = "--byte-features",
				required = false,
				usage = "Output features scaled to bytes rather than floats")
		protected boolean byteFeatures = false;

		private AbstractDenseSiftMode(LocalFeatureMode mode) {
			super(mode);
		}
	}

	private static class DenseSiftMode extends AbstractDenseSiftMode {
		@Option(
				name = "--bin-width",
				aliases = "-bw",
				required = false,
				usage = "Width of a single bin of the sampling window (in pixels). Sampling window width is this multiplied by #numBinX.")
		protected int binWidth = 5;

		@Option(
				name = "--bin-height",
				aliases = "-bh",
				required = false,
				usage = "Height of a single bin of the sampling window (in pixels). Sampling window height is this multiplied by #numBinY.")
		protected int binHeight = 5;

		private DenseSiftMode(LocalFeatureMode mode) {
			super(mode);
		}

		@Override
		public LocalFeatureList<? extends LocalFeature<?, ?>> extract(byte[] image) throws IOException {
			return extract(ImageUtilities.readF(new ByteArrayInputStream(image)));
		}

		@Override
		public LocalFeatureList<? extends LocalFeature<?, ?>> extractFeature(MBFImage image) {
			return extract(Transforms.calculateIntensityNTSC_LUT(image));
		}

		LocalFeatureList<? extends LocalFeature<?, ?>> extract(FImage image) {
			final DenseSIFT dsift;

			if (approximate)
				dsift = new ApproximateDenseSIFT(stepX, stepY, binWidth, binHeight, numBinsX, numBinsY, numOriBins,
						gaussianWindowSize, valueThreshold);
			else
				dsift = new DenseSIFT(stepX, stepY, binWidth, binHeight, numBinsX, numBinsY, numOriBins,
						gaussianWindowSize, valueThreshold);

			dsift.analyseImage(image);

			if (contrastThreshold <= 0) {
				if (byteFeatures)
					return dsift.getByteKeypoints();
				return dsift.getFloatKeypoints();
			} else {
				if (byteFeatures)
					return dsift.getByteKeypoints(contrastThreshold);
				return dsift.getFloatKeypoints(contrastThreshold);
			}
		}

		@Override
		public Class<? extends LocalFeature<?, ?>> getFeatureClass() {
			if (byteFeatures)
				return ByteDSIFTKeypoint.class;
			return FloatDSIFTKeypoint.class;
		}
	}

	private static class ColourDenseSiftMode extends DenseSiftMode {
		@Option(name = "--colour-space", aliases = "-cs", required = false, usage = "Specify the colour space")
		private ColourSpace colourspace = ColourSpace.RGB;

		ColourDenseSiftMode(LocalFeatureMode mode) {
			super(mode);
		}

		@Override
		public LocalFeatureList<? extends LocalFeature<?, ?>> extract(byte[] image) throws IOException {
			return extractFeature(ImageUtilities.readMBF(new ByteArrayInputStream(image)));
		}

		@Override
		public LocalFeatureList<? extends LocalFeature<?, ?>> extractFeature(MBFImage image) {
			final ColourDenseSIFT dsift;

			if (approximate)
				dsift = new ColourDenseSIFT(new ApproximateDenseSIFT(stepX, stepY, binWidth, binHeight, numBinsX,
						numBinsY, numOriBins,
						gaussianWindowSize, valueThreshold), colourspace);
			else
				dsift = new ColourDenseSIFT(new DenseSIFT(stepX, stepY, binWidth, binHeight, numBinsX, numBinsY,
						numOriBins,
						gaussianWindowSize, valueThreshold), colourspace);

			dsift.analyseImage(image);

			if (contrastThreshold <= 0) {
				if (byteFeatures)
					return dsift.getByteKeypoints();
				return dsift.getFloatKeypoints();
			} else {
				if (byteFeatures)
					return dsift.getByteKeypoints(contrastThreshold);
				return dsift.getFloatKeypoints(contrastThreshold);
			}
		}
	}

	private static class PyramidDenseSiftMode extends AbstractDenseSiftMode {
		@Option(
				name = "--sizes",
				aliases = "-s",
				required = true,
				usage = "Scales at which the dense SIFT features are extracted. Each value is used as bin size for the DenseSIFT.")
		List<Integer> sizes = new ArrayList<Integer>();

		@Option(
				name = "--magnification-factor",
				aliases = "-mf",
				usage = "The amount to smooth the image by at each level relative to the bin size (sigma = size/magnification).")
		float magnificationFactor = 6;

		PyramidDenseSiftMode(LocalFeatureMode mode) {
			super(mode);
		}

		@Override
		public LocalFeatureList<? extends LocalFeature<?, ?>> extract(byte[] image) throws IOException {
			return extractFeature(ImageUtilities.readF(new ByteArrayInputStream(image)));
		}

		@Override
		public LocalFeatureList<? extends LocalFeature<?, ?>> extractFeature(MBFImage image) {
			return extractFeature(Transforms.calculateIntensityNTSC_LUT(image));
		}

		protected int[] toArray(List<Integer> in) {
			final int[] out = new int[in.size()];

			for (int i = 0; i < out.length; i++) {
				out[i] = in.get(i);
			}

			return out;
		}

		LocalFeatureList<? extends LocalFeature<?, ?>> extractFeature(FImage image) {
			final PyramidDenseSIFT<FImage> dsift;

			if (approximate)
				dsift = new PyramidDenseSIFT<FImage>(new ApproximateDenseSIFT(stepX, stepY, 1, 1, numBinsX, numBinsY,
						numOriBins,
						gaussianWindowSize, valueThreshold), magnificationFactor, toArray(sizes));
			else
				dsift = new PyramidDenseSIFT<FImage>(new DenseSIFT(stepX, stepY, 1, 1, numBinsX, numBinsY, numOriBins,
						gaussianWindowSize, valueThreshold), magnificationFactor, toArray(sizes));

			dsift.analyseImage(image);

			if (contrastThreshold <= 0) {
				if (byteFeatures)
					return dsift.getByteKeypoints();
				return dsift.getFloatKeypoints();
			} else {
				if (byteFeatures)
					return dsift.getByteKeypoints(contrastThreshold);
				return dsift.getFloatKeypoints(contrastThreshold);
			}
		}

		@Override
		public Class<? extends LocalFeature<?, ?>> getFeatureClass() {
			if (byteFeatures)
				return ByteDSIFTKeypoint.class;
			return FloatDSIFTKeypoint.class;
		}
	}

	private static class PyramidColourDenseSiftMode extends PyramidDenseSiftMode {
		@Option(name = "--colour-space", aliases = "-cs", required = false, usage = "Specify the colour space")
		private ColourSpace colourspace = ColourSpace.RGB;

		PyramidColourDenseSiftMode(LocalFeatureMode mode) {
			super(mode);
		}

		@Override
		public LocalFeatureList<? extends LocalFeature<?, ?>> extract(byte[] image) throws IOException {
			return extractFeature(ImageUtilities.readMBF(new ByteArrayInputStream(image)));
		}

		@Override
		public LocalFeatureList<? extends LocalFeature<?, ?>> extractFeature(MBFImage image) {
			final PyramidDenseSIFT<MBFImage> dsift;

			if (approximate)
				dsift = new PyramidDenseSIFT<MBFImage>(new ColourDenseSIFT(new ApproximateDenseSIFT(stepX, stepY, 1, 1,
						numBinsX,
						numBinsY, numOriBins,
						gaussianWindowSize, valueThreshold), colourspace), magnificationFactor, toArray(sizes));
			else
				dsift = new PyramidDenseSIFT<MBFImage>(new ColourDenseSIFT(new DenseSIFT(stepX, stepY, 1, 1, numBinsX,
						numBinsY,
						numOriBins,
						gaussianWindowSize, valueThreshold), colourspace), magnificationFactor, toArray(sizes));

			dsift.analyseImage(image);

			if (contrastThreshold <= 0) {
				if (byteFeatures)
					return dsift.getByteKeypoints();
				return dsift.getFloatKeypoints();
			} else {
				if (byteFeatures)
					return dsift.getByteKeypoints(contrastThreshold);
				return dsift.getFloatKeypoints(contrastThreshold);
			}
		}
	}
}
