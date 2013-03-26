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
import gnu.trove.set.hash.TIntHashSet;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.experiment.validation.ValidationData;

/**
 * Tests for {@link KFold} Cross-Validation
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class KFoldTest {
	ListBackedDataset<Integer> dataset;
	
	/**
	 * Create dataset with 10 items for testing
	 */
	@Before
	public void setup() {
		dataset = new ListBackedDataset<Integer>();
		
		for (int i=0; i<10; i++)
			dataset.getList().add(new Integer(i));
	}

	/**
	 * Test the iterator with 10 folds; functionally the same as
	 * LOOCV.
	 */
	@Test
	public void test10Fold() {
		KFold<Integer> cv = new KFold<Integer>(10);
		CrossValidationIterable<ListDataset<Integer>> iterable = cv.createIterable(dataset);
		
		for (ValidationData<ListDataset<Integer>> cvData : iterable) {
			ListDataset<Integer> training = cvData.getTrainingDataset();
			ListDataset<Integer> validation = cvData.getValidationDataset();
			
			assertEquals(1, validation.size());
			assertEquals(dataset.size() - 1, training.size());
			assertEquals(dataset.size(), validation.size() + training.size());
			
			TIntHashSet trainingSet = new TIntHashSet();
			for (int j=0; j<training.size(); j++) {
				trainingSet.add( training.getInstance(j) );
			}
			
			assertEquals(training.size(), trainingSet.size());
			
			TIntHashSet validationSet = new TIntHashSet();
			for (int j=0; j<validation.size(); j++) {
				validationSet.add( validation.getInstance(j) );
			}
			
			assertEquals(validation.size(), validationSet.size());

			for (int i : trainingSet.toArray()) {
				assertFalse(validationSet.contains(i));
			}
			
			for (int i : validationSet.toArray()) {
				assertFalse(trainingSet.contains(i));
			}			
		}
	}
	
	/**
	 * Test the iterator with 100 folds; should throw
	 */
	@Test(expected = IllegalArgumentException.class)
	public void test100Fold() {
		KFold<Integer> cv = new KFold<Integer>(100);
		cv.createIterable(dataset);
	}
	
	/**
	 * Test the iterator with 5 folds
	 */
	@Test
	public void test5Fold() {
		KFold<Integer> cv= new KFold<Integer>(5);
		CrossValidationIterable<ListDataset<Integer>> iterable = cv.createIterable(dataset);
		
		for (ValidationData<ListDataset<Integer>> cvData : iterable) {
			ListDataset<Integer> training = cvData.getTrainingDataset();
			ListDataset<Integer> validation = cvData.getValidationDataset();
					
			assertEquals(2, validation.size());
			assertEquals(dataset.size() - 2, training.size());
			assertEquals(dataset.size(), validation.size() + training.size());
			
			TIntHashSet trainingSet = new TIntHashSet();
			for (int j=0; j<training.size(); j++) {
				trainingSet.add( training.getInstance(j) );
			}
			
			assertEquals(training.size(), trainingSet.size());
			
			TIntHashSet validationSet = new TIntHashSet();
			for (int j=0; j<validation.size(); j++) {
				validationSet.add( validation.getInstance(j) );
			}
			
			assertEquals(validation.size(), validationSet.size());

			for (int i : trainingSet.toArray()) {
				assertFalse(validationSet.contains(i));
			}
			
			for (int i : validationSet.toArray()) {
				assertFalse(trainingSet.contains(i));
			}			
		}
	}
}
