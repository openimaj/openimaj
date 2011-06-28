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
import org.openimaj.image.feature.local.affine.ASIFT;
import org.openimaj.image.feature.local.affine.ASIFTEngine;
import org.openimaj.image.feature.local.affine.AffineSimulationKeypoint;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.engine.MinMaxDoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.MinMaxKeypoint;


public enum LocalFeatureMode implements CmdLineOptionsProvider {
//	FACEPART {
//
//		@Override
//		public LocalFeatureList<? extends LocalFeature> getKeypointList(byte[] img) throws IOException {
//			FacePartExtractor engine = new FacePartExtractor();
//			LocalFeatureList<FacialKeypoint> keys  = null;
//			switch(this.cm){
//			case SINGLE_COLOUR:
//			case INTENSITY:
//				FImage image = (FImage) cm.process(img);
//				image = (FImage) it.transform(image);
//				keys = engine.findKeypoints(image);
//				break;
//			case INTENSITY_COLOUR:
//			case COLOUR:
//				break;
//			}
//			return keys;
//		}
//
//		@Override
//		public Class<? extends LocalFeature> getFeatureClass() {
//			return null;
//		}
//		
//	},
	SIFT {
		@Override
		public LocalFeatureList<Keypoint> getKeypointList(byte[] img) throws IOException {
			DoGSIFTEngine engine = new DoGSIFTEngine();
			engine.getOptions().setDoubleInitialImage(!noDoubleImageSize);
			LocalFeatureList<Keypoint> keys  = null;
			switch(this.cm){
			case SINGLE_COLOUR:
			case INTENSITY:
				FImage image = (FImage) cm.process(img);
				image = (FImage) it.transform(image);
				
				keys = engine.findFeatures(image);
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
		public Class<? extends LocalFeature<?>> getFeatureClass() {
			return Keypoint.class;
		}
	},
	MIN_MAX_SIFT {
		@Override
		public LocalFeatureList<? extends Keypoint> getKeypointList(byte[] img) throws IOException {
			MinMaxDoGSIFTEngine engine = new MinMaxDoGSIFTEngine();
			LocalFeatureList<MinMaxKeypoint> keys  = null;
			switch(this.cm) {
			case SINGLE_COLOUR:
			case INTENSITY:
				keys = engine.findFeatures((FImage) cm.process(img));
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
	},
	ASIFT {
		@Override
		public LocalFeatureList<Keypoint> getKeypointList(byte[] image) throws IOException {
			ASIFT engine = new ASIFT(false);
			LocalFeatureList<Keypoint> keys  = null;
			switch(this.cm) {
			case SINGLE_COLOUR:
			case INTENSITY:
				engine.process((FImage) it.transform(cm.process(image)),5);
				keys = engine.getKeypoints();
				break;
			case INTENSITY_COLOUR:
				// TODO: Make this work because you can't just give a list of keypoints, the simulation is required
				//				MBFImage mbfImg = (MBFImage) cm.process(image);
				//				List<Keypoint>intensityKeys = engine.process(mbfImg.flatten());
				//				keys = new ColourKeypointEngine(intensityKeys).findKeypointsFromIntensity(mbfImg);
				throw new UnsupportedOperationException();
			}
			return keys;
		}
		
		@Override
		public Class<? extends LocalFeature<?>> getFeatureClass() {
			return Keypoint.class;
		}
	},
	ASIFTENRICHED {
		@Override
		public LocalFeatureList<AffineSimulationKeypoint> getKeypointList(byte[] image) throws IOException {
			ASIFTEngine engine = new ASIFTEngine(!noDoubleImageSize ,5);
			LocalFeatureList<AffineSimulationKeypoint> keys  = null;
			switch(this.cm){
			case SINGLE_COLOUR:
			case INTENSITY:
				FImage img = (FImage) cm.process(image);
				img = (FImage) it.transform(img);
				keys = engine.findSimulationKeypoints(img);
				break;
			case INTENSITY_COLOUR:
				// TODO: Make this work because you can't just give a list of keypoints, the simulation is required
				//				MBFImage mbfImg = (MBFImage) cm.process(image);
				//				List<Keypoint>intensityKeys = engine.process(mbfImg.flatten());
				//				keys = new ColourKeypointEngine(intensityKeys).findKeypointsFromIntensity(mbfImg);
				throw new UnsupportedOperationException();
			}
			return keys;
		}
		
		@Override
		public Class<? extends LocalFeature<?>> getFeatureClass() {
			return AffineSimulationKeypoint.class;
		}
	},
////	GRID {
////		@Option(name="--grid-spacing", aliases="-g", required=false, usage="Optionally provide the spacing of the dense sift algorithm. This is multiples of the sigma value.", handler=ProxyOptionHandler.class)
////		public float spacing = 1;
////
////		@Override
////		public LocalFeatureList<Keypoint> getKeypointList(byte[] image) throws IOException {
////			GridKeypointEngine engine = new GridKeypointEngine ();
////			engine.setStepScale(spacing);
////			LocalFeatureList<Keypoint> keys  = null;
////			switch(this.cm){
////			case SINGLE_COLOUR:
////			case INTENSITY:
////				keys = engine.findKeypoints((FImage) it.transform(cm.process(image)));
////				break;
////			case INTENSITY_COLOUR:
////				break;
////			}
////			return keys;
////		}
////		
////		@Override
////		public Class<? extends LocalFeature> getFeatureClass() {
////			return Keypoint.class;
////		}
////	},
//	MLSIFT {
//		@Override
//		public LocalFeatureList<Keypoint> getKeypointList(byte[] img) throws IOException {
//			MirrorSiftKeypointEngine engine = new MirrorSiftKeypointEngine();
//			LocalFeatureList<Keypoint> keys  = null;
//			switch(this.cm){
//			case SINGLE_COLOUR:
//			case INTENSITY:
//				keys = engine.findKeypoints((FImage) it.transform(cm.process(img)));
//				break;
//			case INTENSITY_COLOUR:
//			case COLOUR:
//				// TODO: Make an ML-Colour-SIFT
//				break;
//			}
//			return keys;
//		}
//
//		@Override
//		public Class<? extends LocalFeature> getFeatureClass() {
//			return Keypoint.class;
//		}
//	},
//	ILSIFT {
//		@Override
//		public LocalFeatureList<Keypoint> getKeypointList(byte[] img) throws IOException {
//			InversionSiftKeypointEngine engine = new InversionSiftKeypointEngine();
//			LocalFeatureList<Keypoint> keys  = null;
//			switch(this.cm){
//			case SINGLE_COLOUR:
//			case INTENSITY:
//				keys = engine.findKeypoints((FImage) it.transform(cm.process(img)));
//				break;
//			case INTENSITY_COLOUR:
//			case COLOUR:
//				// TODO: Make an ML-Colour-SIFT
//				break;
//			}
//			return keys;
//		}
//
//		@Override
//		public Class<? extends LocalFeature> getFeatureClass() {
//			return Keypoint.class;
//		}
//	}
	;
	
	@Option(name="--colour-mode", aliases="-cm", required=false, usage="Optionally perform sift using the colour of the image in some mode", handler=ProxyOptionHandler.class)
	public ColourMode cm = ColourMode.INTENSITY;

	@Option(name="--image-transform", aliases="-it", required=false, usage="Optionally perform a image transform before keypoint calculation", handler=ProxyOptionHandler.class)
	public ImageTransform it = ImageTransform.NOTHING;
	
	@Option(name="--no-double-size", aliases="-nds", required=false, usage="Double the image sizes for the first iteration")
	public boolean noDoubleImageSize = false;

	public abstract LocalFeatureList<? extends LocalFeature<?>> getKeypointList(byte[] image) throws IOException ;

	public abstract Class<? extends LocalFeature<?>> getFeatureClass();
	
	@Override
	public Object getOptions() {
		return this;
	}
}
