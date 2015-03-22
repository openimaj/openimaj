/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.tools.clusterquantiser.samplebatch;

import gnu.trove.list.array.TIntArrayList;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.openimaj.data.DataSource;
import org.openimaj.data.RandomData;

/**
 * A batched datasource
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SampleBatchByteDataSource implements DataSource<byte[]> {
	private int total;
	private List<SampleBatch> batches;
	private int dims;

	private Random seed;

	/**
	 * Construct with batches
	 *
	 * @param batches
	 * @throws IOException
	 */
	public SampleBatchByteDataSource(List<SampleBatch> batches) throws IOException {
		this.batches = batches;
		this.total = batches.get(batches.size() - 1).getEndIndex();
		this.dims = this.batches.get(0).getStoredSamples(0, 1)[0].length;
		this.seed = new Random();
	}

	/**
	 * Set the random seed
	 *
	 * @param seed
	 */
	public void setSeed(long seed) {
		if (seed < 0)
			this.seed = new Random();
		else
			this.seed = new Random(seed);
	}

	@Override
	public void getData(int startRow, int stopRow, byte[][] output) {
		int added = 0;
		for (final SampleBatch sb : batches) {
			try {
				if (sb.getEndIndex() < startRow)
					continue; // Before this range
				if (sb.getStartIndex() > stopRow)
					continue; // After this range
				// So it must be within this range in some sense, find out where
				final int startDelta = startRow - sb.getStartIndex();
				final int stopDelta = stopRow - sb.getStartIndex();

				final int interestedStart = startDelta < 0 ? 0 : startDelta;
				final int interestedEnd = stopDelta + sb.getStartIndex() > sb.getEndIndex() ? sb.getEndIndex()
						- sb.getStartIndex() : stopDelta;
						if (interestedEnd - interestedStart == 0)
							continue;
						// System.out.print("\rGetting " + interestedStart + "->" +
						// interestedEnd + " from" + sb.sampleSource.getName());
						final byte[][] subSamples = sb.getStoredSamples(interestedStart, interestedEnd);

						for (int i = 0; i < subSamples.length; i++) {
							System.arraycopy(subSamples[i], 0, output[added + i], 0, subSamples[i].length);
						}

						added += subSamples.length;
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void getRandomRows(byte[][] output) {
		final int k = output.length;
		System.err.println("Requested random samples: " + k);
		final int[] indices = RandomData.getUniqueRandomInts(k, 0, this.total, seed);
		System.err.println("Array constructed");
		int l = 0;
		final TIntArrayList samplesToLoad = new TIntArrayList();

		final int[] original = indices.clone();
		Arrays.sort(indices);
		int indicesMarker = 0;
		for (int sbIndex = 0; sbIndex < this.batches.size(); sbIndex++) {
			samplesToLoad.clear();

			final SampleBatch sb = this.batches.get(sbIndex);
			for (; indicesMarker < indices.length; indicesMarker++) {
				final int index = indices[indicesMarker];
				if (sb.getStartIndex() <= index && sb.getEndIndex() > index) {
					samplesToLoad.add(index - sb.getStartIndex());
				}
				if (sb.getEndIndex() <= index)
					break;
			}

			try {
				if (samplesToLoad.size() == 0)
					continue;
				final byte[][] features = sb.getStoredSamples(samplesToLoad.toArray());
				for (int i = 0; i < samplesToLoad.size(); i++) {
					int j = 0;
					for (; j < original.length; j++)
						if (original[j] == samplesToLoad.get(i) + sb.getStartIndex())
							break;
					System.arraycopy(features[i], 0, output[j], 0, features[i].length);
					System.err.printf("\rCreating sample index hashmap %8d/%8d", l++, k);
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		System.err.println();
	}

	@Override
	public int numDimensions() {
		return dims;
	}

	@Override
	public int size() {
		return total;
	}

	@Override
	public byte[] getData(int row) {
		final byte[] data = new byte[dims];

		getData(row, row + 1, new byte[][] { data });

		return data;
	}

	@Override
	public Iterator<byte[]> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[][] createTemporaryArray(int size) {
		return new byte[size][dims];
	}
}
