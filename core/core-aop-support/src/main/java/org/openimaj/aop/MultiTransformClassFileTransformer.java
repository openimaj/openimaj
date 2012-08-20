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
		final CtClass clz = pool.get(classname);

		try {
			transform(classname, clz);
		} catch (final Exception e) {
			throw new CannotCompileException(e);
		}
	}
}
