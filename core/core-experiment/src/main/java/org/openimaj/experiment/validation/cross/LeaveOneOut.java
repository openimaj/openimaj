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

import java.util.Iterator;
import java.util.List;

import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.experiment.validation.DefaultValidationData;
import org.openimaj.experiment.validation.ValidationData;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.list.SkippingListView;

/**
 * Leave-One-Out Cross Validation (LOOCV) with a {@link ListDataset}.
 * The number of iterations performed by the iterator is equal
 * to the number of data items.
 * <p>
 * Upon each iteration, the dataset is split into training
 * and validation sets. The validation set will have exactly one
 * instance. All remaining instances are placed in the training
 * set. As the iterator progresses, every instance will be included
 * in the validation set one time.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <INSTANCE> Type of instances
 */
public class LeaveOneOut<INSTANCE> implements CrossValidator<ListDataset<INSTANCE>> {
	private class LeaveOneOutIterable implements CrossValidationIterable<ListDataset<INSTANCE>> {
		private ListDataset<INSTANCE> dataset;
		private List<INSTANCE> listView;

		/**
		 * Construct {@link LeaveOneOutIterable} on the given dataset.
		 * @param dataset the dataset
		 */
		public LeaveOneOutIterable(ListDataset<INSTANCE> dataset) {
			this.dataset = dataset;
			this.listView = DatasetAdaptors.asList(dataset);
		}

		/**
		 * Get the number of iterations that the {@link Iterator}
		 * returned by {@link #iterator()} will perform.
		 * 
		 * @return the number of iterations that will be performed
		 */
		@Override
		public int numberIterations() {
			return dataset.size();
		}

		@Override
		public Iterator<ValidationData<ListDataset<INSTANCE>>> iterator() {
			return new Iterator<ValidationData<ListDataset<INSTANCE>>>() {
				int validationIndex = 0;

				@Override
				public boolean hasNext() {
					return validationIndex < dataset.size();
				}

				@Override
				public ValidationData<ListDataset<INSTANCE>> next() {
					ListDataset<INSTANCE> training = new ListBackedDataset<INSTANCE>(new SkippingListView<INSTANCE>(listView, validationIndex));
					ListDataset<INSTANCE> validation = new ListBackedDataset<INSTANCE>(new AcceptingListView<INSTANCE>(listView, validationIndex));

					validationIndex++;

					return new DefaultValidationData<ListDataset<INSTANCE>>(training, validation);
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	}

	@Override
	public CrossValidationIterable<ListDataset<INSTANCE>> createIterable(ListDataset<INSTANCE> data) {
		return new LeaveOneOutIterable(data);
	}
	
	@Override
	public String toString() {
		return "Leave-One-Out Cross Validation (LOOCV)";
	}
}