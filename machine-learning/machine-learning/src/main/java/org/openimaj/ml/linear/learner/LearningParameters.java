package org.openimaj.ml.linear.learner;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class LearningParameters extends HashMap<String, Object> {
	protected class Placeholder {
		public Placeholder(String name) {
			this.name = name;
		}

		public String name;
	}

	private static final long serialVersionUID = 8150107681506488394L;

	Map<String, Object> defaults = new HashMap<String, Object>();

	public LearningParameters() {
	}

	public LearningParameters(Map<String, Object> defaults) {
		this.defaults.putAll(defaults);
	}

	@SuppressWarnings("unchecked")
	public <T> T getTyped(String s) {
		Object thisVal = this.get(s);
		if (thisVal == null) {
			thisVal = defaults.get(s);
		}
		if (thisVal != null && thisVal instanceof Placeholder) {
			return getTyped(((Placeholder) thisVal).name);
		}
		return (T) thisVal;
	}
}
