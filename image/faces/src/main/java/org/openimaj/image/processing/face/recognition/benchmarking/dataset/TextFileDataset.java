package org.openimaj.image.processing.face.recognition.benchmarking.dataset;

import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;
import org.openimaj.image.FImage;

public class TextFileDataset extends MapBackedDataset<Integer, ListDataset<FImage>, FImage> {
	private String separator = ",";
	
	
}
