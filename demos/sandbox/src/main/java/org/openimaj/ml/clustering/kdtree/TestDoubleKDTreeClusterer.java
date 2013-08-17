package org.openimaj.ml.clustering.kdtree;

import static org.junit.Assert.assertTrue;
import gnu.trove.list.array.TIntArrayList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.io.FileUtils;
import org.openimaj.math.matrix.JamaDenseMatrix;
import org.openimaj.math.matrix.MatlibMatrixUtils;
import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.ml.clustering.kdtree.ClusterTestDataLoader.TestStats;
import org.openimaj.ml.clustering.spectral.DoubleSpectralClustering;
import org.openimaj.ml.clustering.spectral.GraphLaplacian;
import org.openimaj.ml.clustering.spectral.HardCodedEigenChooser;
import org.openimaj.ml.clustering.spectral.SpectralClusteringConf;
import org.openimaj.ml.clustering.spectral.SpectralIndexedClusters;
import org.openimaj.vis.general.BarVisualisationBasic;
import org.openimaj.vis.general.DotPlotVisualisation;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;

import ch.akuhn.matrix.SparseMatrix;
import ch.akuhn.matrix.Vector;
import ch.akuhn.matrix.eigenvalues.FewEigenvalues;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class TestDoubleKDTreeClusterer {
	private static final int EIGHEIGHT = 100;
	private static final int EIGWIDTH = 600;
	private double[][] testData;
	private int[][] testClusters;
	private TestStats testStats;
	/**
	 * @throws IOException
	 */
	@Before
	public void loadTest() throws IOException{
		String[] data = FileUtils.readlines(TestDoubleKDTreeClusterer.class.getResourceAsStream("/org/openimaj/ml/clustering/dbscan/dbscandata"));
		ClusterTestDataLoader loader = new ClusterTestDataLoader();
		this.testStats = loader.readTestStats(data);
		this.testData = loader.readTestData(data);
		this.testClusters = loader.readTestClusters(data);
	}
	
	public static void main(String[] args) throws IOException {
		TestDoubleKDTreeClusterer tc = new TestDoubleKDTreeClusterer();
		tc.loadTest();
		tc.doTest();
	}

	/**
	 *
	 */
	@Test
	public void doTest(){
		SparseMatrix simmat = normalisedSimilarity();
		MLArray arr = MatlibMatrixUtils.asMatlab(simmat);
		ArrayList<MLArray> tosave = new ArrayList<MLArray>();
		tosave.add(arr);
		try {
			MatFileWriter writer = new MatFileWriter(new File("/home/ss/Experiments/python/sim.mat"),tosave);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		drawdata();
		int neigSelect = 5;
		DoubleKDTreeClusterer spatial = new DoubleKDTreeClusterer(0.01,1,1);
		
		SpectralClusteringConf<double[]> conf = new SpectralClusteringConf<double[]>(spatial);
		conf.laplacian = new GraphLaplacian.Normalised();
		FewEigenvalues fev = FewEigenvalues.of(conf.laplacian.laplacian(simmat)).smallest(simmat.rowCount());
		fev.run();
		BarVisualisationBasic bvbval = new BarVisualisationBasic(EIGWIDTH, EIGHEIGHT,fev.value);
		JFrame evalw = bvbval.showWindow("");
		conf.eigenChooser = new HardCodedEigenChooser(neigSelect);
		DoubleSpectralClustering clust = new DoubleSpectralClustering(conf);
		
		SpectralIndexedClusters res = clust.cluster(simmat);
		evalw.setLocation(EIGWIDTH, 0);
		for (int i = 0; i < neigSelect ; i++) {			
			Vector eig = new JamaDenseMatrix(res.eigenVectors()).column(i);
			double[] evec = new double[eig.size()];
			eig.storeOn(evec, 0);
			BarVisualisationBasic bvb = new BarVisualisationBasic(EIGWIDTH, EIGHEIGHT,evec);
			JFrame w = bvb.showWindow("eig");
			w.setLocation(w.getLocation().x, 100 * i);
		}
		System.out.println(new IndexClusters(this.testClusters));
		System.out.println(res);
		
//		confirmClusters(res);
	}


	private void drawdata() {
		DotPlotVisualisation vis = new DotPlotVisualisation(EIGWIDTH, EIGWIDTH);
		vis.getAxesRenderer().setMaxXValue(50);
		vis.getAxesRenderer().setMaxYValue(50);
		vis.setAutoScaleAxes(false);
		for (double[] d: this.testData) {
//			System.out.printf("%2.2f, %2.2f\n",d[0],d[1]);
			vis.addPoint(d[0], d[1], 0.1);
		}
		JFrame w = vis.showWindow("cheese");
		w.setLocation(EIGWIDTH, EIGHEIGHT);
	}

	private IndexClusters skipOutliers(IndexClusters iclusters) {
		int[][] clusters = iclusters.clusters();
		int[][] fixedClusters = new int[clusters.length][];
		
		for (int i = 0; i < fixedClusters.length; i++) {
			TIntArrayList holder = new TIntArrayList();
			for (int j = 0; j < clusters[i].length; j++) {
				if(clusters[i][j] >= this.testStats.noutliers){
					holder.add(clusters[i][j]);
				}
			}
			fixedClusters[i] = holder.toArray();
		}
		
		IndexClusters withoutOutliers = new IndexClusters(fixedClusters);
		return withoutOutliers;
	}
	
	private SparseMatrix normalisedSimilarity() {
		return normalisedSimilarity(this.testStats.eps);
	}
	private SparseMatrix normalisedSimilarity(double eps) {
		final SparseMatrix mat = new SparseMatrix(testData.length,testData.length);
		final DoubleFVComparison dist = DoubleFVComparison.EUCLIDEAN;
		double maxD = 0;
		for (int i = 0; i < testData.length; i++) {
			for (int j = i; j < testData.length; j++) {
				double d = dist.compare(testData[i], testData[j]);
				if(d>eps) d = Double.NaN;
				else{
					maxD = Math.max(d, maxD);
				}
				mat.put(i, j, d);
				mat.put(j, i, d);
			}
		}
		SparseMatrix mat_norm = new SparseMatrix(testData.length,testData.length);
		for (int i = 0; i < testData.length; i++) {
			for (int j = i+1; j < testData.length; j++) {
				double d = mat.get(i, j);
				if(Double.isNaN(d)){
					continue;
				}
				else{
					d/=maxD;
				}
				mat_norm.put(i, j, 1-d);
				mat_norm.put(j, i, 1-d);
			}
		}
		return mat_norm;
	}

	private void confirmClusters(IndexClusters res) {
		for (int i = 0; i < this.testClusters.length; i++) {
			assertTrue(toSet(this.testClusters[i]).equals(toSet(res.clusters()[i])));
		}
	}
	
	private Set<Integer> toSet(int[] is) {
		Set<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < is.length; i++) {
			set.add(is[i]);
		}
		return set;
	}
}
