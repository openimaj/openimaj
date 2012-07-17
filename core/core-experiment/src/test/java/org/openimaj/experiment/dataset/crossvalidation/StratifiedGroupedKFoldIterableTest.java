package org.openimaj.experiment.dataset.crossvalidation;

import static junit.framework.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;

/**
 * Tests for the {@link StratifiedGroupedKFoldIterable} CV scheme
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class StratifiedGroupedKFoldIterableTest {
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
	
	private MapBackedDataset<String, ListBackedDataset<IntId>, IntId> equalDataset;
	private MapBackedDataset<String, ListBackedDataset<IntId>, IntId> unequalDataset;
	
	/**
	 * Create dataset for testing
	 */
	@Before
	public void setup() {
		equalDataset = new MapBackedDataset<String, ListBackedDataset<IntId>, IntId>();
		
		for (String group : new String[]{ "A", "B", "C"} ) {
			equalDataset.getMap().put(group, new ListBackedDataset<IntId>());
			for (int i=0; i<10; i++) {
				equalDataset.getMap().get(group).add(new IntId(i));
			}
		}
		
		unequalDataset = new MapBackedDataset<String, ListBackedDataset<IntId>, IntId>();
		unequalDataset.getMap().put("A", new ListBackedDataset<IntId>());
		unequalDataset.getMap().get("A").add(new IntId(1));
		unequalDataset.getMap().get("A").add(new IntId(2));
		unequalDataset.getMap().get("A").add(new IntId(3));
		
		unequalDataset.getMap().put("B", new ListBackedDataset<IntId>());
		unequalDataset.getMap().get("B").add(new IntId(1));
		unequalDataset.getMap().get("B").add(new IntId(2));
	}

	/**
	 * Test the iterator on the balanced dataset with 30 folds; should only create 10 folds...
	 */
	@Test
	public void testEqual30() {
		StratifiedGroupedKFoldIterable<String, IntId> iterable = new StratifiedGroupedKFoldIterable<String, IntId>(equalDataset, 30);
		
		assertEquals(10, iterable.numberFolds()); //would expect only 10 folds to be created
		
		for (CrossValidationData<GroupedDataset<String, ListDataset<IntId>, IntId>> cvData : iterable) {
			GroupedDataset<String, ListDataset<IntId>, IntId> training = cvData.getTrainingDataset();
			GroupedDataset<String, ListDataset<IntId>, IntId> validation = cvData.getValidationDataset();
			
			assertEquals(3, validation.size());
			assertEquals(equalDataset.size() - 3, training.size());
		}
	}
	
	/**
	 * Test the iterator on the balanced dataset with 5 folds
	 */
	@Test
	public void testEqual5() {
		StratifiedGroupedKFoldIterable<String, IntId> iterable = new StratifiedGroupedKFoldIterable<String, IntId>(equalDataset, 5);

		assertEquals(5, iterable.numberFolds());
		
		for (CrossValidationData<GroupedDataset<String, ListDataset<IntId>, IntId>> cvData : iterable) {
			GroupedDataset<String, ListDataset<IntId>, IntId> training = cvData.getTrainingDataset();
			GroupedDataset<String, ListDataset<IntId>, IntId> validation = cvData.getValidationDataset();
			
			assertEquals(6, validation.size());
			assertEquals(equalDataset.size() - 6, training.size());
		}
	}
	
	/**
	 * Test the iterator on the balanced dataset with 5 folds; only expect 2 
	 */
	@Test
	public void testUnequal5() {
		StratifiedGroupedKFoldIterable<String, IntId> iterable = new StratifiedGroupedKFoldIterable<String, IntId>(unequalDataset, 5);

		assertEquals(2, iterable.numberFolds()); //would expect only 2 folds to be created
		
		CrossValidationData<GroupedDataset<String, ListDataset<IntId>, IntId>> cvData;
		GroupedDataset<String, ListDataset<IntId>, IntId> training;
		GroupedDataset<String, ListDataset<IntId>, IntId> validation;
		
		Iterator<CrossValidationData<GroupedDataset<String, ListDataset<IntId>, IntId>>> iterator = iterable.iterator();
		
		cvData = iterator.next();
		training = cvData.getTrainingDataset();
		validation = cvData.getValidationDataset();

		assertEquals(2, validation.size());
		assertEquals(unequalDataset.size() - 2, training.size());
		
		cvData = iterator.next();
		training = cvData.getTrainingDataset();
		validation = cvData.getValidationDataset();

		assertEquals(3, validation.size());
		assertEquals(unequalDataset.size() - 3, training.size());
	}
}
