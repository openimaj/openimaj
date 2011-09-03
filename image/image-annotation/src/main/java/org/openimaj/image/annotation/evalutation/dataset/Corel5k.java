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
		
		//BatchAnnotator annotator = new UniformRandomAnnotator();
		//BatchAnnotator annotator = new IndependentPriorRandomAnnotator();
		BatchAnnotator<DoubleFV> annotator = new DenseLinearTransformAnnotator<DoubleFV>();
		annotator.train(c5k.getTrainingData());
		
		for (ImageFeatureAnnotationProvider p : c5k.getTestData()) {
			System.out.println("Expected : " + p.getAnnotations());
			System.out.println("Predicted: " + annotator.annotate(p));
			System.out.println();
		}
	}
}

