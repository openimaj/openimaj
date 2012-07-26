package org.openimaj.image.processing.face.recognition.benchmarking.dataset;

import java.io.File;
import java.io.IOException;

import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.DetectedFace;

public class ATandTDataset extends MapBackedDataset<Integer, ListDataset<DetectedFace>, DetectedFace> {
	public ATandTDataset() throws IOException {
		this(new File(System.getProperty("user.home"), "Data/att_faces"));
	}
	
	public ATandTDataset(File baseDir) throws IOException {
		super();
		
		for (int s=1; s<=40; s++) {
			ListBackedDataset<DetectedFace> list = new ListBackedDataset<DetectedFace>();
			map.put(s, list);

			for (int i=1; i<=10; i++) {
				File file = new File(baseDir, "s" + s + "/" + i + ".pgm");

				FImage image = ImageUtilities.readF(file);

				list.add(new DetectedFace(null, image));
			}
		}
	}
}
