package org.openimaj.ml.clustering.spectral;

import org.openimaj.math.matrix.CFMatrixUtils;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;

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
	public Matrix laplacian(SparseMatrix adj){
		SparseMatrix degree = SparseMatrixFactoryMTJ.INSTANCE.createIdentity(adj.getNumRows(), adj.getNumRows());
		for (int i = 0; i < adj.getNumRows(); i++) {
			degree.setElement(i, i, adj.getRow(i).sum());
		}
		return laplacian(adj,degree);
	}

	/**
	 * @param adj square and symmetric
	 * @param degree the sum of the adjacency for a node in the diagonals
	 * @return the laplacian
	 */
	public abstract Matrix laplacian(SparseMatrix adj, SparseMatrix degree);
	
	/**
	 * The symmetric normalised Laplacian is defined as:
	 * L = I - D^-1/2 A D^-1/2
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class SymmetricNormalised extends GraphLaplacian{

		@Override
		public Matrix laplacian(SparseMatrix adj, SparseMatrix degree) {
			SparseMatrix invSqrtDegree = CFMatrixUtils.powInplace(degree,-1./2.);
			SparseMatrix ident = SparseMatrixFactoryMTJ.INSTANCE.createIdentity(degree.getNumRows(), degree.getNumRows());
			return ident.minus(invSqrtDegree.times(adj).times(invSqrtDegree));
		}
		
	}
}