package org.openimaj.demos.sandbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.content.animation.AnimatedVideo;
import org.openimaj.content.animation.animator.DoubleArrayValueAnimator;
import org.openimaj.content.animation.animator.ValueAnimator;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.shape.PointListConnections;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.video.VideoDisplay;

public class PDMPlayground {
	static PointList readASF(File file) throws IOException {
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

		return pl;
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
		List<PointList> pls = new ArrayList<PointList>();
		for (File f : dir.listFiles()) {
			if (f.getName().endsWith(".asf")) {
				pls.add(readASF(f));
			}
		}

		final PointListConnections conns = readASFConnections(new File(dir, "01-1m.asf"));
		
		final PointDistributionModel pdm = new PointDistributionModel(pls);

		FImage img = new FImage(200,200);
		img.drawPoints(pdm.getMean().transform(TransformUtilities.translateMatrix(100, 100).times(TransformUtilities.scaleMatrix(50, 50))), 1f, 1);
		DisplayUtilities.display(img);

		pdm.setNumComponents(20);

		VideoDisplay.createVideoDisplay(new AnimatedVideo<FImage>(new FImage(200,200)) {
			ValueAnimator<double[]> a = DoubleArrayValueAnimator.makeRandomLinear(60, pdm.getStandardDeviations(3));
			
			@Override
			protected void updateNextFrame(FImage frame) {
				frame.fill(0f);
				
				PointList newShape = pdm.generateNewShape( a.nextValue() );
				PointList tfShape = newShape.transform(TransformUtilities.translateMatrix(100, 100).times(TransformUtilities.scaleMatrix(50, 50)));
				
				List<Line2d> lines = conns.getLines(tfShape);
				frame.drawLines(lines, 1, 1f);
				
				for (Point2d pt : tfShape) {
					Point2d normal = conns.calculateNormal(pt, tfShape);
					
					if (normal != null) {
						normal.setX((normal.getX() * 50) + pt.getX());
						normal.setY((normal.getY() * 50) + pt.getY());
						
						frame.drawLine(new Line2d(pt, normal), 1, 1f);
					}
				}
			}
		});		
	}
}
