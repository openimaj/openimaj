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
package org.openimaj.ml.clustering.kdtree;

import static org.junit.Assert.*;
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
import org.openimaj.math.matrix.DiagonalMatrix;
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
	private final class ExtractWD extends GraphLaplacian {
		DiagonalMatrix D;
		SparseMatrix W;

		@Override
		public SparseMatrix laplacian(SparseMatrix adj,DiagonalMatrix degree) {
			W = adj;
			D = degree;
			return null;
		}
	}

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
//		ClusterTestDataLoader loader = new ClusterTestDataLoader(2,false);
		ClusterTestDataLoader loader = new ClusterTestDataLoader();
		loader.prepare(data);
		this.testStats = loader.getTestStats();
		this.testData = loader.getTestData();
		this.testClusters = loader.getTestClusters();
	}
	
	public static void main(String[] args) throws IOException {
		TestDoubleKDTreeClusterer tc = new TestDoubleKDTreeClusterer();
		tc.loadTest();
		tc.doTest();
	}
	
	@Test
	public void testName() throws Exception {
		
	}
	/**
	 *
	 */
	public void doTest(){
		SparseMatrix simmat = normalisedSimilarity();
		MLArray arr = MatlibMatrixUtils.asMatlab(simmat);
		ArrayList<MLArray> tosave = new ArrayList<MLArray>();
		tosave.add(arr);
		try {
			MatFileWriter writer = new MatFileWriter(new File("/home/ss/Experiments/python/sim.mat"),tosave);
		} catch (IOException e) {
			System.err.println("Failed outputing python");
		}
		drawdata();
		int neigSelect = 5;
		
		ExtractWD gl = new ExtractWD();
		gl.laplacian(simmat);
//		SplitDetectionMode splitmode = new SplitDetectionMode.OPTIMISED(gl.D,gl.W);
		SplitDetectionMode splitmode = new SplitDetectionMode.MEDIAN();
		DoubleKDTreeClusterer spatial = new DoubleKDTreeClusterer(splitmode,0.01,1,4);
		
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
