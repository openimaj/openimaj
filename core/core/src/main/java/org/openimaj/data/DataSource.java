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

/**
 * A source which returns data of type DATATYPE. Classes which implement this
 * interface must support random access to rows as well as the ability to get
 * random subsets of data.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <DATATYPE>
 *            the data type which can be returned
 */
public interface DataSource<DATATYPE> extends Iterable<DATATYPE> {
	/**
	 * Get data between given rows. startRow must be > 0, stopRow must be
	 * smaller than {@link DataSource#size()}.
	 *
	 * data is guaranteed to have (stopRow - startRow) valid entries
	 *
	 * @param startRow
	 *            where to start (inclusive)
	 * @param stopRow
	 *            where to stop (exclusive)
	 * @param data
	 *            the array to fill
	 */
	public void getData(int startRow, int stopRow, DATATYPE[] data);

	/**
	 * Get the data for a given row
	 *
	 * @param row
	 *            the row number
	 * @return the data for the row
	 */
	public DATATYPE getData(int row);

	/**
	 * @return Number of dimensions of each data point
	 */
	public int numDimensions();

	/**
	 * @param data
	 *            a random set of data. This array is guaranteed to be filled.
	 */
	public void getRandomRows(DATATYPE[] data);

	/**
	 * @return the number of data points
	 */
	public int size();

	/**
	 * Create a temporary array suitable for use with the
	 * {@link #getRandomRows(Object[])} and {@link #getData(int, int, Object[])}
	 * methods.
	 *
	 * @param size
	 *            the size of the array
	 * @return the array
	 */
	public DATATYPE[] createTemporaryArray(int size);
}
