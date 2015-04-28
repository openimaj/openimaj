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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.List;

import javax.swing.SwingUtilities;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.model.asm.ActiveShapeModel.IterationResult;
import org.openimaj.image.model.asm.MultiResolutionActiveShapeModel;
import org.openimaj.image.model.asm.datasets.IMMFaceDatabase;
import org.openimaj.image.model.asm.datasets.ShapeModelDataset;
import org.openimaj.image.model.landmark.FNormalLandmarkModel;
import org.openimaj.image.pixel.sampling.FLineSampler;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.PointList;
import org.openimaj.math.geometry.point.PointListConnections;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.matrix.algorithm.pca.PrincipalComponentAnalysis.PercentageEnergyComponentSelector;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

import Jama.Matrix;

public class PDMPlaygroundLive {
	/**
	 * @param args
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws IOException {
		// ASFDataset dataset = new ASFDataset(new
		// File("/Users/jsh2/Work/lmlk/trunk/shared/JAAM-API/data/face-data"));
		// AMPTSDataset dataset = new AMPTSDataset(
		// new File("/Users/jsh2/Downloads/am_tools/points"),
		// new File("/Users/jsh2/Downloads/am_tools/images"),
		// new File("/Users/jsh2/Downloads/am_tools/models/face.parts"));
		final ShapeModelDataset<FImage> dataset = IMMFaceDatabase.load(ImageUtilities.FIMAGE_READER);

		final PointListConnections connections = dataset.getConnections();
		final List<PointList> pointData = dataset.getPointLists();

		final float scale = 0.02f;
		final FNormalLandmarkModel.Factory factory = new FNormalLandmarkModel.Factory(connections,
				FLineSampler.INTERPOLATED_DERIVATIVE, 5, 9, scale);
		// BlockLandmarkModel.Factory factory = new
		// BlockLandmarkModel.Factory();
		final MultiResolutionActiveShapeModel asm = MultiResolutionActiveShapeModel.trainModel(3,
				new PercentageEnergyComponentSelector(0.95), dataset, new PointDistributionModel.BoxConstraint(3),
				factory);
		// final ActiveShapeModel asm = ActiveShapeModel.trainModel(10, data,
		// new PointDistributionModel.BoxConstraint(3), factory);

		final boolean[] tracking = { false };

		final VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay(new VideoCapture(320, 240));
		SwingUtilities.getRoot(vd.getScreen()).addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == 'r')
					tracking[0] = false;
			}
		});
		vd.addVideoListener(new VideoDisplayListener<MBFImage>() {

			HaarCascadeDetector detector = new HaarCascadeDetector(80);
			PointList shape = null;

			@Override
			public void beforeUpdate(MBFImage frame) {
				final FImage image = frame.flatten();
				final List<DetectedFace> faces = detector.detectFaces(image);

				if (faces == null || faces.size() == 0)
					return;

				// for (DetectedFace face : faces)
				final DetectedFace face = faces.get(0);
				{
					frame.drawShape(face.getBounds(), RGBColour.GREEN);

					final Point2d cog = face.getBounds().calculateCentroid();
					final double facescale = face.getBounds().height / 3.5;

					if (!tracking[0]) {
						final Matrix pose = TransformUtilities.translateMatrix(cog.getX(), cog.getY()).times(
								TransformUtilities.scaleMatrix(facescale, facescale));
						shape = asm.getPDM().getMean().transform(pose);
						tracking[0] = true;
					}

					final long t1 = System.currentTimeMillis();
					final IterationResult newData = asm.fit(image, shape);
					final long t2 = System.currentTimeMillis();

					shape = newData.shape;

					frame.drawLines(connections.getLines(shape), 1, RGBColour.RED);

					final float shapeScale = shape.computeIntrinsicScale();
					for (final Point2d pt : shape) {
						final Line2d normal = connections.calculateNormalLine(pt, shape, scale * shapeScale);
						if (normal != null)
							frame.drawLine(normal, 1, RGBColour.BLUE);
					}

					System.out.println(newData.fit);
					System.out.println(t2 - t1);
				}
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
			}
		});
	}
}
