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
import gov.sandia.cognition.io.CSVUtility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.encog.engine.network.activation.ActivationStep;
import org.encog.mathutil.rbf.RBFEnum;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.specific.CSVNeuralDataSet;
import org.encog.ml.svm.SVM;
import org.encog.ml.svm.training.SVMTrain;
import org.encog.ml.train.MLTrain;
import org.encog.neural.cpn.CPN;
import org.encog.neural.cpn.training.TrainInstar;
import org.encog.neural.cpn.training.TrainOutstar;
import org.encog.neural.data.basic.BasicNeuralData;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATTraining;
import org.encog.neural.networks.training.CalculateScore;
import org.encog.neural.networks.training.TrainingSetScore;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.neural.rbf.RBFNetwork;
import org.encog.util.simple.EncogUtility;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 *         Just some experiments using the sandia cognitive foundary neural
 *         nets.
 * 
 */
public class HandWritingNeuralNetENCOG {

	/**
	 * Default location of inputs
	 */
	public static final String INPUT_LOCATION = "/org/openimaj/ml/handwriting/inputouput.csv";

	private int maxTests = 10;

	private TIntIntHashMap examples;
	private TIntObjectHashMap<List<IndependentPair<double[], double[]>>> tests;

	private int totalTests = 0;

	private MLRegression network;

	private MLDataSet training;

	/**
	 * @throws IOException
	 *             Load X input and y output from {@link #INPUT_LOCATION}
	 */
	public HandWritingNeuralNetENCOG() throws IOException {

		examples = new TIntIntHashMap();
		this.tests = new TIntObjectHashMap<List<IndependentPair<double[], double[]>>>();
		prepareDataCollection();
		learnNeuralNet();
		testNeuralNet();
		// new HandWritingInputDisplay(this.training);
	}

	private void testNeuralNet() {
		final double[][] xVals = new double[totalTests][];
		final int[] yVals = new int[totalTests];
		this.tests.forEachEntry(new TIntObjectProcedure<List<IndependentPair<double[], double[]>>>() {
			int done = 0;

			@Override
			public boolean execute(int number, List<IndependentPair<double[], double[]>> xypairs) {
				for (final IndependentPair<double[], double[]> xyval : xypairs) {
					final double[] guessed = network.compute(new BasicNeuralData(xyval.firstObject())).getData(); // estimate
					int maxIndex = 0;
					double maxValue = 0;
					for (int i = 0; i < guessed.length; i++) {
						if (maxValue < guessed[i])
						{
							maxValue = guessed[i];
							maxIndex = i;
						}
					}
					xVals[done] = xyval.firstObject();
					yVals[done] = (maxIndex + 1) % 10;
					done++;
				}
				return true;
			}
		});
		new HandWritingInputDisplay(xVals, yVals);
	}

	private void prepareDataCollection() throws IOException {
		final File tmp = File.createTempFile("data", ".csv");
		final InputStream stream = HandWritingNeuralNetENCOG.class.getResourceAsStream(INPUT_LOCATION);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line = null;
		final PrintWriter writer = new PrintWriter(new FileWriter(tmp));
		while ((line = reader.readLine()) != null) {
			writer.println(line);
		}
		writer.close();
		reader.close();
		training = new CSVNeuralDataSet(tmp.getAbsolutePath(), 400, 10, false);
		final Iterator<MLDataPair> elementItr = this.training.iterator();
		for (; elementItr.hasNext();) {
			final MLDataPair type = elementItr.next();
			final double[] yData = type.getIdealArray();
			final double[] xData = type.getInputArray();
			int yIndex = 0;
			while (yData[yIndex] != 1)
				yIndex++;
			final int currentCount = this.examples.adjustOrPutValue(yIndex, 1, 1);
			if (currentCount < this.maxTests) {

				List<IndependentPair<double[], double[]>> numberTest = this.tests.get(yIndex);
				if (numberTest == null) {
					this.tests.put(yIndex, numberTest = new ArrayList<IndependentPair<double[], double[]>>());
				}
				numberTest.add(IndependentPair.pair(xData, yData));
				totalTests++;
			}
		}

	}

	private void learnNeuralNet() {
		// this.network = EncogUtility.simpleFeedForward(400, 100, 0, 10,
		// false);
		// MLTrain train = new Backpropagation(this.network, this.training);
		// MLTrain train = new ResilientPropagation(this.network,
		// this.training);

		// this.network = withNEAT();
		// this.network = withRBF();
		// this.network = withSVM();
		this.network = withResilieant();
		// this.network = withCPN();
	}

	private MLRegression withNEAT() {
		final NEATPopulation pop = new NEATPopulation(400, 10, 1000);
		final CalculateScore score = new TrainingSetScore(this.training);
		// train the neural network
		final ActivationStep step = new ActivationStep();
		step.setCenter(0.5);
		pop.setOutputActivationFunction(step);
		final MLTrain train = new NEATTraining(score, pop);
		EncogUtility.trainToError(train, 0.01515);
		return (MLRegression) train.getMethod();
	}

	private MLRegression withResilieant() {
		final MLTrain train = new ResilientPropagation(EncogUtility.simpleFeedForward(400, 100, 0, 10, false),
				this.training);
		EncogUtility.trainToError(train, 0.01515);
		return (MLRegression) train.getMethod();
	}

	private MLRegression withSVM() {
		final MLTrain train = new SVMTrain(new SVM(400, true), this.training);
		EncogUtility.trainToError(train, 0.01515);
		return (MLRegression) train.getMethod();
	}

	private MLRegression withRBF() {
		final MLRegression train = new RBFNetwork(400, 20, 10, RBFEnum.Gaussian);
		EncogUtility.trainToError(train, this.training, 0.01515);
		return train;
	}

	private MLRegression withCPN() {
		final CPN result = new CPN(400, 1000, 10, 1);
		final MLTrain trainInstar = new TrainInstar(result, training, 0.1, false);
		EncogUtility.trainToError(trainInstar, 0.01515);
		final MLTrain trainOutstar = new TrainOutstar(result, training, 0.1);
		EncogUtility.trainToError(trainOutstar, 0.01515);
		return result;
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
		new HandWritingNeuralNetENCOG();
	}
}
