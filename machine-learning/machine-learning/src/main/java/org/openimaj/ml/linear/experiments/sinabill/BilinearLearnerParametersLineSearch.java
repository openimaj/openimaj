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
package org.openimaj.ml.linear.experiments.sinabill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.util.pair.IndependentPair;

import com.google.common.primitives.Doubles;

public class BilinearLearnerParametersLineSearch implements Iterable<BilinearLearnerParameters> {
	
	private Map<String,Iterable<?>> itermap = new HashMap<String, Iterable<?>>();
	private BilinearLearnerParameters base;
	
	/**
	 * Set the base paramters. This object will be cloned and set with new values each iteration
	 * @param base
	 */
	public BilinearLearnerParametersLineSearch(BilinearLearnerParameters base) {
		this.base = base;
	}
	
	/**
	 * @param key
	 * @param range
	 */
	public <T> void addIteration(String key, Iterable<T> range){
		this.itermap.put(key,range);
	}

	@Override
	public Iterator<BilinearLearnerParameters> iterator() {
		final ArrayList<IndependentPair<String, Iterator<?>>> iterators = new ArrayList<IndependentPair<String,Iterator<?>>>();
		for (Entry<String, Iterable<?>> iterent : this.itermap.entrySet()) {
			Iterator<?> iterator = iterent.getValue().iterator();
//			iterent.getKey(),iterator
			IndependentPair<String, Iterator<?>> pair = new IndependentPair<String,Iterator<?>>(iterent.getKey(), iterator);
			iterators.add(pair);
		}
		final ArrayList<IndependentPair<String, Object>> current = new ArrayList<IndependentPair<String,Object>>();
		
		return new Iterator<BilinearLearnerParameters>() {
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public BilinearLearnerParameters next() {
				if(current.size() ==  0){
					// It is currently empty, get the first of everything
					for (IndependentPair<String,Iterator<?>> iterip : iterators) {
						Object next = iterip.secondObject().next();
						current.add(IndependentPair.pair(iterip.firstObject(),next));
					}
				} else {
					for (int i = 0; i < iterators.size(); i++) {
						IndependentPair<String, Iterator<?>> namedIterator = iterators.get(i);
						String name = namedIterator.firstObject();
						if(namedIterator.secondObject().hasNext()){
							Object next = namedIterator.secondObject().next();
							current.set(i, IndependentPair.pair(name,next));
							break;
						} else {
							// Refresh this iterator
							Iterator<?> valueIter = itermap.get(name).iterator();
							IndependentPair<String, Iterator<?>> pair = new IndependentPair<String,Iterator<?>>(name, valueIter);
							iterators.set(i, pair);
							Object next = valueIter.next();
							current.set(i,IndependentPair.pair(name,next));
							// and carry on to the next one because we need to tick over
						}
					}
				}
				
				// Turn the current into a BilinearLearnerParameters
				BilinearLearnerParameters params = base.clone();
				for (IndependentPair<String, Object> namedObject : current) {
					params.put(namedObject.firstObject(), namedObject.secondObject());
				}
				return params;
			}
			
			@Override
			public boolean hasNext() {
				for (IndependentPair<String, Iterator<?>> independentPair : iterators) {
					if(independentPair.secondObject().hasNext()) return true;
				}
				return false;
			}
		};
	}
	
	public static void main(String[] args) {
		BilinearLearnerParametersLineSearch ls = new BilinearLearnerParametersLineSearch(new BilinearLearnerParameters());
		ls.addIteration(BilinearLearnerParameters.ETA0_U, Doubles.asList(new double[]{0.1,0.01}));
		ls.addIteration(BilinearLearnerParameters.ETA0_W, Doubles.asList(new double[]{51,52,53}));
		
		for (BilinearLearnerParameters bilinearLearnerParameters : ls) {
			System.out.println(bilinearLearnerParameters);
		}
	}

	

}
