package org.openimaj.demos;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.alignment.AffineAligner;
import org.openimaj.image.processing.face.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.keypoints.KEDetectedFace;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

public class Mustache {
	MBFImage mustache;
	
	public Mustache() {
		try {
			mustache = ImageUtilities.readMBFAlpha(Mustache.class.getResourceAsStream("mustache.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Mustache(MBFImage mustache) {
		this.mustache = mustache;
	}
	
	public MBFImage addMustaches(MBFImage image) {
		MBFImage cimg;
		
		if (image.getWidth() > image.getHeight() && image.getWidth() > 640) {
			cimg = image.process(new ResizeProcessor(640, 480));
		} else 	if (image.getHeight() > image.getWidth() && image.getHeight() > 640) {
			cimg = image.process(new ResizeProcessor(480, 640));
		} else {
			cimg = image.clone();
		}
		
		FImage img = Transforms.calculateIntensityNTSC(cimg);
		
		List<KEDetectedFace> faces = new FKEFaceDetector().detectFaces(img);
		MBFImageRenderer renderer = cimg.createRenderer();
		
		for(KEDetectedFace face : faces) {
			Matrix tf = AffineAligner.estimateAffineTransform(face);
			Shape bounds = face.getBounds();
			
			MBFImage m = mustache.transform(tf.times(TransformUtilities.scaleMatrix(1f/4f, 1f/4f)));
			
			renderer.drawImage(m, (int)bounds.minX(), (int)bounds.minY());
		}
		
		return cimg;
	}
	
	public static void main(String[] args) throws IOException {
//		File image = new File("/Users/jon/Desktop/IMG_5590.jpg");
		File image = new File("/Users/jon/Pictures/Pictures/2003/09/29/DCP_1051.jpg");
		MBFImage cimg = ImageUtilities.readMBF(image);

		cimg = new Mustache().addMustaches(cimg);
		
		DisplayUtilities.display(cimg);
	}
}
