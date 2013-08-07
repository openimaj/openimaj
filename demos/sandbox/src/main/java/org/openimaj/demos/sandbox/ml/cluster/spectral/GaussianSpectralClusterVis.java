package org.openimaj.demos.sandbox.ml.cluster.spectral;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import org.apache.commons.math.random.GaussianRandomGenerator;
import org.apache.log4j.Logger;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.knn.DoubleNearestNeighbours;
import org.openimaj.knn.DoubleNearestNeighboursExact;
import org.openimaj.logger.LoggerUtils;
import org.openimaj.math.matrix.MatlibMatrixUtils;
import org.openimaj.math.statistics.distribution.Histogram;
import org.openimaj.ml.clustering.dbscan.DoubleNNDBSCAN;
import org.openimaj.ml.clustering.spectral.DoubleFVSimilarityFunction;
import org.openimaj.ml.clustering.spectral.DoubleSpectralClustering;
import org.openimaj.ml.clustering.spectral.GraphLaplacian;
import org.openimaj.ml.clustering.spectral.HardCodedEigenChooser;
import org.openimaj.ml.clustering.spectral.NormalisedSimilarityDoubleClustererWrapper;
import org.openimaj.ml.clustering.spectral.SpectralClusteringConf;
import org.openimaj.ml.clustering.spectral.SpectralIndexedClusters;
import org.openimaj.util.function.Function;
import org.openimaj.vis.general.BarVisualisationBasic;

import be.ac.ulg.montefiore.run.distributions.GaussianDistribution;
import ch.akuhn.matrix.DenseMatrix;
import ch.akuhn.matrix.Matrix;
import ch.akuhn.matrix.SparseMatrix;
import ch.akuhn.matrix.Vector;

/**
 * Visualise a number of gaussians as historgrams.
 * Control their means and variances
 * Visualise their top N eigen vectors
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class GaussianSpectralClusterVis {
	private static class CustomSimFunc extends DoubleFVSimilarityFunction<DoubleFV> {
		private CustomSimFunc(FeatureExtractor<DoubleFV, DoubleFV> extractor) {
			super(extractor);
		}

		@Override
		protected SparseMatrix similarity() {
			
			SparseMatrix sm= new SparseMatrix(this.feats.length, this.feats.length);
			double MAX = RANGE/20;
			for (int i = 0; i < feats.length; i++) {
				for (int j = i; j < feats.length; j++) {
					double d = Math.abs(feats[i][0] - feats[j][0]);
					if(d < MAX){
						sm.put(i, j, 1 - d/MAX);
						sm.put(j, i, 1 - d/MAX);
					}
				}
			}
			return sm ;
		}
	}

	private static final class DumbExtractor implements
			FeatureExtractor<DoubleFV, DoubleFV> {
		@Override
		public DoubleFV extractFeature(DoubleFV object) {
			return object;
		}
	}

	private static final int NGAUSSIANS = 5;
	private static final double RANGE = 100;
	private static final int BINS = 1000;
	private static final int POINTS_PER_DIST = 100;
	protected static final double VAR_MIN = 0.0001d;
	protected static final double VAR_MAX = 20;
	private static double diff;
	private static Histogram histogram;
	private static double var = 10d;
	private static BarVisualisationBasic vis;
	private static Logger logger = Logger.getLogger(GaussianRandomGenerator.class);
	private static ChangeListener sliderListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			double meanProp = (meanSlider.getValue() - meanSlider.getMinimum()) / (double)(meanSlider.getMaximum() - meanSlider.getMinimum());
			double varProp  = (varSlider.getValue() - varSlider.getMinimum()) / (double)(varSlider.getMaximum() - varSlider.getMinimum());
			
			var = VAR_MIN + (VAR_MAX - VAR_MIN) * varProp;
			diff = ((RANGE / (NGAUSSIANS + 1)) * 2) * meanProp;
			
			redrawDistributions();
			
		}
	};
	private static JSlider meanSlider;
	private static JSlider varSlider;
	private static SparseMatrix distanceMat;
	private static DoubleSpectralClustering dsc;
	private static List<BarVisualisationBasic> eigenvis;
	
	public static void main(String[] args) {
		LoggerUtils.prepareConsoleLogger();
		vis = new BarVisualisationBasic(800, 200);
		eigenvis = new ArrayList<BarVisualisationBasic>();
		JFrame win = vis.showWindow("Gaussians");
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		for (int i = 0; i < NGAUSSIANS+1; i++) {
			BarVisualisationBasic e = new BarVisualisationBasic(200, 50);
			eigenvis.add(e);
			JFrame w = e.showWindow("Eigenvector " + i);
			Rectangle b = w.getBounds();
			b.y = (win.getY() + win.getHeight() + i * w.getHeight());
			w.setBounds(b);
			
		}
		diff = RANGE / (NGAUSSIANS + 1);
		
		SpectralClusteringConf<double[]> conf = new SpectralClusteringConf<double[]>(
				new DoubleNNDBSCAN(0.2, 3, new DoubleNearestNeighboursExact.Factory()), 
				new GraphLaplacian.Normalised()
		);
//		conf.eigenChooser = new HardCodedEigenChooser(50);
		dsc = new DoubleSpectralClustering(conf);
		
		prepareControls();
		redrawDistributions();
		
	}

	private static void prepareControls() {
		JFrame controlFrame = new JFrame();
		controlFrame.setSize(200, 120);
		JPanel controlPanel = new JPanel();
		meanSlider = new JSlider(0, 100, 50);
		meanSlider.addChangeListener(sliderListener );
		varSlider = new JSlider(0, 100, 50);
		varSlider.addChangeListener(sliderListener );
		
		controlPanel.add(new JLabel("mean"));
		controlPanel.add(meanSlider);
		controlPanel.add(new JLabel("var"));
		controlPanel.add(varSlider);
		
		controlFrame.getContentPane().add(controlPanel );
		
		controlFrame.setVisible(true);
		controlFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private static void redrawDistributions() {
		histogram = regenDistributions();
		vis.setData(histogram.values);
		System.out.println(MatlibMatrixUtils.sparcity(distanceMat));
		SpectralIndexedClusters clust = dsc.cluster(distanceMat);
		Matrix eigenVectors = new DenseMatrix(clust.eigenVectors());
		
		int i = 0;
		int vsize = 0;
		for (Vector vect : eigenVectors.columns()) {
			vsize = vect.size();
			double[] v = new double[vsize];
			vect.storeOn(v, 0);
			eigenvis.get(i).setData(v);
			i++;
			if(i == eigenvis.size())break;
		}
		
		for (; i < eigenvis.size(); i++) {
			eigenvis.get(i).setData(new double[vsize]);
		}
		
		
	}

	private static Histogram regenDistributions() {
		ArrayList<GaussianDistribution> dist = new ArrayList<GaussianDistribution>();
		Histogram points = new Histogram(BINS);
		List<DoubleFV> fvs = new ArrayList<DoubleFV>();
		for (int i = 0; i < NGAUSSIANS; i++) {
			int mult = i - (NGAUSSIANS/2);
			double mean = (RANGE/2) + (mult * diff);
			logger .debug("Setting guassian at mean: " + mean);
			GaussianDistribution gd = new GaussianDistribution(mean, var );
			dist.add(gd);
			for (int j = 0; j < POINTS_PER_DIST; j++) {
				double propGen = gd.generate()/RANGE;
				double genBin = BINS * propGen;
				double genAbs = RANGE * propGen;
				fvs.add(new DoubleFV(new double[]{genAbs}));
				int index = (int) genBin;
				if(index >= 0 && index < BINS){
					points.values[index]++;
				}
			}
		}
		System.out.println("Created points: " + fvs.size());
		DoubleFVSimilarityFunction<DoubleFV> wrapper = new CustomSimFunc(new DumbExtractor());
		SparseMatrix before = wrapper .apply(fvs);
		distanceMat = before;
		return points;
	}

}
