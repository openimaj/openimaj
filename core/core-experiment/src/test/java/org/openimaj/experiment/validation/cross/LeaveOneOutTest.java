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
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import gnu.trove.set.hash.TIntHashSet;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
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
