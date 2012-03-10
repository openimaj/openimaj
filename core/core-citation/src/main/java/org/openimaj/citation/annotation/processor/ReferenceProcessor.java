package org.openimaj.citation.annotation.processor;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.References;

@SupportedAnnotationTypes(value = { "org.openimaj.citation.annotation.Reference", "org.openimaj.citation.annotation.References" })
public class ReferenceProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		
		for (TypeElement te : annotations) {
			for (Element e : roundEnv.getElementsAnnotatedWith(te)) {
				Reference ann1 = e.getAnnotation(Reference.class);
				if (ann1 != null) {
					System.out.println("REFERENCE: " + ann1);
				}
				
				References ann2 = e.getAnnotation(References.class);
				if (ann2 != null) {
					for (Reference r : ann2.references()) {
						System.out.println("REFERENCE: " + r);
					}
				}
			}
		}
		
		return false;
	}

}
