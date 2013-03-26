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
package org.openimaj.image.processing.face.feature;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.FImage;
import org.openimaj.image.model.FisherImages;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.training.BatchTrainer;
import org.openimaj.util.pair.IndependentPair;

/**
 * A {@link FacialFeature} for FisherFaces.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Belhumeur, Peter N.", "Hespanha, Jo\\~{a}o P.", "Kriegman, David J." },
		title = "Fisherfaces vs. Fisherfaces: Recognition Using Class Specific Linear Projection",
		year = "1997",
		journal = "IEEE Trans. Pattern Anal. Mach. Intell.",
		pages = { "711", "", "720" },
		url = "http://dx.doi.org/10.1109/34.598228",
		month = "July",
		number = "7",
		publisher = "IEEE Computer Society",
		volume = "19",
		customData = {
				"issn", "0162-8828",
				"numpages", "10",
				"doi", "10.1109/34.598228",
				"acmid", "261512",
				"address", "Washington, DC, USA",
				"keywords",
				"Appearance-based vision, face recognition, illumination invariance, Fisher's linear discriminant."
		})
public class FisherFaceFeature implements FacialFeature, FeatureVectorProvider<DoubleFV> {
	/**
	 * A {@link FacialFeatureExtractor} for producing FisherFaces. Unlike most
	 * {@link FacialFeatureExtractor}s, this one either needs to be trained or
	 * provided with a pre-trained {@link FisherImages} object.
	 * <p>
	 * A {@link FaceAligner} can be used to produce aligned faces for training
	 * and feature extraction.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 * @param <T>
	 * 
	 */
	public static class Extractor<T extends DetectedFace>
			implements
			FacialFeatureExtractor<FisherFaceFeature, T>,
			BatchTrainer<IndependentPair<?, T>>
	{
		FisherImages fisher = null;
		FaceAligner<T> aligner = null;

		/**
		 * Construct with the requested number of components (the number of PCs
		 * to keep) and a face aligner. The principal components must be learned
		 * by calling {@link #train(List)}.
		 * 
		 * @param numComponents
		 *            the number of principal components to keep.
		 * @param aligner
		 *            the face aligner
		 */
		public Extractor(int numComponents, FaceAligner<T> aligner) {
			this(new FisherImages(numComponents), aligner);
		}

		/**
		 * Construct with given pre-trained {@link FisherImages} basis and a
		 * face aligner.
		 * 
		 * @param basis
		 *            the pre-trained basis
		 * @param aligner
		 *            the face aligner
		 */
		public Extractor(FisherImages basis, FaceAligner<T> aligner) {
			this.fisher = basis;
			this.aligner = aligner;
		}

		@Override
		public FisherFaceFeature extractFeature(T face) {
			final FImage patch = aligner.align(face);

			final DoubleFV fv = fisher.extractFeature(patch);

			return new FisherFaceFeature(fv);
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
			fisher.readBinary(in);

			final String alignerClass = in.readUTF();
			aligner = IOUtils.newInstance(alignerClass);
			aligner.readBinary(in);
		}

		@Override
		public byte[] binaryHeader() {
			return this.getClass().getName().getBytes();
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			fisher.writeBinary(out);

			out.writeUTF(aligner.getClass().getName());
			aligner.writeBinary(out);
		}

		@Override
		public void train(final List<? extends IndependentPair<?, T>> data) {
			final List<IndependentPair<?, FImage>> patches = new AbstractList<IndependentPair<?, FImage>>() {

				@Override
				public IndependentPair<?, FImage> get(int index) {
					return IndependentPair.pair(data.get(index).firstObject(),
							aligner.align(data.get(index).secondObject()));
				}

				@Override
				public int size() {
					return data.size();
				}

			};

			fisher.train(patches);
		}

		/**
		 * Train on a map of data.
		 * 
		 * @param data
		 *            the data
		 */
		public void train(Map<?, ? extends List<T>> data) {
			final List<IndependentPair<?, FImage>> list = new ArrayList<IndependentPair<?, FImage>>();

			for (final Entry<?, ? extends List<T>> e : data.entrySet()) {
				for (final T i : e.getValue()) {
					list.add(IndependentPair.pair(e.getKey(), aligner.align(i)));
				}
			}

			fisher.train(list);
		}

		/**
		 * Train on a grouped dataset.
		 * 
		 * @param <KEY>
		 *            The group type
		 * @param data
		 *            the data
		 */
		public <KEY> void train(GroupedDataset<KEY, ? extends ListDataset<T>, T> data) {
			final List<IndependentPair<?, FImage>> list = new ArrayList<IndependentPair<?, FImage>>();

			for (final KEY e : data.getGroups()) {
				for (final T i : data.getInstances(e)) {
					if (i != null)
						list.add(IndependentPair.pair(e, aligner.align(i)));
				}
			}

			fisher.train(list);
		}
	}

	private DoubleFV fv;

	protected FisherFaceFeature() {
		this(null);
	}

	/**
	 * Construct the FisherFaceFeature with the given feature vector.
	 * 
	 * @param fv
	 *            the feature vector
	 */
	public FisherFaceFeature(DoubleFV fv) {
		this.fv = fv;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		fv = new DoubleFV();
		fv.readBinary(in);
	}

	@Override
	public byte[] binaryHeader() {
		return getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		fv.writeBinary(out);
	}

	@Override
	public DoubleFV getFeatureVector() {
		return fv;
	}
}
