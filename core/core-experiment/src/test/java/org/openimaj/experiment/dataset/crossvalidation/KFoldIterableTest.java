package org.openimaj.experiment.dataset.crossvalidation;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import gnu.trove.set.hash.TIntHashSet;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;

/**
 * Tests for {@link KFoldIterable} Cross-Validation
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class KFoldIterableTest {
	class IntId implements Identifiable {
		int id;

		public IntId(int i) {
			this.id = i;
		}

		@Override
		public String getID() {
			return id + "";
		}
		
		@Override
		public String toString() {
			return getID();
		}
	}
	
	ListBackedDataset<IntId> dataset;
	
	/**
	 * Create dataset with 10 items for testing
	 */
	@Before
	public void setup() {
		dataset = new ListBackedDataset<IntId>();
		
		for (int i=0; i<10; i++)
			dataset.getList().add(new IntId(i));
	}

	/**
	 * Test the iterator with 10 folds; functionally the same as
	 * LOOCV.
	 */
	@Test
	public void test10Fold() {
		KFoldIterable<IntId> iterable = new KFoldIterable<IntId>(dataset, 10);
		
		for (CrossValidationData<ListDataset<IntId>> cvData : iterable) {
			ListDataset<IntId> training = cvData.getTrainingDataset();
			ListDataset<IntId> validation = cvData.getValidationDataset();
			
			assertEquals(1, validation.size());
			assertEquals(dataset.size() - 1, training.size());
			
			TIntHashSet trainingSet = new TIntHashSet();
			for (int j=0; j<training.size(); j++) {
				trainingSet.add( training.getInstance(j).id );
			}
			
			assertEquals(training.size(), trainingSet.size());
			
			TIntHashSet validationSet = new TIntHashSet();
			for (int j=0; j<validation.size(); j++) {
				validationSet.add( validation.getInstance(j).id );
			}
			
			assertEquals(validation.size(), validationSet.size());

			for (int i : trainingSet.toArray()) {
				assertFalse(validationSet.contains(i));
			}
			
			for (int i : validationSet.toArray()) {
				assertFalse(trainingSet.contains(i));
			}
		}
	}
	
	/**
	 * Test the iterator with 100 folds; should throw
	 */
	@Test(expected = IllegalArgumentException.class)
	public void test100Fold() {
		new KFoldIterable<IntId>(dataset, 100);
	}
	
	/**
	 * Test the iterator with 5 folds
	 */
	@Test
	public void test5Fold() {
		KFoldIterable<IntId> iterable = new KFoldIterable<IntId>(dataset, 5);
		
		for (CrossValidationData<ListDataset<IntId>> cvData : iterable) {
			ListDataset<IntId> training = cvData.getTrainingDataset();
			ListDataset<IntId> validation = cvData.getValidationDataset();
					
			assertEquals(2, validation.size());
			assertEquals(dataset.size() - 2, training.size());
			
			TIntHashSet trainingSet = new TIntHashSet();
			for (int j=0; j<training.size(); j++) {
				trainingSet.add( training.getInstance(j).id );
			}
			
			assertEquals(training.size(), trainingSet.size());
			
			TIntHashSet validationSet = new TIntHashSet();
			for (int j=0; j<validation.size(); j++) {
				validationSet.add( validation.getInstance(j).id );
			}
			
			assertEquals(validation.size(), validationSet.size());

			for (int i : trainingSet.toArray()) {
				assertFalse(validationSet.contains(i));
			}
			
			for (int i : validationSet.toArray()) {
				assertFalse(trainingSet.contains(i));
			}
		}
	}
}
