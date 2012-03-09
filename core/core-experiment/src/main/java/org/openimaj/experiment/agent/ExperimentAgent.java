package org.openimaj.experiment.agent;

import java.lang.instrument.Instrumentation;

/**
 * Java instrumentation agent for instrumenting experiments.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class ExperimentAgent {
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
		instrumentation = inst;
		instrumentation.addTransformer(new ReferencesClassFileTransformer());
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
		instrumentation.addTransformer(new ReferencesClassFileTransformer());
	}

//	/**
//	 * Programmatic hook to dynamically load javaagent at runtime.
//	 */
//	public static void initialize() {
//		if (instrumentation == null) {
//			ExperimentAgentLoader.loadAgent();
//		}
//	}
}
