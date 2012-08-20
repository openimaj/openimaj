package org.openimaj.tools;

import java.io.File;

import javassist.ClassPool;
import javassist.Loader;

import org.openimaj.augmentation.MultiTransformClassFileTransformer;
import org.openimaj.augmentation.classloader.ClassLoaderTransform;
import org.openimaj.citation.agent.ReferencesClassTransformer;

public class ReferencesTool {
	static File jar;

	public static void main(String[] args) throws Throwable {
		jar = new File("/Users/jon/Work/openimaj/trunk/demos/sandbox/target/sandbox.jar");

		final MultiTransformClassFileTransformer transformer = new MultiTransformClassFileTransformer(
				new ReferencesClassTransformer());

		final Loader cl = ClassLoaderTransform.run(transformer, jar, args);

		final ClassPool pool = ClassPool.getDefault();
		final Class<?> clz = cl.loadClass(OutputWorker.class.getName());
		final Runnable r = (Runnable) clz.newInstance();

		Runtime.getRuntime().addShutdownHook(new Thread(r));
	}
}
