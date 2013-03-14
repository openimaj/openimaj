package org.openimaj.data;

/**
 * This {@link DataSource} provides randomly sampled view over another
 * {@link DataSource}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <DATATYPE>
 *            the data type which can be returned
 */
public class RandomSampleDataSource<DATATYPE> extends IndexedViewDataSource<DATATYPE> {

	/**
	 * Construct the view over the given {@link DataSource} such that it has
	 * requestedSize items.
	 * 
	 * @param dataSource
	 *            the dataSource to sample
	 * @param requestedSize
	 *            the requested number of rows
	 * 
	 * @throws IllegalArgumentException
	 *             if the requested size is bigger than the number of rows in
	 *             the datasource being sampled, or less than one.
	 */
	public RandomSampleDataSource(DataSource<DATATYPE> dataSource, int requestedSize) {
		super(dataSource, RandomData.getUniqueRandomInts(requestedSize, 0, dataSource.numRows()));
	}

	/**
	 * Construct the view over the given {@link DataSource} such that it has the
	 * given proportion of items from the original.
	 * 
	 * @param dataSource
	 *            the dataSource to sample
	 * @param proportion
	 *            the proportion of rows from the original to include in the
	 *            sample
	 * @throws IllegalArgumentException
	 *             if the proportion is less than 0 or bigger than 1.
	 */
	public RandomSampleDataSource(DataSource<DATATYPE> dataSource, double proportion) {
		super(dataSource, RandomData.getUniqueRandomInts(
				(int) (proportion * dataSource.numRows()),
				0,
				dataSource.numRows()));
	}
}
