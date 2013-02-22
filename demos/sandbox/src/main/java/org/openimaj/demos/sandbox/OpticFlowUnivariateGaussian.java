package org.openimaj.demos.sandbox;
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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FeatureVector;
import org.openimaj.io.FileUtils;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.annotation.bayes.NaiveBayesAnnotator;




public class OpticFlowUnivariateGaussian {
	public static void main(String[] args) throws IOException {
		FeatureExtractor<? extends FeatureVector, Double> extractor = new FeatureExtractor<FeatureVector, Double>() {

			@Override
			public FeatureVector extractFeature(Double object) {
				return new DoubleFV(new double[]{object});
			}
		};
		NaiveBayesAnnotator<Double, Direction, FeatureExtractor<? extends FeatureVector,Double>> ann
			= new NaiveBayesAnnotator<Double, Direction, FeatureExtractor<? extends FeatureVector,Double>>(extractor, NaiveBayesAnnotator.Mode.ALL);
		String[] lines = FileUtils.readlines(OpticFlowUnivariateGaussian.class.getResourceAsStream("directions"));
		for (String line : lines) {
			String[] scoreDir = line.split(",");
			double score = Double.parseDouble(scoreDir[0]);
			Direction dir = Direction.valueOf(scoreDir[1]);
			ann.train(new DirectionScore(score, dir));
		}
		IOUtils.write(ann, new DataOutputStream(new FileOutputStream("/Users/ss/.rhino/opticflowann")));
		ann = IOUtils.read(new DataInputStream(new FileInputStream("/Users/ss/.rhino/opticflowann")));
	}
}
