package org.kohsuke.args4j.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.kohsuke.args4j.spi.Getter;
import org.kohsuke.args4j.spi.Getters;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class ArgsUtil {

	private ArgsUtil() {
	}

	/**
	 * @param bean
	 * @return return the Args4j options which would result in the current bean
	 * @throws Exception
	 */
	public static String[] extractArguments(Object bean) throws Exception {
		List<Getter<?>> arguments = null;
		arguments = parse(bean);
		List<String> args = new ArrayList<String>();
		for (Getter<?> optionHandler : arguments) {
			List<IndependentPair<String, Class<?>>> values = optionHandler.getStringValues();
			for (IndependentPair<String, Class<?>> object : values) {
				if(object.secondObject() == Boolean.class){
					if(object.firstObject() == null){
						continue;
					}
					else{
						args.add(optionHandler.getOptionName());
					}
				}
				else{					
					args.add(optionHandler.getOptionName());
					args.add(object.firstObject());
				}
			}
		}
		return args.toArray(new String[args.size()]);
	}

	private static List<Getter<?>> parse(Object bean) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		ArrayList<Getter<?>> options = new ArrayList<Getter<?>>();
		// recursively process all the methods/fields.
		for (Class<?> c = bean.getClass(); c != null; c = c.getSuperclass()) {

			for (Field f : c.getDeclaredFields()) {
				Option o = f.getAnnotation(Option.class);
				if (o != null) {
					options.add(Getters.create(o.name(),f, bean));
					if (o.handler() == ProxyOptionHandler.class) {
						Field opField = c.getDeclaredField(f.getName() + "Op");
						Object opBean = null;
						try{
							opBean = opField.get(bean);							
						}
						catch (Exception e) {
							opField.setAccessible(true);
							opBean = opField.get(bean);
							
						}
						options.addAll(parse(opBean));
					}
				}
			}
		}
		return options;
	}
}
