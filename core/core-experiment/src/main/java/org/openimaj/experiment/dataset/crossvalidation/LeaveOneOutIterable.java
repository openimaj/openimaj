package org.openimaj.experiment.dataset.crossvalidation;

import java.util.Iterator;
import java.util.List;

import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.list.SkippingListView;

/**
 * An {@link Iterable} that produces an {@link Iterator} for 
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
public class LeaveOneOutIterable<INSTANCE extends Identifiable> implements Iterable<CrossValidationData<ListDataset<INSTANCE>>> {
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
	public int numberIterations() {
		return dataset.size();
	}
	
	@Override
	public Iterator<CrossValidationData<ListDataset<INSTANCE>>> iterator() {
		return new Iterator<CrossValidationData<ListDataset<INSTANCE>>>() {
			int validationIndex = 0;
			
			@Override
			public boolean hasNext() {
				return validationIndex < dataset.size();
			}

			@Override
			public CrossValidationData<ListDataset<INSTANCE>> next() {
				ListDataset<INSTANCE> training = new ListBackedDataset<INSTANCE>(new SkippingListView<INSTANCE>(listView, validationIndex));
				ListDataset<INSTANCE> validation = new ListBackedDataset<INSTANCE>(new AcceptingListView<INSTANCE>(listView, validationIndex));
				
				validationIndex++;
				
				return new CrossValidationData<ListDataset<INSTANCE>>(training, validation);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
