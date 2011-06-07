package org.openimaj.image.processing.face.recognition.benchmarking.dataset;

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
import org.openimaj.image.processing.face.parts.FacePipeline;

public class GeorgiaTechFaceDataset extends FaceDataset {
	static final int N_INSTANCES = 15;
	static final int N_PERSON = 50;
	
	File basedir = new File("/Volumes/Raid/face_databases/gt_db");
	
	public GeorgiaTechFaceDataset() throws IOException, ClassNotFoundException {
		load();
	}
	
	public GeorgiaTechFaceDataset(File basedir) throws IOException, ClassNotFoundException {
		this.basedir = basedir;
		load();
	}
	
	protected void load() throws IOException, ClassNotFoundException {
		System.out.println("Loading dataset: ");
		FacePipeline engine = new FacePipeline();

		for (int p=1; p<=N_PERSON; p++) {
			List<DetectedFace> instances = new ArrayList<DetectedFace>();
			
			for (int i=1; i<=N_INSTANCES; i++) {
				System.out.print(".");
				File imagefile = new File(basedir, String.format("s%02d/%02d.jpg", p, i));
				File featurefile = new File(basedir, String.format("s%02d/%02d.bin", p, i));

				DetectedFace fd = null;
				if (featurefile.exists()) {
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(featurefile));
					fd = (DetectedFace) ois.readObject();
					ois.close();
				} else {
					FImage image = ImageUtilities.readF(imagefile);

					List<DetectedFace> descrs = engine.extractFaces(image);

					if (descrs.size() == 1) {
						ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(featurefile));
						oos.writeObject(descrs.get(0));
						oos.close();

						fd = descrs.get(0);
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
