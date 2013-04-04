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
package org.openimaj.ml.annotation;

import java.util.List;
import java.util.Set;

import org.openimaj.experiment.evaluation.classification.Classifier;

/**
 * Base class for objects capable of annotating things. Annotators are
 * essentially general forms of classifiers; annotation and classification
 * should be seen as synonymous.
 * <p>
 * The annotation interface extends the idea of a classifier with support for
 * feature-extraction from certain forms of object in order to generate the
 * classifications/annotations. The {@link #annotate(Object)} and
 * {@link #classify(Object)} methods do exactly the same thing, but return
 * results in different forms. Which method is used might depend on the task at
 * hand. A simple implementation of the {@link #classify(Object)} method that
 * calls the {@link #annotate(Object)} method can be found in the
 * {@link AbstractAnnotator} class. Implementors are advised to extend the
 * {@link AbstractAnnotator} class or one of its subclasses.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OBJECT>
 *            Type of object being annotated
 * @param <ANNOTATION>
 *            Type of annotation
 */
public interface Annotator<OBJECT, ANNOTATION>
		extends
		Classifier<ANNOTATION, OBJECT>
{
	/**
	 * @return a {@link Set} of all annotations this {@link Annotator} knows
	 *         about
	 */
	public abstract Set<ANNOTATION> getAnnotations();

	/**
	 * Generate annotations for the given object.
	 * 
	 * @param object
	 *            the image
	 * @return generated annotations
	 */
	public abstract List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object);
}
