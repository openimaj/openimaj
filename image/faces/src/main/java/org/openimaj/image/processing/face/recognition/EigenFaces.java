package org.openimaj.image.processing.face.recognition;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.parts.FacePipeline;
import org.openimaj.image.processing.face.parts.FacialDescriptor;

import Jama.Matrix;

public class EigenFaces extends EigenImages {
	protected FacePipeline engine = new FacePipeline();
	
	protected List<FImage> extractAlignedFaces(File file) throws IOException, ClassNotFoundException {
//		FImage image = ImageUtilities.readF(file);
//		
//		List<FacialDescriptor> descrs = engine.extractFaces(image);
//		
//		List<FImage> faces = new ArrayList<FImage>(descrs.size());
//		
//		for (FacialDescriptor d : descrs)
//			faces.add(d.facePatch);
//		
//		return faces;
		
		List<FImage> faces = new ArrayList<FImage>();
		File featurefile = new File(file.getAbsolutePath().replace(".jpg", ".bin"));
		
		if (featurefile.exists()) {
			System.out.println(featurefile);
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(featurefile));
			faces.add(((FacialDescriptor) ois.readObject()).affineFacePatch);
			ois.close();
		}
		return faces;
	}
	
	public Matrix loadImageData(List<File> files) throws IOException, ClassNotFoundException {
		List<FImage> images = new ArrayList<FImage>();
		
		for (File file : files) {
			List<FImage> faces = extractAlignedFaces(file);
			
			if (faces != null)
				images.addAll(faces);
		}
		
		int length = images.get(0).height * images.get(0).width;
		Matrix m = new Matrix(images.size(), length);
		double [][] d = m.getArray(); 
		for (int i=0; i<images.size(); i++) {
			System.arraycopy(images.get(i).getDoublePixelVector(), 0, d[i], 0, length);
		}
		
		return m;
	}
	
	public void learnBasis(List<File> files, int ndims) throws IOException, ClassNotFoundException {
		Matrix m = loadImageData(files);
		learnBasis(m, ndims);
	}
	
	public List<File> getImages(File dir, String suffix) {
		List<File> files = new ArrayList<File>();
		
		for (File f : dir.listFiles()) {
			if (f.isDirectory())
				files.addAll(getImages(f, suffix));
			else if (f.getName().endsWith(suffix)) {
				files.add(f);
			}
		}
		
		return files;
	}
	
	public static void main(String [] args) throws Exception {
		EigenFaces ef = new EigenFaces();
		
		List<File> images = ef.getImages(new File("/Volumes/Raid/face_databases/gt_db/"), ".jpg");
		ef.learnBasis(images, 100);
		
		for (int i=0; i<10; i++) {
			double [][] evecs = ef.eigenvectors.getArray();
			
			FImage image = new FImage(80,80);
			for (int j=0, y=0; y<80; y++)
				for (int x=0; x<80; x++)
					image.pixels[y][x] = (float) evecs[j++][evecs[0].length - 1 - i];
			DisplayUtilities.display(image.normalise());
		}
	}
}
