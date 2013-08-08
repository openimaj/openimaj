package org.openimaj.ml.clustering.spectral;


import java.util.Iterator;

import org.openimaj.math.matrix.DiagonalMatrix;
import org.openimaj.math.matrix.MatlibMatrixUtils;
import org.openimaj.ml.clustering.spectral.FBEigenIterator.Mode;
import org.openimaj.util.pair.DoubleObjectPair;

import ch.akuhn.matrix.SparseMatrix;
import ch.akuhn.matrix.Vector;
import ch.akuhn.matrix.eigenvalues.Eigenvalues;

/**
 * Functions which turn a graph weight adjacency matrix into the Laplacian
 * matrix.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class GraphLaplacian{


	/**
	 * @param adj the adjanceny matrix should be square and symmetric
	 * @return the laplacian
	 */
	public SparseMatrix laplacian(SparseMatrix adj){
		DiagonalMatrix degree = new DiagonalMatrix(adj.rowCount());

		int i = 0;
		for (Vector row : adj.rows()) {
			row.put(i, 1); 
			degree.put(i, i, row.sum());
			i++;
		}

		return laplacian(adj,degree);
	}

	/**
	 * @param adj square and symmetric
	 * @param degree the sum of the adjacency for a node in the diagonals
	 * @return the laplacian
	 */
	public abstract SparseMatrix laplacian(SparseMatrix adj, DiagonalMatrix degree);

	/**
	 * @param evd
	 * @return provides an iterator over the (presumeably sorted)
	 */
	public abstract Iterator<DoubleObjectPair<Vector>> eigenIterator(Eigenvalues evd);

	/**
	 * The symmetric normalised Laplacian is defined as:
	 * L = I - D^-1/2 A D^-1/2
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class Symmetric extends GraphLaplacian{
		@Override
		public SparseMatrix laplacian(SparseMatrix adj, DiagonalMatrix degree) {
			DiagonalMatrix invSqrtDegree = MatlibMatrixUtils.powInplace(degree,-1./2.);
			DiagonalMatrix ident = DiagonalMatrix.ones(degree.rowCount());
			SparseMatrix ret = MatlibMatrixUtils.minusInplace(
				ident,
				MatlibMatrixUtils.times(
					MatlibMatrixUtils.times(
						invSqrtDegree, adj
					),
					invSqrtDegree
				)
			);
			return ret;
		}

		@Override
		public Iterator<DoubleObjectPair<Vector>> eigenIterator(Eigenvalues evd) {
			return new FBEigenIterator(Mode.BACKWARD, evd);
		}

		@Override
		public Mode direction() {
			return Mode.FORWARD;
		}
	}
	
	/**
	 * The symmetric normalised Laplacian is defined as:
	 * L = D - W
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class Unnormalised extends GraphLaplacian{
		@Override
		public SparseMatrix laplacian(SparseMatrix adj, DiagonalMatrix degree) {
			SparseMatrix ret = MatlibMatrixUtils.minusInplace(
				degree,
				adj
			);
			return ret;
		}

		@Override
		public Iterator<DoubleObjectPair<Vector>> eigenIterator(Eigenvalues evd) {
			return new FBEigenIterator(Mode.FORWARD, evd);
		}

		@Override
		public Mode direction() {
			return Mode.FORWARD;
		}
	}

	/**
	 * The inverted symmetric normalised Laplacian is defined as:
	 * L = D^-1/2 A D^-1/2
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class Normalised extends GraphLaplacian{

		@Override
		public SparseMatrix laplacian(SparseMatrix adj, DiagonalMatrix degree) {
			DiagonalMatrix invSqrtDegree = MatlibMatrixUtils.powInplace(degree,-1./2.);
			SparseMatrix ret = MatlibMatrixUtils.times(
				MatlibMatrixUtils.times(
					invSqrtDegree, adj
				),
				invSqrtDegree
			);
			return ret;
		}

		@Override
		public Iterator<DoubleObjectPair<Vector>> eigenIterator(Eigenvalues evd) {
			return new FBEigenIterator(Mode.BACKWARD, evd);
		}

		@Override
		public Mode direction() {
			return Mode.FORWARD;
		}

	}

	/**
	 * @return the direction which this laplacian creates useful eigen values
	 */
	public abstract Mode direction();
}