package org.openimaj.experiment.agent;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import org.apache.log4j.Logger;
import org.openimaj.annotation.Reference;
import org.openimaj.annotation.References;

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
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
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
