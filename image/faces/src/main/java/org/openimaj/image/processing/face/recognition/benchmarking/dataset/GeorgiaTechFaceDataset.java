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
package org.openimaj.image.processing.face.recognition.benchmarking.dataset;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.io.IOUtils;

public class GeorgiaTechFaceDataset<T extends DetectedFace> extends FaceDataset<Integer, T> {
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
					addItem(p, new FaceInstance<T>(fd, String.format("s%02d/%02d", p, i)));
				}
			}
			
			System.out.println();
		}
		System.out.println("Done");
	}
}
