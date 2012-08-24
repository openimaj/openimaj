package org.openimaj.lsh.sketch;

import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.sketch.Sketcher;

public class IntLSHSketcher<O> implements Sketcher<O, int[]> {
	HashFunction<O>[] functions;

	public IntLSHSketcher(HashFunction... functions) {
		this.functions = functions;
	}

	@Override
	public int[] createSketch(O input) {
		final int nints = (int) Math.ceil((double) functions.length / Integer.SIZE);
		final int[] sketch = new int[nints];

		for (int i = 0, j = 0; i < nints; i++) {
			for (int k = 0; k < Integer.SIZE; k++) {
				final int hash = functions[j++].computeHashCode(input);

				sketch[i] = sketch[i] | (hash << k);
			}
		}

		return sketch;
	}
}
