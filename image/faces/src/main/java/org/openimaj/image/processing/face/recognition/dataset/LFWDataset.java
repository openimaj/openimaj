package org.openimaj.image.processing.face.recognition.dataset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;

public class LFWDataset<T extends DetectedFace> extends FaceDataset<T> {
	List<String> metadata = new ArrayList<String>();
	
	public LFWDataset(FaceDetector<T, FImage> detector, File basedir) throws IOException, ClassNotFoundException {
		load(detector, basedir);
	}

	@SuppressWarnings("unchecked")
	protected void load(FaceDetector<T, FImage> detector, File basedir) throws IOException, ClassNotFoundException {
		for (File personDir : basedir.listFiles()) {
			if (!personDir.isHidden() && personDir.isDirectory()) {
				metadata.add(personDir.getName().replace("_", " "));
				
				List<T> faces = new ArrayList<T>();
				data.add(faces);
				
				for (File imgFile : personDir.listFiles()) {
					if (imgFile.isFile() && !imgFile.isHidden() && imgFile.getName().endsWith(".jpg")) {
						File featurefile = new File(imgFile.getParent(), imgFile.getName().replace(".jpg", ".bin"));

						T fd = null;
						if (featurefile.exists()) {
							ObjectInputStream ois = new ObjectInputStream(new FileInputStream(featurefile));
							fd = (T) ois.readObject();
							ois.close();
						} else {
							FImage image = ImageUtilities.readF(imgFile);

							List<T> descrs = detector.detectFaces(image);

							if (descrs.size() == 1) {
								ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(featurefile));
								oos.writeObject(descrs.get(0));
								oos.close();

								fd = descrs.get(0);
							} else {
								System.out.format("Found %d faces in %s\n", descrs.size(), imgFile.getAbsolutePath());
							}
						}
						
						if (fd != null) {
							faces.add(fd);
						}
					}
				}
			}
		}
	}

	@Override
	public String getIdentifier(int id) {
		return metadata.get(id);
	}
}
