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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.cache.GroupedListCache;
import org.openimaj.data.dataset.cache.InMemoryGroupedListCache;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.ScoredAnnotation;

/**
 * A face recogniser that caches detected faces and only performs actual
 * training when required. Provided as a base for the eigen and fisher face
 * recognisers as they typically need to train the feature extractors before
 * use.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <FACE>
 *            Type of {@link DetectedFace}
 * @param <PERSON>
 *            Type of object representing a person
 */
abstract class LazyFaceRecogniser<FACE extends DetectedFace, PERSON, EXTRACTOR extends FeatureExtractor<?, FACE>>
		extends
		FaceRecogniser<FACE, PERSON>
{
	EXTRACTOR extractor;
	FaceRecogniser<FACE, PERSON> internalRecogniser;
	GroupedListCache<PERSON, FACE> faceCache;
	boolean isInvalid = true;

	protected LazyFaceRecogniser() {
	}

	/**
	 * Construct with an in-memory cache and the given internal face recogniser.
	 * It is assumed that the internals of the given recogniser are somehow
	 * linked to or use the given feature extractor.
	 * 
	 * @param extractor
	 *            the feature extractor
	 * @param internalRecogniser
	 *            the internal recogniser.
	 */
	public LazyFaceRecogniser(EXTRACTOR extractor, FaceRecogniser<FACE, PERSON> internalRecogniser)
	{
		this.extractor = extractor;
		this.internalRecogniser = internalRecogniser;
		faceCache = new InMemoryGroupedListCache<PERSON, FACE>();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		final LazyFaceRecogniser<FACE, PERSON, EXTRACTOR> wrapper = IOUtils.read(in);
		this.extractor = wrapper.extractor;
		this.faceCache = wrapper.faceCache;
		this.internalRecogniser = wrapper.internalRecogniser;
		this.isInvalid = wrapper.isInvalid;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		IOUtils.write(this, out);
	}

	@Override
	public byte[] binaryHeader() {
		return "BFRec".getBytes();
	}

	@Override
	public void train(Annotated<FACE, PERSON> annotated) {
		faceCache.add(annotated.getAnnotations(), annotated.getObject());
		isInvalid = true;
	}

	@Override
	public void reset() {
		internalRecogniser.reset();
		faceCache.reset();
		isInvalid = true;
	}

	@Override
	public Set<PERSON> getAnnotations() {
		return faceCache.getDataset().getGroups();
	}

	/**
	 * Called before batch training/re-training takes place.
	 * 
	 * @param dataset
	 *            the dataset
	 */
	protected abstract void beforeBatchTrain(GroupedDataset<PERSON, ListDataset<FACE>, FACE> dataset);

	private void retrain() {
		if (isInvalid) {
			final GroupedDataset<PERSON, ListDataset<FACE>, FACE> dataset = faceCache.getDataset();
			beforeBatchTrain(dataset);
			internalRecogniser.train(dataset);
			isInvalid = false;
		}
	}

	@Override
	public List<ScoredAnnotation<PERSON>> annotate(FACE object, Collection<PERSON> restrict) {
		retrain();
		return internalRecogniser.annotate(object, restrict);
	}

	@Override
	public List<ScoredAnnotation<PERSON>> annotate(FACE object) {
		retrain();
		return internalRecogniser.annotate(object);
	}
}
