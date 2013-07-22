package org.openimaj.ml.clustering.spectral;

import gov.sandia.cognition.math.ComplexNumber;
import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.decomposition.EigenDecomposition;

import java.util.Iterator;

import org.openimaj.util.pair.DoubleObjectPair;

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
	private ComplexNumber[] values;
	private Matrix vectors;
	private int lim;
	/**
	 * @param fb
	 * @param evd
	 */
	public FBEigenIterator(FBEigenIterator.Mode fb, EigenDecomposition evd) {
		this.values = evd.getEigenValues();
		this.vectors = evd.getEigenVectorsRealPart();
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
		DoubleObjectPair<Vector> ret = new DoubleObjectPair<Vector>(this.values[pos].getMagnitude(), this.vectors.getColumn(pos));
		pos += dir;
		return ret;
	}

	@Override
	public boolean hasNext() {
		return pos != lim;
	}
}