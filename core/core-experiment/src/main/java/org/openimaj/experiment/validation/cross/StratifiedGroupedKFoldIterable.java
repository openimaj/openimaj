package org.openimaj.experiment.validation.cross;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.data.RandomData;
import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.experiment.validation.DefaultValidationData;
import org.openimaj.experiment.validation.ValidationData;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.list.SkippingListView;

/**
 * An iterator for creating data for Stratified K-Fold Cross-Validation on grouped datasets.
 * <p>
 * This implementation randomly splits the data in each group into K non-overlapping subsets.
 * The number of folds, K, is set at the size of the smallest group if it is bigger;
 * this ensures that each fold will contain at least one training and validation example
 * for each group, and that the relative distribution of instances per group for each fold is 
 * approximately the same as for the full dataset.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <KEY> Type of groups
 * @param <INSTANCE> Type of instances 
 */
public class StratifiedGroupedKFoldIterable<KEY, INSTANCE> implements CrossValidationIterable<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>> {
	private GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset;
	private Map<KEY, int[][]> subsetIndices = new HashMap<KEY, int[][]>();
	private int numFolds;
	
	/**
	 * Construct a {@link StratifiedGroupedKFoldIterable} with the given dataset and
	 * target number of folds, K. If a group in the dataset has fewer than K instances, then
	 * the number of folds will be reduced to the number of instances.
	 *   
	 * @param dataset the dataset
	 * @param k the target number of folds.
	 */
	public StratifiedGroupedKFoldIterable(GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset, int k) {
		if (k > dataset.size())
			throw new IllegalArgumentException("The number of folds must be less than the number of items in the dataset");
		
		if (k <= 0)
			throw new IllegalArgumentException("The number of folds must be at least one");
		
		this.dataset = dataset;
		
		Set<KEY> keys = dataset.getGroups();
		
		//compute min group size
		int minGroupSize = Integer.MAX_VALUE;
		for (KEY group : keys) {
			int instancesSize = dataset.getInstances(group).size();
			if (instancesSize < minGroupSize)
				minGroupSize = instancesSize; 
		}
		
		//set the num folds
		if (k < minGroupSize)
			this.numFolds = k;
		else
			this.numFolds = minGroupSize;
		
		for (KEY group : keys) {
			int keySize = dataset.getInstances(group).size();
			
			int[] allKeyIndices = RandomData.getUniqueRandomInts(keySize, 0, keySize);
			
			subsetIndices.put(group, new int[numFolds][]);
			int[][] si = subsetIndices.get(group);
		
			int splitSize = keySize / numFolds;
			for (int i=0; i<numFolds-1; i++) { 
				si[i] = Arrays.copyOfRange(allKeyIndices, splitSize * i, splitSize * (i + 1));
			}
			si[numFolds-1] = Arrays.copyOfRange(allKeyIndices, splitSize * (numFolds - 1), allKeyIndices.length);
		}
	}
	
	/**
	 * Get the number of iterations that the {@link Iterator}
	 * returned by {@link #iterator()} will perform.
	 * 
	 * @return the number of iterations that will be performed
	 */
	public int numberIterations() {
		return numFolds;
	}
	
	/**
	 * Get the number of folds. Syntactic sugar for {@link #numberIterations()}.
	 * 
	 * @return the number of folds
	 */
	public int numberFolds() {
		return numFolds;
	}
	
	@Override
	public Iterator<ValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>> iterator() {
		return new Iterator<ValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>>() {
			int validationSubset = 0;
			
			@Override
			public boolean hasNext() {
				return validationSubset < numFolds;
			}

			@Override
			public ValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>> next() {
				Map<KEY, ListDataset<INSTANCE>> train = new HashMap<KEY, ListDataset<INSTANCE>>();
				Map<KEY, ListDataset<INSTANCE>> valid = new HashMap<KEY, ListDataset<INSTANCE>>();
				
				for (KEY group : subsetIndices.keySet()) {
					int[][] si = subsetIndices.get(group);
					
					List<INSTANCE> keyData = DatasetAdaptors.asList(dataset.getInstances(group));
					
					train.put(group, new ListBackedDataset<INSTANCE>(new SkippingListView<INSTANCE>(keyData, si[validationSubset])));
					valid.put(group, new ListBackedDataset<INSTANCE>(new AcceptingListView<INSTANCE>(keyData, si[validationSubset])));
				}
				
				MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> cvTrain = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>(train);
				MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> cvValid = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>(valid);
				
				validationSubset++;
				
				return new DefaultValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>(cvTrain, cvValid);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
