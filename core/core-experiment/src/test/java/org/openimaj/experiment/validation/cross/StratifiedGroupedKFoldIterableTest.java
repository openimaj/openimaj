package org.openimaj.experiment.validation.cross;

import static junit.framework.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;
import org.openimaj.experiment.validation.ValidationData;
import org.openimaj.experiment.validation.cross.StratifiedGroupedKFoldIterable;

/**
 * Tests for the {@link StratifiedGroupedKFoldIterable} CV scheme
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class StratifiedGroupedKFoldIterableTest {
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
	 * Test the iterator on the balanced dataset with 30 folds; should only create 10 folds...
	 */
	@Test
	public void testEqual30() {
		StratifiedGroupedKFoldIterable<String, Integer> iterable = new StratifiedGroupedKFoldIterable<String, Integer>(equalDataset, 30);
		
		assertEquals(10, iterable.numberFolds()); //would expect only 10 folds to be created
		
		for (ValidationData<GroupedDataset<String, ListDataset<Integer>, Integer>> cvData : iterable) {
			GroupedDataset<String, ListDataset<Integer>, Integer> training = cvData.getTrainingDataset();
			GroupedDataset<String, ListDataset<Integer>, Integer> validation = cvData.getValidationDataset();
			
			assertEquals(3, validation.size());
			assertEquals(equalDataset.size() - 3, training.size());
			assertEquals(equalDataset.size(), validation.size() + training.size());
		}
	}
	
	/**
	 * Test the iterator on the balanced dataset with 5 folds
	 */
	@Test
	public void testEqual5() {
		StratifiedGroupedKFoldIterable<String, Integer> iterable = new StratifiedGroupedKFoldIterable<String, Integer>(equalDataset, 5);

		assertEquals(5, iterable.numberFolds());
		
		for (ValidationData<GroupedDataset<String, ListDataset<Integer>, Integer>> cvData : iterable) {
			GroupedDataset<String, ListDataset<Integer>, Integer> training = cvData.getTrainingDataset();
			GroupedDataset<String, ListDataset<Integer>, Integer> validation = cvData.getValidationDataset();
			
			assertEquals(6, validation.size());
			assertEquals(equalDataset.size() - 6, training.size());
			assertEquals(equalDataset.size(), validation.size() + training.size());
		}
	}
	
	/**
	 * Test the iterator on the balanced dataset with 5 folds; only expect 2 
	 */
	@Test
	public void testUnequal5() {
		StratifiedGroupedKFoldIterable<String, Integer> iterable = new StratifiedGroupedKFoldIterable<String, Integer>(unequalDataset, 5);

		assertEquals(2, iterable.numberFolds()); //would expect only 2 folds to be created
		
		ValidationData<GroupedDataset<String, ListDataset<Integer>, Integer>> cvData;
		GroupedDataset<String, ListDataset<Integer>, Integer> training;
		GroupedDataset<String, ListDataset<Integer>, Integer> validation;
		
		Iterator<ValidationData<GroupedDataset<String, ListDataset<Integer>, Integer>>> iterator = iterable.iterator();
		
		cvData = iterator.next();
		training = cvData.getTrainingDataset();
		validation = cvData.getValidationDataset();

		assertEquals(2, validation.size());
		assertEquals(unequalDataset.size() - 2, training.size());
		assertEquals(unequalDataset.size(), validation.size() + training.size());
		
		cvData = iterator.next();
		training = cvData.getTrainingDataset();
		validation = cvData.getValidationDataset();

		assertEquals(3, validation.size());
		assertEquals(unequalDataset.size() - 3, training.size());
		assertEquals(unequalDataset.size(), validation.size() + training.size());
	}
}
