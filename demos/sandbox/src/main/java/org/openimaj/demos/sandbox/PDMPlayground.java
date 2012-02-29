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

public class PDMPlayground {
	static IndependentPair<PointList, FImage> readASF(File file) throws IOException {
		PointList pl = new PointList();
		BufferedReader br = new BufferedReader(new FileReader(file));

		String line;
		while ((line = br.readLine()) != null) {
			if (!line.startsWith("#")) {
				String[] parts = line.split("\\s+");

				if (parts.length < 7)
					continue;

				float x = Float.parseFloat(parts[2].trim());
				float y = Float.parseFloat(parts[3].trim());

				pl.points.add(new Point2dImpl(x, y));
			}
		}
		br.close();

		FImage image = ImageUtilities.readF(new File(file.getAbsolutePath().replace(".asf", ".bmp")));
		
		pl.scaleXY(image.width, image.height);
		
		return new IndependentPair<PointList, FImage>(pl, image);
	}
	
	static PointListConnections readASFConnections(File file) throws IOException {
		PointListConnections plc = new PointListConnections();
		BufferedReader br = new BufferedReader(new FileReader(file));

		String line;
		while ((line = br.readLine()) != null) {
			if (!line.startsWith("#")) {
				String[] parts = line.split("\\s+");

				if (parts.length < 7)
					continue;

				int from = Integer.parseInt(parts[4].trim());
				int to = Integer.parseInt(parts[6].trim());

				plc.addConnection(from, to);
			}
		}
		br.close();

		return plc;
	}
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		File dir = new File("/Users/jsh2/Work/lmlk/trunk/shared/JAAM-API/data/face-data");
		final List<IndependentPair<PointList, FImage>> data = new ArrayList<IndependentPair<PointList, FImage>>();
		
		for (File f : dir.listFiles()) {
			if (f.getName().endsWith(".asf")) {
				data.add(readASF(f));
			}
		}

		final PointListConnections conns = readASFConnections(new File(dir, "01-1m.asf"));
		
		final float scale = 0.2f;
		final ActiveShapeModel asm = ActiveShapeModel.trainModel(2, 6, scale, 15, conns, data);
//		final MultiResolutionActiveShapeModel asm = MultiResolutionActiveShapeModel.trainModel(1, 2, 6, scale, 15, conns, data);
		
		VideoDisplay.createVideoDisplay(new AnimatedVideo<FImage>(new FImage(640,480), 30) {
			Matrix pose = TransformUtilities.translateMatrix(300, 300).times(TransformUtilities.scaleMatrix(70, 70));
			PointList shape = asm.getPDM().getMean().transform(pose);
//			Matrix pose = Matrix.identity(3, 3);
//			PointList model = readASF(new File(dir, "01-1m.asf")).firstObject();
			
			FImage image = data.get(0).secondObject();
			
			@Override
			protected void updateNextFrame(FImage frame) {
				frame.drawImage(image, 0, 0);
				frame.drawLines(conns.getLines(shape), 1, 1f);
				
				float shapeScale = shape.computeIntrinsicScale();
				for (Point2d pt : shape) {
					Line2d normal = conns.calculateNormalLine(pt, shape, scale * shapeScale);
					if (normal != null) frame.drawLine(normal, 1, 0.5f);
				}
				
				IterationResult newData = asm.fit(image, pose, shape);
//				IterationResult newData = asm.performIteration(image, pose, shape);
				
				shape = newData.shape;
				pose = newData.pose;
				
				System.out.println(newData.fit);
			}
		});		
	}

	
//	/**
//	 * @param args
//	 * @throws IOException
//	 */
//	public static void main(String[] args) throws IOException {
//		final File dir = new File("/Users/jsh2/Work/lmlk/trunk/shared/JAAM-API/data/face-data");
//		List<PointList> pls = new ArrayList<PointList>();
//		final List<FImage> imgs = new ArrayList<FImage>();
//		for (File f : dir.listFiles()) {
//			if (f.getName().endsWith(".asf")) {
//				IndependentPair<PointList, FImage> data = readASF(f);
//				pls.add(data.firstObject());
//				imgs.add(data.secondObject());
//			}
//		}
//
//		final PointListConnections conns = readASFConnections(new File(dir, "01-1m.asf"));
//		
//		final List<PixelProfileModel> ppms = new ArrayList<PixelProfileModel>();
//		for (int j=0; j<pls.get(0).size(); j++) {
//			PixelProfileModel ppm = new PixelProfileModel(5);
//			ppms.add(ppm);
//			
//			for (int i=0; i<pls.size(); i++) {
//				ppm.addSample(imgs.get(i), conns.calculateNormalLine(j, pls.get(i), 4f));
//			}
//		}
//		
//		final PointDistributionModel pdm = new PointDistributionModel(new PointDistributionModel.EllipsoidConstraint(3.0), pls);
//		pdm.setNumComponents(15);
//
////		final VideoCapture vc = new VideoCapture(640, 480);
//		
//		VideoDisplay.createVideoDisplay(new AnimatedVideo<FImage>(new FImage(640,480), 300) {
//			Matrix pose = TransformUtilities.translateMatrix(300, 300).times(TransformUtilities.scaleMatrix(70, 70));
//			PointList model = pdm.getMean().transform(pose);
////			Matrix pose = Matrix.identity(3, 3);
////			PointList model = readASF(new File(dir, "01-1m.asf")).firstObject();
//			
//			FImage image = imgs.get(4);
//			
//			@Override
//			protected void updateNextFrame(FImage frame) {
////				if (this.getCurrentFrameIndex() % 30 == 0)
////					image = vc.getNextFrame().flatten();
//					
//				frame.drawImage(image, 0, 0);
//				frame.drawLines(conns.getLines(model), 1, 1f);
//				
//				for (Point2d pt : model) {
//					Line2d normal = conns.calculateNormalLine(pt, model, 8f);
//					if (normal != null) frame.drawLine(normal, 1, 0.5f);
//				}
//
//				PointList newModel = new PointList();
//				for (int i=0; i<ppms.size(); i++) {
//					Line2d testLine = conns.calculateNormalLine(i, model, 8f);
//					Point2dImpl newBest = ppms.get(i).computeNewBest(image, testLine, 9);
//					newModel.points.add(newBest);
//					frame.drawPoint(newBest, 1f, 1);
//				}
////				this.model = newModel;
//				IndependentPair<Matrix, double[]> newModelParams = pdm.fitModel(newModel);
//				pose = newModelParams.firstObject();
//				this.model = pdm.generateNewShape(newModelParams.secondObject()).transform(pose);
//				System.out.println("done");
//			}
//		});
//		
////		VideoDisplay.createVideoDisplay(new AnimatedVideo<FImage>(new FImage(200,200)) {
////			ValueAnimator<double[]> a = DoubleArrayValueAnimator.makeRandomLinear(60, pdm.getStandardDeviations(3));
////			
////			@Override
////			protected void updateNextFrame(FImage frame) {
////				frame.fill(0f);
////				
////				PointList newShape = pdm.generateNewShape( a.nextValue() );
////				PointList tfShape = newShape.transform(TransformUtilities.translateMatrix(100, 100).times(TransformUtilities.scaleMatrix(50, 50)));
////				
////				List<Line2d> lines = conns.getLines(tfShape);
////				frame.drawLines(lines, 1, 1f);
////				
////				for (Point2d pt : tfShape) {
////					Line2d normal = conns.calculateNormalLine(pt, tfShape, 10f);
////					
////					if (normal != null) {
////						frame.drawLine(normal, 1, 0.5f);
////					}
////				}
////			}
////		});		
//	}
}
