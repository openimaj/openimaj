package org.openimaj.ml.dataset;

import static org.junit.Assert.*;

import org.junit.Test;


/**
 * Test the wine dataset
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestWineDataset {
	/**
	 * 
	 */
	@Test
	public void testwinedataset() {
		WineDataset ds = new WineDataset();
		assertTrue(ds.numInstances() == 178);
		assertTrue(ds.size() == 3);
	}
}
