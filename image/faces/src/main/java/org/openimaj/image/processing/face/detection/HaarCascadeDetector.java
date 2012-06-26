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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.algorithm.EqualisationProcessor;
import org.openimaj.image.processing.haar.Cascades;
import org.openimaj.image.processing.haar.ClassifierCascade;
import org.openimaj.image.processing.haar.GroupingPolicy;
import org.openimaj.image.processing.haar.MultiscaleDetection;
import org.openimaj.image.processing.haar.ObjectDetector;
import org.openimaj.image.processing.haar.ScaledImageDetection;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.hash.HashCodeUtil;

/**
 * A face detector based on a Haar cascade.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class HaarCascadeDetector implements FaceDetector<DetectedFace, FImage>, Serializable {
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
		upperbody("haarcascade_upperbody.xml"),
		/**
		 * A frontal face detector
		 */
		lbpcascade_frontalface("lbpcascade_frontalface.xml");
		
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
		 * @return A new {@link HaarCascadeDetector}
		 */
		public HaarCascadeDetector load() {
			try {
				return new HaarCascadeDetector(classFile);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private static final long serialVersionUID = 1L;
	
	protected int minScanWindowSize = 1;
	protected float scaleFactor = 1.1f;
	
	protected String cascadeName;
	protected ClassifierCascade cascade;

	protected GroupingPolicy groupingPolicy;
	protected boolean scaleImage = false;
	protected boolean histogramEqualize = false;
	
	/**
	 * Construct with the given cascade resource. 
	 * See {@link #setCascade(String)} to understand
	 * how the cascade is loaded.
	 * 
	 * @param cas The cascade resource. 
	 * @see #setCascade(String)
	 */
	public HaarCascadeDetector(String cas) {
		try {
			setCascade(cas);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		groupingPolicy = new GroupingPolicy();
	}

	/**
	 * Construct with the {@link BuiltInCascade#frontalface_default}
	 * cascade.
	 */
	public HaarCascadeDetector() {
		this(BuiltInCascade.frontalface_default.classFile());
	}

	/**
	 * Construct with the {@link BuiltInCascade#frontalface_default}
	 * cascade and the given minimum search window size.
	 * @param minSize minimum search window size
	 */
	public HaarCascadeDetector(int minSize) {
		this();
		minScanWindowSize = minSize;
	}
	
	/**
	 * Construct with the given cascade resource and the given minimum 
	 * search window size. See {@link #setCascade(String)} to understand
	 * how the cascade is loaded.
	 * 
	 * @param cas The cascade resource. 
	 * @param minSize minimum search window size.
	 * 
	 * @see #setCascade(String)
	 */
	public HaarCascadeDetector(String cas, int minSize) {
		this(cas);
		minScanWindowSize = minSize;
	}

	/**
	 * @return true if using a single scale detection; false otherwise.
	 * 
	 * @see #getScale()
	 */
	public boolean scaleImage() {
		return scaleImage;
	}

	/**
	 * Set whether to use a single scale detection rather than
	 * multiscale search.
	 * 
	 * @param scaleImage
	 * 
	 * @see #setScale(float)
	 */
	public void setScaleImage(boolean scaleImage) {
		this.scaleImage = scaleImage;
	}
	
	/**
	 * @return The minimum detection window size
	 */
	public int getMinSize() {
		return minScanWindowSize;
	}

	/**
	 * Set the minimum detection window size
	 * @param size
	 */
	public void setMinSize(int size) {
		this.minScanWindowSize = size;
	}

	/**
	 * @return The grouping policy
	 */
	public GroupingPolicy getGroupingPolicy() {
		return groupingPolicy;
	}

	/**
	 * Set the grouping policy for merging detections
	 * @param groupingPolicy
	 */
	public void setGroupingPolicy(GroupingPolicy groupingPolicy) {
		this.groupingPolicy = groupingPolicy;
	}
	
	@Override
	public List<DetectedFace> detectFaces(FImage image) {
		if (histogramEqualize)
			image.processInplace(new EqualisationProcessor()); // = HistogramEqualizer.histoGramEqualizeGray(image);
		
		ObjectDetector detector = new MultiscaleDetection(cascade, scaleFactor);
		if (scaleImage) {
			detector = new ScaledImageDetection(detector);
		}

		List<Rectangle> rects = detector.detectObjects(image, minScanWindowSize);
		rects = groupingPolicy.reduceAreas(rects);
		
		List<DetectedFace> results = new ArrayList<DetectedFace>();
		for (Rectangle r : rects) {
			results.add(new DetectedFace(r, image.extractROI(r)));
		}
		
		return results;
	}

	/**
	 * @return The initial search scale
	 */
	public double getScale() {
		return scaleFactor;
	}

	/**
	 * Set the cascade classifier for this detector. The cascade 
	 * file is first searched for as a java resource, and if it is
	 * not found then a it is assumed to be a file on the filesystem.
	 * 
	 * @param cascadeResource The cascade to load. 
	 * @throws Exception if there is a problem loading the cascade.
	 */
	public void setCascade(String cascadeResource) throws Exception {
		cascadeName = cascadeResource;
		
		// try to load serialized cascade from external XML file
		InputStream in = null;
		try {
			in = Cascades.class.getResourceAsStream(cascadeResource);

			if (in == null) {
				in = new FileInputStream(new File(cascadeResource));
			}
			cascade = Cascades.readFromXML(in);
		} catch (Exception e) {
			throw e;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Set the initial search scale
	 * @param scaleFactor 
	 */
	public void setScale(float scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	/**
	 * Serialize the detector using java serialization to
	 * the given stream
	 * @param os the stream
	 * @throws IOException
	 */
	public void save(OutputStream os) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(this);
	}
	
	/**
	 * Deserialize the detector from a stream. The detector must have
	 * been written with a previous invokation of {@link #save(OutputStream)}.
	 * @param is
	 * @return {@link HaarCascadeDetector} read from stream.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static HaarCascadeDetector read(InputStream is) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(is);
		return (HaarCascadeDetector) ois.readObject();
	}
	
	@Override
	public int hashCode() {
		int hashCode = HashCodeUtil.SEED;
				
		HashCodeUtil.hash(hashCode, this.minScanWindowSize);
		HashCodeUtil.hash(hashCode, this.scaleFactor);
		HashCodeUtil.hash(hashCode, this.cascadeName);
//		HashCodeUtil.hash(hashCode, this.cascade);
//		HashCodeUtil.hash(hashCode, this.groupingPolicy);
		HashCodeUtil.hash(hashCode, this.scaleFactor);
		HashCodeUtil.hash(hashCode, this.histogramEqualize);
		
		return hashCode;
	}

	@Override
	public Class<DetectedFace> getDetectedFaceClass() {
		return DetectedFace.class;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		minScanWindowSize = in.readInt();
		scaleFactor = in.readFloat();
		
		cascadeName = in.readUTF();
		
		int sz = in.readInt();
		byte[] bytes = new byte[sz];
		in.readFully(bytes);
		
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes)); 
		try {
			cascade = (ClassifierCascade) ois.readObject();
			groupingPolicy = (GroupingPolicy) ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		
		scaleImage = in.readBoolean();
		histogramEqualize = in.readBoolean();		
	}

	@Override
	public byte[] binaryHeader() {
		return "HAAR".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(minScanWindowSize);
		out.writeFloat(scaleFactor);
		
		out.writeUTF(cascadeName);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(cascade);
		oos.writeObject(groupingPolicy);
		oos.close();
		out.writeInt(baos.size());
		out.write(baos.toByteArray());
		
		out.writeBoolean(scaleImage);
		out.writeBoolean(histogramEqualize);
	}
	
	@Override
	public String toString() {
		return "HaarCascadeDetector[cascade="+cascadeName+"]";
	}
	
	public ClassifierCascade getCascade() {
		return cascade;
	}
}
