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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * An abstract {@link DataSource} backed by multiple lists of data.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @param <DATATYPE>
 *            the data type which can be returned
 * @param <ELEMENTTYPE>
 *            the datatype of the underlying lists
 *
 */
public abstract class AbstractMultiListDataSource<DATATYPE, ELEMENTTYPE> extends AbstractDataSource<DATATYPE> {
	protected List<? extends List<ELEMENTTYPE>> data;

	/**
	 * Construct with the given lists of data
	 *
	 * @param data
	 *            the data
	 */
	public AbstractMultiListDataSource(List<? extends List<ELEMENTTYPE>> data) {
		this.data = data;
	}

	/**
	 * Construct with the given lists of data
	 *
	 * @param data
	 *            the data
	 */
	@SafeVarargs
	public AbstractMultiListDataSource(List<ELEMENTTYPE>... data) {
		this.data = Arrays.asList(data);
	}

	/**
	 * Construct with the given map of data. The keys are ignored, and only the
	 * values are used.
	 *
	 * @param data
	 *            the data
	 */
	public AbstractMultiListDataSource(Map<?, ? extends List<ELEMENTTYPE>> data) {
		this.data = new ArrayList<List<ELEMENTTYPE>>(data.values());
	}

	@Override
	public void getData(int startRow, int stopRow, DATATYPE[] data) {
		for (int i = 0, row = startRow; row < stopRow; row++, i++) {
			data[i] = getData(row);
		}
	}

	@Override
	public DATATYPE getData(int row) {
		int cumsum = 0;

		for (int i = 0; i < data.size(); i++) {
			final int sz = data.get(i).size();

			if (row < cumsum + sz) {
				return convert(data.get(i).get(row - cumsum));
			}

			cumsum += sz;
		}

		throw new IndexOutOfBoundsException();
	}

	/**
	 * Convert an item's type
	 *
	 * @param ele
	 *            the input item
	 * @return the converted item
	 */
	protected abstract DATATYPE convert(ELEMENTTYPE ele);

	@Override
	public int size() {
		int sum = 0;

		for (final List<ELEMENTTYPE> d : data)
			sum += d.size();

		return sum;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DATATYPE[] createTemporaryArray(int size) {
		return (DATATYPE[]) Array.newInstance(getData(0).getClass(), size);
	}
}
