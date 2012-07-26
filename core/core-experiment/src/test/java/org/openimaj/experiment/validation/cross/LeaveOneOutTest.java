package org.openimaj.experiment.validation.cross;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import gnu.trove.set.hash.TIntHashSet;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.validation.ValidationData;

/**
 * Tests for the {@link LeaveOneOut}
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class LeaveOneOutTest {
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
		LeaveOneOut<Integer> cv = new LeaveOneOut<Integer>();
		CrossValidationIterable<ListDataset<Integer>> iterable = cv.createIterable(dataset);
		
		int i = 0;
		for (ValidationData<ListDataset<Integer>> cvData : iterable) {
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
