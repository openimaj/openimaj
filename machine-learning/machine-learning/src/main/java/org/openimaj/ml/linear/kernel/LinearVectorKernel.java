package org.openimaj.ml.linear.kernel;

import no.uib.cipr.matrix.DenseVector;

import org.openimaj.util.pair.IndependentPair;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class LinearVectorKernel implements VectorKernel{

	@Override
	public Double apply(IndependentPair<double[], double[]> in) {
		double[] first = in.firstObject();
		double[] second = in.secondObject();
		return new DenseVector(first,false).dot(new DenseVector(second,false));
	}

}
