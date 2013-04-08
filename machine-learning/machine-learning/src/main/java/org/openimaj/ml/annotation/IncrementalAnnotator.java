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

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.ml.training.IncrementalTrainer;

/**
 * An {@link Annotator} that can be trained/updated incrementally.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OBJECT>
 *            Type of object
 * @param <ANNOTATION>
 *            Type of annotation
 */
public abstract class IncrementalAnnotator<OBJECT, ANNOTATION>
		extends
		AbstractAnnotator<OBJECT, ANNOTATION>
		implements
		IncrementalTrainer<Annotated<OBJECT, ANNOTATION>>
{
	protected IncrementalAnnotator() {
	}

	/**
	 * Train the annotator with the given data. The default implementation of
	 * this method just calls {@link #train(Object)} on each data item.
	 * Subclasses may override to do something more intelligent if necessary.
	 * 
	 * @param data
	 *            the training data
	 */
	@Override
	public void train(Iterable<? extends Annotated<OBJECT, ANNOTATION>> data) {
		for (final Annotated<OBJECT, ANNOTATION> d : data)
			train(d);
	}

	/**
	 * Train the annotator with the given grouped dataset. This method assumes
	 * that each object only appears in a <b>single</b> group of the dataset
	 * (i.e. a multi-class problem). Each group corresponds to the one single
	 * annotation assigned to each object.
	 * <p>
	 * If your dataset contains multiple labels for each object (through an
	 * object appearing in multiple groups) you should use
	 * {@link #train(GroupedDataset)}.
	 * 
	 * @param dataset
	 *            the dataset to train on
	 */
	public void trainMultiClass(GroupedDataset<ANNOTATION, ? extends ListDataset<OBJECT>, OBJECT> dataset) {
		for (final ANNOTATION grp : dataset.getGroups()) {
			for (final OBJECT inst : dataset.getInstances(grp)) {
				train(new AnnotatedObject<OBJECT, ANNOTATION>(inst, grp));
			}
		}
	}

	/**
	 * Train the annotator with the given grouped dataset. This method assumes
	 * that each object can appear in multiple groups of the dataset (i.e. a
	 * multi-label problem). Internally, the dataset is converted to a list
	 * containing exactly one reference to each object in the dataset with
	 * (potentially) multiple annotations.
	 * <p>
	 * If the dataset is actually multi-class (i.e. each object belongs to only
	 * a single group), then calling this method is equivalent to calling
	 * {@link #trainMultiClass(GroupedDataset)}, but is less efficient as the
	 * dataset has to be converted into a list.
	 * <p>
	 * Some annotator implementations do not care whether the data is
	 * multi-class or multi-label, and might choose to override this method to
	 * just call {@link #trainMultiClass(GroupedDataset)} instead.
	 * 
	 * @param dataset
	 *            the dataset to train on
	 */
	public void train(GroupedDataset<ANNOTATION, ? extends ListDataset<OBJECT>, OBJECT> dataset) {
		for (final AnnotatedObject<OBJECT, ANNOTATION> ao : AnnotatedObject.createList(dataset)) {
			train(ao);
		}
	}
}
