package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.util.function.Function;

public class WindowAverage implements Function<List<Map<String,Double>>,Map<String,Double>> {

	@Override
	public Map<String, Double> apply(List<Map<String, Double>> in) {

		Map<String, Double> ret = new HashMap<String, Double>();
		Map<String, Long> count = new HashMap<String, Long>();

		for (Map<String, Double> map : in) {
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
		return ret ;
	}
}
