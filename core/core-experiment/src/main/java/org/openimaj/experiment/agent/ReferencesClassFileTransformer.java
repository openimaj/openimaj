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

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import org.apache.log4j.Logger;
import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.References;

/**
 * {@link ClassFileTransformer} that dynamically augments classes
 * and methods annotated with {@link Reference} or {@link References}
 * annotations to register the annotations with a global listener if the
 * class is constructed, or the method is invoked.
 * 
 * When used with the {@link ExperimentAgent}, this can be used to
 * dynamically produce a list of references for code as it is run. Importantly,
 * the list will only contain references for the bits of code that
 * are actually used!
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ReferencesClassFileTransformer implements ClassFileTransformer {
	private static Logger logger = Logger.getLogger(ReferencesClassFileTransformer.class);
	
	private ClassPool classPool = ClassPool.getDefault();
	
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
			CtClass ctclz = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
			
			Object ann = ctclz.getAnnotation(Reference.class);
			if (ann == null) ann = ctclz.getAnnotation(References.class);
			
			if (ann != null) {
				logger.trace(String.format("class file transformer invoked for className: %s\n", className));
				
				ctclz.makeClassInitializer().insertBefore("org.openimaj.experiment.agent.ReferenceListener.addReference("+ctclz.getName()+".class);");
			}
			
			CtMethod[] methods = ctclz.getDeclaredMethods();
			for (CtMethod m : methods) {
				ann = m.getAnnotation(Reference.class);
				if (ann == null) ann = m.getAnnotation(References.class);
				
				if (ann != null) {
					logger.trace(String.format("class file transformer invoked for className: %s\n; method: ", className, m.getLongName()));
					
					m.insertBefore("org.openimaj.experiment.agent.ReferenceListener.addReference(this.getClass(),"+m.getName()+","+m.getLongName()+");");
				}
			}
			
			return ctclz.toBytecode();
		} catch (Exception e) {
			logger.error("Error transforming class " + className);
			return classfileBuffer;
		}
    }
}
