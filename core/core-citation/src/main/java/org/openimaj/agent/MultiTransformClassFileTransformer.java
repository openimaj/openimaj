package org.openimaj.agent;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;

import org.apache.log4j.Logger;

/**
 * A {@link ClassFileTransformer} that applies one or more {@link ClassTransformer}s
 * to a class before it is loaded.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class MultiTransformClassFileTransformer implements ClassFileTransformer {
	private static Logger logger = Logger.getLogger(MultiTransformClassFileTransformer.class);

	private ClassPool classPool = ClassPool.getDefault();
	private List<ClassTransformer> transformers = new ArrayList<ClassTransformer>();

	/**
	 * Construct with the given {@link ClassTransformer}s.
	 * 
	 * @param t1 the first transformer
	 * @param transformers any additional transformers
	 */
	public MultiTransformClassFileTransformer(ClassTransformer t1, ClassTransformer... transformers) {
		this.transformers.add(t1);
		
		for (ClassTransformer ct : transformers)
			this.transformers.add(ct);
	}
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		try {
			CtClass ctclz = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

			for (ClassTransformer ct : transformers)
				ct.transform(className, ctclz);

			return ctclz.toBytecode();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error transforming class " + className);
			return classfileBuffer;
		}
	}
}
