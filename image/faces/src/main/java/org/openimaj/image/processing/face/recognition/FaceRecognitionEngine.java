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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.detection.DatasetFaceDetector;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.io.IOUtils;
import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.ml.annotation.AnnotatedObject;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.util.pair.IndependentPair;

/**
 * The {@link FaceRecognitionEngine} ties together the implementations of a
 * {@link FaceDetector} and {@link FaceRecogniser}, and provides a single
 * convenience API with which to interact with a face recognition system.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <FACE>
 *            Type of {@link DetectedFace}
 * @param <PERSON>
 *            Type representing a person
 */
public class FaceRecognitionEngine<FACE extends DetectedFace, PERSON>
		implements
		ReadWriteableBinary
{
	private static final Logger logger = Logger.getLogger(FaceRecognitionEngine.class);

	protected FaceDetector<FACE, FImage> detector;
	protected FaceRecogniser<FACE, PERSON> recogniser;

	protected FaceRecognitionEngine() {
	}

	/**
	 * Construct a {@link FaceRecognitionEngine} with the given face detector
	 * and recogniser.
	 * 
	 * @param detector
	 *            the face detector
	 * @param recogniser
	 *            the face recogniser
	 */
	public FaceRecognitionEngine(final FaceDetector<FACE, FImage> detector,
			final FaceRecogniser<FACE, PERSON> recogniser)
	{
		this.detector = detector;
		this.recogniser = recogniser;
	}

	/**
	 * Create a {@link FaceRecognitionEngine} with the given face detector and
	 * recogniser.
	 * 
	 * @param <FACE>
	 *            Type of {@link DetectedFace}
	 * @param <EXTRACTOR>
	 *            Type of {@link FeatureExtractor}
	 * @param <PERSON>
	 *            Type representing a person
	 * 
	 * @param detector
	 *            the face detector
	 * @param recogniser
	 *            the face recogniser
	 * @return new {@link FaceRecognitionEngine}
	 */
	public static <FACE extends DetectedFace, EXTRACTOR extends FeatureExtractor<?, FACE>, PERSON>
			FaceRecognitionEngine<FACE, PERSON>
			create(final FaceDetector<FACE, FImage> detector, final FaceRecogniser<FACE, PERSON> recogniser)
	{
		return new FaceRecognitionEngine<FACE, PERSON>(detector, recogniser);
	}

	/**
	 * @return the detector
	 */
	public FaceDetector<FACE, FImage> getDetector() {
		return this.detector;
	}

	/**
	 * @return the recogniser
	 */
	public FaceRecogniser<FACE, PERSON> getRecogniser() {
		return this.recogniser;
	}

	/**
	 * Save the {@link FaceRecognitionEngine} to a file, including all the
	 * internal state of the recogniser, etc.
	 * 
	 * @param file
	 *            the file to save to
	 * @throws IOException
	 *             if an error occurs when writing
	 */
	public void save(final File file) throws IOException {
		IOUtils.writeBinaryFull(file, this);
	}

	/**
	 * Load a {@link FaceRecognitionEngine} previously saved by
	 * {@link #save(File)}.
	 * 
	 * @param <O>
	 *            Type of {@link DetectedFace}
	 * @param <P>
	 *            Type representing a person
	 * 
	 * @param file
	 *            the file to read from
	 * @return the created recognition engine
	 * @throws IOException
	 *             if an error occurs during the read
	 */
	public static <O extends DetectedFace, P> FaceRecognitionEngine<O, P> load(final File file) throws IOException
	{
		final FaceRecognitionEngine<O, P> engine = IOUtils.read(file);

		return engine;
	}

	/**
	 * Train with a dataset
	 * 
	 * @param dataset
	 *            the dataset
	 */
	public void train(final GroupedDataset<PERSON, ListDataset<FImage>, FImage> dataset) {
		final GroupedDataset<PERSON, ListDataset<FACE>, FACE> faceDataset = DatasetFaceDetector
				.process(dataset, this.detector);
		this.recogniser.train(faceDataset);
	}

	/**
	 * Train the recogniser with a single example, returning the detected face.
	 * If multiple faces are found, the biggest is chosen.
	 * <p>
	 * If you need more control, consider calling {@link #getDetector()} to get
	 * a detector which you can apply to your image and {@link #getRecogniser()}
	 * to get the recogniser which you can train with the detections directly.
	 * 
	 * @param person
	 *            the person
	 * @param image
	 *            the image with the persons face
	 * @return the detected face
	 */
	public FACE train(final PERSON person, final FImage image) {
		final List<FACE> faces = this.detector.detectFaces(image);

		if (faces == null || faces.size() == 0) {
			FaceRecognitionEngine.logger.warn("no face detected");
			return null;
		} else if (faces.size() == 1) {
			this.recogniser.train(AnnotatedObject.create(faces.get(0), person));
			return faces.get(0);
		} else {
			FaceRecognitionEngine.logger.warn("More than one face found. Choosing biggest.");

			final FACE face = DatasetFaceDetector.getBiggest(faces);
			this.recogniser.train(AnnotatedObject.create(face, person));
			return face;
		}
	}

	/**
	 * Train for the given face patch without doing any face detection. It is
	 * assumed that the given image will be a cropped/aligned image of the face
	 * as is necessary for the given recogniser.
	 * 
	 * @param face
	 *            The detected face implementation
	 * @param person
	 *            The person to whom this face belongs
	 * @return The face image
	 */
	public FACE train(final FACE face, final PERSON person)
	{
		this.recogniser.train(AnnotatedObject.create(face, person));
		return face;
	}

	/**
	 * Detect and recognise the faces in the given image, returning a list of
	 * potential people for each face.
	 * 
	 * @param image
	 *            the image
	 * @return a list of faces and recognitions
	 */
	public List<IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>> recognise(final FImage image) {
		final List<FACE> detectedFaces = this.detector.detectFaces(image);
		final List<IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>> results = new ArrayList<IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>>();

		for (final FACE df : detectedFaces) {
			results.add(new IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>(df, this.recogniser.annotate(df)));
		}

		return results;
	}

	/**
	 * Detect and recognise the faces in the given image, returning the most
	 * likely person for each face.
	 * 
	 * @param image
	 *            the image
	 * @return a list of faces with the most likely person
	 */
	public List<IndependentPair<FACE, ScoredAnnotation<PERSON>>> recogniseBest(final FImage image) {
		final List<FACE> detectedFaces = this.detector.detectFaces(image);
		final List<IndependentPair<FACE, ScoredAnnotation<PERSON>>> results = new ArrayList<IndependentPair<FACE, ScoredAnnotation<PERSON>>>();

		for (final FACE df : detectedFaces) {
			results.add(new IndependentPair<FACE, ScoredAnnotation<PERSON>>(df, this.recogniser.annotateBest(df)));
		}

		return results;
	}

	/**
	 * Detect and recognise the faces in the given image, returning a list of
	 * potential people for each face. The recognised people will be restricted
	 * to the given set.
	 * 
	 * @param image
	 *            the image
	 * @param restrict
	 *            set of people to restrict to
	 * @return a list of faces and recognitions
	 */
	public List<IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>> recognise(final FImage image,
			final Set<PERSON> restrict)
	{
		final List<FACE> detectedFaces = this.detector.detectFaces(image);
		final List<IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>> results = new ArrayList<IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>>();

		for (final FACE df : detectedFaces) {
			results.add(new IndependentPair<FACE, List<ScoredAnnotation<PERSON>>>(df, this.recogniser.annotate(df,
					restrict)));
		}

		return results;
	}

	/**
	 * Detect and recognise the faces in the given image, returning the most
	 * likely person for each face. The recognised people will be restricted to
	 * the given set.
	 * 
	 * @param image
	 *            the image
	 * @param restrict
	 *            set of people to restrict to
	 * @return a list of faces with the most likely person
	 */
	public List<IndependentPair<FACE, ScoredAnnotation<PERSON>>> recogniseBest(final FImage image,
			final Set<PERSON> restrict)
	{
		final List<FACE> detectedFaces = this.detector.detectFaces(image);
		final List<IndependentPair<FACE, ScoredAnnotation<PERSON>>> results = new ArrayList<IndependentPair<FACE, ScoredAnnotation<PERSON>>>();

		for (final FACE df : detectedFaces) {
			results.add(new IndependentPair<FACE, ScoredAnnotation<PERSON>>(df, this.recogniser
					.annotateBest(df, restrict)));
		}

		return results;
	}

	@Override
	public void readBinary(final DataInput in) throws IOException {
		final String detectorClass = in.readUTF();
		this.detector = IOUtils.newInstance(detectorClass);
		this.detector.readBinary(in);

		final String recogniserClass = in.readUTF();
		this.recogniser = IOUtils.newInstance(recogniserClass);
		this.recogniser.readBinary(in);
	}

	@Override
	public byte[] binaryHeader() {
		return "FaRE".getBytes();
	}

	@Override
	public void writeBinary(final DataOutput out) throws IOException {
		out.writeUTF(this.detector.getClass().getName());
		this.detector.writeBinary(out);

		out.writeUTF(this.recogniser.getClass().getName());
		this.recogniser.writeBinary(out);
	}
}
