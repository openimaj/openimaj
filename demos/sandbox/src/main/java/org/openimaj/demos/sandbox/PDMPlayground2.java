package org.openimaj.demos.sandbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.content.animation.AnimatedVideo;
import org.openimaj.content.animation.animator.DoubleArrayValueAnimator;
import org.openimaj.content.animation.animator.ForwardBackwardLoopingValueAnimator;
import org.openimaj.content.animation.animator.LinearDoubleValueAnimator;
import org.openimaj.content.animation.animator.LoopingValueAnimator;
import org.openimaj.content.animation.animator.RandomLinearDoubleValueAnimator;
import org.openimaj.content.animation.animator.ValueAnimator;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.shape.algorithm.GeneralisedProcrustesAnalysis;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.video.VideoDisplay;

public class PDMPlayground2 {
	static List<PointList> readShapes() throws IOException {
		File f = new File("/Users/jsh2/Downloads/all/shapes/shapes.txt");
		int pts = 56; 
		int shapes = 40;
		
		List<PointList> allShapes = new ArrayList<PointList>();
		for (int i=0; i<shapes; i++) allShapes.add(new PointList());
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		String[] data = new String[pts*2];
		for (int i=0; i<pts*2; i++) {
			data[i] = br.readLine();
		}
		br.close();
		
		for (int i=0; i<pts; i++) {
			String xline = data[i];
			String yline = data[i + pts];
			
			String[] xparts = xline.split("\\s+");
			String[] yparts = yline.split("\\s+");
			
			for (int j=0; j<shapes; j++) {
				float x = Float.parseFloat(xparts[j]);
				float y = Float.parseFloat(yparts[j]);
				
				allShapes.get(j).points.add(new Point2dImpl(x, y));
			}
		}
		
		return allShapes;
	}
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		List<PointList> pls = readShapes();
		
		final PointDistributionModel pdm = new PointDistributionModel(pls);

		FImage img = new FImage(200,200);
		img.drawPoints(pdm.getMean().transform(TransformUtilities.translateMatrix(100, 100).times(TransformUtilities.scaleMatrix(50, 50))), 1f, 1);
		DisplayUtilities.display(img);

		pdm.setNumComponents(1);

		VideoDisplay.createVideoDisplay(new AnimatedVideo<FImage>(new FImage(200,200)) {
//			ValueAnimator<double[]> a = DoubleArrayValueAnimator.makeRandomLinear(60, pdm.getBasisRanges(3));
			ValueAnimator<Double> a = LoopingValueAnimator.loop(new LinearDoubleValueAnimator(-pdm.getStandardDeviations(3)[0], pdm.getStandardDeviations(4)[0], 60));
			
			@Override
			protected void updateNextFrame(FImage frame) {
				frame.fill(0f);
				
				PointList newShape = pdm.generateNewShape( new double[] {a.nextValue()} );
				frame.drawPoints(newShape.transform(TransformUtilities.translateMatrix(100, 100).times(TransformUtilities.scaleMatrix(50, 50))), 1f, 1);
			}
		});		
	}
}
