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
				clz = Class.forName("org.openimaj.tools.localfeature." + clzname);
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
