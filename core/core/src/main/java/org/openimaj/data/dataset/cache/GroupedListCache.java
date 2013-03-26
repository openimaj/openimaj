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
package org.openimaj.data.dataset.cache;

import java.util.Collection;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;

/**
 * Definition of a cache for groups of lists.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OBJECT>
 *            Type of instances
 * @param <KEY>
 *            Type of groups
 */
public interface GroupedListCache<KEY, OBJECT> {
	/**
	 * Add an object with many keys to the cache
	 * 
	 * @param keys
	 *            the instance's keys
	 * @param object
	 *            the instance
	 */
	public void add(Collection<KEY> keys, OBJECT object);

	/**
	 * Add an object with a key to the cache
	 * 
	 * @param key
	 *            the instance's key
	 * @param object
	 *            the instance
	 */
	public void add(KEY key, OBJECT object);

	/**
	 * Add an collection of objects with the same key to the cache
	 * 
	 * @param key
	 *            the instance's key
	 * @param objects
	 *            the instances
	 */
	public void add(KEY key, Collection<OBJECT> objects);

	/**
	 * @return a dataset view of the cache
	 */
	public GroupedDataset<KEY, ListDataset<OBJECT>, OBJECT> getDataset();

	/**
	 * Reset the cache
	 */
	public void reset();
}
