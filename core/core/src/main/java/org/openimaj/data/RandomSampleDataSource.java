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
		super(dataSource, RandomData.getUniqueRandomInts(requestedSize, 0, dataSource.size()));
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
				(int) (proportion * dataSource.size()),
				0,
				dataSource.size()));
	}
}
