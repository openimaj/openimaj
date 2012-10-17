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
package org.openimaj.experiment.dataset.util;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListDataset;

/**
 * Helper methods to provide different types of view on
 * a dataset.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class DatasetAdaptors {
	/**
	 * Create a {@link List} view of the given dataset. This method
	 * creates a new {@link List} containing all the instances in the
	 * dataset. The list is populated by iterating through the dataset.
	 * 
	 * @param <INSTANCE> The type of instances in the dataset
	 * @param dataset The dataset.
	 * @return a list of all instances.
	 */
	public static <INSTANCE> List<INSTANCE> asList(final Dataset<INSTANCE> dataset) {
		ArrayList<INSTANCE> list = new ArrayList<INSTANCE>();
		
		for (INSTANCE instance : dataset)
			list.add(instance);
		
		return list;
	}
	
	/**
	 * Create a {@link List} view of the given dataset. This method
	 * creates a new {@link List} containing all the instances in the
	 * dataset. The list is populated by iterating through the dataset.
	 * 
	 * @param <INSTANCE> The type of instances in the dataset
	 * @param dataset The dataset.
	 * @return a list of all instances.
	 */
	public static <INSTANCE> List<INSTANCE> asList(final List<INSTANCE> dataset) {
		return dataset;
	}
	
	/**
	 * Create a {@link List} view of the given {@link ListDataset}. The
	 * returned list is a lightweight read-only wrapper around the
	 * dataset. 
	 * 
	 * @param <INSTANCE> The type of instances in the dataset
	 * @param dataset The dataset.
	 * @return a list of all instances.
	 */
	public static <INSTANCE> List<INSTANCE> asList(final ListDataset<INSTANCE> dataset) {
		return new AbstractList<INSTANCE>() {
			@Override
			public INSTANCE get(int index) {
				return dataset.getInstance(index);
			}

			@Override
			public int size() {
				return dataset.size();
			}
		};
	}
	
	/**
	 * Create a {@link Map} view of the given {@link GroupedDataset}. The
	 * returned map is a lightweight read-only wrapper around the
	 * dataset. 
	 * 
	 * @param <KEY> The type of the group identifier 
	 * @param <DATASET> The type of {@link Dataset}
	 * @param <INSTANCE> The type of instances in the dataset
	 * @param dataset The dataset.
	 * @return a map view of the dataset.
	 */
	public static <KEY, DATASET extends Dataset<INSTANCE>, INSTANCE> 
		Map<KEY, DATASET> asMap(final GroupedDataset<KEY, DATASET, INSTANCE> dataset) 
	{
		return new AbstractMap<KEY, DATASET>() {
			@Override
			public Set<Entry<KEY, DATASET>> entrySet() {
				Set<Entry<KEY, DATASET>> entries = new HashSet<Entry<KEY, DATASET>>();
			
				for (final KEY group : dataset.getGroups()) {
					entries.add( new Entry<KEY, DATASET>() {
						@Override
						public KEY getKey() {
							return group;
						}

						@Override
						public DATASET getValue() {
							return dataset.getInstances(group);
						}

						@Override
						public DATASET setValue(DATASET value) {
							throw new UnsupportedOperationException("not supported");
						}
					}); 
				}
				
				return entries;
			}
		};
	}
}
