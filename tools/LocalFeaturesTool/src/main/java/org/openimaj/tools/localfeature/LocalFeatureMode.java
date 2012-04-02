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
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.engine.MinMaxDoGSIFTEngine;
import org.openimaj.image.feature.local.engine.asift.ASIFTEngine;
import org.openimaj.image.feature.local.engine.asift.ColourASIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.MinMaxKeypoint;
import org.openimaj.tools.localfeature.ColourMode.ColourModeOp;
import org.openimaj.tools.localfeature.ImageTransform.ImageTransformOp;

public enum LocalFeatureMode implements CmdLineOptionsProvider {
	SIFT {
		@Override
		public LocalFeatureModeOp getOptions() {
			return new SiftMode(SIFT);
		}
	},
	MIN_MAX_SIFT {
		@Override
		public LocalFeatureModeOp getOptions() {
			return new MinMaxSiftMode(MIN_MAX_SIFT);
		}
	},
	ASIFT {
		@Override
		public LocalFeatureModeOp getOptions() {
			return new AsiftMode(ASIFT);
		}
	},
	ASIFTENRICHED {
		@Override
		public LocalFeatureModeOp getOptions() {
			return new AsiftEnrichedMode(ASIFTENRICHED);
		}
	}
	;

	@Override
	public abstract LocalFeatureModeOp getOptions();

	public static abstract class LocalFeatureModeOp {
		private LocalFeatureMode mode;
		
		@Option(name="--colour-mode", aliases="-cm", required=false, usage="Optionally perform sift using the colour of the image in some mode", handler=ProxyOptionHandler.class)
		protected ColourMode cm = ColourMode.INTENSITY;
		protected ColourModeOp cmOp = (ColourModeOp) ColourMode.INTENSITY.getOptions();

		@Option(name="--image-transform", aliases="-it", required=false, usage="Optionally perform a image transform before keypoint calculation", handler=ProxyOptionHandler.class)
		protected ImageTransform it = ImageTransform.NOTHING;
		protected ImageTransformOp itOp = (ImageTransformOp) ImageTransform.NOTHING.getOptions();

		@Option(name="--no-double-size", aliases="-nds", required=false, usage="Double the image sizes for the first iteration")
		protected boolean noDoubleImageSize = false;

		public abstract LocalFeatureList<? extends LocalFeature<?>> getKeypointList(byte[] image) throws IOException ;
		public abstract LocalFeatureList<? extends LocalFeature<?>> getKeypointList(Image<?,?> image) throws IOException ;

		public abstract Class<? extends LocalFeature<?>> getFeatureClass();
		
		private LocalFeatureModeOp(LocalFeatureMode mode) {
			this.mode = mode;
		}
		
		public String name() {
			return mode.name();
		}
		
		public LocalFeatureMode getMode() {
			return mode;
		}
	}

	private static class SiftMode extends LocalFeatureModeOp {
		private SiftMode(LocalFeatureMode mode) {
			super(mode);
		}
		
		@Override
		public LocalFeatureList<Keypoint> getKeypointList(byte[] img) throws IOException {
			DoGSIFTEngine engine = new DoGSIFTEngine();
			engine.getOptions().setDoubleInitialImage(!noDoubleImageSize);
			LocalFeatureList<Keypoint> keys  = null;
			switch(this.cm){
			case SINGLE_COLOUR:
			case INTENSITY:
				Image<?,?> image = cmOp.process(img);
				image = itOp.transform(image);

				keys = engine.findFeatures((FImage)image);
				break;
			case INTENSITY_COLOUR:
			case COLOUR:
				//				MBFImage mbfImg = (MBFImage) it.transform(cm.process(img));
				//				List<Keypoint>intensityKeys = engine.findKeypoints((FImage) it.transform(ColourMode.INTENSITY.process(img)));
				//				keys = new ColourKeypointEngine(intensityKeys, cm.ct, cm==ColourMode.INTENSITY_COLOUR).findKeypointsFromIntensity(mbfImg);
				throw new UnsupportedOperationException();
			}
			return keys;
		}
		@Override
		public LocalFeatureList<Keypoint> getKeypointList(Image<?,?> img) throws IOException {
			DoGSIFTEngine engine = new DoGSIFTEngine();
			engine.getOptions().setDoubleInitialImage(!noDoubleImageSize);
			LocalFeatureList<Keypoint> keys  = null;
			;
			switch(this.cm){
			case SINGLE_COLOUR:
			case INTENSITY:
				Image<?, ?> image = itOp.transform(img);
				keys = engine.findFeatures((FImage)image);
				break;
			case INTENSITY_COLOUR:
			case COLOUR:
				throw new UnsupportedOperationException();
			}
			return keys;
		}


		@Override
		public Class<? extends LocalFeature<?>> getFeatureClass() {
			return Keypoint.class;
		}
	}

	private static class MinMaxSiftMode extends LocalFeatureModeOp {
		private MinMaxSiftMode(LocalFeatureMode mode) {
			super(mode);
		}
		
		@Override
		public LocalFeatureList<? extends Keypoint> getKeypointList(byte[] img) throws IOException {
			MinMaxDoGSIFTEngine engine = new MinMaxDoGSIFTEngine();
			LocalFeatureList<MinMaxKeypoint> keys  = null;
			switch(this.cm) {
			case SINGLE_COLOUR:
			case INTENSITY:
				keys = engine.findFeatures((FImage) cmOp.process(img));
				break;
			case INTENSITY_COLOUR:
			case COLOUR:
				//TODO
				throw new UnsupportedOperationException();
			}
			return keys;
		}

		@Override
		public LocalFeatureList<? extends Keypoint> getKeypointList(Image<?,?> img) throws IOException {
			MinMaxDoGSIFTEngine engine = new MinMaxDoGSIFTEngine();
			LocalFeatureList<MinMaxKeypoint> keys  = null;
			switch(this.cm) {
			case SINGLE_COLOUR:
			case INTENSITY:
				keys = engine.findFeatures((FImage) img);
				break;
			case INTENSITY_COLOUR:
			case COLOUR:
				//TODO
				throw new UnsupportedOperationException();
			}
			return keys;
		}

		@Override
		public Class<? extends LocalFeature<?>> getFeatureClass() {
			return MinMaxKeypoint.class;
		}
	}

	private static class AsiftMode extends LocalFeatureModeOp {
		private AsiftMode(LocalFeatureMode mode) {
			super(mode);
		}
		
		@Option(name="--n-tilts", aliases="-nt", required=false, usage="The number of tilts for the affine simulation")
		public int ntilts = 5;

		@Override
		public LocalFeatureList<Keypoint> getKeypointList(byte[] image) throws IOException {

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

		@Override
		public LocalFeatureList<Keypoint> getKeypointList(Image<?,?> image) throws IOException {
			LocalFeatureList<Keypoint> keys  = null;

			switch(this.cm) {
			case SINGLE_COLOUR:
			case INTENSITY:
				BasicASIFT basic = new BasicASIFT(!noDoubleImageSize);
				basic.process((FImage) itOp.transform(image),ntilts);
				keys = basic.getKeypoints();
				break;
			case INTENSITY_COLOUR:
				ColourASIFT colour = new ColourASIFT(!noDoubleImageSize);
				colour.process((MBFImage)itOp.transform(image), ntilts);
			}
			return keys;
		}

		@Override
		public Class<? extends LocalFeature<?>> getFeatureClass() {
			return Keypoint.class;
		}
	}

	private static class AsiftEnrichedMode extends LocalFeatureModeOp {
		private AsiftEnrichedMode(LocalFeatureMode mode) {
			super(mode);
		}
		
		@Option(name="--n-tilts", aliases="-nt", required=false, usage="The number of tilts for the affine simulation")
		public int ntilts = 5;

		@Override
		public LocalFeatureList<AffineSimulationKeypoint> getKeypointList(byte[] image) throws IOException {
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

		@Override
		public LocalFeatureList<AffineSimulationKeypoint> getKeypointList(Image<?,?> image) throws IOException {
			LocalFeatureList<AffineSimulationKeypoint> keys = null;
			switch(this.cm){
			case SINGLE_COLOUR:
			case INTENSITY:
				ASIFTEngine engine = new ASIFTEngine(!noDoubleImageSize ,ntilts);
				FImage img = (FImage) image;
				img = (FImage) itOp.transform(img);
				keys = engine.findSimulationKeypoints(img);
				break;
			case INTENSITY_COLOUR:
				ColourASIFTEngine colourengine = new ColourASIFTEngine(!noDoubleImageSize ,ntilts);
				MBFImage colourimg = (MBFImage) image;
				colourimg = (MBFImage) itOp.transform(colourimg);
				keys = colourengine.findSimulationKeypoints(colourimg);
				break;
			}
			return keys;
		}

		@Override
		public Class<? extends LocalFeature<?>> getFeatureClass() {
			return AffineSimulationKeypoint.class;
		}
	}
}
