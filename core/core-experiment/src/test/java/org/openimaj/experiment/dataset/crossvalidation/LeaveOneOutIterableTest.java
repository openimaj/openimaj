package org.openimaj.experiment.dataset.crossvalidation;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import gnu.trove.set.hash.TIntHashSet;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;

/**
 * Tests for the {@link LeaveOneOutIterable}
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class LeaveOneOutIterableTest {
	ListBackedDataset<Integer> dataset;
	
	/**
	 * Create dataset for testing
	 */
	@Before
	public void setup() {
		dataset = new ListBackedDataset<Integer>();
		
		for (int i=0; i<10; i++)
			dataset.getList().add(new Integer(i));
	}

	/**
	 * Test the iterator
	 */
	@Test
	public void test() {
		LeaveOneOutIterable<Integer> iterable = new LeaveOneOutIterable<Integer>(dataset);
		
		int i = 0;
		for (CrossValidationData<ListDataset<Integer>> cvData : iterable) {
			ListDataset<Integer> training = cvData.getTrainingDataset();
			ListDataset<Integer> validation = cvData.getValidationDataset();
			
			assertEquals(1, validation.size());
			assertEquals(dataset.size() - 1, training.size());
			assertEquals(dataset.size(), validation.size() + training.size());
			
			assertEquals(i, (int)validation.getInstance(0));
			
			TIntHashSet set = new TIntHashSet();
			for (int j=0; j<training.size(); j++) {
				set.add( training.getInstance(j) );
			}
			
			assertEquals(training.size(), set.size());
			assertFalse(set.contains(i));
			
			for (int j=0; j<i; j++)
				assertTrue(set.contains(j));
			for (int j=i+1; j<dataset.size(); j++)
				assertTrue(set.contains(j));
			
			i++;
		}
	}
}
