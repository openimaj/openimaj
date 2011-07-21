package org.openimaj.util.reflection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Utility methods for java reflection
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class ReflectionUtils {
	/**
	 * Test if a class has a "public static main(String [] args)" method 
	 * @param clz the class
	 * @return true if main method exists; false otherwise
	 */
	public static boolean hasMain(Class<?> clz) {
		return getMain(clz) != null;
	}
	
	/**
	 * Get the "public static main(String [] args)" method of the class,
	 * or null if it doesn't have one.
	 * 
	 * @param clz the class
	 * @return the main method, or null if it doesn't exist
	 */
	public static Method getMain(Class<?> clz) {
		try {
			Method m =  clz.getMethod("main", String[].class);
			
			if (Modifier.isPublic(m.getModifiers()) && 
				Modifier.isStatic(m.getModifiers()))
				return m;
		} catch (NoSuchMethodException e1) {
			//do nothing
		}
		return null;
	}
}
