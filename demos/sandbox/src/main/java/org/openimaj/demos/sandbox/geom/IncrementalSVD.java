package org.openimaj.demos.sandbox.geom;

import org.openimaj.data.RandomData;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.math.matrix.ThinSingularValueDecomposition;

import Jama.Matrix;
import Jama.QRDecomposition;

public class IncrementalSVD{
	private int updateK;
	private Matrix Rworkspace;
	public IncrementalSVD(int updateK) {
		this.updateK = updateK;
		this.Rworkspace = new Matrix(updateK*2, updateK*2);
	}

	Matrix U;
	double[] S;
	Matrix Sdiag;
	/**
	 *
	 * @param m
	 */
	public void update(Matrix m){
		ThinSingularValueDecomposition inputSVD = new ThinSingularValueDecomposition(m, updateK);
		Matrix U2 = inputSVD.U;
		double[] S2 = inputSVD.S;
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
		this.Rworkspace.setMatrix(0,this.updateK-1, 0,this.updateK-1, Sdiag); // top left
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
		Matrix A1 = A.getMatrix(0, 2, 0, 3);
		Matrix A2 = A.getMatrix(0, 2, 4, 7);

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
		ThinSingularValueDecomposition ASVD = new ThinSingularValueDecomposition(A, incSVD.updateK);
		ASVD.U.print(5, 5);
		MatrixUtils.diag(ASVD.S).print(5, 5);
	}
}
