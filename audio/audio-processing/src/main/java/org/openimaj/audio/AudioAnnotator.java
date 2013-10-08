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
package org.openimaj.audio;

import java.util.List;
import java.util.Set;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.IdentityFeatureExtractor;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.IncrementalAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.ml.annotation.basic.KNNAnnotator;
import org.openimaj.ml.annotation.bayes.NaiveBayesAnnotator;
import org.openimaj.ml.annotation.bayes.NaiveBayesAnnotator.Mode;

/**
 * A classifier/annotator for audio frames. Note that this is a pretty general
 * annotator that takes {@link DoubleFV}s and String labels. The
 * {@link DoubleFV}s can be extracted from audio with this class also, as it
 * also implements FeatureExtractor interface for {@link SampleChunk}s
 * (returning {@link DoubleFV}s). However, there is no implementation for the
 * necessary {@link FeatureExtractor#extractFeature(Object)} method, so
 * subclasses must implement this. The default annotator in use is a general
 * {@link KNNAnnotator} for {@link DoubleFV}s, however, this can be changed
 * using {@link #setAnnotator(AudioAnnotatorType)}.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 8 Mar 2013
 */
public abstract class AudioAnnotator
		extends
		IncrementalAnnotator<DoubleFV, String>
		implements
		FeatureExtractor<DoubleFV, SampleChunk>
{
	/**
	 * An enumeration that allows different trainers to be used and specified on
	 * the command-line.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 6 Dec 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	public enum AudioAnnotatorType
	{
		/** KNN Annotator */
		KNN {
			private KNNAnnotator<DoubleFV, String, DoubleFV> k;

			@Override
			public IncrementalAnnotator<DoubleFV, String> getAnnotator()
			{
				if (this.k == null)
					this.k = new KNNAnnotator<DoubleFV, String, DoubleFV>(
							new IdentityFeatureExtractor<DoubleFV>(),
							DoubleFVComparison.EUCLIDEAN);
				return this.k;
			}
		},

		/** Naive Bayes annotator */
		BAYES {
			private IncrementalAnnotator<DoubleFV, String> n;

			@Override
			public IncrementalAnnotator<DoubleFV, String> getAnnotator()
			{
				if (this.n == null)
					this.n = new NaiveBayesAnnotator<DoubleFV, String>(
							new IdentityFeatureExtractor<DoubleFV>(),
							Mode.ALL);
				return this.n;
			}
		};

		/**
		 * Returns a annotator that can train a DoubleFV feature with a specific
		 * String label. The annotators will all have
		 * {@link IdentityFeatureExtractor} feature extractors so the audio
		 * features must be extracted before hand.
		 * 
		 * @return The annotator
		 */
		public abstract IncrementalAnnotator<DoubleFV, String> getAnnotator();
	}

	/** The specfic annotator being used */
	private AudioAnnotatorType annotator = AudioAnnotatorType.KNN;

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.ml.training.IncrementalTrainer#train(java.lang.Object)
	 */
	@Override
	public void train(final Annotated<DoubleFV, String> annotated)
	{
		this.getAnnotator().getAnnotator().train(annotated);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.ml.training.IncrementalTrainer#reset()
	 */
	@Override
	public void reset()
	{
		this.getAnnotator().getAnnotator().reset();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.ml.annotation.Annotator#getAnnotations()
	 */
	@Override
	public Set<String> getAnnotations()
	{
		return this.getAnnotator().getAnnotator().getAnnotations();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.ml.annotation.Annotator#annotate(java.lang.Object)
	 */
	@Override
	public List<ScoredAnnotation<String>> annotate(final DoubleFV object)
	{
		return this.getAnnotator().getAnnotator().annotate(object);
	}

	/**
	 * Get the annotator type in use.
	 * 
	 * @return The annotator type
	 */
	public AudioAnnotatorType getAnnotator()
	{
		return this.annotator;
	}

	/**
	 * Set the annotator type to use.
	 * 
	 * @param annotator
	 *            The annotator type.
	 */
	public void setAnnotator(final AudioAnnotatorType annotator)
	{
		this.annotator = annotator;
	}
}
