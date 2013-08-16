package org.openimaj.ml.clustering.kdtree;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.apache.commons.math.stat.descriptive.rank.Max;
import org.openimaj.data.DataSource;
import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.array.IntArrayView;
import org.openimaj.util.pair.IntDoublePair;
import org.openimaj.util.tree.DoubleKDTree;
import org.openimaj.util.tree.DoubleKDTree.SplitChooser;

import scala.actors.threadpool.Arrays;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class DoubleKDTreeClusterer implements SpatialClusterer<KDTreeClusters, double[]>{
	private static final int DEFAULT_MINPTS = 4;
	private double DEFAULT_VARIANCE_PROP = 0.1;
	
	double varprop = DEFAULT_VARIANCE_PROP ;
	int minpts = DEFAULT_MINPTS;
	int ndims = -1;
	private int startindex = -1;
	
	class CappedVarianceSplitChooser implements SplitChooser{
		
		
		double[] firstVariances = null;
		double[] varChange = null;
		Variance variance = new Variance();
		Mean mean = new Mean();
		Max max = new Max();
		
		public CappedVarianceSplitChooser() {
			firstVariances = new double[ndims];
			varChange = new double[ndims];
			Arrays.fill(firstVariances, -1);
			Arrays.fill(varChange, 1);
		}


		@Override
		public IntDoublePair chooseSplit(double[][] pnts, IntArrayView inds, int depth, double[] minBounds, double[] maxBounds) {
			int unnormdim = depth % ndims;
			int dim = startindex + unnormdim; 
			if(inds.size() < minpts) return null;
			
			double[] subarr = new double[inds.size()];
			for (int i = 0; i < subarr.length; i++) {
				subarr[i] = pnts[inds.get(i)][dim];
			}
			
			double var = variance.evaluate(subarr);
			if(firstVariances[unnormdim] == -1){
				firstVariances[unnormdim] = var;
			}
			varChange[unnormdim] = var / firstVariances[unnormdim];
			
			
			if(max.evaluate(varChange) < varprop){
				return null;
			}
			double mid = ArrayUtils.quickSelect(subarr, subarr.length/2);
			
			return IntDoublePair.pair(dim, mid);
		}
		
	}
	/**
	 * @param varprop the proportion of variance change from the root variance before splitting stops
	 * @param startindex the index to start from
	 * @param ndims the number of dimensions to split on
	 */
	public DoubleKDTreeClusterer(double varprop, int startindex, int ndims) {
		this.varprop = varprop;
		this.startindex = startindex;
		this.ndims = ndims;
	}
	
	/**
	 * @param varprop the proportion of variance change from the root variance before splitting stops
	 */
	public DoubleKDTreeClusterer(double varprop) {
		this.varprop = varprop;
	}

	@Override
	public int[][] performClustering(double[][] data) {
		return cluster(data).clusters();
	}

	@Override
	public KDTreeClusters cluster(double[][] data) {
		if(ndims == -1)
		{
			this.startindex = 0;
			ndims = data[0].length;
		}
		
		DoubleKDTree tree = new DoubleKDTree(data, new CappedVarianceSplitChooser());
		return new KDTreeClusters(tree, ndims);
	}

	@Override
	public KDTreeClusters cluster(DataSource<double[]> data) {
		double[][] arrdata = new double[data.numRows()][];
		for (int i = 0; i < arrdata.length; i++) {
			arrdata[i] = data.getData(i);
		}
		return cluster(arrdata);
	}

}
