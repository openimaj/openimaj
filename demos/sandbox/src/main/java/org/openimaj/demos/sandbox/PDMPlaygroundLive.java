package org.openimaj.demos.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.demos.sandbox.asm.AMPTSDataset;
import org.openimaj.demos.sandbox.asm.ASFDataset;
import org.openimaj.demos.sandbox.asm.ActiveShapeModel.IterationResult;
import org.openimaj.demos.sandbox.asm.MultiResolutionActiveShapeModel;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.shape.PointListConnections;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

import Jama.Matrix;

public class PDMPlaygroundLive {
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
//		ASFDataset dataset = new ASFDataset(new File("/Users/jsh2/Work/lmlk/trunk/shared/JAAM-API/data/face-data"));
//		AMPTSDataset dataset = new AMPTSDataset(
//				new File("/Users/jsh2/Downloads/am_tools/points"), 
//				new File("/Users/jsh2/Downloads/am_tools/images"),
//				new File("/Users/jsh2/Downloads/am_tools/models/face.parts"));
		ASFDataset dataset = new ASFDataset(new File("/Users/jsh2/Downloads/imm_face_db"));
				
		final List<IndependentPair<PointList, FImage>> data = dataset.getData();
		final PointListConnections connections = dataset.getConnections();
		
		final float scale = 0.04f;
		final MultiResolutionActiveShapeModel asm = MultiResolutionActiveShapeModel.trainModel(4, 2, 4, scale, 12, connections, data, new PointDistributionModel.BoxConstraint(3));

		VideoDisplay.createVideoDisplay(new VideoCapture(320, 240))
		.addVideoListener(new VideoDisplayListener<MBFImage>() {

			HaarCascadeDetector detector = new HaarCascadeDetector(80);

			@Override
			public void beforeUpdate(MBFImage frame) {
				FImage image = frame.flatten();
				List<DetectedFace> faces = detector.detectFaces(image);

				if (faces == null || faces.size() == 0) return;

				for (DetectedFace face : faces) {
					frame.drawShape(face.getBounds(), RGBColour.GREEN);

					Point2d cog = face.getBounds().getCOG();
					double facescale = (double)face.getBounds().height / 4;

					Matrix pose = TransformUtilities.translateMatrix(cog.getX(), cog.getY()).times(TransformUtilities.scaleMatrix(facescale, facescale));
					PointList shape = asm.getPDM().getMean().transform(pose);

					long t1 = System.currentTimeMillis();
					IterationResult newData = asm.fit(image, pose, shape);
					long t2 = System.currentTimeMillis();

					shape = newData.shape;
					pose = newData.pose;

					frame.drawLines(connections.getLines(shape), 1, RGBColour.RED);

					float shapeScale = shape.computeIntrinsicScale();
					for (Point2d pt : shape) {
						Line2d normal = connections.calculateNormalLine(pt, shape, scale * shapeScale);
						if (normal != null) frame.drawLine(normal, 1, RGBColour.BLUE);
					}

					System.out.println(newData.fit);
					System.out.println(t2 - t1);
				}
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {}
		});
	}
}
