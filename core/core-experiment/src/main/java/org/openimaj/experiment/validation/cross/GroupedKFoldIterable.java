package org.openimaj.experiment.validation.cross;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.data.RandomData;
import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.experiment.validation.ValidationData;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.list.SkippingListView;
import org.openimaj.util.pair.IntObjectPair;

/**
 * An iterator for K-Fold Cross-Validation on grouped datasets.
 * <p>
 * All the instances are split into k subsets. The validation data
 * in each iteration is one of the subsets, whilst the training
 * data is the remaindering subsets. The subsets are not guaranteed
 * to have any particular balance of groups as the splitting is
 * completely random; however if there is the same number of instances 
 * per group, then the subsets should be balanced on average. A
 * particular fold <b>could</b> potentially have no training or
 * validation data for a particular class.
 * <p>
 * Setting the number of splits to be equal to the number of total
 * instances is equivalent to LOOCV. If LOOCV is the aim, the 
 * {@link GroupedLeaveOneOutIterable} class is a more efficient
 * implementation than this class. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <KEY> Type of groups
 * @param <INSTANCE> Type of instances 
 */
public class GroupedKFoldIterable<KEY, INSTANCE> implements Iterable<ValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>> {
	private GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset;
	private Map<KEY, int[][]> subsetIndices = new HashMap<KEY, int[][]>();
	private int numFolds;
	
	/**
	 * Construct the {@link GroupedKFoldIterable} with the given dataset
	 * and number of folds.
	 * 
	 * @param dataset the dataset
	 * @param k the target number of folds.
	 */
	public GroupedKFoldIterable(GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset, int k) {
		if (k > dataset.size())
			throw new IllegalArgumentException("The number of folds must be less than the number of items in the dataset");
		
		if (k <= 0)
			throw new IllegalArgumentException("The number of folds must be at least one");
		
		this.dataset = dataset;
		this.numFolds = k;
		
		int[] allIndices = RandomData.getUniqueRandomInts(dataset.size(), 0, dataset.size());
		int[][] flatSubsetIndices = new int[k][];
		
		int splitSize = dataset.size() / k;
		for (int i=0; i<k-1; i++) { 
			flatSubsetIndices[i] = Arrays.copyOfRange(allIndices, splitSize * i, splitSize * (i + 1));
		}
		flatSubsetIndices[k-1] = Arrays.copyOfRange(allIndices, splitSize * (k - 1), allIndices.length);
		
		ArrayList<KEY> groups = new ArrayList<KEY>(dataset.getGroups());
		
		for (KEY key : groups) {
			subsetIndices.put(key, new int[k][]);
		}
		
		for (int i=0; i<flatSubsetIndices.length; i++) {
			Map<KEY, TIntArrayList> tmp = new HashMap<KEY, TIntArrayList>();
			
			for (int flatIdx : flatSubsetIndices[i]) {
				IntObjectPair<KEY> idx = computeIndex(groups, flatIdx);
				
				TIntArrayList list = tmp.get(idx.second);
				if (list == null) tmp.put(idx.second, list = new TIntArrayList());
				list.add(idx.first);
			}
			
			for (Entry<KEY, TIntArrayList> kv : tmp.entrySet()) {
				subsetIndices.get(kv.getKey())[i] = kv.getValue().toArray();
			}
		}
	}
	
	private IntObjectPair<KEY> computeIndex(ArrayList<KEY> groups, int flatIdx) {
		int count = 0;
		
		for (KEY group : groups) {
			ListDataset<INSTANCE> instances = dataset.getInstances(group);
			int size = instances.size();
			
			if (count + size <= flatIdx) {
				count += size;
			} else {
				return new IntObjectPair<KEY>(flatIdx - count, group);
			}
		}
		
		throw new RuntimeException("Index not found");
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
				
				return new ValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>(cvTrain, cvValid);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
