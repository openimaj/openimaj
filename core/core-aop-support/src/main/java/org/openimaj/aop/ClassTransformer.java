package org.openimaj.aop;

import javassist.CtClass;

/**
 * An interface for objects capable of performing modifications
 * to a class before it's loaded.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public interface ClassTransformer {
	/**
	 * Transform the given class
	 * @param className the name of the class
	 * @param clazz the class
	 * @throws Exception if some error occurs
	 */
	public void transform(String className, CtClass clazz) throws Exception;
}
