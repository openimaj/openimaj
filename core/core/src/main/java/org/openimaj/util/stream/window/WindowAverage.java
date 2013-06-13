package org.openimaj.util.stream.window;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.util.function.Function;

/**
 * Given a window of key,value map instances, this function gets the average of
 * the window
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class WindowAverage implements Function<List<Map<String, Double>>, Map<String, Double>> {

	@Override
	public Map<String, Double> apply(List<Map<String, Double>> in) {

		final Map<String, Double> ret = new HashMap<String, Double>();
		final Map<String, Long> count = new HashMap<String, Long>();

		for (final Map<String, Double> map : in) {
			for (final Entry<String, Double> item : map.entrySet()) {
				final String key = item.getKey();
				if (!count.containsKey(key)) {
					ret.put(key, item.getValue());
					count.put(key, 1l);
				}
				else {
					ret.put(key, ret.get(key) + item.getValue());
					count.put(key, count.get(key) + 1);
				}
			}
		}
		for (final Entry<String, Double> map : ret.entrySet()) {
			final String key = map.getKey();
			ret.put(key, map.getValue() / count.get(key));
		}
		return ret;
	}

}
