package org.openimaj.ml.clustering.spectral;

import java.util.Iterator;

import org.openimaj.ml.clustering.spectral.FBEigenIterator.Mode;
import org.openimaj.util.pair.DoubleObjectPair;

import ch.akuhn.matrix.SparseMatrix;
import ch.akuhn.matrix.Vector;
import ch.akuhn.matrix.eigenvalues.FewEigenvalues;

/**
 * Attempts to automatically choose the number of eigen vectors based on the
 * relative gap between eigen values. In spectral clustering the gap between the
 * eigen values of "good" clusters jumps. This class ignores the gap between 0 and
 * the next item because 0s represent completely isolated objects and in all but the trivial
 * case we must stop after we have run out of 0s.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class AutoSelectingEigenChooser extends EigenChooser {

	private double relativeGap;
	private double maxSelect;

	/**
	 * @param relativeGap the gap between previous and current (treated as absolute if previous value == 0)
	 * @param maxSelect
	 */
	public AutoSelectingEigenChooser(double relativeGap, double maxSelect) {
		this.relativeGap = relativeGap;
		this.maxSelect = maxSelect;
	}

	@Override
	public int nEigenVectors(Iterator<DoubleObjectPair<Vector>> vals, int totalEigenVectors) {
		int count = 0;
		double prevDiff = 0;
		double prevVal = vals.next().first;
		for (;vals.hasNext();) {
			double val = vals.next().first;
			if(val < 0) break;
			double diff = Math.abs(val - prevVal);
			if(prevDiff != 0){
				double l = prevDiff * relativeGap;
				if(diff > l) {
					count++;
					break;
				}
			}
			prevDiff = diff;
			prevVal = val;
			count ++;
		}
		int maxCount = (int) (totalEigenVectors * maxSelect);
		if(count > maxCount){
			return maxCount;
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
