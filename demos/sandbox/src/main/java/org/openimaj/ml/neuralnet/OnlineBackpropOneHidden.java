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

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixFactory;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.mtj.DenseMatrixFactoryMTJ;

import org.openimaj.data.RandomData;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.colour.ColourMap;
import org.openimaj.util.function.Function;


/**
 * Implement an online version of the backprop algorithm against an 2D 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class OnlineBackpropOneHidden {
	

	private static final double LEARNRATE = 0.005;
	private Matrix weightsL1;
	private Matrix weightsL2;
	MatrixFactory<? extends Matrix> DMF = DenseMatrixFactoryMTJ.getDenseDefault();
	/**
	 * @param nInput the number of input values
	 * @param nHidden the number of hidden values
	 * @param nFinal the number of final values
	 */
	private Function<Double, Double> g;
	private Function<Matrix, Matrix> gMat;
	private Function<Double, Double> gPrime;
	private Function<Matrix, Matrix> gPrimeMat;
	public OnlineBackpropOneHidden(int nInput, int nHidden, int nFinal) {
		double[][] weightsL1dat = RandomData.getRandomDoubleArray(nInput+1,nHidden, -1, 1.);
		double[][] weightsL2dat = RandomData.getRandomDoubleArray(nHidden+1,nFinal , -1, 1.);
		
		
		weightsL1 = DMF.copyArray(weightsL1dat);
		weightsL2 = DMF.copyArray(weightsL2dat);;
		
		g = new Function<Double,Double>(){

			@Override
			public Double apply(Double in) {
				
				return 1. / (1 + Math.exp(-in));
			}
			
		};
		
		gPrime = new Function<Double,Double>(){

			@Override
			public Double apply(Double in) {
				
				return g.apply(in) * (1 - g.apply(in));
			}
			
		};
		
		gPrimeMat = new Function<Matrix,Matrix>(){

			@Override
			public Matrix apply(Matrix in) {
				Matrix out = DMF.copyMatrix(in);
				for (int i = 0; i < in.getNumRows(); i++) {
					for (int j = 0; j < in.getNumColumns(); j++) {
						out.setElement(i, j, gPrime.apply(in.getElement(i, j)));
					}
				}
				return out;
			}
			
		};
		
		gMat = new Function<Matrix,Matrix>(){

			@Override
			public Matrix apply(Matrix in) {
				Matrix out = DMF.copyMatrix(in);
				for (int i = 0; i < in.getNumRows(); i++) {
					for (int j = 0; j < in.getNumColumns(); j++) {
						out.setElement(i, j, g.apply(in.getElement(i, j)));
					}
				}
				return out;
			}
			
		};
	}
	
	public void update(double[] x, double[] y){
		Matrix X = prepareMatrix(x);
		Matrix Y = DMF.copyArray(new double[][]{y});
		
		Matrix hiddenOutput = weightsL1.transpose().times(X); // nHiddenLayers x nInputs (usually 2 x 1)
		Matrix gHiddenOutput = prepareMatrix(gMat.apply(hiddenOutput).getColumn(0)); // nHiddenLayers + 1 x nInputs (usually 3x1)
		Matrix gPrimeHiddenOutput = prepareMatrix(gPrimeMat.apply(hiddenOutput).getColumn(0)); // nHiddenLayers + 1 x nInputs (usually 3x1)
		Matrix finalOutput = weightsL2.transpose().times(gHiddenOutput);
		Matrix finalOutputGPrime = gPrimeMat.apply(finalOutput); // nFinalLayers x nInputs (usually 1x1)
		
		Matrix errmat = Y.minus(finalOutput);
		double err = errmat.sumOfColumns().sum();
		
		Matrix dL2 = finalOutputGPrime.times(gHiddenOutput.transpose()).scale(err * LEARNRATE).transpose(); // should be nHiddenLayers + 1 x nInputs (3 x 1)
		Matrix dL1 = finalOutputGPrime.times(weightsL2.transpose().times(gPrimeHiddenOutput).times(X.transpose())).scale(err * LEARNRATE).transpose();
		
		dL1 = repmat(dL1,1,weightsL1.getNumColumns());
		dL2 = repmat(dL2,1,weightsL2.getNumColumns());
		
		this.weightsL1.plusEquals(dL1);
		this.weightsL2.plusEquals(dL2);
		
	}
	
	private Matrix repmat(Matrix dL1, int nRows, int nCols) {
		Matrix out = DMF.createMatrix(nRows * dL1.getNumRows(), nCols * dL1.getNumColumns());
		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				out.setSubMatrix(i * dL1.getNumRows(), j * dL1.getNumColumns(), dL1);
			}
		}
		return out;
	}

	public Matrix predict(double[] x){
		Matrix X = prepareMatrix(x);
		
		Matrix hiddenTimes = weightsL1.transpose().times(X);
		Matrix hiddenVal = prepareMatrix(gMat.apply(hiddenTimes).getColumn(0));
		Matrix finalTimes = weightsL2.transpose().times(hiddenVal);
		Matrix finalVal = gMat.apply(finalTimes);
		
		return finalVal;
		
	}
	
	
	private Matrix prepareMatrix(Vector y) {
		Matrix Y = DMF.createMatrix(1, y.getDimensionality() + 1);
		Y.setElement(0, 0, 1);
		Y.setSubMatrix(0, 1, DMF.copyRowVectors(y));
		return Y.transpose();
	}

	private Matrix prepareMatrix(double[] y) {
		Matrix Y = DMF.createMatrix(1, y.length + 1);
		Y.setElement(0, 0, 1);
		Y.setSubMatrix(0, 1, DMF.copyArray(new double[][]{y}));
		return Y.transpose();
	}
	
	public static void main(String[] args) throws InterruptedException {
		OnlineBackpropOneHidden bp = new OnlineBackpropOneHidden(2, 2, 1);
		FImage img = new FImage(200,200);
		img = imagePredict(bp,img);
		ColourMap m = ColourMap.Hot;
		
		DisplayUtilities.displayName(m.apply(img), "xor");
		int npixels = img.width*img.height;
		int half = img.width/2;
		int[] pixels = RandomData.getUniqueRandomInts(npixels, 0, npixels);
		while(true){
//			for (int i = 0; i < pixels.length; i++) {
//				int pixel = pixels[i];
//				int y = pixel / img.width;
//				int x = pixel - (y * img.width);
//				bp.update(new double[]{x < half ? -1 : 1,y < half ? -1 : 1},new double[]{xorValue(half,x,y)});
////				Thread.sleep(5);
//			}
			bp.update(new double[]{0,0},new double[]{0});
			bp.update(new double[]{1,1},new double[]{0});
			bp.update(new double[]{0,1},new double[]{1});
			bp.update(new double[]{1,0},new double[]{1});
			imagePredict(bp, img);
			DisplayUtilities.displayName(m.apply(img),"xor");
		}
	}

	private static FImage imagePredict(OnlineBackpropOneHidden bp, FImage img) {
		double[] pos = new double[2];
		int half = img.width/2;
		for (int y = 0; y < img.height; y++) {
			for (int x = 0; x < img.width; x++) {
				pos[0] = x < half ? 0 : 1;
				pos[1] = y < half ? 0 : 1;
				float ret = (float) bp.predict(pos).getElement(0, 0);
				img.pixels[y][x] = ret;
			}
		}
		return img;
	}
}
