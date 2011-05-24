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
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processing.algorithm.EqualisationProcessor;
import org.openimaj.image.processing.face.FaceDetector;
import org.openimaj.image.processing.haar.Cascades;
import org.openimaj.image.processing.haar.ClassifierCascade;
import org.openimaj.image.processing.haar.GroupingPolicy;
import org.openimaj.image.processing.haar.MultiscaleDetection;
import org.openimaj.image.processing.haar.ObjectDetector;
import org.openimaj.image.processing.haar.ScaledImageDetection;
import org.openimaj.math.geometry.shape.Rectangle;

public class HaarCascadeDetector implements FaceDetector, Serializable {
	public enum BuiltInCascade {
		FRONTALFACE_ALT("haarcascade_frontalface_alt.bin"),
		FRONTALFACE_ALT2("haarcascade_frontalface_alt2.bin"),
		FRONTALFACE_ALT_TREE("haarcascade_frontalface_alt_tree.bin"),
		FRONTALFACE_DEFAULT("haarcascade_frontalface_default.bin"),
		FULLBODY("haarcascade_fullbody.bin"),
		LOWERBODY("haarcascade_lowerbody.bin"),
		PROFILE_FACE("haarcascade_profileface.bin"),
		UPPERBODY("haarcascade_upperbody.bin")
		;
		
		private String classFile;
		
		private BuiltInCascade(String classFile) {
			this.classFile = classFile;
		}
		
		public HaarCascadeDetector load() {
			try {
				return HaarCascadeDetector.read(HaarCascadeDetector.class.getResourceAsStream(classFile));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private static final long serialVersionUID = 1L;
	
	protected int minScanWindowSize = 1;
	protected float scaleFactor = 1.1f;
	protected ClassifierCascade cascade;

	protected GroupingPolicy groupingPolicy;
	protected boolean scaleImage = false;
	protected boolean histogramEqualize = false;
	
	public HaarCascadeDetector(String cas) throws Exception {
		setCascade(cas);
		groupingPolicy = new GroupingPolicy();
	}

	public HaarCascadeDetector() throws Exception {
		setCascade("haarcascade_frontalface_default.xml");
		groupingPolicy = new GroupingPolicy();
	}

	public boolean scaleImage() {
		return scaleImage;
	}

	public void setScaleImage(boolean scaleImage) {
		this.scaleImage = scaleImage;
	}
	
	public int getMinSize() {
		return minScanWindowSize;
	}

	public void setMinSize(int size) {
		this.minScanWindowSize = size;
	}

	public GroupingPolicy getGroupingPolicy() {
		return groupingPolicy;
	}

	public void setGroupingPolicy(GroupingPolicy groupingPolicy) {
		this.groupingPolicy = groupingPolicy;
	}
	
	public List<Rectangle> detectObjects(MBFImage image) {
		return detectObjects(Transforms.calculateIntensityNTSC(image));
	}
	
	public List<Rectangle> detectObjects(FImage image) {
		if (histogramEqualize)
			image.processInline(new EqualisationProcessor()); // = HistogramEqualizer.histoGramEqualizeGray(image);
		
		ObjectDetector detector = new MultiscaleDetection(cascade, scaleFactor);
		if (scaleImage) {
			detector = new ScaledImageDetection(detector);
		}

		List<Rectangle> result = detector.detectObjects(image, minScanWindowSize);
		return groupingPolicy.reduceAreas(result);
	}

	public double getScale() {
		return scaleFactor;
	}

	public void setCascade(String selection) throws Exception {
		// try to load serialized cascade from external XML file
		InputStream in = null;
		try {
			in = Cascades.class.getResourceAsStream(selection);

			if (in == null) {
				in = new FileInputStream(new File(selection));
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

	public void setScale(float scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	@Override
	public List<ConnectedComponent> findFaces(MBFImage inputRGB) {
		return findFaces(Transforms.calculateIntensityNTSC(inputRGB));
	}
	
	public List<ConnectedComponent> findFaces(FImage image) {
		List<Rectangle> result = detectObjects(image);

		List<ConnectedComponent> ccs = new ArrayList<ConnectedComponent>(result.size());

		for (int i = 0; i < result.size(); i++) {
			Rectangle r = result.get(i);
			ConnectedComponent cc = new ConnectedComponent((int)r.x, (int)r.y, (int)r.getWidth(), (int)r.getHeight());
			ccs.add(cc);
		}

		return ccs;
	}

	public void save(OutputStream os) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(this);
	}
	
	public static HaarCascadeDetector read(InputStream is) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(is);
		return (HaarCascadeDetector) ois.readObject();
	}
	
	public static void main(String [] args) throws Exception {
		String[] cascades = {
				"haarcascade_frontalface_alt.xml",
				"haarcascade_frontalface_alt2.xml",
				"haarcascade_frontalface_alt_tree.xml",
				"haarcascade_frontalface_default.xml",
				"haarcascade_fullbody.xml",
				"haarcascade_lowerbody.xml",
				//"haarcascade_mcs_upperbody.xml",
				"haarcascade_profileface.xml",
				"haarcascade_upperbody.xml",
				//"lbpcascade_frontalface.xml"
		};

//		Float[] colours [] = {
//				new Float[]{0.0f, 0.0f, 1.0f},//"haarcascade_frontalface_alt.xml",
//				new Float[]{0.0f, 1.0f, 0.0f},//"haarcascade_frontalface_alt2.xml",
//				new Float[]{1.0f, 0.0f, 0.0f},//"haarcascade_frontalface_alt_tree.xml",
//				new Float[]{1.0f, 0.0f, 1.0f},//"haarcascade_frontalface_default.xml",
//				new Float[]{0.0f, 1.0f, 1.0f},//"haarcascade_fullbody.xml",
//				new Float[]{1.0f, 1.0f, 0.0f},//"haarcascade_lowerbody.xml",
//				//new Float[]{0.5f, 0.0f, 1.0f},//"haarcascade_mcs_upperbody.xml",
//				new Float[]{0.0f, 0.5f, 1.0f},//"haarcascade_profileface.xml",
//				new Float[]{1.0f, 0.5f, 1.0f},//"haarcascade_upperbody.xml",
//				//new Float[]{1.0f, 0.5f, 0.0f} //"lbpcascade_frontalface.xml"
//		};

		MBFImage img = ImageUtilities.readMBF(new File(args[0]));

		List<List<ConnectedComponent>> results = new ArrayList<List<ConnectedComponent>>(cascades.length);
		for (int i=0; i<cascades.length; i++) {
			HaarCascadeDetector det = HaarCascadeDetector.read(HaarCascadeDetector.class.getResourceAsStream(cascades[i].replace(".xml", ".bin")));
			//FaintHaarDetector det = new FaintHaarDetector(cascades[i]);
			//det.save(new java.io.FileOutputStream(new File("/Users/jsh2/Desktop/" + cascades[i].replace(".xml", ".bin"))));
			results.add(det.findFaces(img));
		}
		
//		BorderRenderer<Float[]> render = new BorderRenderer<Float[]>(img, new Float[]{0f,0f,0f}, ConnectMode.CONNECT_8);
//		for (int i=0; i<cascades.length; i++) {
//			render.setColour(colours[i]);
//			ConnectedComponent.process(results.get(i), render);
//		}
//		ImageUtilities.write(img, "png", new File("/Users/jsh2/Desktop/jfaces.png"));
		
		for (int i=0; i<cascades.length; i++) {
			for (ConnectedComponent cc : results.get(i)) {
				int [] bb = cc.calculateRegularBoundingBox();
				System.out.format("%s, %d, %d, %d, %d\n", cascades[i], bb[0], bb[1], bb[2], bb[3]);
			}
		}
	}
}
