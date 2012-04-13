package org.openimaj.ml.regression;

import java.util.Arrays;
import java.util.List;

import no.uib.cipr.matrix.NotConvergedException;

import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.math.model.Model;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;


/**
 * Given a set of independant variables a linear regressions finds the optimal vector B such that:
 * (Y - Xb)^2 = 0
 * (Y - Xb)^{T}(Y-Xb) = 0
 * 
 * calculated by assuming a convex shape of (Y - Xb) with varying values of b (reasonable as the function is linear)
 * and then calculating the point at which the first derivative of this function is 0. i.e.:
 * 
 * d/db (y - Xb)^{T} (y - Xb) 
 * = -X^{T}(y - Xb) - X^{T}(y - Xb)
 * = - 2 * X^{T}(y - Xb)
 * 
 * which at the 0 is:
 * - 2 * X^{T}(y - Xb) = 0
 *  X^{T}(y - Xb)
 *  X^{T}y - X^{T}Xb = 0
 *  X^{T}y = X^{T}Xb 
 *  b = (X^{T} X)^{-1} X^{T} y
 *  
 *  Calculating this function directly behaves numerically badly when X is extremely skinny and tall (i.e. lots of data, fewer dimentions)
 *  so we calculate this using the SVD, using the SVD we can decompose X as:
 *  
 *  X = UDV^{T}
 *  
 *  s.t. U and V are orthonormal
 *  from this we can calculate:
 *  b = V D^{-1} U^{T} y
 *  
 *  which is equivilant but more numerically stable.
 *  
 *  Note that upon input any vector of independent variables x_n are automatically to turned into an n + 1 vector {1,x0,x1,...,xn}
 *  which handles the constant values added to y
 * @author ss
 *
 */
public class LinearRegression implements Model<double[], double[]>{

	/**
	 * the default error which a point may be from the line to be considered acceptable
	 */
	public static final double DEFAULT_ERROR = 5d;
	private Matrix weights;
	private double error;
	
	/**
	 * linear regression model validated on DEFAULT_ERROR
	 */
	public LinearRegression() {
		this(DEFAULT_ERROR);
	}

	/**
	 * the error to validate against
	 * @param error
	 */
	public LinearRegression(double error) {
		this.error = error;
	}

	@Override
	public void estimate(List<? extends IndependentPair<double[], double[]>> data) {
		if(data.size() == 0) return;
		
		int correctedx = data.get(0).firstObject().length + 1;
		int correctedy = data.get(0).secondObject().length ;
		double[][] y = new double[data.size()][correctedy];
		double[][] x = new double[data.size()][correctedx];
		
		int i = 0;
		for (IndependentPair<double[],double[]> item : data) {
			y[i] = item.secondObject();
			x[i][0] = 1;
			System.arraycopy(item.firstObject(), 0, x[i], 1, item.firstObject().length);
			i+=1;
		}
		
		estimate_internal(new Matrix(y),new Matrix(x));
	}
	
	/**
	 * As in {@link #estimate(List)} but using double arrays for efficiency.
	 * @param yd
	 * @param xd
	 */
	public void estimate(double[][] yd, double[][] xd){
		double[][] x = appendConstant(xd);
		estimate_internal(new Matrix(yd),new Matrix(x));
	}
	
	private double[][] appendConstant(double[][] xd) {
		int corrected = xd[0].length + 1;
		double[][] x = new double[xd.length][corrected];
		
		for (int i = 0; i < xd.length; i++) {
			x[i][0] = 1;
			System.arraycopy(xd[i], 0, x[i], 1, xd[i].length);
		}
		return x;
	}

	/**
	 * As in {@link #estimate(List)} but using double arrays for efficiency.
	 * Estimates:
	 * b = V D^{-1} U^{T} y
	 * s.t. 
	 * X = UDV^{T}
	 * 
	 * @param y
	 * @param x
	 */
	public void estimate(Matrix y, Matrix x){
		estimate(y.getArray(),x.getArray());
	}
	private void estimate_internal(Matrix y, Matrix x){
		try {
			no.uib.cipr.matrix.DenseMatrix mjtX = new no.uib.cipr.matrix.DenseMatrix(x.getArray());
			no.uib.cipr.matrix.SVD svd;
			svd = no.uib.cipr.matrix.SVD.factorize(mjtX);
			Matrix u = MatrixUtils.convert(svd.getU(),svd.getU().numRows(),svd.getS().length);
			Matrix v = MatrixUtils.convert(svd.getVt(),svd.getS().length,svd.getVt().numColumns()).transpose();
			Matrix d = MatrixUtils.diag(svd.getS());
			
			weights = v.times(MatrixUtils.pinv(d)).times(u.transpose()).times(y);
		} catch (NotConvergedException e) {
			throw new RuntimeException(e.getMessage());
		}
		
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean validate(IndependentPair<double[], double[]> data) {
		double diff = calculateError(Arrays.asList(data));
		return diff <= error;
	}

	@Override
	public double[] predict(double[] data) {
		double[][] corrected = new double[][]{new double[data.length + 1]};
		corrected[0][0] = 1;
		System.arraycopy(data, 0, corrected[0], 1, data.length);
		Matrix x = new Matrix(corrected);
		
		return x.times(this.weights).transpose().getArray()[0];
	}
	
	/**
	 * Helper function which adds the constant component to x and returns 
	 * predicted values for y, one per row
	 * @param x
	 * @return predicted y
	 */
	public Matrix predict(Matrix x) {
		x = new Matrix(appendConstant(x.getArray()));
		return x.times(this.weights);
	}
	

	@Override
	public int numItemsToEstimate() {
		return 2;
	}

	@Override
	public double calculateError(List<? extends IndependentPair<double[], double[]>> data) {
		double SSE = 0;
		for (IndependentPair<double[], double[]> independentPair : data) {
			double[] predicted = predict(independentPair.firstObject());
			double[] actual = independentPair.secondObject();
			for (int i = 0; i < predicted.length; i++) {
				double diff = predicted[i] - actual[i];
				SSE += diff * diff;
			}
		}
		return SSE;
	}	
	
	@Override
	public LinearRegression clone() {
		return new LinearRegression(this.error);
	}
	
	@Override
	public boolean equals(Object obj) {
		if((!(obj instanceof LinearRegression))) return false;
		LinearRegression that  = (LinearRegression) obj;
		double[][] thatw = that.weights.getArray();
		double[][] thisw = this.weights.getArray();
		for (int i = 0; i < thisw.length; i++) {
			if(!Arrays.equals(thatw[i], thisw[i]))return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "LinearRegression with coefficients: " + Arrays.toString(this.weights.transpose().getArray()[0]);
	}
	
}
