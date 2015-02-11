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
/**
 *
 */
package org.openimaj.demos.sandbox.audio;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.feature.FeatureVector;
import org.openimaj.ml.annotation.AbstractAnnotator;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.AnnotatedObject;
import org.openimaj.ml.annotation.BatchAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.util.pair.IndependentPair;

/**
 * A general audio classifier class that can use any annotator and any dataset.
 * The {@link AudioClassifier} requires an annotator to be passed in, which in
 * turn requires feature extractor. The feature extractor must be able to
 * extract a {@link FeatureVector} from a {@link SampleBuffer}.
 *
 * <p>
 * The {@link #train(List)} method takes a list of pairs, where the pairs are
 * streams mapped to annotations. Each stream represents a set of
 * {@link SampleBuffer}s for that annotation. They are extracted into
 * {@link AnnotatedObject}s before being passed into the annotator for feature
 * extraction.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 7 May 2013
 * @version $Author$, $Revision$, $Date$
 * @param <ANNOTATION>
 *            The annotation type
 */
public class AudioClassifier<ANNOTATION> extends AbstractAnnotator<SampleBuffer, ANNOTATION>
{
	/** The annotator to use */
	private final BatchAnnotator<SampleBuffer, ANNOTATION> annotator;

	/**
	 * Constructor that takes the actual annotator type to use.
	 *
	 * @param annotator
	 *            The annotator
	 */
	public AudioClassifier(final BatchAnnotator<SampleBuffer, ANNOTATION> annotator)
	{
		this.annotator = annotator;
	}

	/**
	 * Train the annotator on the given streams. The streams are annotated with
	 * the appropriate annotation, and sample chunks (and therefore buffers) are
	 * gathered from the streams into batches to train the annotator.
	 *
	 * @param streams
	 *            The annotated streams
	 */
	public void train(final List<IndependentPair<AudioStream, ANNOTATION>> streams)
	{
		// Convert all the incoming streams into AnnotatedObject instances
		// where the sample buffer for each
		final List<Annotated<SampleBuffer, ANNOTATION>> list = new ArrayList<Annotated<SampleBuffer, ANNOTATION>>();
		for (final IndependentPair<AudioStream, ANNOTATION> stream : streams)
		{
			SampleChunk sc = null;
			while ((sc = stream.firstObject().nextSampleChunk()) != null)
			{
				final SampleBuffer sb = sc.getSampleBuffer();
				final AnnotatedObject<SampleBuffer, ANNOTATION> a = AnnotatedObject.create(sb, stream.secondObject());
				list.add(a);
			}
		}

		// Train the annotator for the streams
		this.annotator.train(list);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.ml.annotation.Annotator#getAnnotations()
	 */
	@Override
	public Set<ANNOTATION> getAnnotations()
	{
		return this.annotator.getAnnotations();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.ml.annotation.Annotator#annotate(java.lang.Object)
	 */
	@Override
	public List<ScoredAnnotation<ANNOTATION>> annotate(final SampleBuffer object)
	{
		return this.annotator.annotate(object);
	}
}
