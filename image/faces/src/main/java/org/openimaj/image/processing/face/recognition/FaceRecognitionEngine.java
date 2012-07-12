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

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.io.FileUtils;
import org.openimaj.io.IOUtils;
import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.ml.annotation.AnnotatedObject;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.ml.feature.FeatureExtractor;
import org.openimaj.util.pair.IndependentPair;

/**
 * The {@link FaceRecognitionEngine} ties together the implementations
 * of a {@link FaceDetector} and {@link FaceRecogniser}, and provides
 * a single convenience API with which to interact a face recognition 
 * system.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <FACE> Type of {@link DetectedFace}
 * @param <EXTRACTOR> Type of {@link FeatureExtractor}
 */
public class FaceRecognitionEngine<FACE extends DetectedFace, EXTRACTOR extends FeatureExtractor<?, FACE>> implements ReadWriteableBinary {
	protected FaceDetector<FACE, FImage> detector;
	protected FaceRecogniser<FACE, EXTRACTOR, String> recogniser;
	
	/**
	 * Construct a {@link FaceRecognitionEngine} with the given face detector and recogniser.
	 * @param detector the face detector
	 * @param recogniser the face recogniser
	 */
	public FaceRecognitionEngine(FaceDetector<FACE, FImage> detector, FaceRecogniser<FACE, EXTRACTOR, String> recogniser) {
		this.detector = detector;
		this.recogniser = recogniser;
	}
	
	/**
	 * Create a {@link FaceRecognitionEngine} with the given face detector and recogniser.
	 * 
	 * @param <FACE> Type of {@link DetectedFace}
	 * @param <EXTRACTOR> Type of {@link FeatureExtractor}
	 * 
	 * @param detector the face detector
	 * @param recogniser the face recogniser
	 * @return new {@link FaceRecognitionEngine}
	 */
	public static <FACE extends DetectedFace, EXTRACTOR extends FeatureExtractor<?, FACE>> 
		FaceRecognitionEngine<FACE, EXTRACTOR> create(FaceDetector<FACE, FImage> detector, FaceRecogniser<FACE, EXTRACTOR, String> recogniser) 
	{
		return new FaceRecognitionEngine<FACE, EXTRACTOR>(detector, recogniser);
	}
	
	/**
	 * @return the detector
	 */
	public FaceDetector<FACE, FImage> getDetector() {
		return detector;
	}
	
	/**
	 * @return the recogniser
	 */
	public FaceRecogniser<FACE, EXTRACTOR, String> getRecogniser() {
		return recogniser;
	}
	
	/**
	 * Save the {@link FaceRecognitionEngine} to a file, including all the
	 * internal state of the recogniser, etc.
	 * @param file the file to save to
	 * @throws IOException if an error occurs when writing
	 */
	public void save(File file) throws IOException {
		IOUtils.writeBinaryFull(file, this);
	}
	
	/**
	 * Load a {@link FaceRecognitionEngine} previously saved by 
	 * {@link #save(File)}.
	 * 
	 * @param <O> Type of {@link DetectedFace}
	 * @param <E> Type of {@link FeatureExtractor}
	 * 
	 * @param file the file to read from
	 * @return the created recognition engine
	 * @throws IOException if an error occurs during the read
	 */
	public static <O extends DetectedFace, E extends FeatureExtractor<?, O>> FaceRecognitionEngine<O, E> load(File file) throws IOException {
		FaceRecognitionEngine<O, E> engine = IOUtils.read(file);
		return engine;
	}
	
	public void trainBatchFile(File identifierFile) throws IOException {
		BufferedReader reader = FileUtils.read(identifierFile);
		String line = null;
		while((line = reader.readLine()) != null){
			String[] parts = line.split(" ");
			String identifier = parts[0];
			List<File> files = new ArrayList<File>();
			for(int i = 1; i < parts.length; i++){
				files.add(new File(parts[i]));
			}
			System.out.println(String.format("Adding %s: %d faces",identifier,files.size()));
			this.trainSingle(identifier, files);
		}
	}
	
	public void trainSingle(String identifier, List<File> dirs) throws IOException {
		for (File f : dirs)
			trainSingle(identifier, f);
	}
	
	public void trainSingle(String identifier, File imgFile) throws IOException {
		FImage image = ImageUtilities.readF(imgFile);
		trainSingle(identifier, image);
	}
	
	public void trainSingle(String identifier, FImage image) {
		List<FACE> faces = detector.detectFaces(image);
		
		if (faces.size() == 1) {
			recogniser.train(AnnotatedObject.create(faces.get(0), identifier));
		} else {
			System.err.format("Found %d faces. Ignoring.", faces.size());
		}
	}
	
	public void trainBatch(List<File> dirs) {
		for (File f : dirs)
			trainBatch(f);
	}
	
	public void trainBatch(File basedir) {
		for (File personDir : basedir.listFiles()) {
			if (!personDir.isHidden() && personDir.isDirectory() && personDir.listFiles().length >= 4) {
				String identifier = personDir.getName().replace("_", " ");

				for (File imgFile : personDir.listFiles()) {
					if (imgFile.isFile() && !imgFile.isHidden()) {
						try {
							trainSingle(identifier, imgFile);
						} catch (IOException e) {
							//ignore; probably wasn't an image file
						}
					}
				}
			}
		}
	}

	public List<IndependentPair<FACE, List<ScoredAnnotation<String>>>> recognise(File imgFile) throws IOException {
		return recognise(ImageUtilities.readF(imgFile));
	}
	
	public List<IndependentPair<FACE, List<ScoredAnnotation<String>>>> recognise(FImage image) {
		List<FACE> detectedFaces = detector.detectFaces(image);
		List<IndependentPair<FACE, List<ScoredAnnotation<String>>>> results = new ArrayList<IndependentPair<FACE, List<ScoredAnnotation<String>>>>();
		
		for (FACE df : detectedFaces) {
			results.add(new IndependentPair<FACE, List<ScoredAnnotation<String>>>(df, recogniser.annotate(df)));
		}
		
		return results;
	}
	
	public List<IndependentPair<FACE, List<ScoredAnnotation<String>>>> recogniseBest(File imgFile) throws IOException {
		return recogniseBest(ImageUtilities.readF(imgFile));
	}
	
	public List<IndependentPair<FACE, List<ScoredAnnotation<String>>>> recogniseBest(FImage image) {
		List<FACE> detectedFaces = detector.detectFaces(image);
		List<IndependentPair<FACE,List<ScoredAnnotation<String>>>> results = new ArrayList<IndependentPair<FACE, List<ScoredAnnotation<String>>>>();
		
		for (FACE df : detectedFaces) {
			results.add(new IndependentPair<FACE, List<ScoredAnnotation<String>>>(df, recogniser.annotate(df)));
		}
		
		return results;
	}
	
	@Override
	public void readBinary(DataInput in) throws IOException {
		String detectorClass = in.readUTF();
		detector = IOUtils.newInstance(detectorClass);
		detector.readBinary(in);
		
		String recogniserClass = in.readUTF();
		recogniser = IOUtils.newInstance(recogniserClass);
		recogniser.readBinary(in);
	}

	@Override
	public byte[] binaryHeader() {
		return "FaRE".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeUTF(detector.getClass().getName());
		detector.writeBinary(out);
		
		out.writeUTF(recogniser.getClass().getName());
		recogniser.writeBinary(out);
	}
}
