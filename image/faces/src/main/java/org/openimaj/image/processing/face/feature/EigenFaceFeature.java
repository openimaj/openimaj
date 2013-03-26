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
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.data.dataset.Dataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.FImage;
import org.openimaj.image.model.EigenImages;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.training.BatchTrainer;

/**
 * A {@link FacialFeature} for EigenFaces.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Turk, M.A.", "Pentland, A.P." },
		title = "Face recognition using eigenfaces",
		year = "1991",
		booktitle = "Computer Vision and Pattern Recognition, 1991. Proceedings CVPR '91., IEEE Computer Society Conference on",
		pages = { "586 ", "591" },
		month = "jun",
		number = "",
		volume = "",
		customData = {
				"keywords", "eigenfaces;eigenvectors;face images;face recognition system;face space;feature space;human faces;two-dimensional recognition;unsupervised learning;computerised pattern recognition;eigenvalues and eigenfunctions;",
				"doi", "10.1109/CVPR.1991.139758"
		})
public class EigenFaceFeature implements FacialFeature, FeatureVectorProvider<DoubleFV> {
	/**
	 * A {@link FacialFeatureExtractor} for producing EigenFaces. Unlike most
	 * {@link FacialFeatureExtractor}s, this one either needs to be trained or
	 * provided with a pre-trained {@link EigenImages} object.
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
			FacialFeatureExtractor<EigenFaceFeature, T>,
			BatchTrainer<T>
	{
		EigenImages eigen = null;
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
			this(new EigenImages(numComponents), aligner);
		}

		/**
		 * Construct with given pre-trained {@link EigenImages} basis and a face
		 * aligner.
		 * 
		 * @param basis
		 *            the pre-trained basis
		 * @param aligner
		 *            the face aligner
		 */
		public Extractor(EigenImages basis, FaceAligner<T> aligner) {
			this.eigen = basis;
			this.aligner = aligner;
		}

		@Override
		public EigenFaceFeature extractFeature(T face) {
			final FImage patch = aligner.align(face);

			final DoubleFV fv = eigen.extractFeature(patch);

			return new EigenFaceFeature(fv);
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
			eigen.readBinary(in);

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
			eigen.writeBinary(out);

			out.writeUTF(aligner.getClass().getName());
			aligner.writeBinary(out);
		}

		@Override
		public void train(final List<? extends T> data) {
			final List<FImage> patches = new AbstractList<FImage>() {

				@Override
				public FImage get(int index) {
					return aligner.align(data.get(index));
				}

				@Override
				public int size() {
					return data.size();
				}

			};

			eigen.train(patches);
		}

		/**
		 * Train from a dataset
		 * 
		 * @param data
		 *            the dataset
		 */
		public void train(final Dataset<? extends T> data) {
			train(DatasetAdaptors.asList(data));
		}

		@Override
		public String toString() {
			return String.format("EigenFaceFeature.Extractor[aligner=%s]", this.aligner);
		}
	}

	private DoubleFV fv;

	protected EigenFaceFeature() {
		this(null);
	}

	/**
	 * Construct the EigenFaceFeature with the given feature vector.
	 * 
	 * @param fv
	 *            the feature vector
	 */
	public EigenFaceFeature(DoubleFV fv) {
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
