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
package org.openimaj.data;

import java.lang.reflect.Array;

/**
 * This {@link DataSource} provides an indexed view of a subset of another
 * {@link DataSource}.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <DATATYPE>
 *            the data type which can be returned
 */
public class IndexedViewDataSource<DATATYPE> extends AbstractDataSource<DATATYPE> {
	DataSource<DATATYPE> innerSource;
	int[] indexes;

	/**
	 * Construct a new {@link IndexedViewDataSource} with the given inner data
	 * and indexes into the inner data.
	 *
	 * @param dataSource
	 *            the inner {@link DataSource}.
	 * @param indexes
	 *            the indexed into the inner datasource.
	 */
	public IndexedViewDataSource(DataSource<DATATYPE> dataSource, int[] indexes) {
		this.innerSource = dataSource;
		this.indexes = indexes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void getData(int startRow, int stopRow, DATATYPE[] data) {
		DATATYPE[] tmp;
		if (data[0] == null) {
			tmp = (DATATYPE[]) Array.newInstance(getData(0).getClass(), 1);
		} else {
			tmp = (DATATYPE[]) Array.newInstance(data[0].getClass(), 1);
		}

		for (int i = 0, j = startRow; j < stopRow; i++, j++) {
			final int row = indexes[j];
			tmp[0] = data[i];
			innerSource.getData(row, row + 1, tmp);
		}
	}

	@Override
	public DATATYPE getData(int row) {
		return innerSource.getData(indexes[row]);
	}

	@Override
	public int numDimensions() {
		return innerSource.numDimensions();
	}

	@Override
	public int size() {
		return indexes.length;
	}

	@Override
	public DATATYPE[] createTemporaryArray(int size) {
		return innerSource.createTemporaryArray(size);
	}
}
