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

import gnu.trove.list.array.TIntArrayList;
import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.mtj.DenseMatrix;
import gov.sandia.cognition.math.matrix.mtj.DenseMatrixFactoryMTJ;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.knn.DoubleNearestNeighboursExact;
import org.openimaj.logger.LoggerUtils;
import org.openimaj.math.matrix.CFMatrixUtils;
import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.ml.clustering.dbscan.DoubleDBSCANClusters;
import org.openimaj.ml.clustering.dbscan.DoubleNNDBSCAN;

import ch.akuhn.matrix.SparseMatrix;

/**
 * Test multiview spectral clustering using the wine dataset
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestDoubleMultiviewSpectralClustering {
	
	private TIntArrayList correct;
	private ArrayList<double[]> alldata;
	private HashMap<Integer, List<double[]>> splitdata;
	private SparseMatrix allsim;
	private double epsd = 10;
	private double epss = 0.04;
	private ArrayList<SparseMatrix> splitsim;

	/**
	 * @throws IOException
	 */
	@Before
	public void begin() throws IOException{
		LoggerUtils.prepareConsoleLogger();
		
		BufferedReader br = new BufferedReader(
				new InputStreamReader(
						TestDoubleMultiviewSpectralClustering.class.getResourceAsStream("wine.data")));
		String line = null;
		this.correct = new TIntArrayList();
		this.alldata = new ArrayList<double[]>();
		this.splitdata= new HashMap<Integer,List<double[]>>();
		while((line = br.readLine())!=null){
			String[] parts = line.split(",");
			int cluster = Integer.parseInt(parts[0].trim());
			correct.add(cluster);
			double[] data = new double[parts.length-1];
			for (int i = 0; i < data.length; i++) {
				data[i] = Double.parseDouble(parts[i+1]);
			}
			List<double[]> list = splitdata.get(cluster);
			if(list == null) splitdata.put(cluster, list = new ArrayList<double[]>());
			
			alldata.add(data);
			list.add(data);
		}
		
		prepareSimilarityMatrix();
	}

	private void prepareSimilarityMatrix() {
		double[][] dataDouble = normalise(asDouble(alldata));
		
		
		this.allsim = normalisedSimilarity(dataDouble,epsd);
		this.splitsim = new ArrayList<SparseMatrix>();
		this.splitsim.add(normalisedSimilarity(cols(dataDouble,0,6),epsd));
		this.splitsim.add(normalisedSimilarity(cols(dataDouble,6,dataDouble[0].length),epsd));
	}

	private double[][] normalise(double[][] asDouble) {
		DenseMatrix m = DenseMatrixFactoryMTJ.INSTANCE.copyArray(asDouble);
		Matrix times = m.dotTimes(m);
		DenseMatrix mean = DenseMatrixFactoryMTJ.INSTANCE.copyRowVectors(times.sumOfRows());
		CFMatrixUtils.powInplace(CFMatrixUtils.timesInplace(mean,1./asDouble.length),0.5);
		for (int c = 0; c < m.getNumColumns(); c++) {
//			double diff = CFMatrixUtils.max(m.getColumn(c)) - CFMatrixUtils.min(m.getColumn(c));
			double cmean = mean.getElement(0, c);
			for (int r = 0; r < m.getNumRows(); r++) {
				asDouble[r][c] = (m.getElement(r, c) - cmean);
			}
		}
		return asDouble;
	}

	private double[][] cols(double[][] dataDouble, int a, int b) {
		double[][] ret = new double[dataDouble.length][b-a];
		for (int i = 0; i < ret.length; i++) {
			for (int j = 0; j < ret[0].length; j++) {
				ret[i][j] = dataDouble[i][j+a];
			}
		}
		return ret;
	}

	private double[][] asDouble(List<double[]> alldata) {
		return alldata.toArray(new double[alldata.size()][]);
	}
	
	private SparseMatrix normalisedSimilarity(double[][] testData, double eps) {
		final SparseMatrix mat = new SparseMatrix(testData.length,testData.length);
		final DoubleFVComparison dist = DoubleFVComparison.EUCLIDEAN;
		double maxD = 0;
		for (int i = 0; i < testData.length; i++) {
			for (int j = i; j < testData.length; j++) {
				double d = dist.compare(testData[i], testData[j]);
				if(d>eps ) d = Double.NaN;
				else{
					maxD = Math.max(d, maxD);
				}
				mat.put(i, j, d);
				mat.put(j, i, d);
			}
		}
		SparseMatrix mat_norm = new SparseMatrix(testData.length,testData.length);
		for (int i = 0; i < testData.length; i++) {
			for (int j = i; j < testData.length; j++) {
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

	/**
	 * 
	 */
	@Test
	public void test() {
		SpatialClusterer<DoubleDBSCANClusters,double[]> inner = new DoubleNNDBSCAN(
				epss, 2,
				new DoubleNearestNeighboursExact.Factory(DoubleFVComparison.EUCLIDEAN));
		MultiviewSpectralClusteringConf<double[]> conf = new MultiviewSpectralClusteringConf<double[]>(
			0.1,inner
		);
		conf.eigenChooser = new ChangeDetectingEigenChooser(100, 1.0);
		conf.stop = new StoppingCondition.HardCoded(10);
		DoubleSpectralClustering clust = new DoubleSpectralClustering(conf);
//		DoubleMultiviewSpectralClustering multi = new DoubleMultiviewSpectralClustering(conf);
		
		IndexClusters clusters1 = clust.cluster(allsim);
//		Clusters clusters2 = multi.cluster(splitsim, false);
		
		System.out.println(clusters1);
//		System.out.println(clusters2);
	}
	
	

}
