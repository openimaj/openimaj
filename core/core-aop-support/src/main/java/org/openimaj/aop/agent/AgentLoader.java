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
package org.openimaj.aop.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.log4j.Logger;

/**
 * Dynamic agent loader. Provides methods to extract an agent jar with
 * a specific agent class, and to attempt to dynamically load agents
 * on Oracle JVMs (including OpenJDK). Dynamic loading won't work on
 * all JVMs (i.e. IBMs), but the standard "-javaagent" commandline option
 * can be used instead. If dynamic loading fails, instructions on using 
 * the command line flag will be printed to stderr.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class AgentLoader {
	private static final Logger logger = Logger.getLogger(AgentLoader.class);
	private static final String VMCLASS = "com.sun.tools.attach.VirtualMachine"; 

	private static long copy(InputStream input, OutputStream output) throws IOException {
		long count = 0;
		int n = 0;
		byte[] buffer = new byte[4096];

		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}

		return count;
	}

	private static byte[] createManifest(Class<?> agentClass) {
		StringBuffer sb = new StringBuffer();

		try {
			agentClass.getDeclaredMethod("premain", String.class, Instrumentation.class);
			sb.append("Premain-Class: "+ agentClass.getName() + "\n");
		} catch (NoSuchMethodException e) { 
			//IGNORE//
		}

		try {
			agentClass.getDeclaredMethod("agentmain", String.class, Instrumentation.class);
			sb.append("Agent-Class: "+ agentClass.getName() + "\n");
		} catch (NoSuchMethodException e) { 
			//IGNORE//
		}

		sb.append("Can-Redefine-Classes: true\n");
		sb.append("Can-Retransform-Classes: true\n");

		try {
			return sb.toString().getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Charset US-ASCII isn't supported!! This should never happen.");
		}
	}

	/**
	 * Create an agent jar file with the required manifest entries.
	 * 
	 * @param file the location to create the jar
	 * @param agentClass the agent class
	 * @throws IOException if an error occurs
	 */
	public static void createAgentJar(File file, Class<?> agentClass) throws IOException {
		JarOutputStream jos = new JarOutputStream(new FileOutputStream(file));

		String classEntryPath = agentClass.getName().replace(".", "/") + ".class";
		InputStream classStream = agentClass.getClassLoader().getResourceAsStream(classEntryPath);

		if (classEntryPath.startsWith("/")) classEntryPath = classEntryPath.substring(1);

		JarEntry entry = new JarEntry(classEntryPath);
		jos.putNextEntry(entry);
		copy(classStream, jos);
		jos.closeEntry();

		entry = new JarEntry("META-INF/MANIFEST.MF");
		jos.putNextEntry(entry);
		jos.write(createManifest(agentClass));
		jos.closeEntry();

		jos.close();
	}

	/**
	 * Attempt to locate potential "tools.jar" jars
	 */
	private static List<File> getPotentialToolsJars() {
		List<File> jars = new ArrayList<File>();

		File javaHome = new File(System.getProperty("java.home"));

		File jreSourced = new File(javaHome, "lib/tools.jar");
		if (jreSourced.exists()) {
			jars.add(jreSourced);
		}

		if ("jre".equals(javaHome.getName())) {
			File jdkHome = new File(javaHome, "../");
			File jdkSourced = new File(jdkHome, "lib/tools.jar");
			if (jdkSourced.exists()) {
				jars.add(jdkSourced);
			}
		}

		return jars;
	}

	/**
	 * Try and get the VirtualMachine class
	 */
	private static Class<?> tryGetVMClass() {
		try {
			return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
				@Override
				public Class<?> run() throws Exception {
					try {
						return ClassLoader.getSystemClassLoader().loadClass(VMCLASS);
					} catch (ClassNotFoundException e) {
						for (File jar : getPotentialToolsJars()) {
							try {
								return new URLClassLoader(new URL[] {jar.toURI().toURL()}).loadClass(VMCLASS);
							} catch (Throwable t) {
								logger.trace("Exception while loading tools.jar from "+ jar, t);
							}
						}
					}
					return null;
				}
			});
		} catch (PrivilegedActionException pae) {
			Throwable actual = pae.getCause();

			if (actual instanceof ClassNotFoundException) {
				logger.trace("No VirtualMachine found");
				return null;
			}

			throw new RuntimeException("Unexpected checked exception : " + actual);
		}
	}

	private static void loadFailed() {
		System.err.println("Unable to load the java agent dynamically");
		//FIXME: instructions
	}

	/**
	 * Attempt to dynamically load the given agent class
	 * 
	 * @param agentClass the agent class
	 * @throws IOException if an error occurs creating the agent jar
	 */
	public static void loadAgent(Class<?> agentClass) throws IOException {
		File tmp = File.createTempFile("agent", ".jar");
		tmp.deleteOnExit();
		createAgentJar(tmp, agentClass);

		String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
		int p = nameOfRunningVM.indexOf('@');
		String pid = nameOfRunningVM.substring(0, p);

		Class<?> vmClass = tryGetVMClass();

		if (vmClass == null) {
			loadFailed();
		} else {
			try {
				Method attach = vmClass.getMethod("attach", String.class);
				Method loadAgent = vmClass.getMethod("loadAgent", String.class);
				Method detach = vmClass.getMethod("detach");
				
				Object vm = attach.invoke(null, pid);
				try {
					loadAgent.invoke(vm, tmp.getAbsolutePath());
				} finally {
					detach.invoke(vm);
				}
			} catch (Exception e) {
				logger.warn("Loading the agent failed", e);
				loadFailed();
			}
		}
	}
}
