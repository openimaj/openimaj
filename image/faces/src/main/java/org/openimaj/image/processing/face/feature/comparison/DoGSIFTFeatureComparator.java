package org.openimaj.image.processing.face.feature.comparison;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.feature.local.matcher.BasicTwoWayMatcher;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.face.feature.DoGSIFTFeature;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.geometry.transforms.TransformedOneToOnePointModel;
import org.openimaj.math.model.fit.RobustModelFitting;
import org.openimaj.math.model.fit.SimpleModelFitting;
import org.openimaj.util.pair.Pair;

public class DoGSIFTFeatureComparator implements FacialFeatureComparator<DoGSIFTFeature> {
	
	@Override
	public void readBinary(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] binaryHeader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double compare(DoGSIFTFeature query, DoGSIFTFeature target) {
		Rectangle unit = new Rectangle(0,0,1,1);
		
		TransformedOneToOnePointModel model = new TransformedOneToOnePointModel(0, 
				TransformUtilities.makeTransform(query.getBounds(), unit), 
				TransformUtilities.makeTransform(target.getBounds(), unit));
		
		RobustModelFitting<Point2d, Point2d> fitting = new SimpleModelFitting<Point2d, Point2d>(model);
		BasicTwoWayMatcher<Keypoint> innerMatcher = new BasicTwoWayMatcher<Keypoint>();
		ConsistentLocalFeatureMatcher2d<Keypoint> matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(innerMatcher, fitting);
		
		matcher.setModelFeatures(target.getKeys());
		matcher.findMatches(query.getKeys());
		
		double score = 0;
		for (Pair<Keypoint> p : matcher.getMatches()) {
			double accum = 0;
			byte[] v1 = p.firstObject().ivec;
			byte[] v2 = p.secondObject().ivec;
			for (int i=0; i<v1.length; i++) {
				accum += ((double)v1[i] - (double)v2[i]) * ((double)v1[i] - (double)v2[i]);
			}
			score += Math.sqrt(accum);
		}
		
		return (score / matcher.getMatches().size()); 
	}

	@Override
	public boolean isAscending() {
		return true;
	}

	public static void main(String [] args) {
		
	}
}
