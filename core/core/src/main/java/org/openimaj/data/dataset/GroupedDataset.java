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
package org.openimaj.data.dataset;

import java.util.Map;
import java.util.Set;

/**
 * A {@link Dataset} that is grouped into separate classes or groups. Each group
 * is represented by a key, and each key corresponds to a sub-dataset.
 * <p>
 * Sub-datasets can be any kind of {@link Dataset}, including
 * {@link GroupedDataset}s, so it is possible to build tree structures.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <KEY>
 *            Type of dataset class key
 * @param <DATASET>
 *            Type of sub-datasets.
 * @param <INSTANCE>
 *            Type of instances in the dataset
 */
public interface GroupedDataset<KEY extends Object, DATASET extends Dataset<INSTANCE>, INSTANCE>
		extends
		Dataset<INSTANCE>, Map<KEY, DATASET>
{
	/**
	 * Get sub-dataset corresponding to the given group key
	 * 
	 * @param key
	 *            the key.
	 * @return the sub dataset, or null if the key was unknown
	 */
	public DATASET getInstances(KEY key);

	/**
	 * Get the set of all defined group keys.
	 * 
	 * @return the the set of all defined group keys.
	 */
	public Set<KEY> getGroups();

	/**
	 * Get a random instance from the sub-dataset corresponding to a specific
	 * group.
	 * 
	 * @param key
	 *            the group key
	 * @return a random item from the group.
	 */
	public INSTANCE getRandomInstance(KEY key);
}
