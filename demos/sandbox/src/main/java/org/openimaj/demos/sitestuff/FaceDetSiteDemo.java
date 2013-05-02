package org.openimaj.demos.sitestuff;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.processing.face.detection.CLMDetectedFace;
import org.openimaj.image.processing.face.detection.CLMFaceDetector;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.util.CLMDetectedFaceRenderer;
import org.openimaj.image.processing.face.util.KEDetectedFaceRenderer;
import org.openimaj.image.processing.face.util.SimpleDetectedFaceRenderer;
import org.openimaj.image.processing.resize.ResizeProcessor;

public class FaceDetSiteDemo {
	public static void main(String[] args) throws MalformedURLException, IOException {
		// Load the image
		FImage img = ImageUtilities.readF(new URL("file:///Users/ss/Desktop/Barack-Obama-02.jpg"));
		img.processInplace(new ResizeProcessor(640, 480));

		MBFImage mbfAll = new MBFImage(img.width*3, img.height, ColourSpace.RGB);
		MBFImage mbf;

		// A simple Haar-Cascade face detector
		HaarCascadeDetector det1 = new HaarCascadeDetector();
		DetectedFace face1 = det1.detectFaces(img).get(0);

		mbf = MBFImage.createRGB(img);
		new SimpleDetectedFaceRenderer().drawDetectedFace(mbf,10,face1);
		mbfAll.drawImage(mbf, 0, 0);


		// Get the facial keypoints
		FKEFaceDetector det2 = new FKEFaceDetector();
		KEDetectedFace face2 = det2.detectFaces(img).get(0);

		mbf = MBFImage.createRGB(img);
		new KEDetectedFaceRenderer().drawDetectedFace(mbf,10,face2);
		mbfAll.drawImage(mbf, img.width, 0);


		// With the CLM Face Model
		CLMFaceDetector det3 = new CLMFaceDetector();
		CLMDetectedFace face3 = det3.detectFaces(img).get(0);

		mbf = MBFImage.createRGB(img);
		new CLMDetectedFaceRenderer().drawDetectedFace(mbf,10,face3);
		mbfAll.drawImage(mbf, img.width*2, 0);

		mbfAll.processInplace(new ResizeProcessor(320,240));

		DisplayUtilities.display(mbfAll);
		ImageUtilities.write(mbfAll, new File("/Users/ss/Desktop/barack-detected.png"));
	}
}
