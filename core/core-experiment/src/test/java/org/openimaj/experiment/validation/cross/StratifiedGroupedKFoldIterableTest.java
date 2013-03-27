/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.experiment.validation.cross;

import static junit.framework.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.experiment.validation.ValidationData;

/**
 * Tests for the {@link StratifiedGroupedKFold} CV scheme
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class StratifiedGroupedKFoldIterableTest {
	private MapBackedDataset<String, ListDataset<Integer>, Integer> equalDataset;
	private MapBackedDataset<String, ListDataset<Integer>, Integer> unequalDataset;

	/**
	 * Create dataset for testing
	 */
	@Before
	public void setup() {
		equalDataset = new MapBackedDataset<String, ListDataset<Integer>, Integer>();

		for (final String group : new String[] { "A", "B", "C" }) {
			equalDataset.getMap().put(group, new ListBackedDataset<Integer>());
			for (int i = 0; i < 10; i++) {
				((ListBackedDataset<Integer>) equalDataset.getMap().get(group)).add(new Integer(i));
			}
		}

		unequalDataset = new MapBackedDataset<String, ListDataset<Integer>, Integer>();
		unequalDataset.getMap().put("A", new ListBackedDataset<Integer>());
		((ListBackedDataset<Integer>) unequalDataset.getMap().get("A")).add(new Integer(1));
		((ListBackedDataset<Integer>) unequalDataset.getMap().get("A")).add(new Integer(2));
		((ListBackedDataset<Integer>) unequalDataset.getMap().get("A")).add(new Integer(3));

		unequalDataset.getMap().put("B", new ListBackedDataset<Integer>());
		((ListBackedDataset<Integer>) unequalDataset.getMap().get("B")).add(new Integer(1));
		((ListBackedDataset<Integer>) unequalDataset.getMap().get("B")).add(new Integer(2));
	}

	/**
	 * Test the iterator on the balanced dataset with 30 folds; should only
	 * create 10 folds...
	 */
	@Test
	public void testEqual30() {
		final StratifiedGroupedKFold<String, Integer> cv = new StratifiedGroupedKFold<String, Integer>(30);
		final CrossValidationIterable<GroupedDataset<String, ListDataset<Integer>, Integer>> iterable = cv
				.createIterable(equalDataset);

		assertEquals(10, iterable.numberIterations()); // would expect only 10
														// folds to be created

		for (final ValidationData<GroupedDataset<String, ListDataset<Integer>, Integer>> cvData : iterable) {
			final GroupedDataset<String, ListDataset<Integer>, Integer> training = cvData.getTrainingDataset();
			final GroupedDataset<String, ListDataset<Integer>, Integer> validation = cvData.getValidationDataset();

			assertEquals(3, validation.numInstances());
			assertEquals(equalDataset.numInstances() - 3, training.numInstances());
			assertEquals(equalDataset.numInstances(), validation.numInstances() + training.numInstances());
		}
	}

	/**
	 * Test the iterator on the balanced dataset with 5 folds
	 */
	@Test
	public void testEqual5() {
		final StratifiedGroupedKFold<String, Integer> cv = new StratifiedGroupedKFold<String, Integer>(5);
		final CrossValidationIterable<GroupedDataset<String, ListDataset<Integer>, Integer>> iterable = cv
				.createIterable(equalDataset);

		assertEquals(5, iterable.numberIterations());

		for (final ValidationData<GroupedDataset<String, ListDataset<Integer>, Integer>> cvData : iterable) {
			final GroupedDataset<String, ListDataset<Integer>, Integer> training = cvData.getTrainingDataset();
			final GroupedDataset<String, ListDataset<Integer>, Integer> validation = cvData.getValidationDataset();

			assertEquals(6, validation.numInstances());
			assertEquals(equalDataset.numInstances() - 6, training.numInstances());
			assertEquals(equalDataset.numInstances(), validation.numInstances() + training.numInstances());
		}
	}

	/**
	 * Test the iterator on the balanced dataset with 5 folds; only expect 2
	 */
	@Test
	public void testUnequal5() {
		final StratifiedGroupedKFold<String, Integer> cv = new StratifiedGroupedKFold<String, Integer>(5);
		final CrossValidationIterable<GroupedDataset<String, ListDataset<Integer>, Integer>> iterable = cv
				.createIterable(unequalDataset);

		assertEquals(2, iterable.numberIterations()); // would expect only 2
														// folds to be created

		ValidationData<GroupedDataset<String, ListDataset<Integer>, Integer>> cvData;
		GroupedDataset<String, ListDataset<Integer>, Integer> training;
		GroupedDataset<String, ListDataset<Integer>, Integer> validation;

		final Iterator<ValidationData<GroupedDataset<String, ListDataset<Integer>, Integer>>> iterator = iterable
				.iterator();

		cvData = iterator.next();
		training = cvData.getTrainingDataset();
		validation = cvData.getValidationDataset();

		assertEquals(2, validation.numInstances());
		assertEquals(unequalDataset.numInstances() - 2, training.numInstances());
		assertEquals(unequalDataset.numInstances(), validation.numInstances() + training.numInstances());

		cvData = iterator.next();
		training = cvData.getTrainingDataset();
		validation = cvData.getValidationDataset();

		assertEquals(3, validation.numInstances());
		assertEquals(unequalDataset.numInstances() - 3, training.numInstances());
		assertEquals(unequalDataset.numInstances(), validation.numInstances() + training.numInstances());
	}
}
