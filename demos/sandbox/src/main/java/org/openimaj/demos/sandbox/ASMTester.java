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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.model.asm.ActiveShapeModel.IterationResult;
import org.openimaj.image.model.asm.MultiResolutionActiveShapeModel;
import org.openimaj.image.model.landmark.FNormalLandmarkModel;
import org.openimaj.image.pixel.sampling.FLineSampler;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.point.PointList;
import org.openimaj.math.geometry.point.PointListConnections;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.math.geometry.shape.Triangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.matrix.algorithm.pca.PrincipalComponentAnalysis.NumberComponentSelector;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

public class ASMTester {
	public static List<IndependentPair<PointList, FImage>> generateData(int number) {
		final List<IndependentPair<PointList, FImage>> data = new ArrayList<IndependentPair<PointList, FImage>>(number);

		final Random rnd = new Random();

		for (int i = 0; i < number; i++) {
			final float c1 = rnd.nextFloat();
			float c2 = rnd.nextFloat();

			while (Math.abs(c1 - c2) < 0.1) {
				c2 = rnd.nextFloat();
			}

			final Triangle t = new Triangle(
					new Point2dImpl(((float) i / (number - 1)) * 80 + 10f, 10),
					new Point2dImpl(90, 90),
					new Point2dImpl(10, 90)
					);

			final FImage img = new FImage(100, 100);
			img.fill(Math.min(c1, c2));
			img.drawShapeFilled(t, Math.max(c1, c2));

			data.add(new IndependentPair<PointList, FImage>(t.asPolygon(), img));
		}

		return data;
	}

	public static void main(String[] args) {
		final List<IndependentPair<PointList, FImage>> data = generateData(10);
		final PointListConnections connections = new PointListConnections();
		connections.addConnection(0, 1);
		connections.addConnection(1, 2);
		connections.addConnection(2, 0);

		final float scale = 0.4f;
		final FNormalLandmarkModel.Factory factory = new FNormalLandmarkModel.Factory(connections,
				FLineSampler.INTERPOLATED_DERIVATIVE, 5, 13, scale);
		final MultiResolutionActiveShapeModel<FImage> asm = MultiResolutionActiveShapeModel.trainModel(1,
				new NumberComponentSelector(10), data, new PointDistributionModel.BoxConstraint(3), factory);

		for (final IndependentPair<PointList, FImage> inst : generateData(10)) {
			final MBFImage rgb = inst.secondObject().toRGB();

			// Matrix pose = TransformUtilities.translateMatrix(50,
			// 50).times(TransformUtilities.scaleMatrix(50, 50));
			// PointList shape = asm.getPDM().getMean().transform(pose);
			// Matrix pose = Matrix.identity(3, 3);
			final Matrix pose = TransformUtilities.scaleMatrixAboutPoint(0.8, 0.8, 50, 50);
			final PointList shape = inst.firstObject().transform(pose);
			rgb.drawLines(connections.getLines(shape), 1, RGBColour.RED);

			final IterationResult res = asm.fit(inst.secondObject(), shape);

			rgb.drawLines(connections.getLines(res.shape), 1, RGBColour.GREEN);

			DisplayUtilities.display(rgb);
		}
	}
}
