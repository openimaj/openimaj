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
 * @param <FACE> Type of {@link DetectedFace}
 * @param <EXTRACTOR> Type of {@link FeatureExtractor}
 * @param <PERSON> Type of object representing a person
 */
public class AnnotatorFaceRecogniser<FACE extends DetectedFace, EXTRACTOR extends FeatureExtractor<?, FACE>, PERSON>
	extends 
		FaceRecogniser<FACE, EXTRACTOR, PERSON>
{
	protected IncrementalAnnotator<FACE, PERSON, EXTRACTOR> annotator;

	/**
	 * Construct with the given underlying annotator.
	 * @param annotator the annotator
	 */
	public AnnotatorFaceRecogniser(IncrementalAnnotator<FACE, PERSON, EXTRACTOR> annotator) {
		super(annotator.extractor);
		
		this.annotator = annotator;
	}

	/**
	 * Convenience method to create {@link AnnotatorFaceRecogniser} instances 
	 * from an annotator.
	 * 
	 * @param <FACE> Type of {@link DetectedFace}
	 * @param <EXTRACTOR> Type of {@link FeatureExtractor}
	 * @param <PERSON> Type of object representing a person
	 * @param annotator the annotator
	 * @return the new {@link AnnotatorFaceRecogniser} instance
	 */
	public static <FACE extends DetectedFace, EXTRACTOR extends FeatureExtractor<?, FACE>, PERSON> 
		AnnotatorFaceRecogniser<FACE, EXTRACTOR, PERSON> create(IncrementalAnnotator<FACE, PERSON, EXTRACTOR> annotator) {
		return new AnnotatorFaceRecogniser<FACE, EXTRACTOR, PERSON>(annotator);
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
	public List<ScoredAnnotation<PERSON>> annotate(FACE object, Collection<PERSON> restrict) {
		if (annotator instanceof RestrictedAnnotator) {
			return ((RestrictedAnnotator<FACE, PERSON>)annotator).annotate(object, restrict);
		}
		
		List<ScoredAnnotation<PERSON>> pot = annotator.annotate(object);
		
		if (pot == null || pot.size() == 0)
			return null;
		
		List<ScoredAnnotation<PERSON>> toKeep = new ArrayList<ScoredAnnotation<PERSON>>();
		
		for (ScoredAnnotation<PERSON> p : pot) {
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
	public void train(Dataset<? extends Annotated<FACE, PERSON>> data) {
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
}
