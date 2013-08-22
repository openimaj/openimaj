package org.openimaj.ml.clustering.spectral;

import java.util.Iterator;

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
		valids[0] = vals.next().first; // Skip the first item in the calculation of max
		int i = 1; // start from the second index
		for (; vals.hasNext();) {
			double val = vals.next().first;
			if(val < 0) break;
			valids[i] = val;
			max = Math.max(max, valids[i]);
			i++;
		}
		int maxindex = i+1;
		int count = 2; // the first and the second must be included
		double first = valids[1]; // the second is what we compare against
		for (int j = 2; j < maxindex; j++) {
			double diff = Math.abs(first - valids[j]);
			if(diff / max > absoluteGap) 
				break;
			count++;
		}
		return count;
	}

	@Override
	public FewEigenvalues prepare(final SparseMatrix laplacian) {
		int total = laplacian.columnCount();
		FewEigenvalues eig = FewEigenvalues.of(laplacian);
		return eig.greatest((int) (total*maxSelect));
	}
	
	@Override
	public String toString() {
		return String.format("AbsVal=%2.2f,%2.2f",this.absoluteGap,this.maxSelect);
	}

}
