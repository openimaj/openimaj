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
package org.openimaj.demos.sandbox;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import org.openimaj.demos.utils.FeatureClickListener;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.detector.ipd.collector.CircularInterestPointKeypoint;
import org.openimaj.image.feature.local.engine.ipd.AbstractIPDSIFTEngine;
import org.openimaj.image.feature.local.engine.ipd.EllipticIPDSIFTEngine;
import org.openimaj.image.feature.local.engine.ipd.FinderMode;
import org.openimaj.image.feature.local.interest.AffineAdaption;
import org.openimaj.image.feature.local.interest.EllipticInterestPointData;
import org.openimaj.image.feature.local.interest.HarrisIPD;
import org.openimaj.image.feature.local.interest.IPDSelectionMode;
import org.openimaj.image.feature.local.interest.InterestPointData;
import org.openimaj.image.feature.local.interest.InterestPointVisualiser;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;
import org.openimaj.io.IOUtils;

public class ImageIPDSIFTEngine {

	public static void main(String[] args) throws IOException {
		MBFImage image = ImageUtilities.readMBF(ImageIPDSIFTEngine.class.getResourceAsStream("/org/openimaj/image/feature/validator/graf/img1.ppm"));
		FImage fimage = Transforms.calculateIntensity(image);

		File featureOut = new File("/tmp/img1.oi-sift-features");
		LocalFeatureList<? extends InterestPointKeypoint<? extends InterestPointData>> kps = null;
		boolean force = true;
		HarrisIPD harrisIPD = new HarrisIPD(1.4f);
		harrisIPD.setImageBlurred(true);
		AffineAdaption affineIPD = new AffineAdaption(harrisIPD,new IPDSelectionMode.Threshold(250f));
		affineIPD.setFastDifferentiationScale(true);
		AbstractIPDSIFTEngine<EllipticInterestPointData> engine = new EllipticIPDSIFTEngine(affineIPD);
		engine.setFinderMode(new FinderMode.Basic<EllipticInterestPointData>());
//		engine.setFinderMode(new FinderMode.Basic<InterestPointData>());
//		engine.setSelectionMode(new IPDSelectionMode.Threshold(10000f));
		engine.setSelectionMode(new IPDSelectionMode.All());
		engine.setAcrossScales(true);
		if (!featureOut.exists() || force) {
			kps = engine.findFeatures(fimage);
			IOUtils.writeBinary(featureOut, kps);
		} else {
			kps = MemoryLocalFeatureList.read(featureOut,
					CircularInterestPointKeypoint.class);
		}

		InterestPointVisualiser<Float[], MBFImage> visualiser = InterestPointVisualiser
				.visualiseKeypoints(image, kps);
		MBFImage out = visualiser.drawPatches(RGBColour.RED, RGBColour.GREEN);

		JFrame f = DisplayUtilities.display(out);
		FeatureClickListener l = new FeatureClickListener();
		l.setImage(kps, image);
		l.setDisplayFrame(f);
		f.getContentPane().addMouseListener(l);

	}
}
