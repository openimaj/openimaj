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

	/**
	 * The mode
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static enum Mode{
		/**
		 * Forward from index 0
		 */
		FORWARD,
		/**
		 *  Backward from indes n-1
		 */
		BACKWARD;
	}

	private int pos;
	private int dir;
	private double[] values;
	private Vector[] vectors;
	private int lim;
	/**
	 * @param fb
	 * @param evd
	 */
	public FBEigenIterator(FBEigenIterator.Mode fb, Eigenvalues evd) {
		this.values = evd.value;
		this.vectors = evd.vector;
		switch (fb) {
		case FORWARD:
			this.pos = 0;
			this.dir = 1;
			this.lim = values.length;
		break;
		case BACKWARD:
			this.pos = values.length-1;
			this.dir = -1;
			this.lim = -1;
			break;
		default:
			break;
		}
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