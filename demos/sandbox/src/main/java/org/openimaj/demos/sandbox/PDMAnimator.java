package org.openimaj.demos.sandbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openimaj.content.animation.AnimatedVideo;
import org.openimaj.content.animation.animator.DoubleArrayValueAnimator;
import org.openimaj.content.animation.animator.ValueAnimator;
import org.openimaj.demos.sandbox.asm.ActiveShapeModel;
import org.openimaj.demos.sandbox.asm.ActiveShapeModel.IterationResult;
import org.openimaj.demos.sandbox.asm.ASFDataset;
import org.openimaj.demos.sandbox.asm.MultiResolutionActiveShapeModel;
import org.openimaj.demos.sandbox.asm.NormalLandmark;
import org.openimaj.demos.sandbox.asm.PixelProfileModel;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.shape.PointListConnections;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;

import Jama.Matrix;

public class PDMAnimator {
		public static void main(String[] args) throws IOException {
			ASFDataset dataset = new ASFDataset(new File("/Users/jsh2/Downloads/imm_face_db"));
			
			final List<IndependentPair<PointList, FImage>> data = dataset.getData();
			final PointListConnections connections = dataset.getConnections();
			
			List<PointList> pointData = IndependentPair.getFirst(data);
			
			final PointDistributionModel pdm = new PointDistributionModel(pointData);
			pdm.setNumComponents(1);
			
			VideoDisplay.createVideoDisplay(new AnimatedVideo<FImage>(new FImage(600,600)) {
				ValueAnimator<double[]> a = DoubleArrayValueAnimator.makeRandomLinear(60, pdm.getStandardDeviations(3));
				
				@Override
				protected void updateNextFrame(FImage frame) {
					frame.fill(0f);
					
					PointList newShape = pdm.generateNewShape( a.nextValue() );
					PointList tfShape = newShape.transform(TransformUtilities.translateMatrix(300, 300).times(TransformUtilities.scaleMatrix(150, 150)));
					
					List<Line2d> lines = connections.getLines(tfShape);
					frame.drawLines(lines, 1, 1f);
					
					for (Point2d pt : tfShape) {
						Line2d normal = connections.calculateNormalLine(pt, tfShape, 10f);
						
						if (normal != null) {
							frame.drawLine(normal, 1, 0.5f);
						}
					}
				}
			});		
		}
}
