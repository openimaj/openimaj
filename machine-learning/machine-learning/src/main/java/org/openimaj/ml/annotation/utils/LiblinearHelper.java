package org.openimaj.ml.annotation.utils;

import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.SparseByteFV;
import org.openimaj.feature.SparseDoubleFV;
import org.openimaj.feature.SparseFloatFV;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.SparseLongFV;
import org.openimaj.feature.SparseShortFV;
import org.openimaj.util.array.SparseByteArray;
import org.openimaj.util.array.SparseDoubleArray;
import org.openimaj.util.array.SparseFloatArray;
import org.openimaj.util.array.SparseIntArray;
import org.openimaj.util.array.SparseLongArray;
import org.openimaj.util.array.SparseShortArray;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;

/**
 * Helper methods for interoperability of OpenIMAJ types with Liblinear.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class LiblinearHelper {
	/**
	 * Convert a {@link FeatureVector} to an array of {@link Feature}s.
	 * 
	 * @param feature
	 *            input {@link FeatureVector}
	 * @return output {@link Feature} array
	 */
	public static Feature[] convert(FeatureVector feature) {
		final Feature[] out;
		int i = 0;

		if (feature instanceof SparseDoubleFV) {
			out = new Feature[((SparseDoubleFV) feature).values.used()];

			for (final SparseDoubleArray.Entry entry : ((SparseDoubleFV) feature).getVector().entries()) {
				out[i++] = new FeatureNode(entry.index + 1, entry.value);
			}
		} else if (feature instanceof SparseFloatFV) {
			out = new Feature[((SparseFloatFV) feature).values.used()];

			for (final SparseFloatArray.Entry entry : ((SparseFloatFV) feature).getVector().entries()) {
				out[i++] = new FeatureNode(entry.index + 1, entry.value);
			}
		} else if (feature instanceof SparseByteFV) {
			out = new Feature[((SparseByteFV) feature).values.used()];

			for (final SparseByteArray.Entry entry : ((SparseByteFV) feature).getVector().entries()) {
				out[i++] = new FeatureNode(entry.index + 1, entry.value);
			}
		} else if (feature instanceof SparseShortFV) {
			out = new Feature[((SparseShortFV) feature).values.used()];

			for (final SparseShortArray.Entry entry : ((SparseShortFV) feature).getVector().entries()) {
				out[i++] = new FeatureNode(entry.index + 1, entry.value);
			}
		} else if (feature instanceof SparseIntFV) {
			out = new Feature[((SparseIntFV) feature).values.used()];

			for (final SparseIntArray.Entry entry : ((SparseIntFV) feature).getVector().entries()) {
				out[i++] = new FeatureNode(entry.index + 1, entry.value);
			}
		} else if (feature instanceof SparseLongFV) {
			out = new Feature[((SparseLongFV) feature).values.used()];

			for (final SparseLongArray.Entry entry : ((SparseLongFV) feature).getVector().entries()) {
				out[i++] = new FeatureNode(entry.index + 1, entry.value);
			}
		} else {
			final double[] array = feature.asDoubleVector();
			int numZero = 0;

			for (i = 0; i < array.length; i++) {
				if (array[i] == 0)
					numZero++;
			}

			out = new Feature[array.length - numZero];

			int j;
			for (i = 0, j = 0; i < array.length; i++) {
				if (array[i] != 0)
					out[j++] = new FeatureNode(i + 1, array[i]);
			}
		}

		return out;
	}
}
