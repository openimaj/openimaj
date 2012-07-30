package org.openimaj.citation.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import com.sun.tools.attach.VirtualMachine;

/**
 * Dynamic agent loader
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class AgentLoader {
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
	
	public static void loadAgent(Class<?> agentClass) throws IOException {
		File tmp = File.createTempFile("agent", ".jar");
		
		createAgentJar(tmp, agentClass);
		
		String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);

        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            vm.loadAgent(tmp.getAbsolutePath(), "");
            vm.detach();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
}
