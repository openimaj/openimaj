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
package org.openimaj.aop;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.Translator;

import org.apache.log4j.Logger;

/**
 * A {@link ClassFileTransformer} that applies one or more
 * {@link ClassTransformer}s to a class before it is loaded.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class MultiTransformClassFileTransformer implements ClassFileTransformer, Translator {
	private static Logger logger = Logger.getLogger(MultiTransformClassFileTransformer.class);

	private ClassPool classPool;
	private List<ClassTransformer> transformers = new ArrayList<ClassTransformer>();

	/**
	 * Construct with the given {@link ClassTransformer}s.
	 * 
	 * @param t1
	 *            the first transformer
	 * @param transformers
	 *            any additional transformers
	 */
	public MultiTransformClassFileTransformer(ClassTransformer t1, ClassTransformer... transformers) {
		this.transformers.add(t1);

		for (final ClassTransformer ct : transformers)
			this.transformers.add(ct);
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException
	{
		return transform(className, classfileBuffer);
	}

	/**
	 * Transform the given class.
	 * 
	 * @param className
	 *            the name of the class
	 * @param classfileBuffer
	 *            the class bytes
	 * @return the transformed bytes
	 */
	public byte[] transform(String className, byte[] classfileBuffer) {
		if (classPool == null)
			classPool = ClassPool.getDefault();

		try {
			final CtClass ctclz = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

			transform(className, ctclz);

			return ctclz.toBytecode();
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("Error transforming class " + className);
			return classfileBuffer;
		}
	}

	private void transform(String className, CtClass ctclz) throws Exception {
		for (final ClassTransformer ct : transformers)
			ct.transform(className, ctclz);
	}

	@Override
	public void start(ClassPool pool) throws NotFoundException, CannotCompileException {
		this.classPool = pool;
	}

	@Override
	public void onLoad(ClassPool pool, String classname) throws NotFoundException, CannotCompileException {
		if (classname.endsWith(".package-info"))
			return;

		final CtClass clz = pool.get(classname);

		try {
			transform(classname, clz);
		} catch (final Exception e) {
			throw new CannotCompileException(e);
		}
	}
}
