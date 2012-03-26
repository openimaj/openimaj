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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;

public class LFWDataset<T extends DetectedFace> extends FaceDataset<String, T> {
	public LFWDataset(FaceDetector<T, FImage> detector, File basedir) throws IOException, ClassNotFoundException {
		load(detector, basedir);
	}

	@SuppressWarnings("unchecked")
	protected void load(FaceDetector<T, FImage> detector, File basedir) throws IOException, ClassNotFoundException {
		for (File personDir : basedir.listFiles()) {
			if (!personDir.isHidden() && personDir.isDirectory()) {
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
							addItem(personDir.getName().replace("_", " "), new FaceInstance<T>(fd, imgFile.getParentFile().getName() + ":" + imgFile.getName().replace(".jpg", "")));
						}
					}
				}
			}
		}
	}
}
