package org.openimaj.image.processing.face.recognition.benchmarking.dataset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.io.IOUtils;

public class GeorgiaTechFaceDataset<T extends DetectedFace> extends FaceDataset<T> {
	static final int N_INSTANCES = 15;
	static final int N_PERSON = 50;
	
	public GeorgiaTechFaceDataset(FaceDetector<T, FImage> detector) throws IOException, ClassNotFoundException {
		load(detector, new File("/Volumes/Raid/face_databases/gt_db"));
	}
	
	public GeorgiaTechFaceDataset(FaceDetector<T, FImage> detector, File basedir) throws IOException, ClassNotFoundException {
		load(detector, basedir);
	}
	
	protected void load(FaceDetector<T, FImage> detector, File basedir) throws IOException, ClassNotFoundException {
		System.out.println("Loading dataset: ");

		for (int p=1; p<=N_PERSON; p++) {
			List<T> instances = new ArrayList<T>();
			
			for (int i=1; i<=N_INSTANCES; i++) {
				System.out.print(".");
				File imagefile = new File(basedir, String.format("s%02d/%02d.jpg", p, i));
				File featurefile = new File(basedir, String.format("s%02d/%02d-%d.bin", p, i, detector.hashCode()));

				T fd = null;
				if (featurefile.exists()) {
					fd = IOUtils.read(featurefile, detector.getDetectedFaceClass());
				} else {
					FImage image = ImageUtilities.readF(imagefile);

					List<T> descrs = detector.detectFaces(image);

					if (descrs.size() == 1) {
						fd = descrs.get(0);
						IOUtils.writeBinary(featurefile, fd);
					}
				}
				
				if (fd != null) {
					instances.add(fd);
				}
			}
			
			this.getData().add(instances);
			System.out.println();
		}
		System.out.println("Done");
	}
}
