package org.openimaj.demos.sandbox;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.openimaj.demos.sandbox.asm.ActiveShapeModel.IterationResult;
import org.openimaj.demos.sandbox.asm.MultiResolutionActiveShapeModel;
import org.openimaj.demos.sandbox.asm.landmark.NormalLandmarkModel;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.sampling.FLineSampler;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.shape.PointListConnections;
import org.openimaj.math.geometry.shape.Triangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.matrix.algorithm.pca.PrincipalComponentAnalysis.NumberComponentSelector;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

public class ASMTester {
	public static List<IndependentPair<PointList, FImage>> generateData(int number) {
		List<IndependentPair<PointList, FImage>> data = new ArrayList<IndependentPair<PointList, FImage>>(number);
		
		Random rnd = new Random();
		
		for (int i = 0; i < number; i++) {
			float c1 = rnd.nextFloat();
			float c2 = rnd.nextFloat();
			
			while (Math.abs(c1 - c2) < 0.1) {
				c2 = rnd.nextFloat();
			}
			
			Triangle t = new Triangle(
				new Point2dImpl(((float)i / (number-1))*80 + 10f, 10),
				new Point2dImpl(90, 90),
				new Point2dImpl(10, 90)
			);
			
			FImage img = new FImage(100, 100);
			img.fill(Math.min(c1, c2));
			img.drawShapeFilled(t, Math.max(c1, c2));
			
			data.add( new IndependentPair<PointList, FImage>( t.asPolygon(), img ) );
		}
		
		return data;
	}
	
	public static void main(String[] args) {
		List<IndependentPair<PointList, FImage>> data = generateData(10);
		PointListConnections connections = new PointListConnections();
		connections.addConnection(0, 1);
		connections.addConnection(1, 2);
		connections.addConnection(2, 0);
		
		final float scale = 0.4f;
		NormalLandmarkModel.Factory factory = new NormalLandmarkModel.Factory(connections, FLineSampler.INTERPOLATED_DERIVATIVE, 5, 13, scale);
		final MultiResolutionActiveShapeModel asm = MultiResolutionActiveShapeModel.trainModel(1, new NumberComponentSelector(10), data, new PointDistributionModel.BoxConstraint(3), factory);
		
		for (IndependentPair<PointList, FImage> inst : generateData(10)) {
			MBFImage rgb = inst.secondObject().toRGB();
			
//			Matrix pose = TransformUtilities.translateMatrix(50, 50).times(TransformUtilities.scaleMatrix(50, 50));
//			PointList shape = asm.getPDM().getMean().transform(pose);
//			Matrix pose = Matrix.identity(3, 3);
			Matrix pose = TransformUtilities.scaleMatrixAboutPoint(0.8, 0.8, 50, 50);
			PointList shape = inst.firstObject().transform(pose);
			rgb.drawLines(connections.getLines(shape), 1, RGBColour.RED);
			
			IterationResult res = asm.fit(inst.secondObject(), shape);
			
			rgb.drawLines(connections.getLines(res.shape), 1, RGBColour.GREEN);
			
			DisplayUtilities.display(rgb);
		}
	}
}
