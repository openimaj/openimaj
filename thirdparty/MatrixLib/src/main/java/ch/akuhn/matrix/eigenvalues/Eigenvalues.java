package ch.akuhn.matrix.eigenvalues;

import ch.akuhn.matrix.Matrix;
import ch.akuhn.matrix.Vector;

public class Eigenvalues {

	public double[] value;
	public Vector[] vector;
	
	protected int n;
	protected int nev;
	
	public Eigenvalues(int n) {
		this.n = n;
		this.nev = n;
	}

	public static Eigenvalues of(Matrix A) {
		if (A.columnCount() == 0) {
			Eigenvalues eigen = new Eigenvalues(0);
			eigen.value = new double[0];
			eigen.vector = new Vector[0];
			return eigen;
		}
		if (A.columnCount() == 1) {
			Eigenvalues eigen = new Eigenvalues(0);
			eigen.value = new double[] { A.get(0, 0) };
			eigen.vector = new Vector[] { Vector.from(1.0) };
			return eigen;
		}
		if (A.columnCount() < 10) return new AllEigenvalues(A);
		return FewEigenvalues.of(A);
	}

	public Eigenvalues largest(int nev) {
		this.nev = nev;
		return this;
	}
	
	public Eigenvalues run() {
		return this;
	}
	
	
	/**
	 * @return the total number of possible eigen vectors (i.e. the number of rows of the input)
	 */
	public int getN(){
		return n;
	}
}
