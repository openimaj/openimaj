/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.util.reflection;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Utility methods for finding classes.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ClassFinder {
	/**
	 * Scans all classes accessible from the context class loader which belong
	 * to the given package and sub-packages.
	 * 
	 * @param pkg
	 *            The base package
	 * @return The classes
	 * @throws IOException
	 */
	public static List<Class<?>> findClasses(Package pkg) throws IOException {
		return findClasses(pkg.getName());
	}

	/**
	 * Scans all classes accessible from the context class loader which belong
	 * to the given package and subpackages.
	 * 
	 * @param packageName
	 *            The base package
	 * @return The classes
	 * @throws IOException
	 */
	public static List<Class<?>> findClasses(String packageName) throws IOException {
		final List<Class<?>> classes = new ArrayList<Class<?>>();

		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		final String path = packageName.replace('.', '/');
		final Enumeration<URL> resources = classLoader.getResources(path);

		while (resources.hasMoreElements()) {
			final URL resource = resources.nextElement();

			if (resource.getProtocol().equals("file")) {
				classes.addAll(findClassesInDir(new File(resource.getFile()), packageName));
			} else if (resource.getProtocol().equals("jar")) {
				final String rf = resource.getFile();
				final File file = new File(rf.substring(5, rf.indexOf("!")));
				classes.addAll(findClassesInJar(file, packageName));
			}
		}
		return classes;
	}

	/**
	 * Recursive method to find all classes in a given directory and subdirs.
	 * 
	 * @param directory
	 *            The base directory
	 * @param packageName
	 *            The package name for classes found inside the base directory
	 * @return The classes
	 */
	public static List<Class<?>> findClassesInDir(File directory, String packageName) {
		final List<Class<?>> classes = new ArrayList<Class<?>>();
		if (!directory.exists()) {
			return classes;
		}
		final File[] files = directory.listFiles();
		for (final File file : files) {
			if (file.isDirectory()) {
				classes.addAll(findClassesInDir(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				try {
					classes.add(Class.forName(packageName + '.'
							+ file.getName().substring(0, file.getName().length() - 6)));
				} catch (final ClassNotFoundException e) {
					// do nothing
				}
			}
		}
		return classes;
	}

	/**
	 * Finds all the classes in a given package or its subpackages within a jar
	 * file.
	 * 
	 * @param jarFile
	 *            The jar file
	 * @param packageName
	 *            The package name
	 * @return The classes
	 * @throws IOException
	 */
	public static List<Class<?>> findClassesInJar(File jarFile, String packageName) throws IOException {
		final List<Class<?>> classes = new ArrayList<Class<?>>();

		JarFile jar = null;
		try {
			jar = new JarFile(jarFile);
			final Enumeration<JarEntry> enu = jar.entries();

			final String path = packageName.replace(".", "/");

			while (enu.hasMoreElements()) {
				final JarEntry je = enu.nextElement();
				final String name = je.getName();

				if (name.startsWith(path) && name.endsWith(".class")) {
					try {
						classes.add(Class.forName(name.replace("/", ".").substring(0, name.length() - 6)));
					} catch (final ClassNotFoundException e) {
						// do nothing
					}
				}
			}
		} finally {
			if (jar != null) {
				try {
					jar.close();
				} catch (final IOException e) {
				}
			}
		}

		return classes;
	}
}
