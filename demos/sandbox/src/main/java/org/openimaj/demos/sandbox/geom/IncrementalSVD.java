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
package org.openimaj.demos.sandbox.geom;

import org.openimaj.data.RandomData;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.math.matrix.ThinSingularValueDecomposition;

import Jama.Matrix;
import Jama.QRDecomposition;

public class IncrementalSVD{
	private static final double DEFAULT_DAMPENING = 1.f;
	private int updateK;
	private Matrix Rworkspace;
	public IncrementalSVD(int updateK) {
		this.updateK = updateK;
		this.Rworkspace = new Matrix(updateK*2, updateK*2);
	}

	Matrix U;
	double[] S;
	Matrix Sdiag;
	private double defaultDamp = DEFAULT_DAMPENING ;
	/**
	 * @param m new data to update the SVD with
	 */
	public void update(Matrix m){
		update(m,this.defaultDamp);
	}
	/**
	 * @param m new data to update the SVD with
	 * @param dampening each current eigen vector is weighted by this amount
	 */
	public void update(Matrix m, double dampening){
		Matrix d = Matrix.identity(this.updateK, this.updateK);
		update(m,d.timesEquals(dampening));
	}
	/**
	 * @param m
	 * @param dampening multiplied by the current eigen values (see the paper)
	 */
	public void update(Matrix m, Matrix dampening){
		ThinSingularValueDecomposition inputSVD = new ThinSingularValueDecomposition(m, updateK);
		Matrix U2 = inputSVD.U;
		double[] S2 = inputSVD.S;
		if(S2.length < updateK){
			double[] rep = new double[updateK];
			for (int i = 0; i < S2.length; i++) {
				rep[i] = S2[i];
			}
			S2 = rep;
		}
		Matrix S2diag = MatrixUtils.diag(S2);
		if(U == null){
			U = U2;
			S = S2;
			Sdiag = S2diag;
			return;
		}

		Matrix Z = U.transpose().times(U2);
		Matrix U1U2Delta = U2.minus(U.times(Z));
		QRDecomposition qr = new QRDecomposition(U1U2Delta);
		Matrix R = qr.getR();
		Matrix Uprime = qr.getQ();
		this.Rworkspace.setMatrix(0,this.updateK-1, 0,this.updateK-1, dampening.times(Sdiag)); // top left
		this.Rworkspace.setMatrix(0,this.updateK-1, this.updateK,this.updateK*2 -1, Z.times(S2diag)); // top right
		this.Rworkspace.setMatrix(this.updateK,this.updateK*2 -1, this.updateK,this.updateK*2 -1, R.times(S2diag)); // bottom right
		// this.Rworkspace bottom left = 0

		ThinSingularValueDecomposition rWorkspaceSVD = new ThinSingularValueDecomposition(this.Rworkspace, updateK);
		S = rWorkspaceSVD.S;
		Sdiag = MatrixUtils.diag(S);
		Matrix UR = rWorkspaceSVD.U;

		Matrix topUR = UR.getMatrix(0,this.updateK-1,0,this.updateK-1);
		Matrix bottomUR = UR.getMatrix(this.updateK,this.updateK*2-1,0,this.updateK-1);
		U = U.times(topUR);
		U.plus(Uprime.times(bottomUR));
	}

	public static void main(String[] args) {
		IncrementalSVD incSVD = new IncrementalSVD(3);

		Matrix A = new Matrix(RandomData.getRandomDoubleArray(3, 8, 0d, 1d,1));
		System.out.println("This is A: ");
		A.print(8, 8);
		Matrix A1 = A.getMatrix(0, 2, 0, 3);
		Matrix A2 = A.getMatrix(0, 2, 4, 6);
		Matrix A3 = A.getMatrix(0, 2, 7, 7);
		System.out.println("This is A1: ");
		A1.print(8, 8);
		System.out.println("This is A2: ");
		A2.print(8, 8);
		System.out.println("This is A3: ");
		A3.print(8, 8);

		incSVD.update(A1);
		System.out.println("From IncSVD (A1): ");
		incSVD.U.print(5, 5);
		incSVD.Sdiag.print(5, 5);

		System.out.println("From Thinsvd: ");
		ThinSingularValueDecomposition A1SVD = new ThinSingularValueDecomposition(A1, incSVD.updateK);
		A1SVD.U.print(5, 5);
		MatrixUtils.diag(A1SVD.S).print(5, 5);

		incSVD.update(A2);
		System.out.println("From IncSVD (A1,A2): ");
		incSVD.U.print(5, 5);
		incSVD.Sdiag.print(5, 5);
		System.out.println("From Thinsvd: ");
		ThinSingularValueDecomposition A1A2SVD = new ThinSingularValueDecomposition(MatrixUtils.hstack(A1,A2), incSVD.updateK);
		A1A2SVD.U.print(5, 5);
		MatrixUtils.diag(A1A2SVD.S).print(5, 5);

		incSVD.update(A3);
		System.out.println("From IncSVD (A1,A2,A3): ");
		incSVD.U.print(5, 5);
		incSVD.Sdiag.print(5, 5);
		System.out.println("From Thinsvd: ");
		ThinSingularValueDecomposition ASVD = new ThinSingularValueDecomposition(A, incSVD.updateK);
		ASVD.U.print(5, 5);
		MatrixUtils.diag(ASVD.S).print(5, 5);



	}

	public void setDefaultWeighting(double d) {
		this.defaultDamp = d;
	}
}
