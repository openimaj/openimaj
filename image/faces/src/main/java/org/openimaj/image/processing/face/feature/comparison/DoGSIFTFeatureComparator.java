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

/**
 * A {@link FacialFeatureComparator} for comparing {@link DoGSIFTFeature}s.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class DoGSIFTFeatureComparator implements FacialFeatureComparator<DoGSIFTFeature> {
	@Override
	public void readBinary(DataInput in) throws IOException {
		// Do nothing
	}

	@Override
	public byte[] binaryHeader() {
		// Do nothing
		return null;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		// Do nothing
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
				double v1i = ((double)v1[i]);
				double v2i = ((double)v2[i]);
				accum += (v1i - v2i) * (v1i - v2i);
			}
			score += Math.sqrt(accum);
		}

		if (matcher.getMatches().size() == 0) return Math.sqrt(255*255*128);
		
		return (score / matcher.getMatches().size()); 
	}

	@Override
	public boolean isAscending() {
		return true;
	}
}
