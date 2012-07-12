package org.kohsuke.args4j.spi;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.pair.IndependentPair;

/**
 * A field getter calls .toString() on the underlying object
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class FieldGetter extends AbstractGetter<String> {

	/**
	 * @param name
	 * @param bean
	 * @param f
	 */
	public FieldGetter(String name, Object bean, Field f) {
		super(name, bean, f);
	}

	@Override
	public List<IndependentPair<String, Class<?>>> getStringValues() {
		List<IndependentPair<String, Class<?>>> ret = new ArrayList<IndependentPair<String, Class<?>>>();

		Object b;
		try {
			b = f.get(bean);
		} catch (Exception _) {
			// try again
			f.setAccessible(true);
			try {
				b = f.get(bean);

			} catch (Exception e) {
				throw new IllegalAccessError(e.getMessage());
			}
		}
		
		if (b == null)
			return ret;
		
		Class<?> c = b.getClass();
		IndependentPair<String, Class<?>> pair;
		if (c == Boolean.class) {
			if (!(Boolean) b) {
				pair = new IndependentPair<String, Class<?>>(null, c);
			} else {
				pair = new IndependentPair<String, Class<?>>(b.toString(), c);
			}
		} else {
			pair = new IndependentPair<String, Class<?>>(b.toString(), c);
		}

		ret.add(pair);

		return ret;
	}
}
