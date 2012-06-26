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
package org.openimaj.tools.web;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.reflection.ClassFinder;
import org.openimaj.util.reflection.ReflectionUtils;

/**
 * Wrapper for all web-tools.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class WebTools {
	/**
	 * Main method.
	 * @param args
	 */
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
