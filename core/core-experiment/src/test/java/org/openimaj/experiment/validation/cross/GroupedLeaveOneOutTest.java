package org.openimaj.experiment.validation.cross;

import static junit.framework.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;
import org.openimaj.experiment.validation.ValidationData;

/**
 * Tests for the {@link GroupedLeaveOneOut}
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class GroupedLeaveOneOutTest {
	private MapBackedDataset<String, ListDataset<Integer>, Integer> equalDataset;
	private MapBackedDataset<String, ListDataset<Integer>, Integer> unequalDataset;
	
	/**
	 * Create dataset for testing
	 */
	@Before
	public void setup() {
		equalDataset = new MapBackedDataset<String, ListDataset<Integer>, Integer>();
		
		for (String group : new String[]{ "A", "B", "C"} ) {
			equalDataset.getMap().put(group, new ListBackedDataset<Integer>());
			for (int i=0; i<10; i++) {
				((ListBackedDataset<Integer>)equalDataset.getMap().get(group)).add(new Integer(i));
			}
		}
		
		unequalDataset = new MapBackedDataset<String, ListDataset<Integer>, Integer>();
		unequalDataset.getMap().put("A", new ListBackedDataset<Integer>());
		((ListBackedDataset<Integer>)unequalDataset.getMap().get("A")).add(new Integer(1));
		((ListBackedDataset<Integer>)unequalDataset.getMap().get("A")).add(new Integer(2));
		((ListBackedDataset<Integer>)unequalDataset.getMap().get("A")).add(new Integer(3));
		
		unequalDataset.getMap().put("B", new ListBackedDataset<Integer>());
		((ListBackedDataset<Integer>)unequalDataset.getMap().get("B")).add(new Integer(1));
		((ListBackedDataset<Integer>)unequalDataset.getMap().get("B")).add(new Integer(2));
	}

	/**
	 * Test the iterator on the balanced dataset
	 */
	@Test
	public void testEqual() {
		GroupedLeaveOneOut<String, Integer> cv = new GroupedLeaveOneOut<String, Integer>();
		CrossValidationIterable<GroupedDataset<String, ListDataset<Integer>, Integer>> iterable = cv.createIterable(equalDataset);
		
		for (ValidationData<GroupedDataset<String, ListDataset<Integer>, Integer>> cvData : iterable) {
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
		GroupedLeaveOneOut<String, Integer> cv = new GroupedLeaveOneOut<String, Integer>();
		CrossValidationIterable<GroupedDataset<String, ListDataset<Integer>, Integer>> iterable = cv.createIterable(unequalDataset);
		
		for (ValidationData<GroupedDataset<String, ListDataset<Integer>, Integer>> cvData : iterable) {
			GroupedDataset<String, ListDataset<Integer>, Integer> training = cvData.getTrainingDataset();
			GroupedDataset<String, ListDataset<Integer>, Integer> validation = cvData.getValidationDataset();
			
			assertEquals(1, validation.size());
			assertEquals(unequalDataset.size() - 1, training.size());
			assertEquals(unequalDataset.size(), validation.size() + training.size());
		}
	}
}
