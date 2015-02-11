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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.data.dataset.Dataset;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;

/**
 * Helper methods to provide different types of view on a dataset.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class DatasetAdaptors {
	/**
	 * Create a {@link List} view of the given dataset. If the dataset is a
	 * {@link ListDataset} it is returned, otherwise this method creates a new
	 * {@link List} containing all the instances in the dataset. The list is
	 * populated by iterating through the dataset.
	 *
	 * @param <INSTANCE>
	 *            The type of instances in the dataset
	 * @param dataset
	 *            The dataset.
	 * @return a list of all instances.
	 */
	public static <INSTANCE> List<INSTANCE> asList(final Dataset<INSTANCE> dataset) {
		if (dataset instanceof ListDataset)
			return (ListDataset<INSTANCE>) dataset;

		final ArrayList<INSTANCE> list = new ArrayList<INSTANCE>();

		for (final INSTANCE instance : dataset)
			list.add(instance);

		return list;
	}

	/**
	 * if you have a grouped dataset where the groups contains lists of feature
	 * objects (i.e. GroupedDataset<KEY,ListDataset<List<INSTANCE>>,INSTANCE>)
	 * then this will flatten those internal list, so that all the instances
	 * from those lists are directly associated with the key. This type of thing
	 * might occur if your dataset element reader can extract multiple media
	 * parts from a single dataset item, that will all end up with the same key.
	 *
	 * @param dataset
	 *            The dataset
	 * @return The new dataset
	 */
	public static <ANN, INSTANCE> GroupedDataset<ANN, ListDataset<INSTANCE>, INSTANCE>
	flattenListGroupedDataset(
			final GroupedDataset<ANN, ? extends ListDataset<List<INSTANCE>>, ? extends List<INSTANCE>> dataset)
			{
		// Create a grouped dataset without the lists
		final MapBackedDataset<ANN, ListDataset<INSTANCE>, INSTANCE> g =
				new MapBackedDataset<ANN, ListDataset<INSTANCE>, INSTANCE>();

		// Go through each of the groups...
		for (final ANN a : dataset.getGroups())
		{
			// Get the group
			final ListDataset<? extends List<INSTANCE>> l = dataset.getInstances(a);

			// Add each of the instances in that dataset to a new list dataset
			final ListBackedDataset<INSTANCE> newListDataset = new ListBackedDataset<INSTANCE>();
			for (final List<INSTANCE> le : l)
				for (final INSTANCE ll : le)
					newListDataset.add(ll);

			// Put that list dataset straight into the new grouped dataset.
			g.add(a, newListDataset);
		}

		return g;
			}

	/**
	 * Takes a grouped dataset and returns a new dataset that contains only
	 * those groups specified. If the given groups do not exist in the provided
	 * dataset, then they will be ignored.
	 *
	 * @param data
	 *            The dataset to take the groups from
	 * @param groups
	 *            The groups to take
	 * @return the new dataset containing only those groups.
	 */
	@SafeVarargs
	public static <ANN, DATASET extends Dataset<INSTANCE>, INSTANCE> GroupedDataset<ANN, DATASET, INSTANCE>
	getGroupedDatasetSubset(final GroupedDataset<ANN, DATASET, INSTANCE> data, final ANN... groups)
	{
		// New dataset
		final MapBackedDataset<ANN, DATASET, INSTANCE> newDataset = new MapBackedDataset<ANN, DATASET, INSTANCE>();

		// Loop through each of the groups specified...
		for (final ANN group : groups)
		{
			// Copy the dataset into the new dataset (if it's not null)
			final DATASET ds = data.getInstances(group);
			if (ds != null)
				newDataset.put(group, ds);
		}

		return newDataset;
	}

	/**
	 * Takes a grouped dataset and returns a new dataset with the groups
	 * re-shuffled as specified in the regrouping criteria.
	 *
	 * The regrouping criteria is a map from new group name to old group name.
	 * Instances in the old group names will be mapped to the new group names.
	 *
	 * Where many old groups map to a single new group, the groups will be
	 * merged.
	 *
	 * For example:
	 *
	 * <pre>
	 * <code>
	 * 	old == GroupedDataset: {G1=[1,2,3],G2=[4,5,6],G3=[7,8,9]}
	 *
	 * 		new = getGroupedDatasetSubset( old, {A->[G1,G3],B->[G2]} )
	 *
	 * 		new == GroupedDataset: {A=[1,2,3,7,8,9],B=[4,5,6]}
	 * 	</code>
	 * </pre>
	 *
	 * If the given groups do not exist in the provided dataset, then they will
	 * be ignored.
	 *
	 * @param data
	 *            The dataset to take the groups from
	 * @param regroupCriteria
	 *            The regrouping criteria
	 * @return the new dataset containing the new regrouping.
	 */
	public static <ANN, DATASET extends ListDataset<INSTANCE>, INSTANCE>
	GroupedDataset<ANN, ListBackedDataset<INSTANCE>, INSTANCE>
	getRegroupedDataset(final GroupedDataset<ANN, DATASET, INSTANCE> data, final Map<ANN, ANN[]> regroupCriteria)
	{
		// New dataset
		final MapBackedDataset<ANN, ListBackedDataset<INSTANCE>, INSTANCE> newDataset =
				new MapBackedDataset<ANN, ListBackedDataset<INSTANCE>, INSTANCE>();

		// Loop through each of the new groups specified...
		for (final ANN newGroup : regroupCriteria.keySet())
		{
			for (final ANN oldGroup : regroupCriteria.get(newGroup))
			{
				// Copy the dataset into the new dataset (if it's not null)
				final DATASET ds = data.getInstances(oldGroup);
				if (ds != null)
				{
					// Create a new list backed dataset (which we know we can
					// write to)...
					final ListBackedDataset<INSTANCE> lbd = new ListBackedDataset<INSTANCE>();
					lbd.addAll(ds);

					// We merge the groups if there's already one in our new
					// dataset
					if (newDataset.get(newGroup) != null)
						newDataset.get(newGroup).addAll(lbd);
					else
						newDataset.put(newGroup, lbd);
				}
			}
		}

		return newDataset;
	}
}
