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
package org.openimaj.demos.facestuff;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.alignment.AffineAligner;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.FacialKeypoint;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.transforms.TransformUtilities;

public class Alignment {
	public static void main(String[] args) throws MalformedURLException, IOException {
		final FImage img = ImageUtilities.readF(new File(
				"/Volumes/Raid/face_databases/lfw/Aaron_Peirsol/Aaron_Peirsol_0002.jpg"));
		final FKEFaceDetector detector = new FKEFaceDetector(1.6f);
		final FaceAligner<KEDetectedFace> aligner = new AffineAligner(125, 160, 0.1f);

		final KEDetectedFace face = detector.detectFaces(img).get(0);

		final int facePatchSize = Math.max(160, 125);
		final double size = facePatchSize + 2.0 * facePatchSize * 0.1f;
		final double sc = 80 / size;

		final float[][] Pmu = {
				{ 25.0347f, 34.1802f, 44.1943f, 53.4623f, 34.1208f, 39.3564f, 44.9156f, 31.1454f, 47.8747f },
				{ 34.1580f, 34.1659f, 34.0936f, 33.8063f, 45.4179f, 47.0043f, 45.3628f, 53.0275f, 52.7999f } };

		FImage model = new FImage(125, 160);
		for (int i = 0; i < Pmu[0].length; i++) {
			Point2dImpl pt = new Point2dImpl(Pmu[0][i], Pmu[1][i]);

			pt = pt.transform(TransformUtilities.scaleMatrixAboutPoint(1 / sc, 1 / sc, new Point2dImpl(Pmu[0][0],
					Pmu[1][0])));

			model.drawPoint(pt, 1f, 3);
		}

		for (final FacialKeypoint kpt : face.getKeypoints())
			img.drawPoint(
					kpt.position.transform(TransformUtilities.translateMatrix(face.getBounds().x, face.getBounds().y)),
					1f, 3);
		model = model.inverse();
		ImageUtilities.write(img, new File("/Users/jsh2/keypoints.png"));
		ImageUtilities.write(model, new File("/Users/jsh2/model.png"));
		ImageUtilities.write(aligner.align(face), new File("/Users/jsh2/aligned.png"));
	}
}
