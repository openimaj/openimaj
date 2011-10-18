package org.openimaj.image.processing.face.feature.comparison;

import gov.sandia.cognition.learning.algorithm.bayes.VectorNaiveBayesCategorizer;
import gov.sandia.cognition.statistics.DataHistogram;
import gov.sandia.cognition.statistics.distribution.MapBasedDataHistogram;
import gov.sandia.cognition.statistics.distribution.UnivariateGaussian.PDF;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.feature.local.matcher.BasicTwoWayMatcher;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.face.feature.DoGSIFTFeature;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.geometry.transforms.TransformedOneToOnePointModel;
import org.openimaj.math.model.Model;
import org.openimaj.math.model.UnivariateGaussianNaiveBayesModel;
import org.openimaj.math.model.fit.RobustModelFitting;
import org.openimaj.math.model.fit.SimpleModelFitting;
import org.openimaj.math.util.distance.ModelDistanceCheck;
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

	/**
	 * Build a DistanceCheck for the spatial layout of matching
	 * normalised facial features. The parameters for the default
	 * model were learned using naive bayes on the spatial distances.
	 * The default model was trained on 4128 manually annotated match pairs. 
	 * @return the default DistanceCheck
	 */
	public static ModelDistanceCheck buildDefaultDistanceCheck() {
//		Class distributions:
		//		{false=Mean: 0.4461058073589149 Variance: 0.04829317710091845, true=Mean: 0.029852218270328083 Variance: 0.003255709240977441}
		//		Class priors:
		//		Histogram has 2 domain objects and 4128 total count:
		//		true: 3380 (0.8187984496124031)
		//		false: 748 (0.1812015503875969)

		DataHistogram<Boolean> priors = new MapBasedDataHistogram<Boolean>();
		priors.add(true, 3380);
		priors.add(false, 748);
		Map<Boolean, List<PDF>> conditionals = new HashMap<Boolean, List<PDF>>();
		conditionals.put(true, Arrays.asList(new PDF[] {new PDF(0.029852218270328083, 0.003255709240977441)}));
		conditionals.put(false, Arrays.asList(new PDF[] {new PDF(0.4461058073589149, 0.04829317710091845)}));

		VectorNaiveBayesCategorizer<Boolean, PDF> bayes = new VectorNaiveBayesCategorizer<Boolean, PDF>(priors, conditionals); 
		Model<Double, Boolean> distanceModel = new UnivariateGaussianNaiveBayesModel<Boolean>(bayes);
		ModelDistanceCheck dc = new ModelDistanceCheck(distanceModel);

		return dc;
	}
	
	@Override
	public double compare(DoGSIFTFeature query, DoGSIFTFeature target) {
		Rectangle unit = new Rectangle(0,0,1,1);

		
		TransformedOneToOnePointModel model = new TransformedOneToOnePointModel(buildDefaultDistanceCheck(), 
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
