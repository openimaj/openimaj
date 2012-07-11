package org.openimaj.image.processing.face.recognition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openimaj.experiment.dataset.Dataset;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.ml.annotation.FeatureExtractor;
import org.openimaj.ml.annotation.IncrementalAnnotator;
import org.openimaj.ml.annotation.RestrictedAnnotator;

/**
 * A {@link FaceRecogniser} built on top of an {@link IncrementalAnnotator}.
 * This class essentially adapts standard {@link IncrementalAnnotator} to
 * work in the face recognition scenario. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <O> Type of {@link DetectedFace}
 * @param <E> Type of {@link FeatureExtractor}
 */
public class AnnotatorFaceRecogniser<O extends DetectedFace, E extends FeatureExtractor<?, O>>
	extends 
		FaceRecogniser<O, E>
{
	protected IncrementalAnnotator<O, String, E> annotator;

	/**
	 * Construct with the given underlying annotator.
	 * @param annotator the annotator
	 */
	public AnnotatorFaceRecogniser(IncrementalAnnotator<O, String, E> annotator) {
		super(annotator.extractor);
		
		this.annotator = annotator;
	}

	/**
	 * Convenience method to create {@link AnnotatorFaceRecogniser} instances 
	 * from an annotator.
	 * 
	 * @param <O> Type of {@link DetectedFace}
	 * @param <E> Type of {@link FeatureExtractor}
	 * @param annotator the annotator
	 * @return the new {@link AnnotatorFaceRecogniser} instance
	 */
	public static <O extends DetectedFace, E extends FeatureExtractor<?, O>> 
		AnnotatorFaceRecogniser<O, E> create(IncrementalAnnotator<O, String, E> annotator) {
		return new AnnotatorFaceRecogniser<O, E>(annotator);
	}
	
	@Override
	public void readBinary(DataInput in) throws IOException {
		annotator = IOUtils.read(in);
		extractor = annotator.extractor;
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
	public List<ScoredAnnotation<String>> annotate(O object, Collection<String> restrict) {
		if (annotator instanceof RestrictedAnnotator) {
			return ((RestrictedAnnotator<O,String>)annotator).annotate(object, restrict);
		}
		
		List<ScoredAnnotation<String>> pot = annotator.annotate(object);
		
		if (pot == null || pot.size() == 0)
			return null;
		
		List<ScoredAnnotation<String>> toKeep = new ArrayList<ScoredAnnotation<String>>();
		
		for (ScoredAnnotation<String> p : pot) {
			if (restrict.contains(p.annotation))
				toKeep.add(p);
		}
		
		return toKeep;
	}

	@Override
	public List<ScoredAnnotation<String>> annotate(O object) {
		return annotator.annotate(object);
	}

	@Override
	public void train(Annotated<O, String> annotedImage) {
		annotator.train(annotedImage);
	}
	
	@Override
	public void train(Dataset<? extends Annotated<O, String>> data) {
		annotator.train(data);
	}

	@Override
	public Set<String> getAnnotations() {
		return annotator.getAnnotations();
	}

	@Override
	public void reset() {
		annotator.reset();
	}
}
