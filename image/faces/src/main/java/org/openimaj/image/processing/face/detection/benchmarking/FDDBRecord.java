package org.openimaj.image.processing.face.detection.benchmarking;

import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;

public interface FDDBRecord {
	public String getImageName();

	public FImage getFImage();

	public MBFImage getMBFImage();

	public List<? extends DetectedFace> getGroundTruth();
}
