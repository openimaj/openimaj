package org.openimaj.data;

import java.util.Arrays;
import java.util.List;

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
	public AbstractMultiListDataSource(List<ELEMENTTYPE>... data) {
		this.data = Arrays.asList(data);
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
			final int sz = data.size();

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
	public int numRows() {
		int sum = 0;

		for (final List<ELEMENTTYPE> d : data)
			sum += d.size();

		return sum;
	}
}
