package org.openimaj.ml.clustering.spectral;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.openimaj.data.dataset.Dataset;
import org.openimaj.experiment.evaluation.cluster.processor.ClustererWrapper;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.ml.clustering.SimilarityClusterer;
import org.openimaj.ml.clustering.TrainingIndexClusters;

import ch.akuhn.matrix.SparseMatrix;

/**
 * Wraps the functionality of a {@link SimilarityClusterer} around a dataset
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T> 
 *
 */
public abstract class SimilarityDoubleClustererWrapper<T> implements ClustererWrapper{
	Logger logger = Logger.getLogger(SimilarityDoubleClustererWrapper.class);
	double[][] feats = null;
	private final class ExtractedIterator implements Iterator<double[]> {
		private final Iterator<T> dataIter;
		int seen = 0;

		private ExtractedIterator(Iterator<T> dataIter) {
			this.dataIter = dataIter;
		}

		@Override
		public void remove() { throw new UnsupportedOperationException();}

		@Override
		public double[] next() {
			if(seen++ % 1000 == 0){
				logger.info(String.format("Extracting feature for %dth image",seen-1));
			}
			return extractor.extractFeature(dataIter.next()).values;
		}

		@Override
		public boolean hasNext() {
			return dataIter.hasNext();
		}
	}
	private FeatureExtractor<DoubleFV, T> extractor;
	private SimilarityClusterer<? extends TrainingIndexClusters> dbscan;
	private Dataset<T> data;
	/**
	 * @param data the data to be clustered
	 * @param extractor
	 * @param dbscan
	 *
	 */
	public SimilarityDoubleClustererWrapper(Dataset<T> data, FeatureExtractor<DoubleFV, T> extractor, SimilarityClusterer<? extends TrainingIndexClusters> dbscan) {
		this.data = data;
		this.extractor = extractor;
		this.dbscan = dbscan;
	}
	@Override
	public int[][] cluster() {
		int numInstances = data.numInstances();
		SparseMatrix mat = new SparseMatrix(numInstances,numInstances);
		prepareFeats();
		logger.info(String.format("Constructing sparse matrix with %d features",feats.length));
		mat = similarity(feats);
		logger.info(String.format("Similarity matrix sparcity: %2.5f",MatrixUtils.sparcity(mat)));
		TrainingIndexClusters res = dbscan.cluster(mat,false);
		return res.clusters();
	}
	
	protected void prepareFeats() {
		if(feats!=null)return;
		int numInstances = data.numInstances();
		feats = new double[numInstances][];
		int index = 0;
		for (Iterator<double[]> iterator = new ExtractedIterator(data.iterator()); iterator.hasNext();) {
			feats[index++] = iterator.next();
		}
	}
	
	abstract SparseMatrix similarity(double[][] testData);
}
