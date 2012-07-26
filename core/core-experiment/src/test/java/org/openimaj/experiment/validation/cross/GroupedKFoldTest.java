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
 * Tests for the {@link GroupedKFold} CV scheme
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class GroupedKFoldTest {
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
	 * Test the iterator on the balanced dataset with 30 folds. Equivalent to LOOCV
	 */
	@Test
	public void testEqual30() {
		GroupedKFold<String, Integer> cv = new GroupedKFold<String, Integer>(30);
		CrossValidationIterable<GroupedDataset<String, ListDataset<Integer>, Integer>> iterable = cv.createIterable(equalDataset);
		
		assertEquals(30, iterable.numberIterations());
		
		for (ValidationData<GroupedDataset<String, ListDataset<Integer>, Integer>> cvData : iterable) {
			GroupedDataset<String, ListDataset<Integer>, Integer> training = cvData.getTrainingDataset();
			GroupedDataset<String, ListDataset<Integer>, Integer> validation = cvData.getValidationDataset();
						
			assertEquals(1, validation.size());
			assertEquals(equalDataset.size() - 1, training.size());
			assertEquals(equalDataset.size(), validation.size() + training.size());
		}
	}
	
	/**
	 * Test the iterator on the balanced dataset with 10 folds.
	 */
	@Test
	public void testEqual10() {
		GroupedKFold<String, Integer> cv = new GroupedKFold<String, Integer>(10);
		CrossValidationIterable<GroupedDataset<String, ListDataset<Integer>, Integer>> iterable = cv.createIterable(equalDataset);
		
		assertEquals(10, iterable.numberIterations());
		
		for (ValidationData<GroupedDataset<String, ListDataset<Integer>, Integer>> cvData : iterable) {
			GroupedDataset<String, ListDataset<Integer>, Integer> training = cvData.getTrainingDataset();
			GroupedDataset<String, ListDataset<Integer>, Integer> validation = cvData.getValidationDataset();
						
			assertEquals(3, validation.size());
			assertEquals(equalDataset.size() - 3, training.size());
			assertEquals(equalDataset.size(), validation.size() + training.size());
		}
	}
}
