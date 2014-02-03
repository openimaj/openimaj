package org.openimaj.demos.ml.linear.data;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.ml.linear.data.FixedDataGenerator;
import org.openimaj.ml.linear.kernel.LinearVectorKernel;
import org.openimaj.ml.linear.learner.perceptron.MatrixKernelPerceptron;
import org.openimaj.ml.linear.learner.perceptron.MeanCenteredKernelPerceptron;
import org.openimaj.ml.linear.learner.perceptron.PerceptronClass;
import org.openimaj.ml.linear.learner.perceptron.ThresholdMatrixKernelPerceptron;
import org.openimaj.util.pair.IndependentPair;

import cern.colt.Arrays;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class WikipediaPerceptronExample {
	
	public static void main(String[] args) {
		thresholded(createData());
		centered(createData());
	}

	private static void centered(FixedDataGenerator<double[], PerceptronClass> fdg) {
		System.out.println("CENTERED");
		MatrixKernelPerceptron mkp = new MeanCenteredKernelPerceptron(new LinearVectorKernel());
		for (int i = 0; i < 10; i++) {
			System.out.println("Iteration: " + i);
			for (int j = 0; j < 4; j++) {
				IndependentPair<double[], PerceptronClass> v = fdg.generate();
				double[] x = v.firstObject();
				PerceptronClass y = v.secondObject();
				PerceptronClass yestb = mkp.predict(x);
				mkp.process(x, y);
				PerceptronClass yesta = mkp.predict(x);
				
				System.out.println(String.format("x: %s, y: %s, ypred_b: %s, ypred_a: %s",Arrays.toString(x), y, yestb,yesta));
				System.out.println(mkp.getWeights());
			}
		}
	}

	private static FixedDataGenerator<double[], PerceptronClass> createData() {
		
		List<IndependentPair<double[], PerceptronClass>> data = new ArrayList<IndependentPair<double[],PerceptronClass>>();
		data.add(IndependentPair.pair(new double[]{1,0,0},PerceptronClass.TRUE));
		data.add(IndependentPair.pair(new double[]{1,0,1},PerceptronClass.TRUE));
		data.add(IndependentPair.pair(new double[]{1,1,0},PerceptronClass.TRUE));
		data.add(IndependentPair.pair(new double[]{1,1,1},PerceptronClass.FALSE));
		FixedDataGenerator<double[], PerceptronClass> fdg = new FixedDataGenerator<double[], PerceptronClass>(data);
		return fdg;
	}

	private static void thresholded(
			FixedDataGenerator<double[], PerceptronClass> fdg) {
		System.out.println("Thresholded");
		MatrixKernelPerceptron mkp = new ThresholdMatrixKernelPerceptron(new LinearVectorKernel());
		for (int i = 0; i < 10; i++) {
			System.out.println("Iteration: " + i);
			for (int j = 0; j < 4; j++) {
				IndependentPair<double[], PerceptronClass> v = fdg.generate();
				double[] x = v.firstObject();
				PerceptronClass y = v.secondObject();
				PerceptronClass yestb = mkp.predict(x);
				mkp.process(x, y);
				PerceptronClass yesta = mkp.predict(x);
				
				System.out.println(String.format("x: %s, y: %s, ypred_b: %s, ypred_a: %s",Arrays.toString(x), y, yestb,yesta));
				System.out.println(mkp.getWeights());
			}
		}
	}

}
