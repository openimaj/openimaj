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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;

/**
 * Basic implementation of {@link Annotated}.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OBJECT>
 *            Type of object.
 * @param <ANNOTATION>
 *            Type of annotations
 */
public class AnnotatedObject<OBJECT, ANNOTATION> implements Annotated<OBJECT, ANNOTATION> {
	/**
	 * The annotated object
	 */
	public OBJECT object;

	/**
	 * The annotations
	 */
	public Collection<ANNOTATION> annotations;

	/**
	 * Construct with the given object and its annotations.
	 *
	 * @param object
	 *            the object
	 * @param annotations
	 *            the objects annotations.
	 */
	public AnnotatedObject(OBJECT object, Collection<ANNOTATION> annotations) {
		this.object = object;
		this.annotations = annotations;
	}

	/**
	 * Construct with the given object and its annotation.
	 *
	 * @param object
	 *            the object
	 * @param annotation
	 *            the object's annotation.
	 */
	public AnnotatedObject(OBJECT object, ANNOTATION annotation) {
		this.object = object;

		this.annotations = new ArrayList<ANNOTATION>();
		annotations.add(annotation);
	}

	@Override
	public OBJECT getObject() {
		return object;
	}

	@Override
	public Collection<ANNOTATION> getAnnotations() {
		return annotations;
	}

	/**
	 * Create an {@link AnnotatedObject} with the given object and its
	 * annotations.
	 *
	 * @param <OBJECT>
	 *            Type of object.
	 * @param <ANNOTATION>
	 *            Type of annotations
	 * @param object
	 *            the object
	 * @param annotations
	 *            the objects annotations.
	 * @return the new {@link AnnotatedObject}
	 */
	public static <OBJECT, ANNOTATION> AnnotatedObject<OBJECT, ANNOTATION> create(OBJECT object,
			Collection<ANNOTATION> annotations)
			{
		return new AnnotatedObject<OBJECT, ANNOTATION>(object, annotations);
			}

	/**
	 * Create an {@link AnnotatedObject} with the given object and its
	 * annotation.
	 *
	 * @param <OBJECT>
	 *            Type of object.
	 * @param <ANNOTATION>
	 *            Type of annotations
	 * @param object
	 *            the object
	 * @param annotation
	 *            the object's annotation.
	 * @return the new {@link AnnotatedObject}
	 */
	public static <OBJECT, ANNOTATION> AnnotatedObject<OBJECT, ANNOTATION> create(OBJECT object, ANNOTATION annotation) {
		return new AnnotatedObject<OBJECT, ANNOTATION>(object, annotation);
	}

	/**
	 * Convert a grouped dataset to a list of annotated objects. The annotations
	 * correspond to the type of group. If the same object appears in multiple
	 * groups within the dataset then it will have multiple annotations.
	 *
	 * @param <OBJECT>
	 *            Type of object.
	 * @param <ANNOTATION>
	 *            Type of annotations.
	 * @param dataset
	 *            the dataset
	 * @return the list of annotated instances
	 */
	public static <OBJECT, ANNOTATION> List<AnnotatedObject<OBJECT, ANNOTATION>> createList(
			GroupedDataset<ANNOTATION, ? extends ListDataset<OBJECT>, OBJECT> dataset)
			{
		final Map<OBJECT, AnnotatedObject<OBJECT, ANNOTATION>> annotated = new HashMap<OBJECT, AnnotatedObject<OBJECT, ANNOTATION>>(
				dataset.numInstances());

		for (final ANNOTATION grp : dataset.getGroups()) {
			for (final OBJECT inst : dataset.getInstances(grp)) {
				final AnnotatedObject<OBJECT, ANNOTATION> ao = annotated.get(inst);

				if (ao == null)
					annotated.put(inst, new AnnotatedObject<OBJECT, ANNOTATION>(inst, grp));
				else
					ao.annotations.add(grp);
			}
		}

		return new ArrayList<AnnotatedObject<OBJECT, ANNOTATION>>(annotated.values());
			}

	/**
	 * Convert parallel arrays of objects and annotations to a list of
	 * {@link AnnotatedObject}.
	 *
	 * @param objs
	 *            the objects
	 * @param anns
	 *            the annotation for each object (assumes 1 annotation per
	 *            object).
	 * @return the list
	 */
	public static <OBJECT, ANNOTATION> List<AnnotatedObject<OBJECT, ANNOTATION>> createList(OBJECT[] objs,
			ANNOTATION[] anns)
	{
		final List<AnnotatedObject<OBJECT, ANNOTATION>> list = new ArrayList<AnnotatedObject<OBJECT, ANNOTATION>>();

		for (int i = 0; i < objs.length; i++) {
			list.add(create(objs[i], anns[i]));
		}

		return list;
	}

	/**
	 * Convert parallel arrays of objects and annotations to a list of
	 * {@link AnnotatedObject}.
	 *
	 * @param objs
	 *            the objects
	 * @param anns
	 *            the annotations for each object.
	 * @return the list
	 */
	public static <OBJECT, ANNOTATION> List<AnnotatedObject<OBJECT, ANNOTATION>> createList(OBJECT[] objs,
			ANNOTATION[][] anns)
	{
		final List<AnnotatedObject<OBJECT, ANNOTATION>> list = new ArrayList<AnnotatedObject<OBJECT, ANNOTATION>>();

		for (int i = 0; i < objs.length; i++) {
			for (final ANNOTATION a : anns[i])
				list.add(create(objs[i], a));
		}

		return list;
	}
}
