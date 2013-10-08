/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
