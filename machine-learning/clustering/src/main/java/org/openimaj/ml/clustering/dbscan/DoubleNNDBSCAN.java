package org.openimaj.ml.clustering.dbscan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.data.DataSource;
import org.openimaj.knn.DoubleNearestNeighbours;
import org.openimaj.knn.DoubleNearestNeighboursExact;
import org.openimaj.knn.NearestNeighboursFactory;
import org.openimaj.ml.clustering.DataClusterer;
import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.ml.clustering.dbscan.neighbourhood.RegionMode;
import org.openimaj.util.pair.IntDoublePair;

/**
 * Implementation of DBSCAN (http://en.wikipedia.org/wiki/DBSCAN) using
 * a
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DoubleNNDBSCAN extends DBSCAN implements SpatialClusterer<DoubleDBSCANClusters, double[]>, DataClusterer<double[][], DoubleDBSCANClusters>{

	private NearestNeighboursFactory<? extends DoubleNearestNeighbours, double[]> nnf;
	private double eps;
	private int minPts;
	private boolean noiseAsClusters = false;

	/**
	 * Perform a DBScane with this configuration
	 * @param eps 
	 * @param minPts 
	 * @param nnf 
	 */
	public DoubleNNDBSCAN(double eps, int minPts, NearestNeighboursFactory<? extends DoubleNearestNeighbours, double[]> nnf) {
		this.eps = eps;
		this.nnf = nnf;
		this.minPts = minPts;
	}
	/**
	 * @param eps
	 * @param minPts
	 */
	public DoubleNNDBSCAN(double eps, int minPts) {
		this(eps,minPts,new DoubleNearestNeighboursExact.Factory());
	}
	class NNRegionMode implements RegionMode<IntDoublePair>{
		double[][] data;
		DoubleNearestNeighbours nn;
		public NNRegionMode(double[][] data) {
			this.data = data;
			this.nn = nnf.create(data);
		}
		@Override
		public List<IntDoublePair> regionQuery(int index) {
			List<IntDoublePair> res = nn.searchKNN(data[index], data.length);
			List<IntDoublePair> ret = new ArrayList<IntDoublePair>();
			for (IntDoublePair intFloatPair : res) {
				if(intFloatPair.second<eps)ret.add(intFloatPair);
				else break;
			}
			return ret;
		}
		
		@Override
		public boolean validRegion(List<IntDoublePair> region) {
			return region.size() >= minPts;
		}

	}

	
	
	@Override
	public DoubleDBSCANClusters cluster(double[][] data) {
		State state = new State(data.length,new NNRegionMode(data),this.noiseAsClusters);
		return dbscan(state);
	}

	@Override
	public DoubleDBSCANClusters cluster(DataSource<double[]> data) {
		double[][] allData = new double[data.numRows()][];
		Iterator<double[]> iterator = data.iterator();
		for (int i = 0; i < allData.length; i++) {
			allData[i] = iterator.next();
		}
		return this.cluster(allData);
	}

	@Override
	public int[][] performClustering(double[][] data) {
		return cluster(data).clusters();
	}

	/**
	 * @return the epse parameter
	 */
	public double getEps() {
		return this.eps;
	}
	
	@Override
	public String toString() {
		return String.format("%s: eps=%2.2f, minpts=%d, NN=%s",this.getClass().getSimpleName(),eps,minPts,this.nnf.getClass().getSimpleName());
	}
	
	/**
	 * Treat noise as clusters on their own
	 * @param b
	 */
	public void setNoiseAsClusters(boolean b) {
		this.noiseAsClusters  = b;
	}

}