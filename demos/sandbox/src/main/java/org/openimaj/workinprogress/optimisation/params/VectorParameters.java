package org.openimaj.workinprogress.optimisation.params;

public final class VectorParameters implements Parameters<VectorParameters> {
	public double[] vector;

	public VectorParameters(int ndims) {
		vector = new double[ndims];
	}

	public VectorParameters(double[] vec) {
		vector = vec;
	}

	@Override
	public void multiplyInplace(VectorParameters other) {
		for (int i = 0; i < vector.length; i++)
			vector[i] *= other.vector[i];

	}

	@Override
	public void addInplace(VectorParameters other) {
		for (int i = 0; i < vector.length; i++)
			vector[i] += other.vector[i];
	}

	@Override
	public void multiplyInplace(double value) {
		for (int i = 0; i < vector.length; i++)
			vector[i] *= value;
	}

	@Override
	public void addInplace(double value) {
		for (int i = 0; i < vector.length; i++)
			vector[i] += value;
	}
}
