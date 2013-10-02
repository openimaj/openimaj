package ch.akuhn.matrix.eigenvalues;

import ch.akuhn.matrix.Matrix;
import ch.akuhn.matrix.Vector;

/**
 * The eigen-decomposition of a matrix.
 * 
 * @author Adrian Kuhn
 * 
 */
public class Eigenvalues {

	/**
	 * The eigenvalues
	 */
	public double[] value;

	/**
	 * The eigenvectors
	 */
	public Vector[] vector;

	protected int n;
	protected int nev;

	/**
	 * Construct with the given dimensions
	 * 
	 * @param n
	 */
	public Eigenvalues(int n) {
		this.n = n;
		this.nev = n;
	}

	/**
	 * Get an object that can compute the eigendecomposition of the given
	 * matrix. If the matrix has fewer than 10 columns, this will be an
	 * {@link AllEigenvalues}, otherwise it will be a {@link FewEigenvalues}.
	 * 
	 * @param A
	 * @return the object to compute the eigen decomposition
	 */
	public static Eigenvalues of(Matrix A) {
		if (A.columnCount() == 0) {
			final Eigenvalues eigen = new Eigenvalues(0);
			eigen.value = new double[0];
			eigen.vector = new Vector[0];
			return eigen;
		}
		if (A.columnCount() == 1) {
			final Eigenvalues eigen = new Eigenvalues(0);
			eigen.value = new double[] { A.get(0, 0) };
			eigen.vector = new Vector[] { Vector.from(1.0) };
			return eigen;
		}
		if (A.columnCount() < 10)
			return new AllEigenvalues(A);
		return FewEigenvalues.of(A);
	}

	/**
	 * Configure to compute the largest <code>nev</code> values/vectors.
	 * 
	 * @param nev
	 * @return this
	 */
	public Eigenvalues largest(int nev) {
		this.nev = nev;
		return this;
	}

	/**
	 * Run the decomposition algorithm. Subclasses should override as necessary.
	 * 
	 * @return this
	 */
	public Eigenvalues run() {
		return this;
	}

	/**
	 * @return the total number of possible eigen vectors (i.e. the number of
	 *         rows of the input)
	 */
	public int getN() {
		return n;
	}
}
