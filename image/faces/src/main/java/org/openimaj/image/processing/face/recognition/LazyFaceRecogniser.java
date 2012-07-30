package org.openimaj.image.processing.face.recognition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.cache.GroupedListCache;
import org.openimaj.experiment.dataset.cache.InMemoryGroupedListCache;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.ScoredAnnotation;

/**
 * A face recogniser that caches detected faces and only performs
 * actual training when required. Provided as a base for the eigen
 * and fisher face recognisers as they typically need to train the
 * feature extractors before use.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <FACE> Type of {@link DetectedFace}
 * @param <EXTRACTOR> Type of {@link FeatureExtractor}
 * @param <PERSON> Type of object representing a person
 */
abstract class LazyFaceRecogniser<FACE extends DetectedFace, EXTRACTOR extends FeatureExtractor<?, FACE>, PERSON>
	extends 
		FaceRecogniser<FACE, EXTRACTOR, PERSON>
{
	FaceRecogniser<FACE, ? extends FeatureExtractor<?, FACE>, PERSON> internalRecogniser;
	GroupedListCache<PERSON, FACE> faceCache;
	boolean isInvalid = true;
	
	/**
	 * Construct with an in-memory cache and the given
	 * internal face recogniser. It is assumed that the
	 * FeatureExtractor of the given recogniser is somehow
	 * linked to the given feature extractor, but they might 
	 * not be the same object. 
	 * 
	 * @param extractor the feature extractor 
	 * @param internalRecogniser the internal recogniser.
	 */
	public LazyFaceRecogniser(EXTRACTOR extractor, FaceRecogniser<FACE, ? extends FeatureExtractor<?, FACE>, PERSON> internalRecogniser) {
		super(extractor);
		
		this.internalRecogniser = internalRecogniser;
		faceCache = new InMemoryGroupedListCache<PERSON, FACE>();
	}
	
	/**
	 * Construct with an in-memory cache and the given
	 * internal face recogniser.
	 * 
	 * @param internalRecogniser the internal recogniser.
	 */
	public LazyFaceRecogniser(FaceRecogniser<FACE, EXTRACTOR, PERSON> internalRecogniser) {
		super(internalRecogniser.extractor);
		
		this.internalRecogniser = internalRecogniser;
		faceCache = new InMemoryGroupedListCache<PERSON, FACE>();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		internalRecogniser = IOUtils.newInstance(in.readUTF());
		internalRecogniser.readBinary(in);
		faceCache = IOUtils.read(in);
		isInvalid = in.readBoolean();
	}

	@Override
	public byte[] binaryHeader() {
		return "BFRec".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeUTF(internalRecogniser.getClass().getName());
		internalRecogniser.writeBinary(out);
		IOUtils.write(faceCache, out);
		out.writeBoolean(isInvalid);
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
	 * @param dataset the dataset
	 */
	protected abstract void beforeBatchTrain(GroupedDataset<PERSON, ListDataset<FACE>, FACE> dataset);
	
	private void retrain() {
		if (isInvalid) {
			GroupedDataset<PERSON, ListDataset<FACE>, FACE> dataset = faceCache.getDataset();
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
