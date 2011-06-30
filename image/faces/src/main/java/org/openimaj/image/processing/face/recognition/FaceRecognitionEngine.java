package org.openimaj.image.processing.face.recognition;

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
import org.openimaj.io.IOUtils;
import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.util.pair.IndependentPair;

public class FaceRecognitionEngine<T extends DetectedFace> implements ReadWriteableBinary {
	protected FaceDetector<T, FImage> detector;
	protected FaceRecogniser<T> recogniser;

	public FaceRecognitionEngine() {}
	
	public FaceRecognitionEngine(FaceDetector<T, FImage> detector, FaceRecogniser<T> recogniser) {
		this.detector = detector;
		this.recogniser = recogniser;
	}
	
	public FaceDetector<T, FImage> getDetector() {
		return detector;
	}
	
	public FaceRecogniser<T> getRecogniser() {
		return recogniser;
	}
	
	public void save(File file) throws IOException {
		IOUtils.writeBinaryFull(file, this);
	}
	
	public static <T extends DetectedFace> FaceRecognitionEngine<T> load(File file) throws IOException {
		FaceRecognitionEngine<T> engine = IOUtils.read(file);
		return engine;
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
		List<T> faces = detector.detectFaces(image);
		
		if (faces.size() == 1) {
			recogniser.addInstance(identifier, faces.get(0));
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

	public List<IndependentPair<T, List<FaceMatchResult>>> query(File imgFile) throws IOException {
		return query(ImageUtilities.readF(imgFile));
	}
	
	public List<IndependentPair<T, List<FaceMatchResult>>> query(FImage image) {
		List<T> detectedFaces = detector.detectFaces(image);
		List<IndependentPair<T, List<FaceMatchResult>>> results = new ArrayList<IndependentPair<T, List<FaceMatchResult>>>();
		
		for (T df : detectedFaces) {
			results.add(new IndependentPair<T, List<FaceMatchResult>>(df, recogniser.query(df)));
		}
		
		return results;
	}
	
	public List<IndependentPair<T, FaceMatchResult>> queryBestMatch(File imgFile) throws IOException {
		return queryBestMatch(ImageUtilities.readF(imgFile));
	}
	
	public List<IndependentPair<T, FaceMatchResult>> queryBestMatch(FImage image) {
		List<T> detectedFaces = detector.detectFaces(image);
		List<IndependentPair<T, FaceMatchResult>> results = new ArrayList<IndependentPair<T, FaceMatchResult>>();
		
		for (T df : detectedFaces) {
			results.add(new IndependentPair<T, FaceMatchResult>(df, recogniser.queryBestMatch(df)));
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
