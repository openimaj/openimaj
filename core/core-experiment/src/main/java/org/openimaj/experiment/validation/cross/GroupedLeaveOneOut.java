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
package org.openimaj.experiment.validation.cross;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.experiment.validation.DefaultValidationData;
import org.openimaj.experiment.validation.ValidationData;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.list.SkippingListView;

/**
 * Leave-One-Out Cross Validation (LOOCV) with a {@link GroupedDataset}. The
 * number of iterations performed by the iterator is equal to the number of data
 * items.
 * <p>
 * Upon each iteration, the dataset is split into training and validation sets.
 * The validation set will have exactly one instance. All remaining instances
 * are placed in the training set. As the iterator progresses, every instance
 * will be included in the validation set one time. The iterator maintains the
 * respective groups of the training and validation items.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <KEY>
 *            Type of groups
 * @param <INSTANCE>
 *            Type of instances
 * 
 */
public class GroupedLeaveOneOut<KEY, INSTANCE>
		implements
		CrossValidator<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>
{
	private class GroupedLeaveOneOutIterable
			implements
			CrossValidationIterable<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>
	{
		private GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset;

		/**
		 * Construct the {@link GroupedLeaveOneOutIterable} with the given
		 * dataset.
		 * 
		 * @param dataset
		 *            the dataset.
		 */
		public GroupedLeaveOneOutIterable(GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset) {
			this.dataset = dataset;
		}

		/**
		 * Get the number of iterations that the {@link Iterator} returned by
		 * {@link #iterator()} will perform.
		 * 
		 * @return the number of iterations that will be performed
		 */
		@Override
		public int numberIterations() {
			return dataset.numInstances();
		}

		@Override
		public Iterator<ValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>> iterator() {
			return new Iterator<ValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>>() {
				int validationIndex = 0;
				int validationGroupIndex = 0;
				Iterator<KEY> groupIterator = dataset.getGroups().iterator();
				KEY currentGroup = groupIterator.hasNext() ? groupIterator.next() : null;
				List<INSTANCE> currentValues = currentGroup == null ? null : DatasetAdaptors.asList(dataset
						.getInstances(currentGroup));

				@Override
				public boolean hasNext() {
					return validationIndex < dataset.numInstances();
				}

				@Override
				public ValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>> next() {
					int selectedIndex;

					if (currentValues != null && validationGroupIndex < currentValues.size()) {
						selectedIndex = validationGroupIndex;
						validationGroupIndex++;
					} else {
						validationGroupIndex = 0;
						currentGroup = groupIterator.next();
						currentValues = currentGroup == null ? null : DatasetAdaptors.asList(dataset
								.getInstances(currentGroup));

						return next();
					}

					final Map<KEY, ListDataset<INSTANCE>> train = new HashMap<KEY, ListDataset<INSTANCE>>();
					for (final KEY group : dataset.getGroups()) {
						if (group != currentGroup)
							train.put(group, dataset.getInstances(group));
					}
					train.put(currentGroup, new ListBackedDataset<INSTANCE>(new SkippingListView<INSTANCE>(currentValues,
							selectedIndex)));

					final Map<KEY, ListDataset<INSTANCE>> valid = new HashMap<KEY, ListDataset<INSTANCE>>();
					valid.put(currentGroup, new ListBackedDataset<INSTANCE>(new AcceptingListView<INSTANCE>(
							currentValues, selectedIndex)));

					final GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> cvTrain = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>(
							train);
					final GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> cvValid = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>(
							valid);

					validationIndex++;

					return new DefaultValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>(cvTrain,
							cvValid);
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	}

	@Override
	public CrossValidationIterable<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>> createIterable(
			GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> data)
	{
		return new GroupedLeaveOneOutIterable(data);
	}

	@Override
	public String toString() {
		return "Leave-One-Out Cross Validation (LOOCV) for grouped data.";
	}
}
