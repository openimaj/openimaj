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


import org.apache.commons.math.stat.descriptive.rank.Max;
import org.openimaj.data.DataSource;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.ml.clustering.SpatialClusterer;
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
	private SplitDetectionMode detectionMode = new SplitDetectionMode.VARIABLE_MEDIAN();
//	private SplitDetectionMode detectionMode = new SplitDetectionMode.MEAN();
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
				double mid = detectionMode.detect(col);
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
	 * calls: {@link #DoubleKDTreeClusterer()} with 0.01
	 */
	public DoubleKDTreeClusterer() {
		
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
	 * @param detectionMode The {@link SplitDetectionMode} given the feature of highest variance
	 * @param varprop The minimum proportional variance (compared to the first variance)
	 * @param startindex the feature start index
	 * @param ndims the number of features to use
	 * 
	 */
	public DoubleKDTreeClusterer(SplitDetectionMode detectionMode, double varprop, int startindex, int ndims) {
		this.detectionMode = detectionMode;
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
		double[][] arrdata = new double[data.size()][];
		for (int i = 0; i < arrdata.length; i++) {
			arrdata[i] = data.getData(i);
		}
		return cluster(arrdata);
	}

}
