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
