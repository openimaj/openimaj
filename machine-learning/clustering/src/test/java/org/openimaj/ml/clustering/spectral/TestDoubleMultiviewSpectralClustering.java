package org.openimaj.ml.clustering.spectral;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import gnu.trove.list.array.TIntArrayList;
import gov.sandia.cognition.io.CSVUtility;
import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.mtj.DenseMatrix;
import gov.sandia.cognition.math.matrix.mtj.DenseMatrixFactoryMTJ;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.knn.DoubleNearestNeighbours;
import org.openimaj.knn.DoubleNearestNeighboursExact;
import org.openimaj.logger.LoggerUtils;
import org.openimaj.math.matrix.CFMatrixUtils;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.ml.clustering.dbscan.DBSCANConfiguration;
import org.openimaj.ml.clustering.dbscan.DoubleDBSCAN;
import org.openimaj.ml.clustering.dbscan.DoubleDBSCANClusters;
import org.openimaj.util.array.ArrayUtils;

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
			double diff = CFMatrixUtils.max(m.getColumn(c)) - CFMatrixUtils.min(m.getColumn(c));
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

	@Test
	public void test() {
		DBSCANConfiguration<DoubleNearestNeighbours, double[]> dbsConf = new DBSCANConfiguration<DoubleNearestNeighbours, double[]>(
				epss, 2,
				new DoubleNearestNeighboursExact.Factory(DoubleFVComparison.EUCLIDEAN)
		);
		SpatialClusterer<DoubleDBSCANClusters,double[]> inner = new DoubleDBSCAN(dbsConf);
		MultiviewSpectralClusteringConf<double[]> conf = new MultiviewSpectralClusteringConf<double[]>(
			0.1,inner
		);
		conf.eigenChooser = new AutoSelectingEigenChooser(100, 1.0);
		conf.stop = new StoppingCondition.HardCoded(10);
		DoubleSpectralClustering clust = new DoubleSpectralClustering(conf);
		DoubleMultiviewSpectralClustering multi = new DoubleMultiviewSpectralClustering(conf);
		
		Clusters clusters1 = clust.cluster(allsim, false);
//		Clusters clusters2 = multi.cluster(splitsim, false);
		
		System.out.println(clusters1);
//		System.out.println(clusters2);
	}
	
	

}
