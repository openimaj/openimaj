/**
 * ImageTerrier - The Terabyte Retriever for Images
 * Webpage: http://www.imageterrier.org/
 * Contact: jsh2@ecs.soton.ac.uk
 * Electronics and Computer Science, University of Southampton
 * http://www.ecs.soton.ac.uk/
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is ImageTerrierMain.java
 *
 * The Original Code is Copyright (C) 2011 the University of Southampton
 * and the original contributors.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Jonathon Hare <jsh2@ecs.soton.ac.uk> (original contributor)
 *   Sina Samangooei <ss@ecs.soton.ac.uk>
 *   David Dupplaw <dpd@ecs.soton.ac.uk>
 */
package org.openimaj.tools.localfeature;

import java.lang.reflect.Method;

/**
 * Main tool entry point
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class LocalFeaturesMain {
	/**
	 * Run the tool
	 * @param args the arguments
	 */
	public static void main(String [] args) {
		if (args.length < 1) {
			System.err.println("Class name not specified");
			return;
		}
		
		String clzname = args[0];
		Class<?> clz;  
		
		try {
			clz = Class.forName(clzname);
		} catch (ClassNotFoundException e) {
			try {
				clz = Class.forName(".openimaj.tools.localfeature." + clzname);
			} catch (ClassNotFoundException e1) {
				System.err.println("Class corresponding to " + clzname +" not found.");
				return;
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
}
