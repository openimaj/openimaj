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
package org.openimaj.image.annotation.evalutation.dataset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openimaj.feature.DoubleFV;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.annotation.BatchAnnotator;
import org.openimaj.image.annotation.ImageFeatureAnnotationProvider;
import org.openimaj.image.annotation.xform.DenseLinearTransformAnnotator;
import org.openimaj.image.annotation.xform.UniformRandomAnnotator;
import org.openimaj.image.pixel.statistics.HistogramModel;

public class Corel5k {
	File baseDir = new File("/Users/jsh2/Data/corel-5k");
	File trainingIndex = new File(baseDir, "test_1_image_nums.txt");
	File imageDir = new File(baseDir, "images");
	File metaDir = new File(baseDir, "metadata");
	
	List<String> trainingImages = new ArrayList<String>();
	List<String> testImages = new ArrayList<String>();
	
	public Corel5k() {
		loadTest();
		loadTraining();
	}
	
	private void loadTest() {
		try {
			for (String s : FileUtils.readLines(trainingIndex)) {
				testImages.add(s);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void loadTraining() {
		for (File f : imageDir.listFiles()) {
			if (!testImages.contains(f) && f.getName().endsWith(".jpeg")) {
				trainingImages.add(f.getName().replace(".jpeg", ""));
			}
		}
	}
	
	public List<ImageFeatureAnnotationProvider<DoubleFV>> getTrainingData() {
		List<ImageFeatureAnnotationProvider<DoubleFV>> data = new ArrayList<ImageFeatureAnnotationProvider<DoubleFV>>();
		
		for (final String s : trainingImages) {
			data.add( new ImageFeatureAnnotationProvider<DoubleFV>() {

				@Override
				public MBFImage getImage() {
					try {
						return ImageUtilities.readMBF(new File(imageDir, s+".jpeg"));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public List<String> getAnnotations() {
					try {
						return FileUtils.readLines(new File(metaDir, s+"_1.txt"));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public DoubleFV getFeature() {
					HistogramModel hm = new HistogramModel(4,4,4);
					hm.estimateModel(getImage());
					return hm.getFeatureVector();
				}
				
			});
		}
		
		return data;
	}
	
	public List<ImageFeatureAnnotationProvider<DoubleFV>> getTestData() {
		List<ImageFeatureAnnotationProvider<DoubleFV>> data = new ArrayList<ImageFeatureAnnotationProvider<DoubleFV>>();
		
		for (final String s : testImages) {
			data.add( new ImageFeatureAnnotationProvider<DoubleFV>() {

				@Override
				public MBFImage getImage() {
					try {
						return ImageUtilities.readMBF(new File(imageDir, s+".jpeg"));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public List<String> getAnnotations() {
					try {
						return FileUtils.readLines(new File(metaDir, s+"_1.txt"));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public DoubleFV getFeature() {
					HistogramModel hm = new HistogramModel(4,4,4);
					hm.estimateModel(getImage());
					return hm.getFeatureVector();
				}
			});
		}		
		
		return data;
	}
	
	public static void main(String [] args) {
		Corel5k c5k = new Corel5k();
		
//		BatchAnnotator annotator = new UniformRandomAnnotator();
//		BatchAnnotator annotator = new IndependentPriorRandomAnnotator();
		BatchAnnotator<DoubleFV> annotator = new DenseLinearTransformAnnotator<DoubleFV>();
		annotator.train(c5k.getTrainingData());
		
		for (ImageFeatureAnnotationProvider p : c5k.getTestData()) {
			System.out.println("Expected : " + p.getAnnotations());
			System.out.println("Predicted: " + annotator.annotate(p));
			System.out.println();
		}
	}
}

