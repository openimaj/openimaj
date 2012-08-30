package org.openimaj.data;

import java.util.Iterator;

/**
 * Abstract base class for {@link DataSource} implementations. Simplifies
 * implementation by providing {@link #iterator()} and
 * {@link #getRandomRows(Object[])} methods that proxy their work to
 * {@link #getData(int)}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <DATATYPE>
 *            the data type which can be returned
 */
public abstract class AbstractDataSource<DATATYPE> implements DataSource<DATATYPE> {
	@Override
	public Iterator<DATATYPE> iterator() {
		return new Iterator<DATATYPE>() {
			int index = 0;

			@Override
			public boolean hasNext() {
				return (index + 1) < numRows();
			}

			@Override
			public DATATYPE next() {
				index++;

				return getData(index);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public void getRandomRows(DATATYPE[] data) {
		final int[] rndIndexes = RandomData.getUniqueRandomInts(data.length, 0, numRows());

		for (int i = 0; i < rndIndexes.length; i++)
			data[i] = getData(rndIndexes[i]);
	}
}
