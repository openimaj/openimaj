package org.openimaj.ml.clustering.spectral;

import java.util.Iterator;

import org.openimaj.util.pair.DoubleObjectPair;

import ch.akuhn.matrix.Vector;
import ch.akuhn.matrix.eigenvalues.Eigenvalues;

/**
 * A forward or backward iterator of eigen vector/value pairs
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public final class FBEigenIterator implements
		Iterator<DoubleObjectPair<Vector>> {

	private int pos;
	private int dir;
	private double[] values;
	private Vector[] vectors;
	private int lim;
	/**
	 * @param evd
	 */
	public FBEigenIterator(Eigenvalues evd) {
		this.values = evd.value;
		this.vectors = evd.vector;
		this.pos = values.length-1;
		this.dir = -1;
		this.lim = -1;
	}

	@Override
	public void remove() { throw new UnsupportedOperationException();}

	@Override
	public DoubleObjectPair<Vector> next() {
		DoubleObjectPair<Vector> ret = new DoubleObjectPair<Vector>(this.values[pos], this.vectors[pos]);
		pos += dir;
		return ret;
	}

	@Override
	public boolean hasNext() {
		return pos != lim;
	}
}