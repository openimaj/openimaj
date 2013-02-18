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
package org.openimaj.ml.neuralnet;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gov.sandia.cognition.algorithm.IterativeAlgorithm;
import gov.sandia.cognition.algorithm.IterativeAlgorithmListener;
import gov.sandia.cognition.io.CSVUtility;
import gov.sandia.cognition.learning.algorithm.gradient.GradientDescendable;
import gov.sandia.cognition.learning.algorithm.minimization.FunctionMinimizerLiuStorey;
import gov.sandia.cognition.learning.algorithm.regression.ParameterDifferentiableCostMinimizer;
import gov.sandia.cognition.learning.data.DefaultInputOutputPair;
import gov.sandia.cognition.learning.data.InputOutputPair;
import gov.sandia.cognition.learning.function.scalar.AtanFunction;
import gov.sandia.cognition.learning.function.vector.ThreeLayerFeedforwardNeuralNetwork;
import gov.sandia.cognition.math.DifferentiableUnivariateScalarFunction;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorEntry;
import gov.sandia.cognition.math.matrix.VectorFactory;
import gov.sandia.cognition.math.matrix.mtj.DenseVectorFactoryMTJ;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 *         Just some experiments using the sandia cognitive foundary neural
 *         nets.
 * 
 */
public class HandWritingNeuralNetSANDIA implements IterativeAlgorithmListener {

	/**
	 * Default location of inputs
	 */
	public static final String INPUT_LOCATION = "/org/openimaj/ml/handwriting/inputs.csv";

	/**
	 * Default location of outputs
	 */
	public static final String OUTPUT_LOCATION = "/org/openimaj/ml/handwriting/outputs.csv";

	private Matrix xVals;

	private Matrix yVals;

	private ArrayList<InputOutputPair<Vector, Vector>> dataCollection;

	private int maxExamples = 400;
	private int maxTests = 10;
	private int nHiddenLayer = 20;

	private TIntIntHashMap examples;
	private TIntObjectHashMap<List<IndependentPair<Vector, Vector>>> tests;

	private GradientDescendable neuralNet;

	private int totalTests = 0;

	/**
	 * @throws IOException
	 *             Load X input and y output from {@link #INPUT_LOCATION} and
	 *             {@link #OUTPUT_LOCATION}
	 */
	public HandWritingNeuralNetSANDIA() throws IOException {
		final BufferedReader xReader = new BufferedReader(new InputStreamReader(
				HandWritingNeuralNetSANDIA.class.getResourceAsStream(INPUT_LOCATION)));
		final BufferedReader yReader = new BufferedReader(new InputStreamReader(
				HandWritingNeuralNetSANDIA.class.getResourceAsStream(OUTPUT_LOCATION)));
		this.xVals = fromCSV(xReader, 5000);
		this.yVals = fromCSV(yReader, 5000);

		examples = new TIntIntHashMap();
		this.tests = new TIntObjectHashMap<List<IndependentPair<Vector, Vector>>>();
		prepareDataCollection();
		learnNeuralNet();
		testNeuralNet();
		// new HandWritingInputDisplay(xVals);
	}

	private void testNeuralNet() {
		final double[][] xVals = new double[totalTests][];
		final int[] yVals = new int[totalTests];
		this.tests.forEachEntry(new TIntObjectProcedure<List<IndependentPair<Vector, Vector>>>() {
			int done = 0;
			DenseVectorFactoryMTJ fact = new DenseVectorFactoryMTJ();

			@Override
			public boolean execute(int number, List<IndependentPair<Vector, Vector>> xypairs) {
				for (final IndependentPair<Vector, Vector> xyval : xypairs) {
					final Vector guessed = neuralNet.evaluate(xyval.firstObject());
					int maxIndex = 0;
					double maxValue = 0;
					for (final VectorEntry vectorEntry : guessed) {
						if (maxValue < vectorEntry.getValue())
						{
							maxValue = vectorEntry.getValue();
							maxIndex = vectorEntry.getIndex();
						}
					}
					xVals[done] = fact.copyVector(xyval.firstObject()).getArray();
					yVals[done] = maxIndex;
					done++;
				}
				return true;
			}
		});
		new HandWritingInputDisplay(xVals, yVals);
	}

	private void prepareDataCollection() {
		this.dataCollection = new ArrayList<InputOutputPair<Vector, Vector>>();
		final double[][] xArr = this.xVals.getArray();
		final double[][] yArr = this.yVals.getArray();

		for (int i = 0; i < xArr.length; i++) {
			final Vector xVector = VectorFactory.getDefault().copyArray(xArr[i]);
			final double[] yValues = new double[10];
			final int number = (int) (yArr[i][0] % 10);
			final int count = examples.adjustOrPutValue(number, 1, 1);
			yValues[number] = 1;
			final Vector yVector = VectorFactory.getDefault().copyValues(yValues);
			if (this.maxExamples != -1 && count > maxExamples) {
				if (count > maxTests + maxExamples) {
					continue;
				}
				List<IndependentPair<Vector, Vector>> numberTest = this.tests.get(number);
				if (numberTest == null) {
					this.tests.put(number, numberTest = new ArrayList<IndependentPair<Vector, Vector>>());
				}
				numberTest.add(IndependentPair.pair(xVector, yVector));
				totalTests++;
			}
			else {
				this.dataCollection.add(DefaultInputOutputPair.create(xVector, yVector));
			}

		}
	}

	private void learnNeuralNet() {
		// ArrayList<Integer> nodesPerLayer = toArrayList(
		// new
		// Integer[]{this.xVals.getColumnDimension(),this.xVals.getColumnDimension()/4,10}
		// );
		// ArrayList<DifferentiableUnivariateScalarFunction> squashFunctions =
		// toArrayList(
		// new DifferentiableUnivariateScalarFunction[]{new
		// SigmoidFunction(),new SigmoidFunction()}
		// );
		final ArrayList<Integer> nodesPerLayer = toArrayList(
				new Integer[] { this.xVals.getColumnDimension(), nHiddenLayer, 10 }
				);
		final ArrayList<DifferentiableUnivariateScalarFunction> squashFunctions = toArrayList(
				new DifferentiableUnivariateScalarFunction[] { new AtanFunction(), new AtanFunction() }
				);
		// DifferentiableFeedforwardNeuralNetwork nn = new
		// DifferentiableFeedforwardNeuralNetwork(
		// nodesPerLayer,
		// squashFunctions,
		// new Random()
		// );
		final ThreeLayerFeedforwardNeuralNetwork nn = new ThreeLayerFeedforwardNeuralNetwork(
				this.xVals.getColumnDimension(), nHiddenLayer, 10);
		final ParameterDifferentiableCostMinimizer conjugateGradient = new ParameterDifferentiableCostMinimizer(
				new FunctionMinimizerLiuStorey());
		conjugateGradient.setObjectToOptimize(nn);
		// conjugateGradient.setCostFunction( new MeanSquaredErrorCostFunction()
		// );
		conjugateGradient.addIterativeAlgorithmListener(this);
		conjugateGradient.setMaxIterations(50);
		// FletcherXuHybridEstimation minimiser = new
		// FletcherXuHybridEstimation();
		// minimiser.setObjectToOptimize( nn );
		// minimiser.setMaxIterations(50);
		neuralNet = conjugateGradient.learn(this.dataCollection);
	}

	private static <T> ArrayList<T> toArrayList(T[] values) {
		final ArrayList<T> configList = new ArrayList<T>();
		for (final T t : values) {
			configList.add(t);
		}
		return configList;
	}

	private Matrix fromCSV(BufferedReader bufferedReader, int nLines) throws IOException {

		String[] lineValues = null;
		double[][] outArr = null;
		Matrix retMat = null;
		int row = 0;
		while ((lineValues = CSVUtility.nextNonEmptyLine(bufferedReader)) != null) {
			if (outArr == null) {
				retMat = new Matrix(nLines, lineValues.length);
				outArr = retMat.getArray();
			}

			for (int col = 0; col < lineValues.length; col++) {
				outArr[row][col] = Double.parseDouble(lineValues[col]);
			}
			row++;
		}
		return retMat;
	}

	public static void main(String[] args) throws IOException {
		new HandWritingNeuralNetSANDIA();
	}

	@Override
	public void algorithmStarted(IterativeAlgorithm algorithm) {
		System.out.println("Learning neural network");
	}

	@Override
	public void algorithmEnded(IterativeAlgorithm algorithm) {
		System.out.println("Done Learning!");
	}

	@Override
	public void stepStarted(IterativeAlgorithm algorithm) {
		System.out.println("... starting step: " + algorithm.getIteration());
	}

	@Override
	public void stepEnded(IterativeAlgorithm algorithm) {
		System.out.println("... ending step: " + algorithm.getIteration());
	}
}
