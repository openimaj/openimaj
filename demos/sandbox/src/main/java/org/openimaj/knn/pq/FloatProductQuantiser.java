package org.openimaj.knn.pq;

import java.util.Arrays;

import org.openimaj.ml.clustering.assignment.hard.ExactFloatAssigner;

public class FloatProductQuantiser {
	ExactFloatAssigner[] assigners;

	public byte[] quantise(float[] data) {
		final byte[] quantised = new byte[assigners.length];

		for (int i = 0, from = 0; i < assigners.length; i++) {
			final int to = assigners[i].numDimensions();
			quantised[i] = (byte) (assigners[i].assign(Arrays.copyOfRange(data, from, to)) - 128);
			from += to;
		}

		return quantised;
	}
}
