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
package org.openimaj.math.matrix;

import java.io.PrintWriter;
import java.io.StringWriter;


import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * Miscellaneous matrix operations.
 * 
 * @author Sina Samangooei <ss@ecs.soton.ac.uk>
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class MatrixUtils {
	
	/**
	 * Are any values NaN or Inf?
	 * 
	 * @param matrix matrix to test
	 * @return true if any elements are NaN or Inf; false otherwise
	 */
	public static boolean anyNaNorInf(Matrix matrix) {
		for(double[] arrLine : matrix.getArray()){
			for(double d : arrLine){
				if(Double.isNaN(d) || Double.isInfinite(d)) return true;
			}
		}
		return false;
	}

	/**
	 * Get the maximum absolute value of the diagonal.
	 * 
	 * @param matrix the matrix
	 * @return the maximum absolute value
	 */
	public static double maxAbsDiag(Matrix matrix) {
		double max = -1;
		
		for(int i = 0 ; i < matrix.getColumnDimension(); i++){
			double curr = Math.abs(matrix.get(i, i));
			if (max < curr){
				max = curr;
			}
		}
		return max;
	}

	/**
	 * Get the minimum absolute value of the diagonal.
	 * 
	 * @param matrix the matrix
	 * @return the minimum absolute value
	 */
	public static double minAbsDiag(Matrix matrix) {
		double min = Double.MAX_VALUE;
		
		for(int i = 0 ; i < matrix.getColumnDimension(); i++){
			double curr = Math.abs(matrix.get(i, i));
			if (min > curr){
				min = curr;
			}
		}
		return min;
	}
	
	/**
	 * Compute the principle square root, X,
	 * of the matrix A such that A=X*X
	 *  
	 * @param matrix the matrix
	 * @return the sqrt of the matrix
	 */
	public static Matrix sqrt(Matrix matrix) {
		//A = V*D*V'
		EigenvalueDecomposition evd = matrix.eig();
		Matrix v = evd.getV();
		Matrix d = evd.getD();

		// sqrt of cells of D and store in-place
		for (int r = 0; r < d.getRowDimension(); r++)
			for (int c = 0; c < d.getColumnDimension(); c++)
				d.set(r, c, Math.sqrt(d.get(r, c)));
		
		//Y = V*D/V
		//Y = V'.solve(V*D)'
		Matrix a = v.inverse();
		Matrix b = v.times(d).inverse();
		return a.solve(b).inverse();
	}
	
	public static Matrix abs(Matrix mat) {
		Matrix copy = mat.copy();
		for(int i = 0; i < mat.getRowDimension(); i ++){
			for(int j = 0; j < mat.getColumnDimension(); j++){
				copy.set(i, j, Math.abs(mat.get(i, j)));
			}
		}
		return copy;
	}

	/**
	 * Check if two matrices are equal
	 * @param m1 first matrix
	 * @param m2 second matrix
	 * @param eps epsilon for checking values
	 * @return true if matrices have same size and all elements are equal within eps; false otherwise
	 */
	public static boolean equals(Matrix m1, Matrix m2, double eps) {
		double [][] a1 = m1.getArray();
		double [][] a2 = m2.getArray();
		
		if (a1.length != a2.length || a1[0].length != a2[0].length)
			return false;
		
		for (int r=0; r<a1.length; r++)
			for (int c=0; c<a1[r].length; c++)
				if (Math.abs(a1[r][c] - a2[r][c]) > eps) return false;
		
		return true;
	}

	public static Matrix pow(Matrix mat, double exp) {
		Matrix copy = mat.copy();
		for(int i = 0; i < mat.getRowDimension(); i ++){
			for(int j = 0; j < mat.getColumnDimension(); j++){
				copy.set(i, j, Math.pow(mat.get(i, j),exp));
			}
		}
		return copy;
	}

	public static String toString(Matrix mat) {
		StringWriter matWriter = new StringWriter();
		mat.print(new PrintWriter(matWriter), 5, 5);
		return matWriter.getBuffer().toString();
	}

	public static double sum(Matrix mat) {
		double sum = 0;
		for(int i = 0; i < mat.getRowDimension(); i ++){
			for(int j = 0; j < mat.getColumnDimension(); j++){
				sum += mat.get(i, j);
			}
		}
		return sum;
	}
	
	/**
	 * Zero the matrix 
	 * @param m the matrix
	 */
	public static void zero(Matrix m) {
		m.timesEquals(0);
	}
	
	
	public static EigenValueVectorPair symmetricEig2x2(Matrix m){
		double a = m.get(0, 0);
		double b = m.get(0, 1);
		double c = b;
		double d = m.get(1, 1);
		
		double trace = a + d;
		double det = a*d - b*c;
		
		Matrix val = new Matrix(2,2);
		double sqrtInner = (trace*trace/4) - det;
		// FIXME: make it deal with imaginary numbers.
		if(sqrtInner < 0){
			EigenvalueDecomposition e = m.eig();
			return new EigenValueVectorPair(e.getD(),e.getV());
		}
		
		sqrtInner = Math.sqrt(sqrtInner);
		double firstEig =  trace/2 + sqrtInner ;
		double secondEig = trace/2 - sqrtInner ;
		if(firstEig > secondEig){
			double tmp = firstEig;
			firstEig = secondEig;
			secondEig = tmp;
		}
		
		val.set(0, 0, firstEig);
		val.set(1, 1, secondEig);
		
		Matrix vec = new Matrix(2,2);
		
		double v1 = firstEig - a;
		double v2 = secondEig - a;
		double norm1 = Math.sqrt(v1*v1 + b*b);
		double norm2 = Math.sqrt(b*b + v2*v2);
		vec.set(0, 0, b/norm1);
		vec.set(0, 1, b/norm2);
		vec.set(1, 0, v1/norm1);
		vec.set(1, 1, v2/norm2);
		
		// To deal with rounding error
		vec.set(1,0,vec.get(0, 1));
		
		EigenValueVectorPair ret = new EigenValueVectorPair(val,vec);
		return ret;
	}
	
	/**
	 * An eigen decomposition that uses a deterministic method if the matrix is 2x2.
	 * 
	 * This function returns values as in {@link EigenvalueDecomposition} i.e. the largest eigen
	 * value is held in the [m.rows - 1,m.cols-1] (i.e. [1,1]) location
	 * 
	 * @param m
	 * @return
	 */
	public static EigenValueVectorPair eig2x2(Matrix m){
		if(m.getColumnDimension() != 2 || m.getRowDimension() !=2){
			EigenvalueDecomposition e = m.eig();
			return new EigenValueVectorPair(e.getD(),e.getV());
		}
		/**
		 * A = 1
		 * B = a + d
		 * C = ad - bc
		 * 
		 * x = ( - B (+/-) sqrt(B^2 - 4AC) )/ (2A) 
		 */
		double a = m.get(0, 0);
		double b = m.get(0, 1);
		double c = m.get(1, 0);
		double d = m.get(1, 1);
		
		double trace = a + d;
		double det = a*d - b*c;
		
		Matrix val = new Matrix(2,2);
		double sqrtInner = (trace*trace/4) - det;
		// FIXME: make it deal with imaginary numbers.
		if(sqrtInner < 0){
			EigenvalueDecomposition e = m.eig();
			return new EigenValueVectorPair(e.getD(),e.getV());
		}
		
		sqrtInner = Math.sqrt(sqrtInner);
		double firstEig =  trace/2 + sqrtInner ;
		double secondEig = trace/2 - sqrtInner ;
		if(firstEig > secondEig){
			double tmp = firstEig;
			firstEig = secondEig;
			secondEig = tmp;
		}
		
		val.set(0, 0, firstEig);
		val.set(1, 1, secondEig);
		
		Matrix vec = new Matrix(2,2);
		if(b == 0 && c == 0){
			vec.set(0, 0, 1);
			vec.set(1, 1, 1);
		}
		else
		{	
			if(c != 0){
				double v1 = firstEig - d;
				double v2 = secondEig - d;
				double norm1 = Math.sqrt(v1*v1 + c*c);
				double norm2 = Math.sqrt(c*c + v2*v2);
				vec.set(0, 0, v1/norm1);
				vec.set(0, 1, v2/norm2);
				vec.set(1, 0, c/norm1);
				vec.set(1, 1, c/norm2);
			}
			else if(b != 0){
				double v1 = firstEig - a;
				double v2 = secondEig - a;
				double norm1 = Math.sqrt(v1*v1 + b*b);
				double norm2 = Math.sqrt(b*b + v2*v2);
				vec.set(0, 0, b/norm1);
				vec.set(0, 1, b/norm2);
				vec.set(1, 0, v1/norm1);
				vec.set(1, 1, v2/norm2);
			}
		}
		
		EigenValueVectorPair ret = new EigenValueVectorPair(val,vec);
		return ret;
	}

	public static Matrix matrixFromFloat(float[][] pixels) {
		Matrix out = new Matrix(pixels.length,pixels[0].length);
		for(int i = 0; i < pixels.length;i ++){
			for(int j = 0; j < pixels[i].length; j++){
				out.set(j, i, pixels[i][j]);
			}
		}
		return out;
	}
}
