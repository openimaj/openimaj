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
package org.openimaj.ml.clustering.spectral;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.openimaj.experiment.evaluation.cluster.ClusterEvaluator;
import org.openimaj.experiment.evaluation.cluster.analyser.FullMEAnalysis;
import org.openimaj.experiment.evaluation.cluster.analyser.FullMEClusterAnalyser;
import org.openimaj.experiment.evaluation.cluster.processor.Clusterer;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.knn.DoubleNearestNeighboursExact;
import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.ml.clustering.SpatialClusters;
import org.openimaj.ml.clustering.dbscan.DistanceDBSCAN;
import org.openimaj.ml.clustering.dbscan.DoubleDBSCANClusters;
import org.openimaj.ml.clustering.dbscan.DoubleNNDBSCAN;
import org.openimaj.ml.clustering.dbscan.SparseMatrixDBSCAN;
import org.openimaj.ml.dataset.WineDataset;
import org.openimaj.util.function.Function;
import org.openimaj.util.pair.DoubleObjectPair;

import ch.akuhn.matrix.SparseMatrix;
import ch.akuhn.matrix.Vector;
import ch.akuhn.matrix.eigenvalues.AllEigenvalues;
import ch.akuhn.matrix.eigenvalues.Eigenvalues;

/**
 * Perform spectral clustering experiments using the Wine Dataset
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class WineDatasetExperiment {
	private static final int MAXIMUM_DISTANCE = 300;
	private static Logger logger = Logger.getLogger(WineDatasetExperiment.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WineDataset ds = new WineDataset(2,3);
		
//		logger.info("Clustering using spectral clustering");
//		DoubleSpectralClustering clust = prepareSpectralClustering();
//		ClustererWrapper spectralWrapper = new NormalisedSimilarityDoubleClustererWrapper<double[]>(
//			ds, 
//			new WrapperExtractor(), 
//			clust, 
//			MAXIMUM_DISTANCE
//		);
//		evaluate(ds, clust);
//		logger.info("Clustering using DBScan");
//		DoubleDBSCAN dbScan = prepareDBScane();
//		ClustererWrapper dbScanWrapper = new NormalisedSimilarityDoubleClustererWrapper<double[]>(
//			ds, 
//			new WrapperExtractor(), 
//			dbScan, 
//			MAXIMUM_DISTANCE
//		);
//		evaluate(ds, dbScan);
		
		logger.info("Clustering using modified spectral clustering");
		DoubleSpectralClustering clustCSP = prepareCSPSpectralClustering(ds);
		Function<List<double[]>,SparseMatrix> func = new RBFSimilarityDoubleClustererWrapper<double[]>(new DummyExtractor());
		evaluate(ds, clustCSP, func);
	}

	private static DoubleSpectralClustering prepareCSPSpectralClustering(WineDataset ds) {
		SpatialClusterer<? extends SpatialClusters<double[]>, double[]> cl = null;
		// Creater the spectral clustering
		SpectralClusteringConf<double[]> conf = new SpectralClusteringConf<double[]>(cl );
		conf.eigenChooser = new EigenChooser() {
			
			@Override
			public Eigenvalues prepare(SparseMatrix laplacian) {
				Eigenvalues eig = new AllEigenvalues(laplacian);
				return eig;
			}
			
			@Override
			public int nEigenVectors(Iterator<DoubleObjectPair<Vector>> vals, int totalEigenVectors) {
				// TODO Auto-generated method stub
				return 0;
			}
		};
		DoubleSpectralClustering clust = new DoubleSpectralClustering(conf);
		return clust;
	}

	private static SparseMatrixDBSCAN prepareDBScane() {
		// Creater the spectral clustering
		double epss = 0.5;
		SparseMatrixDBSCAN inner = new DistanceDBSCAN(epss, 1);
		return inner;
	}

	private static DoubleSpectralClustering prepareSpectralClustering() {
		// Creater the spectral clustering
		double epss = 0.6;
		SpatialClusterer<DoubleDBSCANClusters,double[]> inner = new DoubleNNDBSCAN(epss, 2,new DoubleNearestNeighboursExact.Factory(DoubleFVComparison.EUCLIDEAN));
		SpectralClusteringConf<double[]> conf = new SpectralClusteringConf<double[]>(
			inner
		);
//		conf.eigenChooser = new AutoSelectingEigenChooser(100, 1.0);
		conf.eigenChooser = new HardCodedEigenChooser(10);
		DoubleSpectralClustering clust = new DoubleSpectralClustering(conf);
		return clust;
	}

	private static void evaluate(WineDataset ds, Clusterer<SparseMatrix> clust, Function<List<double[]>, SparseMatrix> func) {
		ClusterEvaluator<SparseMatrix, FullMEAnalysis> eval = new ClusterEvaluator<SparseMatrix, FullMEAnalysis>(clust,ds,func,new FullMEClusterAnalyser());
		int[][] evaluate = eval.evaluate();
		logger.info("Expected Classes: " + ds.size());
		logger.info("Detected Classes: " + evaluate.length);
		FullMEAnalysis res = eval.analyse(evaluate);
		System.out.println(res.getSummaryReport());
	}
}
