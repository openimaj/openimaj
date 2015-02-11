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
package org.openimaj.experiment.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

import org.openimaj.aop.MultiTransformClassFileTransformer;
import org.openimaj.aop.agent.AgentLoader;
import org.openimaj.citation.CitationAgent;
import org.openimaj.citation.ReferencesClassTransformer;

/**
 * Java instrumentation agent for instrumenting experiments.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ExperimentAgent {
	private static boolean isLoaded = false;
	private static Instrumentation instrumentation;

	/**
	 * JVM hook to statically load the javaagent at startup.
	 *
	 * After the Java Virtual Machine (JVM) has initialized, the premain method
	 * will be called. Then the real application main method will be called.
	 *
	 * @param args
	 * @param inst
	 * @throws Exception
	 */
	public static void premain(String args, Instrumentation inst) throws Exception {
		agentmain(args, inst);
	}

	/**
	 * JVM hook to dynamically load javaagent at runtime.
	 *
	 * The agent class may have an agentmain method for use when the agent is
	 * started after VM startup.
	 *
	 * @param args
	 * @param inst
	 * @throws Exception
	 */
	public static void agentmain(String args, Instrumentation inst) throws Exception {
		instrumentation = inst;
		instrumentation.addTransformer(new MultiTransformClassFileTransformer(
				new ReferencesClassTransformer(),
				new TimeClassTransformer()
				));
	}

	/**
	 * Programmatic hook to dynamically load {@link CitationAgent} at runtime.
	 *
	 * @throws IOException
	 *             if an error occurs
	 */
	public static synchronized void initialise() throws IOException {
		if (!isLoaded) {
			AgentLoader.loadAgent(ExperimentAgent.class);
			isLoaded = true;
		}
	}

	/**
	 * Is the agent loaded?
	 *
	 * @return true if the agent is already loaded; false otherwise
	 */
	public synchronized static boolean isLoaded() {
		return isLoaded;
	}
}
