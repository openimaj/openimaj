package org.openimaj.feature.local.matcher;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.knn.approximate.ByteNearestNeighboursKDTree;
import org.openimaj.util.pair.Pair;

public class MultipleMatchesMatcher<T extends Keypoint> implements LocalFeatureMatcher<T> {

	private int count;
	protected List <Pair<T>> matches;
	private ByteNearestNeighboursKDTree modelKeypointsKNN;
	private double thresh;
	private List<T> modelKeypoints;
	
	public MultipleMatchesMatcher(int count,double thresh){
		this.count = count;
		if(this.count < 2) this.count = 2;
		this.thresh = thresh;
		matches = new ArrayList<Pair<T>>();
	}
	
	@Override
	public void setModelFeatures(List<T> modelkeys) {
		this.modelKeypoints = modelkeys;
		byte [][] data = new byte[modelkeys.size()][];
		for (int i=0; i<modelkeys.size(); i++)
			data[i] = modelkeys.get(i).ivec;
		
		modelKeypointsKNN = new ByteNearestNeighboursKDTree(data, 1, 100);
	}

	@Override
	public boolean findMatches(List<T> keys1) {
		byte [][] data = new byte[keys1.size()][];
		for (int i=0; i<keys1.size(); i++)
			data[i] = keys1.get(i).ivec;
		
		int [][] argmins = new int[keys1.size()][this.count];
		int [][] mins = new int[keys1.size()][this.count];
		modelKeypointsKNN.searchKNN(data, this.count, argmins, mins);
		double threshProp = (1.0 + thresh) * (1.0 + thresh) ;
		System.out.println("The count for multiple matches is: " + this.count);
		for (int i=0; i<keys1.size(); i++) {
			// Get the first distance

			boolean matchesMultiple = true;
			if(mins[i].length > 0 && mins[i].length >= this.count){
				double distsq1 = mins[i][0];
				for (int j = 1; j < this.count; j++) {
					double distsq2 = mins[i][j];
					if (distsq2 > distsq1 * threshProp) {
						// Then there is a mismatch within the first this.count, break
						matchesMultiple = false;
						break;
				    }
				}
			}
			else{
				matchesMultiple = false;
			}
			if(matchesMultiple){
				// Add each of the pairs that match
				for (int j = 0; j < this.count; j++) {
					matches.add(new Pair<T>(keys1.get(i), modelKeypoints.get(argmins[i][j])));
				}
			}
		}
		
		return true;
	}

	@Override
	public List<Pair<T>> getMatches() {
		return this.matches;
	}

}
