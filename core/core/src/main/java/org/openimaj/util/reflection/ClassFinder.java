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
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class ClassFinder {
	/**
     * Scans all classes accessible from the context class loader which belong to the 
     * given package and subpackages.
     *
     * @param pkg The base package
     * @return The classes
     * @throws IOException
     */
    public static List<Class<?>> findClasses(Package pkg) throws IOException {
    	return findClasses(pkg.getName());
    }
	
	/**
     * Scans all classes accessible from the context class loader which belong to the 
     * given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws IOException
     */
    public static List<Class<?>> findClasses(String packageName) throws IOException {
    	List<Class<?>> classes = new ArrayList<Class<?>>();
        
    	ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
                        
            if (resource.getProtocol().equals("file")) {
            	classes.addAll(findClassesInDir(new File(resource.getFile()), packageName));
            } else if (resource.getProtocol().equals("jar")) {
            	String rf = resource.getFile();
            	File file = new File(rf.substring(5, rf.indexOf("!")));
            	classes.addAll(findClassesInJar(file, packageName));
            }
        }
        return classes;
    }
    
    /**
     * Recursive method to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     */
    public static List<Class<?>> findClassesInDir(File directory, String packageName) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClassesInDir(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                try {
					classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
				} catch (ClassNotFoundException e) {
					//do nothing
				}
            }
        }
        return classes;
    }
    
    /**
     * Finds all the classes in a given package or its subpackages within a jar file.
     * 
     * @param jarFile The jar file
     * @param packageName The package name 
     * @return The classes
     * @throws IOException
     */
    public static List<Class<?>> findClassesInJar(File jarFile, String packageName) throws IOException {
    	List<Class<?>> classes = new ArrayList<Class<?>>();
    	
    	JarFile jar = new JarFile(jarFile);
        Enumeration<JarEntry> enu = jar.entries();
        
        String path = packageName.replace(".", "/");
        
        while (enu.hasMoreElements()) {
        	JarEntry je = enu.nextElement();
        	String name = je.getName();
        	
        	if (name.startsWith(path) && name.endsWith(".class")) {
        		try {
					classes.add(Class.forName(name.replace("/", ".").substring(0, name.length() - 6)));
				} catch (ClassNotFoundException e) {
					//do nothing
				}
        	}
        }
        
        return classes;
    }

}
