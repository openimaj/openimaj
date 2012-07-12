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

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.ml.feature.FeatureExtractor;
import org.openimaj.ml.training.IncrementalTrainer;

/**
 * An {@link Annotator} that can be trained/updated incrementally.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OBJECT> Type of object
 * @param <ANNOTATION> Type of annotation
 * @param <EXTRACTOR> Type of object capable of extracting features from the object
 */
public abstract class IncrementalAnnotator<
	OBJECT, 
	ANNOTATION,
	EXTRACTOR extends FeatureExtractor<?, OBJECT>> 
extends 
	AbstractAnnotator<OBJECT, ANNOTATION, EXTRACTOR> 
implements
	IncrementalTrainer<Annotated<OBJECT, ANNOTATION>>
{
	/**
	 * Construct with the given feature extractor.
	 * @param extractor the feature extractor
	 */
	public IncrementalAnnotator(EXTRACTOR extractor) {
		super(extractor);
	}

	/**
	 * Train the annotator with the given data. The
	 * default implementation of this method just
	 * calls {@link #train(Object)} on each data
	 * item. Subclasses may override to do something
	 * more intelligent if necessary. 
	 * 
	 * @param data the training data
	 */
	@Override
	public void train(Dataset<? extends Annotated<OBJECT, ANNOTATION>> data) {
		for (Annotated<OBJECT, ANNOTATION> d : data) 
			train(d);
	}
}
