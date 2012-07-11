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
package org.openimaj.image.processing.face.recognition;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.ml.annotation.FeatureExtractor;
import org.openimaj.ml.annotation.IncrementalAnnotator;
import org.openimaj.ml.annotation.RestrictedAnnotator;

/**
 * Base class for all Face Recognisers.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <O> Type of {@link DetectedFace}
 * @param <E> Type of {@link FeatureExtractor}
 */
public abstract class FaceRecogniser<O extends DetectedFace, E extends FeatureExtractor<?, O>> 
	extends 
		IncrementalAnnotator<O, String, E>
	implements 
		RestrictedAnnotator<O, String>,
		ReadWriteableBinary 
{
	/**
	 * Construct with the given feature extractor.
	 * @param extractor the feature extractor
	 */
	public FaceRecogniser(E extractor) {
		super(extractor);
	}

	/**
	 * Attempt to recognize the given face, restricting
	 * the potential people to coming from the given set.
	 * @param object the detected face
	 * @param restrict the set of allowed people
	 * @return potential people
	 */
	@Override
	public abstract List<ScoredAnnotation<String>> annotate(O object, Collection<String> restrict);
	
	/**
	 * Attempt to recognize the given face, restricting
	 * the potential people to coming from the given set.
	 * @param object the detected face
	 * @param restrict the set of allowed people
	 * @return potential people
	 */
	public ScoredAnnotation<String> annotateBest(O object, Collection<String> restrict) {
		List<ScoredAnnotation<String>> pot = annotate(object, restrict);
		
		if (pot == null || pot.size() == 0)
			return null;
		
		Collections.sort(pot);
		
		return pot.get(0);
	}
	
	/**
	 * Attempt to recognize the given face.
	 * @param object the detected face
	 * @return potential people
	 */
	@Override
	public abstract List<ScoredAnnotation<String>> annotate(O object);
	
	/**
	 * Attempt to recognize the given face.
	 * @param object the detected face
	 * @return potential people
	 */
	public ScoredAnnotation<String> annotateBest(O object) {
		List<ScoredAnnotation<String>> pot = annotate(object);
		
		if (pot == null || pot.size() == 0)
			return null;
		
		return pot.get(0);
	}

	/**
	 * Convenience method for {@link #getAnnotations()}
	 * @see #getAnnotations()
	 * @return the people that can be recognised
	 */
	public Set<String> listPeople() {
		return this.getAnnotations();
	}
}
