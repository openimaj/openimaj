package org.openimaj.ml.clustering.kdtree;


import org.apache.commons.math.stat.descriptive.rank.Max;
import org.openimaj.data.DataSource;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.array.IntArrayView;
import org.openimaj.util.pair.IntDoublePair;
import org.openimaj.util.tree.DoubleKDTree;
import org.openimaj.util.tree.DoubleKDTree.SplitChooser;

import Jama.Matrix;

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
		
		
		Matrix firstVariances = null;
		Max max = new Max();
		
		public CappedVarianceSplitChooser() {
		}


		@Override
		public IntDoublePair chooseSplit(double[][] pnts, IntArrayView inds, int depth, double[] minBounds, double[] maxBounds) {
			 
			if(inds.size() < minpts) return null;
			
			double[][] subarr = new double[inds.size()][ndims];
			for (int i = 0; i < subarr.length; i++) {
				double[] pnti = pnts[inds.get(i)];
				for (int dim = startindex; dim < startindex + ndims; dim++) {					
					subarr[i][dim-startindex] = pnti[dim];
				}
			}
			
			Matrix mat = new Matrix(subarr);
			Matrix mean = MatrixUtils.sumCols(mat).times(1./inds.size());
			Matrix var = MatrixUtils.sumCols(mat.arrayTimes(mat)).times(1./inds.size()).minus(mean.arrayTimes(mean));
			
			if(firstVariances == null){
				firstVariances = var;
				double[] variances = var.getArray()[0];
				if(max.evaluate(variances) == 0){
					return null; // special case, if the first variance is null we've been handed ALL the same, screw it
				}
			}
			
			Matrix propchange = var.arrayRightDivide(firstVariances);
			if(max.evaluate(propchange.getArray()[0]) < varprop){
				return null;
			}
			else{
				IntDoublePair maxDim = maxDim(MatrixUtils.abs(var).getArray()[0]);
				double[] col = mat.getMatrix(0, inds.size()-1, maxDim.first, maxDim.first).transpose().getArray()[0];
				double mid = ArrayUtils.quickSelect(col, col.length/2);
				if(ArrayUtils.minValue(col) == mid) mid += Double.MIN_NORMAL;
				if(ArrayUtils.maxValue(col) == mid) mid -= Double.MIN_NORMAL;
				return IntDoublePair.pair(maxDim.first+startindex,mid);
			}
		}


		private IntDoublePair maxDim(double[] ds) {
			double maxv = -Double.MAX_VALUE;
			int maxi = -1;
			for (int i = 0; i < ds.length; i++) {
				if(maxv < ds[i]){
					maxi = i;
					maxv = ds[i];
				}
			}
			return IntDoublePair.pair(maxi, maxv);
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
