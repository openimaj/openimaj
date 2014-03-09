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

import Jama.CholeskyDecomposition;
import Jama.Matrix;

/**
 * Cholesky Update and Downdate
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class UpdateableCholeskyDecomposition extends CholeskyDecomposition {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7456377954967521480L;

	/**
	 * @param m
	 */
	public UpdateableCholeskyDecomposition(Matrix m) {
		super(m);
	}

	
	/**
	 * @param x
	 */
	public void choldowndate(double[] x) {
		choldowndate(x,true);
	}
	
	/**
	 * see {@link UpdateableCholeskyDecomposition#choldowndate(double[][], double[])}
	 * @param x
	 * @param b
	 */
	public void choldowndate(double[] x, boolean b) {
		if(b)  x = x.clone();
		Matrix L = this.getL();
		// work is done on an upper triangular matrix
		double[][] data = L.transpose().getArray();
		choldowndate(data, x);
		// Make the output lower triangular again
		int Ll = L.getRowDimension();
		L.setMatrix(0, Ll-1, 0, Ll-1, new Matrix(data, Ll, Ll).transpose());
	}


	/**
	 * See {@link UpdateableCholeskyDecomposition#cholupdate(double[][], double[])}
	 * @param x
	 */
	public void cholupdate(double[] x){
		cholupdate(x,true);
	}
	
	/**
	 * See {@link #cholupdate(double[][], double[])}
	 * @param x
	 * @param copyX copy x, x is used as a workspace
	 */
	public void cholupdate(double[] x, boolean copyX){
		if(copyX){
			x = x.clone();
		}
		Matrix L = this.getL();
		// work is done on an upper triangular matrix
		double[][] data = L.transpose().getArray();
		cholupdate(data, x);
		// Make the output lower triangular again
		int Ll = L.getRowDimension();
		L.setMatrix(0, Ll-1, 0, Ll-1, new Matrix(data, Ll, Ll).transpose());
	}
	/**
	 * Updates L such that the matrix A where:
	 * 	A = LL*
	 * becomes
	 *  A = A + xx*
	 * @param Larr the upper triangular matrix to update
	 * @param x the vector to add
	 */
	public static void cholupdate(double[][] Larr,  double[] x){
//	    x = x';
		for (int k = 0; k < x.length; k++) {
	        double Lkk = Larr[k][k];
	        double xk = x[k];
			double r = Math.sqrt(Lkk*Lkk + xk*xk);
	        double c = r / Lkk;
	        double s = xk / Lkk;
	        Larr[k][k] = r;
	        updateL(k,Larr,x,s,c);
	        updateX(k,Larr,x,s,c);
		}
	}
	
	/**
	 * Updates L such that the matrix A where:
	 * 	A = LL*
	 * becomes
	 *  A = A - xx*
	 * @param Larr the upper triangular matrix to update
	 * @param x the vector to add
	 */
	public static void choldowndate(double[][] Larr,  double[] x){
//	    x = x';
		for (int k = 0; k < x.length; k++) {
	        double Lkk = Larr[k][k];
	        double xk = x[k];
			double r = Math.sqrt(Lkk*Lkk - xk*xk);
	        double c = r / Lkk;
	        double s = xk / Lkk;
	        Larr[k][k] = r;
	        downdateL(k,Larr,x,s,c);
	        updateX(k,Larr,x,s,c);
		}
	}
	
	/**
	 * x(k+1:p) = c*x(k+1:p) - s*L(k, k+1:p);
	 * 
	 * p = x.length
	 * 
	 * @param k
	 * @param Larr
	 * @param x
	 * @param s
	 * @param c
	 */
	private static void updateX(int k, double[][] Larr, double[] x, double s, double c) {
		for (int i = k+1; i < x.length; i++) {
			x[i] = c*x[i] - s * Larr[k][i]; 
		}
	}

	/**
	 * L(k,k+1:p) = (L(k,k+1:p) + s*x(k+1:p)) / c; 
	 * 
	 * p = x.length
	 * @param k
	 * @param Larr
	 * @param s
	 * @param x
	 * @param c
	 */
	private static void updateL(int k, double[][] Larr, double[] x, double s, double c) {
		for (int i = k+1; i < x.length; i++) {
			Larr[k][i] = (Larr[k][i] + s * x[i])/c; 
		}
	}
	
	/**
	 * L(k,k+1:p) = (L(k,k+1:p) - s*x(k+1:p)) / c; 
	 * 
	 * p = x.length
	 * @param k
	 * @param Larr
	 * @param s
	 * @param x
	 * @param c
	 */
	private static void downdateL(int k, double[][] Larr, double[] x, double s, double c) {
		for (int i = k+1; i < x.length; i++) {
			Larr[k][i] = (Larr[k][i] - s * x[i])/c; 
		}
	}
}
