package org.openimaj.demos.ml.linear.data;


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
import org.openimaj.ml.linear.learner.perceptron.KernelPerceptron;
import org.openimaj.ml.linear.learner.perceptron.MarginMeanCenteredPerceptron;
import org.openimaj.ml.linear.learner.perceptron.MatrixKernelPerceptron;
import org.openimaj.ml.linear.learner.perceptron.MeanCenteredKernelPerceptron;
import org.openimaj.ml.linear.learner.perceptron.MeanCenteredProjectron;
import org.openimaj.ml.linear.learner.perceptron.PerceptronClass;
import org.openimaj.ml.linear.learner.perceptron.Projectron;
import org.openimaj.ml.linear.learner.perceptron.ThresholdMatrixKernelPerceptron;
import org.openimaj.util.pair.IndependentPair;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class DrawLinearData {
	
	private static final int SLEEP_TIME = 0;
	private static final int SEED = 1;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		drawPoints();
//		MatrixKernelPerceptron mkp = new MeanCenteredKernelPerceptron(new LinearVectorKernel());
		MatrixKernelPerceptron mkp = new MarginMeanCenteredPerceptron(new LinearVectorKernel(),10000d);
//		MatrixKernelPerceptron mkp = new MeanCenteredProjectron(new LinearVectorKernel());
//		MatrixKernelPerceptron mkp = new Projectron(new LinearVectorKernel());
//		MatrixKernelPerceptron mkp = new ThresholdMatrixKernelPerceptron(0.1, 0, new LinearVectorKernel());
//		MatrixKernelPerceptron mkp = new MatrixKernelPerceptron(new LinearVectorKernel());
		leanrnPoints(mkp);
//		leanrnPointsProjectron();
//		leanrnCogFound();
	}

	private static void leanrnCogFound() {
		MBFImage img = new MBFImage(300,300,ColourSpace.RGB);
		LinearPerceptronDataGenerator dg = new LinearPerceptronDataGenerator(300, 2, 0.3, SEED);
		gov.sandia.cognition.learning.algorithm.perceptron.kernel.KernelPerceptron<gov.sandia.cognition.math.matrix.Vector> mkp = new gov.sandia.cognition.learning.algorithm.perceptron.kernel.KernelPerceptron<gov.sandia.cognition.math.matrix.Vector>(new LinearKernel());
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

	private static void drawMkpLine(MatrixKernelPerceptron mkp) {
		MBFImage img = new MBFImage(300,300,ColourSpace.RGB);

		List<double[]> sup = mkp.getSupports();
		List<Double> weights = mkp.getWeights();
		double bias = mkp.getBias();
		System.out.println("Bias: " + bias);
		double[] startD = null;
		double[] endD = null;
		
		double[] mean = new double[2];
		if(mkp instanceof MeanCenteredKernelPerceptron){
			mean  = ((MeanCenteredKernelPerceptron)mkp).getMean();
		} else if (mkp instanceof MeanCenteredProjectron){
			mean = ((MeanCenteredProjectron)mkp).getMean();
		}
		startD=LinearVectorKernel.getPlanePoint(sup,weights,bias,-mean[0],Double.NaN);
		endD=LinearVectorKernel.getPlanePoint(sup,weights,bias,img.getWidth()-mean[0],Double.NaN);
		startD[0] += mean[0];
		startD[1] += mean[1];
		endD[0] += mean[0];
		endD[1] += mean[1];
		Point2d lineStart = new Point2dImpl((float)startD[0], (float)startD[1]);
		Point2d lineEnd = new Point2dImpl((float)endD[0], (float)endD[1]);
		
		Line2d line = new Line2d(lineStart, lineEnd);
		System.out.println("Drawing: " + line);
		img.drawLine(line, 3, RGBColour.GREEN);
//		img.drawPoint(new Point2dImpl((float)origin.get(0),(float) origin.get(1)), RGBColour.RED, 5);
		DisplayUtilities.displayName(img,"line");
	}

	private static void leanrnPoints(MatrixKernelPerceptron mkp ) {
		MBFImage img = new MBFImage(300,300,ColourSpace.RGB);
		LinearPerceptronDataGenerator dg = new LinearPerceptronDataGenerator(300, 2, 0.3, SEED);
		for (int i = 0; i < 50000; i++) {
			
			IndependentPair<double[], PerceptronClass> pointClass = dg.generate();
			
			double[] pc = pointClass.firstObject();
			Point2dImpl point = new Point2dImpl((float)pc[0], (float)pc[1]);
			PerceptronClass cls = pointClass.getSecondObject();
			int errorBefore = mkp.getErrors();
			mkp.process(pc, cls);
			if(errorBefore != mkp.getErrors()){
				System.out.println(i + " is misclassified!");
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
				drawMkpLine(mkp);
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {}
				System.out.println("Total mistakes: " + mkp.getErrors());
			}
		}
		System.out.println(mkp.getSupports().size());
		
		
	}

	private static void drawPoints() {
		MBFImage img = new MBFImage(300,300,ColourSpace.RGB);
		LinearPerceptronDataGenerator dg = new LinearPerceptronDataGenerator(300, 2, 0.3, SEED);
		dg.setError(0.05);
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
		Vector ret = origin.copy().add(10000, dir);
		return new Point2dImpl((float)ret.get(0),(float)ret.get(1));
	}

	private static Point2d start(Vector origin, Vector dir) {
		Vector ret = origin.copy().add(-10000, dir);
		return new Point2dImpl((float)ret.get(0),(float)ret.get(1));
	}

}
