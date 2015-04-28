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

import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.model.asm.ActiveShapeModel.IterationResult;
import org.openimaj.image.model.asm.MultiResolutionActiveShapeModel;
import org.openimaj.image.model.asm.datasets.ShapeModelDataset;
import org.openimaj.image.model.asm.datasets.ShapeModelDatasets;
import org.openimaj.image.model.landmark.FNormalLandmarkModel;
import org.openimaj.image.pixel.sampling.FLineSampler;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.PointList;
import org.openimaj.math.geometry.point.PointListConnections;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.matrix.algorithm.pca.PrincipalComponentAnalysis.NumberComponentSelector;

import Jama.Matrix;

public class ASMPlayground {
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final ShapeModelDataset<FImage> dataset = ShapeModelDatasets.loadASFDataset("/Users/jsh2/Downloads/imm_face_db",
				ImageUtilities.FIMAGE_READER);
		final PointListConnections conns = dataset.getConnections();

		final float scale = 0.02f;
		final FNormalLandmarkModel.Factory factory = new FNormalLandmarkModel.Factory(conns,
				FLineSampler.INTERPOLATED_DERIVATIVE, 5, 9, scale);
		final MultiResolutionActiveShapeModel<FImage> asm = MultiResolutionActiveShapeModel.trainModel(3,
				new NumberComponentSelector(19), dataset, new PointDistributionModel.BoxConstraint(3), factory);

		final Matrix pose = TransformUtilities.translateMatrix(300, 300).times(TransformUtilities.scaleMatrix(70, 70));
		PointList shape = asm.getPDM().getMean().transform(pose);
		// PointList shape = ASFDataset.readASF(new File(dir,
		// "01-1m.asf")).firstObject();
		final FImage img = dataset.get(0).secondObject();
		// PointList shape = ASFDataset.readASF(new File(dir,
		// "16-6m.asf")).firstObject();
		// FImage img = ASFDataset.readASF(new File(dir,
		// "16-6m.asf")).secondObject();

		final MBFImage image = img.toRGB();
		image.drawLines(conns.getLines(shape), 1, RGBColour.RED);

		final long t1 = System.currentTimeMillis();
		final IterationResult newData = asm.fit(img, shape);
		final long t2 = System.currentTimeMillis();

		shape = newData.shape;

		System.out.println(newData.fit);
		System.out.println(t2 - t1);

		image.drawLines(conns.getLines(shape), 1, RGBColour.GREEN);

		final float shapeScale = shape.computeIntrinsicScale();
		for (final Point2d pt : shape) {
			final Line2d normal = conns.calculateNormalLine(pt, shape, scale * shapeScale);
			if (normal != null)
				image.drawLine(normal, 1, RGBColour.BLUE);
		}

		DisplayUtilities.display(image);
	}
}
