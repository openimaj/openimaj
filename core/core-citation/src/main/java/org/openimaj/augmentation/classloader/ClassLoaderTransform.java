package org.openimaj.augmentation.classloader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javassist.ClassPool;
import javassist.Loader;

import org.openimaj.augmentation.MultiTransformClassFileTransformer;
import org.openimaj.citation.agent.ReferencesClassTransformer;

public class ClassLoaderTransform {
	public static Loader run(MultiTransformClassFileTransformer tf, File jarFile, String[] args) throws Throwable {
		JarFile jar = null;

		try {
			jar = new JarFile(jarFile);
			final Manifest manifest = jar.getManifest();
			final Attributes attr = manifest.getMainAttributes();
			final String mainClass = attr.getValue("Main-Class");

			final ClassPool pool = ClassPool.getDefault();
			pool.appendClassPath(jarFile.getAbsolutePath());

			final URLClassLoader parent = new URLClassLoader(new URL[] { jarFile.toURI().toURL() });

			return run(parent, pool, tf, mainClass, args);
		} finally {
			try {
				if (jar != null)
					jar.close();
			} catch (final IOException e) {
			}
		}
	}

	// public static void run(MultiTransformClassFileTransformer tf, String
	// classpath, String mainClass, String[] args)
	// throws Throwable
	// {
	// final ClassPool pool = ClassPool.getDefault();
	// pool.appendPathList(classpath);
	//
	// run(pool, tf, mainClass, args);
	// }

	protected static Loader run(ClassLoader parent, ClassPool pool, MultiTransformClassFileTransformer tf,
			String mainClass, String[] args)
			throws Throwable
	{
		final Loader cl = new Loader(parent, pool);

		// Set the correct app name on OSX. Are there similar controls for other
		// platforms?
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", mainClass);

		// skip args4j
		cl.delegateLoadingOf("org.apache.log4j.");

		cl.addTranslator(pool, tf);

		cl.run(mainClass, args);

		return cl;
	}

	public static void main(String[] args) throws Throwable {
		final Class<ClassLoaderTransform> clazz = ClassLoaderTransform.class;
		final String className = clazz.getSimpleName() + ".class";
		final String classPath = clazz.getResource(className).toString();
		if (!classPath.startsWith("jar")) {
			// Class not from JAR
			return;
		}
		final String manifestPath = classPath.substring(0,
				classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
		final Manifest manifest = new Manifest(new URL(manifestPath).openStream());
		final Attributes attr = manifest.getMainAttributes();
		final String value = attr.getValue("OIMainClass");

		final ClassPool cp = ClassPool.getDefault();

		run(null, cp, new MultiTransformClassFileTransformer(new ReferencesClassTransformer()), value, args);
	}
}
