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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.IncrementalAnnotator;
import org.openimaj.ml.annotation.RestrictedAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;

/**
 * A {@link FaceRecogniser} built on top of an {@link IncrementalAnnotator}.
 * This class essentially adapts standard {@link IncrementalAnnotator} to work
 * in the face recognition scenario.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <FACE>
 *            Type of {@link DetectedFace}
 * @param <PERSON>
 *            Type of object representing a person
 */
public class AnnotatorFaceRecogniser<FACE extends DetectedFace, PERSON>
		extends
		FaceRecogniser<FACE, PERSON>
{
	protected IncrementalAnnotator<FACE, PERSON> annotator;

	protected AnnotatorFaceRecogniser() {
	}

	/**
	 * Construct with the given underlying annotator.
	 * 
	 * @param annotator
	 *            the annotator
	 */
	public AnnotatorFaceRecogniser(IncrementalAnnotator<FACE, PERSON> annotator) {
		this.annotator = annotator;
	}

	/**
	 * Convenience method to create {@link AnnotatorFaceRecogniser} instances
	 * from an annotator.
	 * 
	 * @param <FACE>
	 *            Type of {@link DetectedFace}
	 * @param <EXTRACTOR>
	 *            Type of {@link FeatureExtractor}
	 * @param <PERSON>
	 *            Type of object representing a person
	 * @param annotator
	 *            the annotator
	 * @return the new {@link AnnotatorFaceRecogniser} instance
	 */
	public static <FACE extends DetectedFace, EXTRACTOR extends FeatureExtractor<?, FACE>, PERSON>
			AnnotatorFaceRecogniser<FACE, PERSON> create(
					IncrementalAnnotator<FACE, PERSON> annotator)
	{
		return new AnnotatorFaceRecogniser<FACE, PERSON>(annotator);
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		annotator = IOUtils.read(in);
	}

	@Override
	public byte[] binaryHeader() {
		return "FREC".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		IOUtils.write(annotator, out);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ScoredAnnotation<PERSON>> annotate(FACE object, Collection<PERSON> restrict) {
		if (annotator instanceof RestrictedAnnotator) {
			return ((RestrictedAnnotator<FACE, PERSON>) annotator).annotate(object, restrict);
		}

		final List<ScoredAnnotation<PERSON>> pot = annotator.annotate(object);

		if (pot == null || pot.size() == 0)
			return null;

		final List<ScoredAnnotation<PERSON>> toKeep = new ArrayList<ScoredAnnotation<PERSON>>();

		for (final ScoredAnnotation<PERSON> p : pot) {
			if (restrict.contains(p.annotation))
				toKeep.add(p);
		}

		return toKeep;
	}

	@Override
	public List<ScoredAnnotation<PERSON>> annotate(FACE object) {
		return annotator.annotate(object);
	}

	@Override
	public void train(Annotated<FACE, PERSON> annotedImage) {
		annotator.train(annotedImage);
	}

	@Override
	public void train(Iterable<? extends Annotated<FACE, PERSON>> data) {
		annotator.train(data);
	}

	@Override
	public Set<PERSON> getAnnotations() {
		return annotator.getAnnotations();
	}

	@Override
	public void reset() {
		annotator.reset();
	}

	@Override
	public String toString() {
		return String.format("AnnotatorFaceRecogniser[recogniser=%s]", this.annotator);
	}
}
