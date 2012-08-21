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
package org.openimaj.demos.faces;

import java.io.IOException;
import java.util.List;

import org.openimaj.demos.Demo;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.alignment.AffineAligner;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

import Jama.Matrix;

/**
 * Mustache demo
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@Demo(
		author = "Jonathon Hare",
		description = "Demonstration of the face keypoint pipeline by taking the " +
				"webcam image, detecting faces and applying a moustache to the found " +
				"faces.",
		keywords = { "moustache", "video", "face", "webcam" },
		title = "Moustaches",
		arguments = { "-v" },
		icon = "/org/openimaj/demos/icons/video/mustache-icon.png")
public class Mustache {
	MBFImage mustache;
	private FKEFaceDetector detector;

	/**
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * 
	 * @created 26 Sep 2011
	 */
	public static class VideoMustache {
		private Mustache m = new Mustache();

		/**
		 * Default constructor
		 * 
		 * @throws IOException
		 */
		public VideoMustache() throws IOException {
			final VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay(new VideoCapture(320, 240));

			vd.addVideoListener(new VideoDisplayListener<MBFImage>()
			{
				@Override
				public void beforeUpdate(MBFImage frame)
				{
					frame.internalAssign(m.addMustaches(frame));
				}

				@Override
				public void afterUpdate(VideoDisplay<MBFImage> display)
				{
				}
			});
		}
	}

	/**
	 * Default constructor
	 * 
	 * @throws IOException
	 */
	public Mustache() throws IOException {
		this(ImageUtilities.readMBFAlpha(Mustache.class.getResourceAsStream("mustache.png")));
	}

	/**
	 * Construct with custom mustache image
	 * 
	 * @param mustache
	 */
	public Mustache(MBFImage mustache) {
		this.mustache = mustache;
		this.detector = new FKEFaceDetector(new HaarCascadeDetector(80));
	}

	/**
	 * Detect faces in the image and render mustaches.
	 * 
	 * @param image
	 * @return image with rendered mustaches
	 */
	public MBFImage addMustaches(MBFImage image) {
		MBFImage cimg;

		if (image.getWidth() > image.getHeight() && image.getWidth() > 640) {
			cimg = image.process(new ResizeProcessor(640, 480));
		} else if (image.getHeight() > image.getWidth() && image.getHeight() > 640) {
			cimg = image.process(new ResizeProcessor(480, 640));
		} else {
			cimg = image.clone();
		}

		final FImage img = Transforms.calculateIntensityNTSC(cimg);

		final List<KEDetectedFace> faces = detector.detectFaces(img);
		final MBFImageRenderer renderer = cimg.createRenderer();

		for (final KEDetectedFace face : faces) {
			final Matrix tf = AffineAligner.estimateAffineTransform(face);
			final Shape bounds = face.getBounds();

			final MBFImage m = mustache.transform(tf.times(TransformUtilities.scaleMatrix(1f / 4f, 1f / 4f)));

			renderer.drawImage(m, (int) bounds.minX(), (int) bounds.minY());
		}

		return cimg;
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		args = new String[] { "-v" };
		if (args.length > 0 && args[0].equals("-v")) {
			new Mustache.VideoMustache();
		} else {
			MBFImage cimg = ImageUtilities.readMBF(Mustache.class
					.getResourceAsStream("/org/openimaj/demos/image/sinaface.jpg"));

			cimg = new Mustache().addMustaches(cimg);

			DisplayUtilities.display(cimg);
		}
	}
}
