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
package org.openimaj.text.nlp.textpipe.annotations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Classes that extend {@link TextPipeAnnotation} are annotations and
 * annotatable. Annotations are a HashMap keyed on the class of the annotation,
 * adding a new annotation T extends {@link TextPipeAnnotation} will be added to
 * the the list of Annotations of that class already added.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * 
 */
public abstract class TextPipeAnnotation {

	private HashMap<Class<? extends TextPipeAnnotation>, List<TextPipeAnnotation>> annotations;

	public TextPipeAnnotation() {
		annotations = new HashMap<Class<? extends TextPipeAnnotation>, List<TextPipeAnnotation>>();
	}

	public <T extends TextPipeAnnotation> void addAnnotation(T annotation) {
		if (annotations.containsKey(annotation.getClass())) {
			annotations.get(annotation.getClass()).add(annotation);
		} else {
			final ArrayList<TextPipeAnnotation> annos = new ArrayList<TextPipeAnnotation>();
			annos.add(annotation);
			annotations.put(annotation.getClass(), annos);
		}
	}

	public <T extends TextPipeAnnotation> void addAllAnnotations(
			Collection<T> annotationCollection)
	{
		if (annotationCollection != null && annotationCollection.size() > 0) {
			final Class<? extends TextPipeAnnotation> key = annotationCollection.iterator().next().getClass();

			if (annotations.containsKey(key)) {
				annotations.get(key).addAll(annotationCollection);
			} else {
				final ArrayList<TextPipeAnnotation> annos = new ArrayList<TextPipeAnnotation>();
				annos.addAll(annotationCollection);
				annotations.put(key, annos);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends TextPipeAnnotation> List<T> getAnnotationsFor(Class<T> key) {
		if (annotations.containsKey(key)) {
			return (List<T>) annotations.get(key);
		}
		return null;
	}

	public Set<Class<? extends TextPipeAnnotation>> getAnnotationKeyList() {
		return annotations.keySet();
	}

}
