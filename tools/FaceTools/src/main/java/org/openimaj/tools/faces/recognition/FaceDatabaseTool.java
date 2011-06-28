package org.openimaj.tools.faces.recognition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.recognition.FaceRecogniser;

public class FaceDatabaseTool {
	public static <T extends DetectedFace> FaceRecogniser<T> loadRecogniser(File data) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(data));
		
		@SuppressWarnings("unchecked")
		FaceRecogniser<T> rec = (FaceRecogniser<T>) ois.readObject();
		
		ois.close();
		
		return rec;
	}
	
	private static void saveRecogniser(FaceRecogniser recogniser, File output) throws FileNotFoundException, IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(output));
		
		oos.writeObject(recogniser);
		
		oos.close();
	}
	
	public static void train(FaceRecogniser recogniser, File basedir) throws IOException, ClassNotFoundException {
		int people=0, images=0;
		
		for (File personDir : basedir.listFiles()) {
			if (!personDir.isHidden() && personDir.isDirectory() && personDir.listFiles().length >= 4) {
				people++;
				String identifier = personDir.getName().replace("_", " ");
				
				System.out.println("processing " + identifier);
				
				int i=0;
				for (File imgFile : personDir.listFiles()) {
					if (imgFile.isFile() && !imgFile.isHidden() && imgFile.getName().endsWith(".jpg")) {
						File featurefile = new File(imgFile.getParent(), imgFile.getName().replace(".jpg", ".bin"));

						KEDetectedFace fd = null;
						if (featurefile.exists()) {
							ObjectInputStream ois = new ObjectInputStream(new FileInputStream(featurefile));
							fd = (KEDetectedFace) ois.readObject();
							ois.close();
						}
						
						if (fd != null) {
							images++;
							recogniser.addInstance(identifier, fd);
							
							if (++i == 15) {
								break;
							}
						}
					}
				}
			}
		}
		
		System.out.println("Indexed " + people + " people in " + images + " images.");
	}
	
//	public static void main(String [] args) throws IOException, ClassNotFoundException {
//		FacialFeatureFactory<ReversedTruncatedDistanceLTPFeature> factory = new ReversedTruncatedDistanceLTPFeature.Factory(new AffineAligner());
//		FaceRecogniser recogniser = new SimpleKNNRecogniser<ReversedTruncatedDistanceLTPFeature>(factory, 1);
//		
//		train(recogniser, new File(args[0]));
//		
//		saveRecogniser(recogniser, new File(args[1]));
//	}
}
