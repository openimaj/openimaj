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
package org.openimaj.image.processing.face.recognition;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
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
