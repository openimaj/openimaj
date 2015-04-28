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

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.model.asm.ActiveShapeModel;
import org.openimaj.image.model.asm.ActiveShapeModel.IterationResult;
import org.openimaj.image.model.asm.datasets.ShapeModelDataset;
import org.openimaj.image.model.asm.datasets.ShapeModelDatasets;
import org.openimaj.image.model.landmark.FNormalLandmarkModel;
import org.openimaj.image.pixel.sampling.FLineSampler;
import org.openimaj.math.geometry.point.PointList;
import org.openimaj.math.geometry.point.PointListConnections;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.matrix.algorithm.pca.PrincipalComponentAnalysis.PercentageEnergyComponentSelector;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.video.AnimatedVideo;
import org.openimaj.video.VideoDisplay;

public class ASMFitAnimation {
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final ShapeModelDataset<FImage> dataset = ShapeModelDatasets.loadASFDataset("/Users/jsh2/Downloads/imm_face_db",
				ImageUtilities.FIMAGE_READER);
		final PointListConnections conns = dataset.getConnections();

		final float scale = 0.02f;
		final FNormalLandmarkModel.Factory factory = new FNormalLandmarkModel.Factory(conns, FLineSampler.INTERPOLATED,
				5, 9, scale);
		// BlockLandmarkModel.Factory factory = new
		// BlockLandmarkModel.Factory();
		final ActiveShapeModel<FImage> asm = ActiveShapeModel.trainModel(new PercentageEnergyComponentSelector(0.95),
				dataset,
				new PointDistributionModel.BoxConstraint(3), factory);

		final IndependentPair<PointList, FImage> initial = dataset.get(0);
		// final IndependentPair<PointList, FImage> initial =
		// ASFDataset.readASF(new File(dir, "01-1m.asf"));

		// final int idx = 2;
		// final Point2d pt = initial.firstObject().get(idx);
		// pt.translate(4, 0);

		VideoDisplay.createVideoDisplay(new AnimatedVideo<MBFImage>(new MBFImage(640, 480, 3), 30) {
			PointList shape = initial.firstObject().transform(
					TransformUtilities.scaleMatrixAboutPoint(0.9, 0.9, 320, 240));
			FImage img = initial.secondObject();

			@Override
			protected void updateNextFrame(MBFImage frame) {
				frame.drawImage(img.toRGB(), 0, 0);
				frame.drawLines(conns.getLines(shape), 1, RGBColour.BLUE);

				final IterationResult next = asm.performIteration(img, shape);
				shape = next.shape;

				// frame.drawPoint(pt, RGBColour.RED, 3);
				// Point2d newpt =
				// ((NormalLandmarkModel)asm.getLandmarkModels()[idx]).updatePosition(img,
				// pt, shape).first;
				// pt.setX(newpt.getX());
				// pt.setY(newpt.getY());
			}
		});

	}
}
