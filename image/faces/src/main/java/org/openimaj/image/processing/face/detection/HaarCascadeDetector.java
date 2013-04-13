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
package org.openimaj.image.processing.face.detection;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.objectdetection.filtering.DetectionFilter;
import org.openimaj.image.objectdetection.filtering.OpenCVGrouping;
import org.openimaj.image.objectdetection.haar.Detector;
import org.openimaj.image.objectdetection.haar.OCVHaarLoader;
import org.openimaj.image.objectdetection.haar.StageTreeClassifier;
import org.openimaj.image.processing.algorithm.EqualisationProcessor;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.hash.HashCodeUtil;
import org.openimaj.util.pair.ObjectIntPair;

/**
 * A face detector based on a Haar cascade. The cascades provided by
 * {@link BuiltInCascade} are the same as those available in OpenCV.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Viola, P.", "Jones, M." },
		title = "Rapid object detection using a boosted cascade of simple features",
		year = "2001",
		booktitle = "Computer Vision and Pattern Recognition, 2001. CVPR 2001. Proceedings of the 2001 IEEE Computer Society Conference on",
		pages = { " I", "511 ", " I", "518 vol.1" },
		number = "",
		volume = "1",
		customData = {
				"keywords",
				" AdaBoost; background regions; boosted simple feature cascade; classifiers; face detection; image processing; image representation; integral image; machine learning; object specific focus-of-attention mechanism; rapid object detection; real-time applications; statistical guarantees; visual object detection; feature extraction; image classification; image representation; learning (artificial intelligence); object detection;",
				"doi", "10.1109/CVPR.2001.990517",
				"ISSN", "1063-6919 "
		})
public class HaarCascadeDetector implements FaceDetector<DetectedFace, FImage> {
	/**
	 * The set of pre-trained cascades from OpenCV.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public enum BuiltInCascade {
		/**
		 * A eye detector
		 */
		eye("haarcascade_eye.xml"),
		/**
		 * A eye with glasses detector
		 */
		eye_tree_eyeglasses("haarcascade_eye_tree_eyeglasses.xml"),
		/**
		 * A frontal face detector
		 */
		frontalface_alt("haarcascade_frontalface_alt.xml"),
		/**
		 * A frontal face detector
		 */
		frontalface_alt2("haarcascade_frontalface_alt2.xml"),
		/**
		 * A frontal face detector
		 */
		frontalface_alt_tree("haarcascade_frontalface_alt_tree.xml"),
		/**
		 * A frontal face detector
		 */
		frontalface_default("haarcascade_frontalface_default.xml"),
		/**
		 * A fullbody detector
		 */
		fullbody("haarcascade_fullbody.xml"),
		/**
		 * A left eye detector
		 */
		lefteye_2splits("haarcascade_lefteye_2splits.xml"),
		/**
		 * A lower body detector
		 */
		lowerbody("haarcascade_lowerbody.xml"),
		/**
		 * A detector for a pair of eyes
		 */
		mcs_eyepair_big("haarcascade_mcs_eyepair_big.xml"),
		/**
		 * A detector for a pair of eyes
		 */
		mcs_eyepair_small("haarcascade_mcs_eyepair_small.xml"),
		/**
		 * A left eye detector
		 */
		mcs_lefteye("haarcascade_mcs_lefteye.xml"),
		/**
		 * A mouth detector
		 */
		mcs_mouth("haarcascade_mcs_mouth.xml"),
		/**
		 * A nose detector
		 */
		mcs_nose("haarcascade_mcs_nose.xml"),
		/**
		 * A right eye detector
		 */
		mcs_righteye("haarcascade_mcs_righteye.xml"),
		/**
		 * An upper body detector
		 */
		mcs_upperbody("haarcascade_mcs_upperbody.xml"),
		/**
		 * A profile face detector
		 */
		profileface("haarcascade_profileface.xml"),
		/**
		 * A right eye detector
		 */
		righteye_2splits("haarcascade_righteye_2splits.xml"),
		/**
		 * An upper body detector
		 */
		upperbody("haarcascade_upperbody.xml");

		private String classFile;

		private BuiltInCascade(String classFile) {
			this.classFile = classFile;
		}

		/**
		 * @return The name of the cascade resource
		 */
		public String classFile() {
			return classFile;
		}

		/**
		 * Create a new detector with the this cascade.
		 * 
		 * @return A new {@link HaarCascadeDetector}
		 */
		public HaarCascadeDetector load() {
			try {
				return new HaarCascadeDetector(classFile);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected Detector detector;
	protected DetectionFilter<Rectangle, ObjectIntPair<Rectangle>> groupingFilter;
	protected boolean histogramEqualize = false;

	/**
	 * Construct with the given cascade resource. See
	 * {@link #setCascade(String)} to understand how the cascade is loaded.
	 * 
	 * @param cas
	 *            The cascade resource.
	 * @see #setCascade(String)
	 */
	public HaarCascadeDetector(String cas) {
		try {
			setCascade(cas);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
		groupingFilter = new OpenCVGrouping();
	}

	/**
	 * Construct with the {@link BuiltInCascade#frontalface_default} cascade.
	 */
	public HaarCascadeDetector() {
		this(BuiltInCascade.frontalface_default.classFile());
	}

	/**
	 * Construct with the {@link BuiltInCascade#frontalface_default} cascade and
	 * the given minimum search window size.
	 * 
	 * @param minSize
	 *            minimum search window size
	 */
	public HaarCascadeDetector(int minSize) {
		this();
		this.detector.setMinimumDetectionSize(minSize);
	}

	/**
	 * Construct with the given cascade resource and the given minimum search
	 * window size. See {@link #setCascade(String)} to understand how the
	 * cascade is loaded.
	 * 
	 * @param cas
	 *            The cascade resource.
	 * @param minSize
	 *            minimum search window size.
	 * 
	 * @see #setCascade(String)
	 */
	public HaarCascadeDetector(String cas, int minSize) {
		this(cas);
		this.detector.setMinimumDetectionSize(minSize);
	}

	/**
	 * @return The minimum detection window size
	 */
	public int getMinSize() {
		return this.detector.getMinimumDetectionSize();
	}

	/**
	 * Set the minimum detection window size
	 * 
	 * @param size
	 *            the window size
	 */
	public void setMinSize(int size) {
		this.detector.setMinimumDetectionSize(size);
	}

	/**
	 * @return The maximum detection window size
	 */
	public int getMaxSize() {
		return this.detector.getMaximumDetectionSize();
	}

	/**
	 * Set the maximum detection window size
	 * 
	 * @param size
	 *            the window size
	 */
	public void setMaxSize(int size) {
		this.detector.setMaximumDetectionSize(size);
	}

	/**
	 * @return The grouping filter
	 */
	public DetectionFilter<Rectangle, ObjectIntPair<Rectangle>> getGroupingFilter() {
		return groupingFilter;
	}

	/**
	 * Set the filter for merging detections
	 * 
	 * @param grouping
	 */
	public void setGroupingFilter(DetectionFilter<Rectangle, ObjectIntPair<Rectangle>> grouping) {
		this.groupingFilter = grouping;
	}

	@Override
	public List<DetectedFace> detectFaces(FImage image) {
		if (histogramEqualize)
			image.processInplace(new EqualisationProcessor());

		final List<Rectangle> rects = detector.detect(image);
		final List<ObjectIntPair<Rectangle>> filteredRects = groupingFilter.apply(rects);

		final List<DetectedFace> results = new ArrayList<DetectedFace>();
		for (final ObjectIntPair<Rectangle> r : filteredRects) {
			results.add(new DetectedFace(r.first, image.extractROI(r.first), r.second));
		}

		return results;
	}

	/**
	 * @see Detector#getScaleFactor()
	 * @return The detector scale factor
	 */
	public double getScaleFactor() {
		return detector.getScaleFactor();
	}

	/**
	 * Set the cascade classifier for this detector. The cascade file is first
	 * searched for as a java resource, and if it is not found then a it is
	 * assumed to be a file on the filesystem.
	 * 
	 * @param cascadeResource
	 *            The cascade to load.
	 * @throws Exception
	 *             if there is a problem loading the cascade.
	 */
	public void setCascade(String cascadeResource) throws Exception {
		// try to load serialized cascade from external XML file
		InputStream in = null;
		try {
			in = OCVHaarLoader.class.getResourceAsStream(cascadeResource);

			if (in == null) {
				in = new FileInputStream(new File(cascadeResource));
			}
			final StageTreeClassifier cascade = OCVHaarLoader.read(in);

			if (this.detector == null)
				this.detector = new Detector(cascade);
			else
				this.detector = new Detector(cascade, this.detector.getScaleFactor());
		} catch (final Exception e) {
			throw e;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
				}
			}
		}
	}

	/**
	 * Set the detector scale factor
	 * 
	 * @see Detector#setScaleFactor(float)
	 * 
	 * @param scaleFactor
	 *            the scale factor
	 */
	public void setScale(float scaleFactor) {
		this.detector.setScaleFactor(scaleFactor);
	}

	/**
	 * Serialize the detector using java serialization to the given stream
	 * 
	 * @param os
	 *            the stream
	 * @throws IOException
	 */
	public void save(OutputStream os) throws IOException {
		final ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(this);
	}

	/**
	 * Deserialize the detector from a stream. The detector must have been
	 * written with a previous invokation of {@link #save(OutputStream)}.
	 * 
	 * @param is
	 * @return {@link HaarCascadeDetector} read from stream.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static HaarCascadeDetector read(InputStream is) throws IOException, ClassNotFoundException {
		final ObjectInputStream ois = new ObjectInputStream(is);
		return (HaarCascadeDetector) ois.readObject();
	}

	@Override
	public int hashCode() {
		int hashCode = HashCodeUtil.SEED;

		hashCode = HashCodeUtil.hash(hashCode, this.detector.getMinimumDetectionSize());
		hashCode = HashCodeUtil.hash(hashCode, this.detector.getScaleFactor());
		hashCode = HashCodeUtil.hash(hashCode, this.detector.getClassifier().getName());
		hashCode = HashCodeUtil.hash(hashCode, this.groupingFilter);
		hashCode = HashCodeUtil.hash(hashCode, this.histogramEqualize);

		return hashCode;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		this.detector = IOUtils.read(in);
		this.groupingFilter = IOUtils.read(in);

		histogramEqualize = in.readBoolean();
	}

	@Override
	public byte[] binaryHeader() {
		return "HAAR".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		IOUtils.write(detector, out);
		IOUtils.write(groupingFilter, out);

		out.writeBoolean(histogramEqualize);
	}

	@Override
	public String toString() {
		return "HaarCascadeDetector[cascade=" + detector.getClassifier().getName() + "]";
	}

	/**
	 * @return the underlying Haar cascade.
	 */
	public StageTreeClassifier getCascade() {
		return detector.getClassifier();
	}

	/**
	 * @return the underlying {@link Detector}.
	 */
	public Detector getDetector() {
		return detector;
	}
}
