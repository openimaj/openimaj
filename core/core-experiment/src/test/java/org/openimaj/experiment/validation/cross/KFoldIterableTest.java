package org.openimaj.experiment.validation.cross;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import gnu.trove.set.hash.TIntHashSet;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.validation.ValidationData;
import org.openimaj.experiment.validation.cross.KFoldIterable;

/**
 * Tests for {@link KFoldIterable} Cross-Validation
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class KFoldIterableTest {
	ListBackedDataset<Integer> dataset;
	
	/**
	 * Create dataset with 10 items for testing
	 */
	@Before
	public void setup() {
		dataset = new ListBackedDataset<Integer>();
		
		for (int i=0; i<10; i++)
			dataset.getList().add(new Integer(i));
	}

	/**
	 * Test the iterator with 10 folds; functionally the same as
	 * LOOCV.
	 */
	@Test
	public void test10Fold() {
		KFoldIterable<Integer> iterable = new KFoldIterable<Integer>(dataset, 10);
		
		for (ValidationData<ListDataset<Integer>> cvData : iterable) {
			ListDataset<Integer> training = cvData.getTrainingDataset();
			ListDataset<Integer> validation = cvData.getValidationDataset();
			
			assertEquals(1, validation.size());
			assertEquals(dataset.size() - 1, training.size());
			assertEquals(dataset.size(), validation.size() + training.size());
			
			TIntHashSet trainingSet = new TIntHashSet();
			for (int j=0; j<training.size(); j++) {
				trainingSet.add( training.getInstance(j) );
			}
			
			assertEquals(training.size(), trainingSet.size());
			
			TIntHashSet validationSet = new TIntHashSet();
			for (int j=0; j<validation.size(); j++) {
				validationSet.add( validation.getInstance(j) );
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
		new KFoldIterable<Integer>(dataset, 100);
	}
	
	/**
	 * Test the iterator with 5 folds
	 */
	@Test
	public void test5Fold() {
		KFoldIterable<Integer> iterable = new KFoldIterable<Integer>(dataset, 5);
		
		for (ValidationData<ListDataset<Integer>> cvData : iterable) {
			ListDataset<Integer> training = cvData.getTrainingDataset();
			ListDataset<Integer> validation = cvData.getValidationDataset();
					
			assertEquals(2, validation.size());
			assertEquals(dataset.size() - 2, training.size());
			assertEquals(dataset.size(), validation.size() + training.size());
			
			TIntHashSet trainingSet = new TIntHashSet();
			for (int j=0; j<training.size(); j++) {
				trainingSet.add( training.getInstance(j) );
			}
			
			assertEquals(training.size(), trainingSet.size());
			
			TIntHashSet validationSet = new TIntHashSet();
			for (int j=0; j<validation.size(); j++) {
				validationSet.add( validation.getInstance(j) );
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
