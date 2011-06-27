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
import org.openimaj.image.processing.face.parts.DetectedFace;
import org.openimaj.image.processing.face.parts.FrontalFaceEngine;

public class LFWDataset extends FaceDataset {
	List<String> metadata = new ArrayList<String>();
	
	public LFWDataset(File basedir) throws IOException, ClassNotFoundException {
		load(basedir);
	}

	protected void load(File basedir) throws IOException, ClassNotFoundException {
		FrontalFaceEngine engine = new FrontalFaceEngine("haarcascade_frontalface_default.xml");
		
		for (File personDir : basedir.listFiles()) {
			if (!personDir.isHidden() && personDir.isDirectory()) {
				metadata.add(personDir.getName().replace("_", " "));
				
				List<DetectedFace> faces = new ArrayList<DetectedFace>();
				data.add(faces);
				
				for (File imgFile : personDir.listFiles()) {
					if (imgFile.isFile() && !imgFile.isHidden() && imgFile.getName().endsWith(".jpg")) {
						File featurefile = new File(imgFile.getParent(), imgFile.getName().replace(".jpg", ".bin"));

						DetectedFace fd = null;
						if (featurefile.exists()) {
							ObjectInputStream ois = new ObjectInputStream(new FileInputStream(featurefile));
							fd = (DetectedFace) ois.readObject();
							ois.close();
						} else {
							FImage image = ImageUtilities.readF(imgFile);

							List<DetectedFace> descrs = engine.extractFaces(image);

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
