package org.openimaj.demos.sandbox.ml.cluster.spectral;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.math.random.GaussianRandomGenerator;
import org.apache.log4j.Logger;
import org.la4j.matrix.Matrices;
import org.la4j.matrix.sparse.CRSMatrix;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.colour.ColourMap;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.knn.DoubleNearestNeighboursExact;
import org.openimaj.logger.LoggerUtils;
import org.openimaj.math.matrix.MatlibMatrixUtils;
import org.openimaj.math.statistics.distribution.Histogram;
import org.openimaj.ml.clustering.dbscan.DoubleNNDBSCAN;
import org.openimaj.ml.clustering.kmeans.DoubleKMeans;
import org.openimaj.ml.clustering.spectral.AbsoluteValueEigenChooser;
import org.openimaj.ml.clustering.spectral.DoubleFVSimilarityFunction;
import org.openimaj.ml.clustering.spectral.DoubleSpectralClustering;
import org.openimaj.ml.clustering.spectral.GraphLaplacian;
import org.openimaj.ml.clustering.spectral.HardCodedEigenChooser;
import org.openimaj.ml.clustering.spectral.SpectralClusteringConf;
import org.openimaj.ml.clustering.spectral.SpectralIndexedClusters;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.vis.general.BarVisualisationBasic;

import be.ac.ulg.montefiore.run.distributions.GaussianDistribution;
import ch.akuhn.matrix.DenseMatrix;
import ch.akuhn.matrix.Matrix;
import ch.akuhn.matrix.SparseMatrix;
import ch.akuhn.matrix.Vector;
import ch.akuhn.matrix.Vector.Entry;

/**
 * Visualise a number of gaussians as historgrams.
 * Control their means and variances
 * Visualise their top N eigen vectors
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class GaussianSpectralClusterKMeansVis extends GaussianSpectralClusterVis{
	
	public GaussianSpectralClusterKMeansVis() {
		super();
	}
	
	protected SpectralClusteringConf<double[]> prepareConf() {
		SpectralClusteringConf<double[]> conf = new SpectralClusteringConf<double[]>(
				DoubleKMeans.createExact(0, 5,1000), 
				new GraphLaplacian.Normalised()
		);
//		conf.eigenChooser = new HardCodedEigenChooser(5);
		conf.eigenChooser = new AbsoluteValueEigenChooser(0.3, 0.1);
		return conf;
	}
	
	public static void main(String[] args) {
		LoggerUtils.prepareConsoleLogger();
		GaussianSpectralClusterVis gscvis = new GaussianSpectralClusterKMeansVis();
		
	}

}
