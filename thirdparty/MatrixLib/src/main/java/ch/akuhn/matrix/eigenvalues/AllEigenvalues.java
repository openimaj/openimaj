package ch.akuhn.matrix.eigenvalues;

import java.util.Arrays;

import org.netlib.lapack.LAPACK;
import org.netlib.util.intW;

import ch.akuhn.matrix.Matrix;
import ch.akuhn.matrix.Vector;

/**
 * Finds all eigenvalues of a matrix.
 * <P>
 * Computes for an <code>n</code>&times;<code>n</code> real nonsymmetric matrix
 * <code>A</code>, the eigenvalues (&lambda;) and, optionally, the left and/or
 * right eigenvectors. The computed eigenvectors are normalized to have
 * Euclidean norm equal to 1 and largest component real.
 * <P>
 * 
 * @author Adrian Kuhn
 * 
 * @see "http://www.netlib.org/lapack/double/dgeev.f"
 * 
 */
public class AllEigenvalues extends Eigenvalues {

	private LAPACK lapack = LAPACK.getInstance();

	private boolean l = true;
	private boolean r = false;

	/**
	 * Construct with the given matrix
	 * 
	 * @param A
	 */
	public AllEigenvalues(Matrix A) {
		super(A.columnCount());
		assert A.isSquare();
		this.A = A;
	}

	private Matrix A;

	@Override
	public AllEigenvalues run() {
		final double[] wr = new double[n];
		final double[] wi = new double[n];
		final intW info = new intW(0);
		final double[] a = A.asColumnMajorArray();
		final double[] vl = new double[l ? n * n : 0];
		final double[] vr = new double[r ? n * n : 0];
		final double[] work = allocateWorkspace();
		lapack.dgeev(
				jobv(l),
				jobv(r),
				n,
				a, // overwritten on output!
				n,
				wr, // output: real eigenvalues
				wi, // output: imaginary eigenvalues
				vl, // output:: left eigenvectors
				n,
				vr, // output:: right eigenvectors
				n,
				work,
				work.length,
				info);
		if (info.val != 0)
			throw new Error("dgeev ERRNO=" + info.val);
		postprocess(wr, vl);
		return this;
	}

	/**
	 * <PRE>
	 * [wr,vl.enum_cons(n)]
	 *  .transpose
	 *  .sort_by(&:first)
	 *  .tranpose
	 *  .revert
	 * </PRE>
	 * 
	 */
	private void postprocess(double[] wr, double[] vl) {
		class Eigen implements Comparable<Eigen> {
			double value0;
			Vector vector0;

			@Override
			public int compareTo(Eigen eigen) {
				return Double.compare(value0, eigen.value0);
			}
		}
		final Eigen[] eigen = new Eigen[n];
		for (int i = 0; i < n; i++) {
			eigen[i] = new Eigen();
			eigen[i].value0 = wr[i];
			eigen[i].vector0 = Vector.copy(vl, i * n, n);
		}
		Arrays.sort(eigen);
		value = new double[nev];
		vector = new Vector[nev];
		for (int i = 0; i < nev; i++) {
			value[i] = eigen[n - nev + i].value0;
			vector[i] = eigen[n - nev + i].vector0;
		}
	}

	private String jobv(boolean canHasVectors) {
		return canHasVectors ? "V" : "N";
	}

	/**
	 * If LWORK = -1, then a workspace query is assumed; the routine only
	 * calculates the optimal size of the WORK array, returns this value as the
	 * first entry of the WORK array.
	 * 
	 */
	private double[] allocateWorkspace() {
		int lwork = ((l || r) ? 4 : 3) * n;
		final double[] query = new double[1];
		final intW info = new intW(0);
		lapack.dgeev(
				jobv(l),
				jobv(r),
				n,
				null,
				n,
				null,
				null,
				null,
				n,
				null,
				n,
				query,
				-1,
				info);
		if (info.val == 0)
			lwork = (int) query[0];
		return new double[lwork];
	}

}
