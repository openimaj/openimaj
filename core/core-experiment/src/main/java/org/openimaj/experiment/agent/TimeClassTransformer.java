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

import java.lang.instrument.ClassFileTransformer;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import org.openimaj.aop.ClassTransformer;
import org.openimaj.experiment.annotations.Time;

/**
 * {@link ClassFileTransformer} that dynamically augments classes and methods
 * annotated with {@link Time} annotations in order to register and collect the
 * method timing information.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class TimeClassTransformer implements ClassTransformer {
	@Override
	public void transform(String className, CtClass ctclz) throws Exception {
		final CtMethod[] methods = ctclz.getDeclaredMethods();

		for (final CtMethod m : methods) {
			final Time ann = (Time) m.getAnnotation(Time.class);

			if (ann != null) {
				String timerName = ann.identifier();

				if (timerName == null || timerName.length() == 0)
					timerName = String.format("%s#%s", className, m.getLongName());

				addTimingInterceptor(ctclz, m, timerName);
			}
		}
	}

	/*
	 * Inspired by
	 * http://www.ibm.com/developerworks/java/library/j-dyn0916/index.html
	 */
	private static void addTimingInterceptor(CtClass clazz, CtMethod method, String timerName)
			throws CannotCompileException, NotFoundException
	{
		final String oname = method.getName();
		final String nname = oname + "$impl";
		method.setName(nname);
		final CtMethod interceptor = CtNewMethod.copy(method, oname, clazz, null);

		final String type = method.getReturnType().getName();
		final StringBuffer body = new StringBuffer();
		body.append(
				"{\n" +
						"org.openimaj.time.NanoTimer timer = org.openimaj.time.NanoTimer.timer();\n"
				);

		if (!"void".equals(type)) {
			body.append(type + " result = ");
		}
		body.append(nname + "($$);\n");

		body.append(
				"timer.stop();" +
						"org.openimaj.experiment.agent.TimeTracker.accumulate(\"" + timerName
						+ "\", timer.duration());\n"
				);

		if (!"void".equals(type)) {
			body.append("return result;\n");
		}
		body.append("}");

		interceptor.setBody(body.toString());
		clazz.addMethod(interceptor);
	}
}
