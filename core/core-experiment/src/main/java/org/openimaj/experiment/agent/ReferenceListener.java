package org.openimaj.experiment.agent;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.openimaj.annotation.Reference;
import org.openimaj.annotation.References;

/**
 * Listener that registers instances of {@link Reference} annotations
 * and prints the list of references to stdout on application
 * shutdown.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class ReferenceListener {
	private static Set<Reference> references = new HashSet<Reference>();
	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println(references);
			}
		});
	}
	
	/**
	 * Register the given {@link Reference}
	 * @param r the {@link Reference}
	 */
	public static synchronized void addReference(Reference r) {
		references.add(r);
	}
	
	/**
	 * Register the any {@link Reference} or {@link References} from the given class.
	 * @param clz the class
	 */
	public static void addReference(Class<?> clz) {
		Reference ann = clz.getAnnotation(Reference.class);
		
		if (ann != null)
			addReference(ann);
		
		References ann2 = clz.getAnnotation(References.class);
		if (ann2 != null)
			for (Reference r : ann2.references())
				addReference(r);
	}

	/**
	 * Register the any {@link Reference} or {@link References} from the given method.
	 * @param clz the class
	 * @param methodName
	 * @param signature
	 */
	public static void addReference(Class<?> clz, String methodName, String signature) {
		for (Method m : clz.getMethods()) {
			if (m.getName().equals(methodName) && m.toString().endsWith(signature)) {
				Reference ann = m.getAnnotation(Reference.class);
				
				if (ann != null)
					addReference(ann);
				
				References ann2 = m.getAnnotation(References.class);
				if (ann2 != null)
					for (Reference r : ann2.references())
						addReference(r);
			}
		}
	}
}
