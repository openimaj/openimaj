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
package org.openimaj.citation;

import javassist.CtClass;
import javassist.CtMethod;

import org.apache.log4j.Logger;
import org.openimaj.aop.ClassTransformer;
import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.References;

/**
 * {@link ClassTransformer} that dynamically augments classes and methods
 * annotated with {@link Reference} or {@link References} annotations to
 * register the annotations with a global listener if the class is constructed,
 * or the method is invoked.
 * <p>
 * When used with the {@link CitationAgent}, this can be used to dynamically
 * produce a list of references for code as it is run. Importantly, the list
 * will only contain references for the bits of code that are actually used!
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class ReferencesClassTransformer implements ClassTransformer {
	private static Logger logger = Logger.getLogger(ReferencesClassTransformer.class);

	@Override
	public void transform(String className, CtClass ctclz) throws Exception {
		Object ann = ctclz.getAnnotation(Reference.class);
		if (ann == null)
			ann = ctclz.getAnnotation(References.class);

		if (ann != null) {
			logger.trace(String.format("class file transformer invoked for className: %s\n", className));

			ctclz.makeClassInitializer().insertBefore(
					ReferenceListener.class.getName() + ".addReference(" + ctclz.getName() + ".class);");
		}

		final CtMethod[] methods = ctclz.getDeclaredMethods();
		for (final CtMethod m : methods) {
			ann = m.getAnnotation(Reference.class);
			if (ann == null)
				ann = m.getAnnotation(References.class);

			if (ann != null) {
				logger.trace(String.format("class file transformer invoked for className: %s\n; method: ", className,
						m.getLongName()));

				final String code = ReferenceListener.class.getName() + ".addReference(" + ctclz.getName()
						+ ".class,\"" + m.getName() + "\",\"" + m.getLongName() + "\");";

				m.insertBefore(code);
			}
		}
	}
}
