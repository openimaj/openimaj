package org.openimaj.workinprogress.sgdsvm;

public interface Loss {
	public double loss(double a, double y);

	public double dloss(double a, double y);
}
