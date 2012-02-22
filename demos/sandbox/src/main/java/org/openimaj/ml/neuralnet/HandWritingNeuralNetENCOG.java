package org.openimaj.ml.neuralnet;

import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectProcedure;
import gov.sandia.cognition.io.CSVUtility;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.encog.ml.data.MLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.util.simple.EncogUtility;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 * Just some experiments using the sandia cognitive foundary neural nets.
 *
 */
public class HandWritingNeuralNetENCOG {
	
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


	private int maxExamples = 400;
	private int maxTests = 10;
	private int nHiddenLayer = 20;

	private TIntIntHashMap examples;
	private TIntObjectHashMap<List<IndependentPair<double[],double[]>>> tests;

	

	private int totalTests = 0;

	private BasicNetwork network;

	private MLDataSet training;
	
	/**
	 * @throws IOException
	 * Load X input and y output from {@link #INPUT_LOCATION} and {@link #OUTPUT_LOCATION}
	 */
	public HandWritingNeuralNetENCOG() throws IOException {
		BufferedReader xReader = new BufferedReader(new InputStreamReader(HandWritingNeuralNetENCOG.class.getResourceAsStream(INPUT_LOCATION)));
		BufferedReader yReader = new BufferedReader(new InputStreamReader(HandWritingNeuralNetENCOG.class.getResourceAsStream(OUTPUT_LOCATION)));
		this.xVals = fromCSV(xReader,5000);
		this.yVals = fromCSV(yReader,5000);
		
		examples = new TIntIntHashMap();
		this.tests = new TIntObjectHashMap<List<IndependentPair<double[],double[]>>>();
		prepareDataCollection();
		learnNeuralNet();
		testNeuralNet();
//		new HandWritingInputDisplay(xVals);
	}
	
	private void testNeuralNet() {
		final double[][] xVals = new double[totalTests ][];
		final int[] yVals = new int[totalTests ];
		this.tests.forEachEntry(new TIntObjectProcedure<List<IndependentPair<double[],double[]>>>() {
			int done = 0;
			@Override
			public boolean execute(int number, List<IndependentPair<double[], double[]>> xypairs) {
				for (IndependentPair<double[], double[]> xyval: xypairs) {
					double[] guessed = null; // estimate
					int maxIndex = 0;
					double maxValue = 0;
					for (int i = 0; i < guessed.length; i++) {
						if(maxValue  < guessed[i])
						{
							maxValue = guessed[i];
							maxIndex = i;
						}
					}
					xVals[done] = xyval.firstObject();
					yVals[done] = maxIndex;
					done ++;
				}
				return true;
			}
		});
		new HandWritingInputDisplay(xVals, yVals);
	}

	private void prepareDataCollection() {
		double[][] xArr = this.xVals.getArray();
		double[][] yArr = this.yVals.getArray();
		
		for (int i = 0; i < xArr.length; i++) {
			double[] xVector = xArr[i];
			double[] yValues = new double[10];
			int number = (int) (yArr[i][0] % 10);
			int count = examples.adjustOrPutValue(number, 1, 1);
			yValues[number] = 1;
			double[] yVector = yValues;
			if(this.maxExamples != -1 && count >  maxExamples){
				if(count>maxTests+maxExamples){
					continue;
				}
				List<IndependentPair<double[] ,double[] >> numberTest = this.tests.get(number);
				if(numberTest == null){
					this.tests.put(number, numberTest = new ArrayList<IndependentPair<double[] ,double[] >>());
				}
				numberTest.add(IndependentPair.pair(xVector,yVector));
				totalTests  ++;
			}
			else{
				// Add the data to the collection for learning
			}
			
		}
	}

	private void learnNeuralNet() {
		this.network = EncogUtility.simpleFeedForward(400, 20, 0, 10, false);
		Backpropagation train = new Backpropagation(this.network, this.training);
	}


	private static <T> ArrayList<T> toArrayList(T[] values) {
		ArrayList<T> configList = new ArrayList<T>();
		for (T t : values) {
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
			if(outArr == null) {
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