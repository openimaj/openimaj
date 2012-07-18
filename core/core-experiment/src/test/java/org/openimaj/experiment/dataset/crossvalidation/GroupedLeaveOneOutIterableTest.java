package org.openimaj.experiment.dataset.crossvalidation;

import static junit.framework.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;

/**
 * Tests for the {@link GroupedLeaveOneOutIterable}
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class GroupedLeaveOneOutIterableTest {
	private MapBackedDataset<String, ListBackedDataset<Integer>, Integer> equalDataset;
	private MapBackedDataset<String, ListBackedDataset<Integer>, Integer> unequalDataset;
	
	/**
	 * Create dataset for testing
	 */
	@Before
	public void setup() {
		equalDataset = new MapBackedDataset<String, ListBackedDataset<Integer>, Integer>();
		
		for (String group : new String[]{ "A", "B", "C"} ) {
			equalDataset.getMap().put(group, new ListBackedDataset<Integer>());
			for (int i=0; i<10; i++) {
				equalDataset.getMap().get(group).add(new Integer(i));
			}
		}
		
		unequalDataset = new MapBackedDataset<String, ListBackedDataset<Integer>, Integer>();
		unequalDataset.getMap().put("A", new ListBackedDataset<Integer>());
		unequalDataset.getMap().get("A").add(new Integer(1));
		unequalDataset.getMap().get("A").add(new Integer(2));
		unequalDataset.getMap().get("A").add(new Integer(3));
		
		unequalDataset.getMap().put("B", new ListBackedDataset<Integer>());
		unequalDataset.getMap().get("B").add(new Integer(1));
		unequalDataset.getMap().get("B").add(new Integer(2));
	}

	/**
	 * Test the iterator on the balanced dataset
	 */
	@Test
	public void testEqual() {
		GroupedLeaveOneOutIterable<String, Integer> iterable = new GroupedLeaveOneOutIterable<String, Integer>(equalDataset);
		
		for (CrossValidationData<GroupedDataset<String, ListDataset<Integer>, Integer>> cvData : iterable) {
			GroupedDataset<String, ListDataset<Integer>, Integer> training = cvData.getTrainingDataset();
			GroupedDataset<String, ListDataset<Integer>, Integer> validation = cvData.getValidationDataset();
			
			assertEquals(1, validation.size());
			assertEquals(equalDataset.size() - 1, training.size());
			assertEquals(equalDataset.size(), validation.size() + training.size());
		}
	}
	
	/**
	 * Test the iterator on the balanced dataset
	 */
	@Test
	public void testUnequal() {
		GroupedLeaveOneOutIterable<String, Integer> iterable = new GroupedLeaveOneOutIterable<String, Integer>(unequalDataset);
		
		for (CrossValidationData<GroupedDataset<String, ListDataset<Integer>, Integer>> cvData : iterable) {
			GroupedDataset<String, ListDataset<Integer>, Integer> training = cvData.getTrainingDataset();
			GroupedDataset<String, ListDataset<Integer>, Integer> validation = cvData.getValidationDataset();
			
			assertEquals(1, validation.size());
			assertEquals(unequalDataset.size() - 1, training.size());
			assertEquals(unequalDataset.size(), validation.size() + training.size());
		}
	}
}
