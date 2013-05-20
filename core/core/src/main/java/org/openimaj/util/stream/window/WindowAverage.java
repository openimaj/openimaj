package org.openimaj.util.stream.window;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.util.function.Function;

/**
 * Given a window of key,value map instances, this function gets the average of the
 * window
 * @author Jonathan Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk), David Duplaw (dpd@ecs.soton.ac.uk)
 * @param <W>
 * @param <T>
 *
 */
public class WindowAverage<W extends Aggregation<List<Map<String,Double>>,T>,T> implements Function<W,Aggregation<Map<String,Double>,T>> {

	@Override
	public Aggregation<Map<String, Double>,T> apply(W in) {

		Map<String, Double> ret = new HashMap<String, Double>();
		Map<String, Long> count = new HashMap<String, Long>();

		for (Map<String, Double> map : in.getPayload()) {
			for (Entry<String, Double> item : map.entrySet()) {
				String key = item.getKey();
				if(!count.containsKey(key)){
					ret.put(key, item.getValue());
					count.put(key, 1l);
				}
				else{
					ret.put(key, ret.get(key) + item.getValue());
					count.put(key, count.get(key) + 1);
				}
			}
		}
		for (Entry<String, Double> map : ret.entrySet()) {
			String key = map.getKey();
			ret.put(key, map.getValue()/count.get(key));
		}
		return new Aggregation<Map<String,Double>, T>(ret, in.getMeta()) ;
	}

}
