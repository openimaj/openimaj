package org.openimaj.image.processing.face.recognition;

import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.FVProviderExtractor;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.feature.FisherFaceFeature.Extractor;
import org.openimaj.ml.annotation.IncrementalAnnotator;
import org.openimaj.ml.annotation.basic.KNNAnnotator;

/**
 * Implementation of a {@link FaceRecogniser} based on Fisherfaces. Any kind
 * of machine learning implementation can be used for the actual classification.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <FACE> Type of {@link DetectedFace}
 * @param <PERSON> Type of object representing a person
 */
public class FisherFaceRecogniser<FACE extends DetectedFace, PERSON>
	extends 
		LazyFaceRecogniser<FACE, Extractor<FACE>, PERSON>
{
	/**
	 * Construct with the given underlying {@link FaceRecogniser}.
	 * 
	 * @param internalRecogniser the face recogniser
	 */
	public FisherFaceRecogniser(FaceRecogniser<FACE, Extractor<FACE>, PERSON> internalRecogniser) {
		super(internalRecogniser);
	}
	
	/**
	 * Construct with the given feature extractor and underlying {@link FaceRecogniser}.
	 * 
	 * @param extractor the feature extractor
	 * @param internalRecogniser the face recogniser
	 */
	public FisherFaceRecogniser(Extractor<FACE> extractor, FaceRecogniser<FACE, ? extends FeatureExtractor<?, FACE>, PERSON> internalRecogniser) {
		super(extractor, internalRecogniser);
	}
	
	/**
	 * Construct with the given underlying {@link IncrementalAnnotator}.
	 * @param annotator the annotator
	 */
	public FisherFaceRecogniser(IncrementalAnnotator<FACE, PERSON, Extractor<FACE>> annotator) {
		this(AnnotatorFaceRecogniser.create(annotator));
	}
	
	/**
	 * Construct with the given feature extractor and underlying {@link IncrementalAnnotator}.
	 * @param extractor the feature extractor
	 * @param annotator the annotator
	 */
	public FisherFaceRecogniser(Extractor<FACE> extractor, IncrementalAnnotator<FACE, PERSON, ? extends FeatureExtractor<?, FACE>> annotator) {
		this(extractor, AnnotatorFaceRecogniser.create(annotator));
	}
	
	/**
	 * Convenience method to create an {@link FisherFaceRecogniser} with a
	 * standard KNN classifier.
	 * 
	 * @param <FACE> The type of {@link DetectedFace}
	 * @param <PERSON> the type representing a person
	 * @param numComponents the number of principal components to keep
	 * @param aligner the face aligner
	 * @param k the number of nearest neighbours
	 * @param compar the distance comparison function
	 * @param threshold a distance threshold to limit matches.
	 * @return a new {@link FisherFaceRecogniser}
	 */
	public static <FACE extends DetectedFace, PERSON> 
	FisherFaceRecogniser<FACE, PERSON> create(int numComponents, FaceAligner<FACE> aligner, int k, DoubleFVComparison compar, float threshold) 
{
	Extractor<FACE> extractor = new Extractor<FACE>(numComponents, aligner);
	FVProviderExtractor<DoubleFV, FACE, Extractor<FACE>> extractor2 = FVProviderExtractor.create(extractor);
	
	KNNAnnotator<FACE, PERSON, FVProviderExtractor<DoubleFV, FACE, Extractor<FACE>>, DoubleFV> knn = 
		KNNAnnotator.create(extractor2, compar, k, threshold);
	
	return new FisherFaceRecogniser<FACE, PERSON>(extractor, knn);
}

	@Override
	protected void beforeBatchTrain(GroupedDataset<PERSON, ListDataset<FACE>, FACE> dataset) {
		extractor.train(dataset);
	}
}
