package org.openimaj.ml.clustering.spectral;


import java.util.Iterator;

import org.openimaj.math.matrix.DiagonalMatrix;
import org.openimaj.math.matrix.MatlibMatrixUtils;
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
			row.put(i, 0);
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
	public Iterator<DoubleObjectPair<Vector>> eigenIterator(Eigenvalues evd) {
		return new FBEigenIterator(evd);
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
			SparseMatrix ret = MatlibMatrixUtils.plusInplace(
				DiagonalMatrix.ones(degree.rowCount()), 
				MatlibMatrixUtils.minusInplace(
					degree,
					adj
				)
			);
			return ret;
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
			for (int i = 0; i < degree.rowCount(); i++) {
				if(Double.isNaN(degree.get(i, i)) || Double.isInfinite(degree.get(i, i)) ) 
					invSqrtDegree.put(i, i, 0); 
			}
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
			return new FBEigenIterator(evd);
		}

	}
	
	/**
	 * The inverted symmetric normalised Laplacian is defined as:
	 * L = D^-1 . W
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class Warped extends GraphLaplacian{

		@Override
		public SparseMatrix laplacian(SparseMatrix adj, DiagonalMatrix degree) {
			DiagonalMatrix invDegree = MatlibMatrixUtils.powInplace(degree,-1.);
			for (int i = 0; i < degree.rowCount(); i++) {
				if(Double.isNaN(degree.get(i, i)) || Double.isInfinite(degree.get(i, i)) ) 
					invDegree.put(i, i, 0); 
			}
			SparseMatrix ret = MatlibMatrixUtils.times(
				MatlibMatrixUtils.times(
					invDegree, adj
				),
				invDegree
			);
			return ret;
		}

		@Override
		public Iterator<DoubleObjectPair<Vector>> eigenIterator(Eigenvalues evd) {
			return new FBEigenIterator(evd);
		}

	}

}