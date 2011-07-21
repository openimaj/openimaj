package org.openimaj.tools.web;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.reflection.ClassFinder;
import org.openimaj.util.reflection.ReflectionUtils;

public class WebTools {
	public static void main(String [] args) {
		if (args.length < 1) {
			List<String> tools = getToolClassNames();
			
			if (tools != null) {
				System.err.println("Tool name not specified. Possible tools are:");
				for (String s : tools) System.err.println(s);
			} else {
				System.err.println("No tools are available");
			}
			
			return;
		}
		
		String clzname = args[0];
		Class<?> clz = null; 
		
		try {
			clz = Class.forName(clzname);
		} catch (ClassNotFoundException e) {
			try {
				clz = Class.forName(WebTools.class.getPackage().getName() + "." + clzname);
			} catch (ClassNotFoundException e1) {
				System.err.println("Class corresponding to " + clzname +" not found.");
				System.exit(0);
			}
		}
		
		String [] newArgs = new String[args.length-1];
		for (int i=0; i<newArgs.length; i++) newArgs[i] = args[i+1];
		Method method;
		try {
			method = clz.getMethod("main", String[].class);
			method.invoke(null, (Object)newArgs);
		} catch (Exception e) {
			System.err.println("Error invoking class " + clz +". Nested exception is:\n");
			e.printStackTrace(System.err);
		}
	}

	private static List<String> getToolClassNames() {
		try {
			List<Class<?>> classes = ClassFinder.findClasses(WebTools.class.getPackage());
			
			List<String> classNames = new ArrayList<String>();
			for (Class<?> clz : classes) {
				if (clz == WebTools.class)
					continue;
				
				if (ReflectionUtils.hasMain(clz)) {
					classNames.add(clz.getName().replace(WebTools.class.getPackage().getName() + ".", ""));
				}
			}
			
			return classNames;
		} catch (Exception e) {
			return null;
		}
	}
}
