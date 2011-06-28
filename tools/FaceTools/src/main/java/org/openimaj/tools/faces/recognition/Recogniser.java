package org.openimaj.tools.faces.recognition;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.recognition.FaceMatchResult;
import org.openimaj.image.processing.face.recognition.FaceRecogniser;
import org.openimaj.image.processing.face.recognition.SimpleKNNRecogniser;

public class Recogniser {
	public static void main(String [] args) throws IOException, ClassNotFoundException {
		args = new String[] {
//				"http://euobserver.com/onm/media/file2/31673eae96bb.png",
				"http://www.bbc.co.uk/suffolk/content/images/2006/02/01/edwina_currie_203_203x152.jpg",
//				"http://media.schwarzenegger.ca/arnold/Schwarzenegger.jpg",
//				"http://ecdn2.hark.com/images/000/001/667/1667/original.jpg",
//				"http://2.bp.blogspot.com/_pAgHyh3Ytyk/TIEQG6qf-AI/AAAAAAAAASE/_NvW4pBQVqU/s1600/John-Travolta1.jpg",
//				"http://www.reviewexplorer.com/wp-content/uploads/2010/08/arnold-schwarzeneggerwork.jpg",
//				"http://primetime.unrealitytv.co.uk/wp-content/uploads/2010/02/gordon_brown.jpg",
//				"http://www4.pictures.gi.zimbio.com/Sinn+Fein+Leader+Gerry+Adams+Speaks+Nat+l+jM95ONpW5s7l.jpg",
//				"/Volumes/Raid/face_databases/lfw/John_Travolta/John_Travolta_0001.jpg",
//				"/Volumes/Raid/face_databases/lfw/John_Travolta/John_Travolta_0002.jpg",
//				"/Volumes/Raid/face_databases/lfw/John_Travolta/John_Travolta_0003.jpg",
//				"/Volumes/Raid/face_databases/lfw/John_Travolta/John_Travolta_0004.jpg",
				};
		
//		FacePipeline engine = new FacePipeline();
		FKEFaceDetector engine = new FKEFaceDetector(new HaarCascadeDetector("haarcascade_frontalface_default.xml"));
		FaceRecogniser<KEDetectedFace> recogniser = FaceDatabaseTool.loadRecogniser(new File("/Users/jsh2/Work/openimaj/trunk/demos/FaceRecognitionDemo/src/main/resources/org/openimaj/demos/facerec/data/database.bin"));
		
		Set<String> restrict = new HashSet<String>();
		restrict.add("Arnold Schwarzenegger");
//		restrict.add("Gordon Brown");
//		restrict.add("Gerry Adams");
		restrict.add("Edwina Currie");
//		restrict.add("Jose Manuel Durao Barroso");
//		restrict.add("John Travolta");
//		restrict.add("Nicolas Cage");
		
		for (String arg : args) {
			FImage image = null;
			
			if (arg.contains("://")) {
				image = ImageUtilities.readF(new URL(arg));
			} else {
				image = ImageUtilities.readF(new File(arg));
			}
			
			List<KEDetectedFace> faces = engine.detectFaces(image);
			
			System.out.println("Image: " + arg);
			for (KEDetectedFace face : faces) {
				FaceMatchResult result = ((SimpleKNNRecogniser)recogniser).queryBestMatch(face, restrict);
//				FaceMatchResult result = ((SimpleKNNRecogniser)recogniser).queryBestMatch(face);
				
				System.out.println("bounds: " + face.getBounds() + "\tidentity: " + result.getIdentifier());
			}
		}
	}
}
