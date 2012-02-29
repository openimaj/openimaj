package org.openimaj.demos.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.content.animation.AnimatedVideo;
import org.openimaj.demos.sandbox.asm.ASFDataset;
import org.openimaj.demos.sandbox.asm.ActiveShapeModel;
import org.openimaj.demos.sandbox.asm.ActiveShapeModel.IterationResult;
import org.openimaj.demos.sandbox.asm.PixelProfileModel;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.shape.PointListConnections;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.video.VideoDisplay;

import Jama.Matrix;

public class ASMFitAnimation {
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
//		final File dir = new File("/Users/jsh2/Work/lmlk/trunk/shared/JAAM-API/data/face-data");
		final File dir = new File("/Users/jsh2/Downloads/imm_face_db");
		ASFDataset dataset = new ASFDataset(dir);
		
		final List<IndependentPair<PointList, FImage>> data = dataset.getData();
		final PointListConnections conns = dataset.getConnections();

		final float scale = 0.03f;
		final int k = 5;
		final int m = 8;
		final ActiveShapeModel asm = ActiveShapeModel.trainModel(k, m, scale, 20, conns, data, new PointDistributionModel.BoxConstraint(3));

		final IndependentPair<PointList, FImage> initial = ASFDataset.readASF(new File(dir, "16-6m.asf"));
				
		VideoDisplay.createVideoDisplay(new AnimatedVideo<MBFImage>(new MBFImage(640,480, 3), 1) {
			Matrix pose = Matrix.identity(3, 3);
			PointList shape = initial.firstObject();
			FImage img = initial.secondObject();

			@Override
			protected void updateNextFrame(MBFImage frame) {
				frame.drawImage(img.toRGB(), 0, 0);
				frame.drawLines(conns.getLines(shape), 1, RGBColour.BLUE);

//				IterationResult next = asm.performIteration(img, pose, shape);
//				pose = next.pose;
//				shape = next.shape;
				
				//
				float shapeScale = shape.computeIntrinsicScale();
				float scale2 = (2*m + 1) * scale * shapeScale / (2*k + 1); 
				PointList newShape = new PointList();
				
				int inliers = 0;
				int outliers = 0;
				for (int i=0; i<asm.getPPMs().length; i++) {
					
					Line2d testLine = conns.calculateNormalLine(i, shape, scale2);
					
					frame.drawLine(testLine, 1, RGBColour.MAGENTA);
					frame.drawLine(conns.calculateNormalLine(i, shape, scale*shapeScale), 1, RGBColour.GREEN);
					
					Point2dImpl newBest = asm.getPPMs()[i].computeNewBest(img, testLine, 2*m+1);
					newShape.points.add( newBest );
					
					frame.drawPoint(newBest, RGBColour.RED, 1);
					
					double percentageFromStart = Line2d.distance(testLine.begin, newBest) / testLine.calculateLength();
					if (percentageFromStart > 0.25 && percentageFromStart < 0.75)
						inliers++;
					else 
						outliers++;
				}

//				IndependentPair<Matrix, double[]> newModelParams = asm.getPDM().fitModel(newShape);
//				pose = newModelParams.firstObject();
//				shape = asm.getPDM().generateNewShape(newModelParams.secondObject()).transform(pose);
				shape = newShape;
				//
			}
		});

		
	}
}
