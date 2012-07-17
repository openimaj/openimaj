package org.openimaj.experiment.dataset.crossvalidation;

import static junit.framework.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.Identifiable;
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
	 * Test the iterator on the balanced dataset
	 */
	@Test
	public void testEqual() {
		GroupedLeaveOneOutIterable<String, IntId> iterable = new GroupedLeaveOneOutIterable<String, IntId>(equalDataset);
		
		for (CrossValidationData<GroupedDataset<String, ListDataset<IntId>, IntId>> cvData : iterable) {
			GroupedDataset<String, ListDataset<IntId>, IntId> training = cvData.getTrainingDataset();
			GroupedDataset<String, ListDataset<IntId>, IntId> validation = cvData.getValidationDataset();
			
			assertEquals(1, validation.size());
			assertEquals(equalDataset.size() - 1, training.size());
		}
	}
	
	/**
	 * Test the iterator on the balanced dataset
	 */
	@Test
	public void testUnequal() {
		GroupedLeaveOneOutIterable<String, IntId> iterable = new GroupedLeaveOneOutIterable<String, IntId>(unequalDataset);
		
		for (CrossValidationData<GroupedDataset<String, ListDataset<IntId>, IntId>> cvData : iterable) {
			GroupedDataset<String, ListDataset<IntId>, IntId> training = cvData.getTrainingDataset();
			GroupedDataset<String, ListDataset<IntId>, IntId> validation = cvData.getValidationDataset();
			
			assertEquals(1, validation.size());
			assertEquals(unequalDataset.size() - 1, training.size());
		}
	}
}
