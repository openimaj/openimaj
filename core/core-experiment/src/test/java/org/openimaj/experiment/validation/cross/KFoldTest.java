package org.openimaj.experiment.validation.cross;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import gnu.trove.set.hash.TIntHashSet;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.validation.ValidationData;

/**
 * Tests for {@link KFold} Cross-Validation
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class KFoldTest {
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
		KFold<Integer> cv = new KFold<Integer>(10);
		CrossValidationIterable<ListDataset<Integer>> iterable = cv.createIterable(dataset);
		
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
		KFold<Integer> cv = new KFold<Integer>(100);
		cv.createIterable(dataset);
	}
	
	/**
	 * Test the iterator with 5 folds
	 */
	@Test
	public void test5Fold() {
		KFold<Integer> cv= new KFold<Integer>(5);
		CrossValidationIterable<ListDataset<Integer>> iterable = cv.createIterable(dataset);
		
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
