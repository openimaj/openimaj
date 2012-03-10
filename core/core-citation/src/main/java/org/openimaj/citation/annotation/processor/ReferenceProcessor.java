package org.openimaj.citation.annotation.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.References;
import org.openimaj.citation.annotation.output.StandardFormatters;

/**
 * {@link Processor} implementation that is capable of finding
 * {@link Reference} and {@link References} annotations and generating
 * lists which are then written.
 * 
 * Currently the processor produces a BibTeX bibliography containing all
 * references in the project.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
@SupportedAnnotationTypes(value = { "org.openimaj.citation.annotation.Reference", "org.openimaj.citation.annotation.References" })
public class ReferenceProcessor extends AbstractProcessor {
	Set<Reference> references = new HashSet<Reference>();

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		for (TypeElement te : annotations) {
			for (Element e : roundEnv.getElementsAnnotatedWith(te)) {
				Reference ann1 = e.getAnnotation(Reference.class);
				if (ann1 != null) {
					references.add(ann1);
				}

				References ann2 = e.getAnnotation(References.class);
				if (ann2 != null) {
					for (Reference r : ann2.references()) {
						references.add(r);
					}
				}
			}
		}

		if (roundEnv.processingOver()) {
			processingEnv.getMessager().printMessage(Kind.NOTE, "Creating project bibliography");
			
			String bibtex = StandardFormatters.BIBTEX.formatReferences(references);
			try {
				FileObject file = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "", "bibliography.bib");

				Writer writer = new PrintWriter(file.openOutputStream());
				writer.append(bibtex);
				writer.close();

			} catch (IOException e) {
				processingEnv.getMessager().printMessage(Kind.ERROR, "Error writing bibtex " + e);
			}
		}
		
		return true;
	}

}
