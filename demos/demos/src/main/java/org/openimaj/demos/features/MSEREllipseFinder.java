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
package org.openimaj.demos.features;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.demos.Demo;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.watershed.Component;
import org.openimaj.image.analysis.watershed.feature.MomentFeature;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.detector.mser.MSERFeatureGenerator;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.shape.Ellipse;

/**
 * Demo showing ellipse fitted to MSERs
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
@Demo(
		author = "Sina Samangooei",
		description = "Shows ellipse fitting for regions detected using an MSER feature extractor",
		keywords = {
				"mser", "ellipse", "feat" },
		title = "MSER Ellipse Finder")
public class MSEREllipseFinder {
	/**
	 * Construct demo
	 */
	public MSEREllipseFinder() {
		final MBFImage image = new MBFImage(400, 400, ColourSpace.RGB);
		final MBFImageRenderer renderer = image.createRenderer();

		image.fill(RGBColour.WHITE);
		final List<Ellipse> ellipses = new ArrayList<Ellipse>();
		ellipses.add(new Ellipse(200, 100, 100, 80, Math.PI / 4));
		ellipses.add(new Ellipse(200, 300, 50, 30, -Math.PI / 4));
		ellipses.add(new Ellipse(100, 300, 30, 50, -Math.PI / 3));

		for (final Ellipse ellipse : ellipses) {
			renderer.drawShapeFilled(ellipse, RGBColour.BLACK);
		}

		final MSERFeatureGenerator mser = new MSERFeatureGenerator(MomentFeature.class);
		final List<Component> features = mser.generateMSERs(Transforms
				.calculateIntensityNTSC(image));
		for (final Component c : features) {
			final MomentFeature feature = c.getFeature(MomentFeature.class);
			renderer.drawShape(feature.getEllipse(2), RGBColour.RED);
			renderer.drawShape(feature.getEllipse(2)
					.calculateOrientedBoundingBox(), RGBColour.GREEN);
		}
		DisplayUtilities.display(image);
	}

	/**
	 * The main method
	 *
	 * @param args
	 *            ignored
	 */
	public static void main(String args[]) {
		new MSEREllipseFinder();
	}
}
