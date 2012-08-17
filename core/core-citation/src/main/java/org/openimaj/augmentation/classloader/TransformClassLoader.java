package org.openimaj.augmentation.classloader;

import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javassist.ClassPool;
import javassist.Loader;

import org.openimaj.augmentation.agent.MultiTransformClassFileTransformer;
import org.openimaj.citation.agent.ReferencesClassTransformer;

public class TransformClassLoader {
	public static void main(String[] args) throws Throwable {
		final Class<TransformClassLoader> clazz = TransformClassLoader.class;
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

		// Set the correct app name on OSX. Are there similar controls for other
		// platforms?
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", value);

		final ClassPool cp = ClassPool.getDefault();
		final Loader cl = new Loader(cp);
		cl.delegateLoadingOf("org.apache.log4j.");

		cl.addTranslator(cp, new MultiTransformClassFileTransformer(new ReferencesClassTransformer()));
		cl.run(value, args);
	}
}
