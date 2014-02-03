package org.openimaj.demos.ml.linear.data;

import gov.sandia.cognition.learning.algorithm.perceptron.kernel.KernelPerceptron;
import gov.sandia.cognition.learning.data.DefaultInputOutputPair;
import gov.sandia.cognition.learning.data.InputOutputPair;
import gov.sandia.cognition.learning.function.kernel.LinearKernel;
import gov.sandia.cognition.math.matrix.VectorFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.ml.linear.data.LinearPerceptronDataGenerator;
import org.openimaj.ml.linear.kernel.LinearVectorKernel;
import org.openimaj.ml.linear.learner.perceptron.MatrixKernelPerceptron;
import org.openimaj.ml.linear.learner.perceptron.PerceptronClass;
import org.openimaj.util.pair.DoubleObjectPair;
import org.openimaj.util.pair.IndependentPair;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class DrawLinearData {
	
	private static final int SEED = 1;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		drawPoints();
//		leanrnPoints();
//		leanrnCogFound();
	}

	private static void leanrnCogFound() {
		MBFImage img = new MBFImage(300,300,ColourSpace.RGB);
		LinearPerceptronDataGenerator dg = new LinearPerceptronDataGenerator(300, 2, 0.3, SEED);
		KernelPerceptron<gov.sandia.cognition.math.matrix.Vector> mkp = new KernelPerceptron<gov.sandia.cognition.math.matrix.Vector>(new LinearKernel());
		mkp.learn(createData());
		System.out.println(mkp.getErrorCount());
		
	}

	private static Collection<? extends InputOutputPair<? extends gov.sandia.cognition.math.matrix.Vector, Boolean>> createData() {
		List<InputOutputPair<gov.sandia.cognition.math.matrix.Vector, Boolean>> ret = new ArrayList<InputOutputPair<gov.sandia.cognition.math.matrix.Vector, Boolean>>();
		LinearPerceptronDataGenerator dg = new LinearPerceptronDataGenerator(300, 2, 0.3, SEED);
		for (int i = 0; i < 50000; i++) {
			IndependentPair<double[], PerceptronClass> pointClass = dg.generate();
			double[] pc = pointClass.firstObject();
			PerceptronClass pcc = pointClass.secondObject();
			boolean bool = pcc.equals(PerceptronClass.TRUE);
			gov.sandia.cognition.math.matrix.Vector vec = VectorFactory.getDenseDefault().copyArray(pc);
			InputOutputPair<gov.sandia.cognition.math.matrix.Vector, Boolean> item = DefaultInputOutputPair.create(vec, bool) ;
			ret.add(item);
		}
		System.out.println("Data created");
		return ret;
	}

	private static void leanrnPoints() {
		MBFImage img = new MBFImage(300,300,ColourSpace.RGB);
		LinearPerceptronDataGenerator dg = new LinearPerceptronDataGenerator(300, 2, 0.3, SEED);
//		MatrixKernelPerceptron mkp = new MeanCenteredKernelPerceptron(new LinearVectorKernel());
//		MatrixKernelPerceptron mkp = new PlusOneMatrixKernelPerceptron(new LinearVectorKernel());
		MatrixKernelPerceptron mkp = new MatrixKernelPerceptron(new LinearVectorKernel());
		for (int i = 0; i < 50000; i++) {
			
			IndependentPair<double[], PerceptronClass> pointClass = dg.generate();
			
			double[] pc = pointClass.firstObject();
			Point2dImpl point = new Point2dImpl((float)pc[0], (float)pc[1]);
			PerceptronClass cls = pointClass.getSecondObject();
			PerceptronClass before = mkp.predict(pc);
			mkp.process(pc, cls);
			PerceptronClass after = mkp.predict(pc);
			if(before!=after){
				System.out.println(i + " is misclassified!");
				drawSupportLine(mkp.getSupports(),img);
				switch (cls) {
				case TRUE:
					img.drawShapeFilled(new Circle(point, 5), RGBColour.GREEN);
					break;
				case FALSE:
					img.drawShape(new Circle(point, 5),3, RGBColour.RED);
					break;
				case NONE:
					throw new RuntimeException("NOPE");
				}
				DisplayUtilities.displayName(img,"supports");
//				try {
//					Thread.sleep(500);
//				} catch (InterruptedException e) {}
			}
		}
		System.out.println(mkp.getSupports().size());
		
		
	}

	private static void drawSupportLine(List<DoubleObjectPair<double[]>> supports, MBFImage img) {
		Vector mid = new DenseVector(2);
		for (DoubleObjectPair<double[]> dop : supports) {
			mid.add(new DenseVector(dop.second,false));
		}
		
		mid.scale(1f/supports.size());
//		img.drawPoint(new Point2dImpl((float)mid.get(0),(float)mid.get(1)), RGBColour.WHITE, 20);
		
	}

	private static void drawPoints() {
		MBFImage img = new MBFImage(300,300,ColourSpace.RGB);
		LinearPerceptronDataGenerator dg = new LinearPerceptronDataGenerator(300, 2, 0.3);
		Vector origin = dg.getOrigin();
		Vector dir = dg.getPlane()[0];
		Point2d lineStart = start(origin,dir);
		Point2d lineEnd = end(origin,dir);
		Line2d line = new Line2d(lineStart, lineEnd);
		
		img.drawLine(line, 3, RGBColour.BLUE);
		
		
		for (int i = 0; i < 100; i++) {
			IndependentPair<double[], PerceptronClass> pointClass = dg.generate();
			
			double[] pc = pointClass.firstObject();
			Point2dImpl point = new Point2dImpl((float)pc[0], (float)pc[1]);
			PerceptronClass cls = pointClass.getSecondObject();
			switch (cls) {
			case TRUE:
				img.drawShapeFilled(new Circle(point, 5), RGBColour.GREEN);
				break;
			case FALSE:
				img.drawShape(new Circle(point, 5),3, RGBColour.RED);
				break;
			case NONE:
				throw new RuntimeException("NOPE");
			}
		}
		DisplayUtilities.displayName(img,"random");
	}

	private static Point2d end(Vector origin, Vector dir) {
		Vector ret = origin.copy().add(1000, dir);
		return new Point2dImpl((float)ret.get(0),(float)ret.get(1));
	}

	private static Point2d start(Vector origin, Vector dir) {
		Vector ret = origin.copy().add(-1000, dir);
		return new Point2dImpl((float)ret.get(0),(float)ret.get(1));
	}

}
