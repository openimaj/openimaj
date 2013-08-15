package org.openimaj.ml.clustering.spectral;

import java.util.Iterator;

import org.openimaj.ml.clustering.spectral.FBEigenIterator.Mode;
import org.openimaj.util.pair.DoubleObjectPair;

import ch.akuhn.matrix.SparseMatrix;
import ch.akuhn.matrix.Vector;
import ch.akuhn.matrix.eigenvalues.FewEigenvalues;

/**
 * Attempts to automatically choose the number of eigen vectors based on the
 * comparative value of the eigen value with the first eigen value seen.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class AbsoluteValueEigenChooser extends EigenChooser{

	private double absoluteGap;
	private double maxSelect;

	/**
	 * @param absoluteGap the gap between the first and the current value 
	 * @param maxSelect
	 */
	public AbsoluteValueEigenChooser(double absoluteGap, double maxSelect) {
		this.absoluteGap = absoluteGap;
		this.maxSelect = maxSelect;
	}
	
	@Override
	public int nEigenVectors(Iterator<DoubleObjectPair<Vector>> vals, int totalEigenVectors) {
		double max = -Double.MAX_VALUE;
		double[] valids = new double[totalEigenVectors];
		int i = 0;
		for (; vals.hasNext();) {
			valids[i] = vals.next().first;
			max = Math.max(max, valids[i]);
			i++;
		}
		int count = 1;
		double first = valids[0];
		for (int j = 1; j < valids.length; j++) {
			double diff = Math.abs(first - valids[j]);
			if(diff / max > absoluteGap) break;
			count++;
		}
		return count;
	}

	@Override
	public FewEigenvalues prepare(final SparseMatrix laplacian, Mode direction) {
		int total = laplacian.columnCount();
		if(direction == Mode.FORWARD){
			FewEigenvalues eig = FewEigenvalues.of(laplacian);
			return eig.greatest((int) (total*maxSelect));
		}
		else{
			FewEigenvalues eig = FewEigenvalues.of(laplacian);
			return eig.lowest((int) (total*maxSelect));
		}
	}

}
