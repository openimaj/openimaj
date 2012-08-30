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
	public int numRows() {
		return indexes.length;
	}
}
