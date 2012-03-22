//package org.openimaj.image.annotation.evalutation.dataset;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//
//import org.apache.commons.io.FileUtils;
//import org.openimaj.experiment.dataset.ListDataset;
//import org.openimaj.experiment.dataset.split.TrainTestSplitter;
//
//public class StandardCorel5kSplit implements TrainTestSplitter<Corel5kDataset, ListDataset<CorelAnnotatedImage>> {
//	List<String> testIds;
//	ListDataset<CorelAnnotatedImage> training;
//	ListDataset<CorelAnnotatedImage> test;
//	
//	public StandardCorel5kSplit() throws IOException {
//		testIds = FileUtils.readLines(new File("/Users/jsh2/Data/corel-5k/test_1_image_nums.txt"));
//	}
//	
//	@Override
//	public void split(Corel5kDataset dataset) {
//		training = new ListDataset<CorelAnnotatedImage>();
//		test = new ListDataset<CorelAnnotatedImage>();
//		
//		for (CorelAnnotatedImage img : dataset) {
//			if (testIds.contains(img.getId())) {
//				test.addItem(img);
//			} else {
//				training.addItem(img);
//			}
//		}
//	}
//
//	@Override
//	public ListDataset<CorelAnnotatedImage> getTrainingDataset() {
//		return training;
//	}
//
//	@Override
//	public ListDataset<CorelAnnotatedImage> getTestDataset() {
//		return test;
//	}
//}
