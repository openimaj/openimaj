package ch.akuhn.matrix.eigenvalues;

import java.util.Random;

import ch.akuhn.matrix.Matrix;
import ch.akuhn.matrix.SparseMatrix;
import ch.akuhn.matrix.Vector;

/**
 * Compute the Singular Value Decomposition.
 * 
 * @author Adrian Kuhn
 */
public class SingularValues {

	/**
	 * Singular values
	 */
	public double[] value;

	/**
	 * Left singular vectors
	 */
	public Vector[] vectorLeft;

	/**
	 * Right singular vectors
	 */
	public Vector[] vectorRight;

	Matrix A;
	private int nev;

	/**
	 * Construct with the given matrix and required number of S.V.s
	 * 
	 * @param A
	 * @param nev
	 */
	public SingularValues(Matrix A, int nev) {
		this.A = A;
		this.nev = nev;
	}

	/**
	 * Perform the decomposition
	 * 
	 * @return this
	 */
	public SingularValues decompose() {
		final Eigenvalues eigen = new FewEigenvalues(A.rowCount()) {
			@Override
			protected Vector callback(Vector v) {
				return A.mult(A.transposeMultiply(v));
			}
		}.greatest(nev).run();

		nev = eigen.nev;

		value = new double[nev];
		vectorLeft = eigen.vector;
		vectorRight = new Vector[nev];
		for (int i = 0; i < nev; i++) {
			value[i] = Math.sqrt(eigen.value[i]);
			vectorRight[i] = A.transposeMultiply(vectorLeft[i]);
			vectorRight[i].timesEquals(1.0 / vectorRight[i].norm());
		}
		return this;
	}

	/**
	 * Sample main
	 * 
	 * @param args
	 *            ignored
	 */
	public static void main(String... args) {
		final SparseMatrix A = Matrix.sparse(400, 5000);
		final Random rand = new Random(1);
		for (int i = 0; i < A.rowCount(); i++) {
			for (int j = 0; j < A.columnCount(); j++) {
				if (rand.nextDouble() > 0.2)
					continue;
				A.put(i, j, rand.nextDouble() * 23);
			}
		}

		final SingularValues singular = new SingularValues(A, 10).decompose();
		System.out.println(singular.value);
	}
}
