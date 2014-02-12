/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
	
	/**
	 * @param args
	 */
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
//				System.out.println(mkp.getWeights());
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
//				System.out.println(mkp.getWeights());
			}
		}
	}

}
