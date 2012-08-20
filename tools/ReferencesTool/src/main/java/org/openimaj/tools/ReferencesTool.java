package org.openimaj.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javassist.ClassPool;
import javassist.Loader;

import org.kohsuke.args4j.CmdLineException;
import org.openimaj.aop.MultiTransformClassFileTransformer;
import org.openimaj.aop.classloader.ClassLoaderTransform;
import org.openimaj.citation.ReferencesClassTransformer;

/**
 * A tool for running another program and extracting a bibliography for all the
 * annotated methods and classes that are used during the programs execution.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ReferencesTool {
	/**
	 * Create an {@link OutputWorker} with the specified classloader
	 * 
	 * @param cl
	 *            the classloader
	 * @param args
	 *            the arguments string
	 * @return the new {@link OutputWorker} as a {@link Runnable}.
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private static Runnable loadOutputWorker(Loader cl, String[] args)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException,
			NoSuchMethodException, IllegalArgumentException, InvocationTargetException
	{
		final Class<?> clz = cl.loadClass(OutputWorker.class.getName());
		final Constructor<?> ctr = clz.getConstructor(String[].class);

		return (Runnable) ctr.newInstance((Object) args);
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * 
	 * @throws Throwable
	 *             if an error occurs
	 */
	public static void main(String[] args) throws Throwable {
		ReferencesToolOpts options = null;
		try {
			options = new ReferencesToolOpts(args);
			options.validate();
		} catch (final CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println();
			System.err.println("Usage:");
			System.err
					.println("java -jar ReferencesTool.jar [references output options] -jar jarFile [tool arguments and options]");
			System.err
					.println("java -jar ReferencesTool.jar [references output options] -cp classpath mainClass [tool arguments and options]");
			System.err
					.println("java -jar ReferencesTool.jar [references output options] mainClass [tool arguments and options]");
			return;
		}

		final MultiTransformClassFileTransformer transformer = new MultiTransformClassFileTransformer(
				new ReferencesClassTransformer());

		Loader cl = null;
		if (options.isJar()) {
			cl = ClassLoaderTransform.run(ClassPool.getDefault(), transformer, options.jarFile,
					options.arguments.toArray(new String[options.arguments.size()]));
		} else {
			cl = ClassLoaderTransform.run(ClassPool.getDefault(), transformer, options.classpath, options.mainMethod,
					options.arguments.toArray(new String[options.arguments.size()]));
		}

		Runtime.getRuntime().addShutdownHook(new Thread(loadOutputWorker(cl, args)));
	}
}
