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
import java.util.List;

import org.openimaj.content.animation.animator.DoubleArrayValueAnimator;
import org.openimaj.content.animation.animator.ValueAnimator;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.model.asm.datasets.IMMFaceDatabase;
import org.openimaj.image.model.asm.datasets.ShapeModelDataset;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.PointList;
import org.openimaj.math.geometry.point.PointListConnections;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.math.geometry.transforms.TransformUtilities;

public class PDMDisplay {
	public static void main(String[] args) throws IOException {
		final ShapeModelDataset<FImage> dataset = IMMFaceDatabase.load(ImageUtilities.FIMAGE_READER);

		final PointListConnections connections = dataset.getConnections();
		final List<PointList> pointData = dataset.getPointLists();

		final int width = 200, height = 200;

		final PointDistributionModel pdm = new PointDistributionModel(pointData);
		pdm.setNumComponents(15);

		final FImage images = new FImage(4 * width, 4 * height);
		for (int j = 0; j < 4; j++) {
			for (int i = 0; i < 4; i++) {
				final ValueAnimator<double[]> a = DoubleArrayValueAnimator.makeRandomLinear(0,
						pdm.getStandardDeviations(3));
				final FImage image = new FImage(width, height);
				image.fill(1);
				final PointList newShape = pdm.generateNewShape(a.nextValue());
				final PointList tfShape = newShape.transform(TransformUtilities.translateMatrix(100, 100).times(
						TransformUtilities.scaleMatrix(50, 50)));

				final List<Line2d> lines = connections.getLines(tfShape);
				image.drawLines(lines, 1, 0f);

				images.drawImage(image, i * width, j * height);
			}
		}

		DisplayUtilities.display(images);
		ImageUtilities.write(images, new File("/Users/jsh2/Desktop/faces.png"));
	}
}
