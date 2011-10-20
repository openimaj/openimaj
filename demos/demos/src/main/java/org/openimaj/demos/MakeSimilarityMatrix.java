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
package org.openimaj.demos;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.feature.DoGSIFTFeature;
import org.openimaj.image.processing.face.feature.comparison.DoGSIFTFeatureComparator;

import Jama.Matrix;

public class MakeSimilarityMatrix {
	static DoGSIFTFeature.Factory factory = new DoGSIFTFeature.Factory();
	static DoGSIFTFeatureComparator comp = new DoGSIFTFeatureComparator();
	
	protected static DetectedFace loadFace(File file) throws IOException {
		FImage img = ImageUtilities.readF(file);
		return new DetectedFace(img.getBounds(), img);
	}

	public static void main(String[] args) throws IOException {
		File dir = new File("/Volumes/data/livememories/RTTR/faces");
		File[] allFaces = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".png")) return true;
				return false;
			}
			
		});
		
		System.out.println("Loading data");
		DoGSIFTFeature[] allFeatures = makeFeatures(allFaces);
		
		System.out.println("Computing similarity matrix");
		Matrix similarity = new Matrix(allFaces.length, allFaces.length);
		for (int j=0; j<allFaces.length; j++) {
			System.err.println(j +"/" + allFaces.length);
			DoGSIFTFeature fj = allFeatures[j];
			
			for (int i=j+1; i<allFaces.length; i++) {
				DoGSIFTFeature fi = allFeatures[i];
				
				double sim = comp.compare(fi, fj);
				
				similarity.set(j, i, sim);
				similarity.set(i, j, sim);
			}
		}
		
		PrintWriter pw = new PrintWriter(new FileWriter(new File("/Users/jsh2/Desktop/faces-sift-sim.mat")));
		similarity.print(pw, 5, 5);
		pw.close();
	}

	private static DoGSIFTFeature[] makeFeatures(File[] allFaces) throws IOException {
		DoGSIFTFeature[] features = new DoGSIFTFeature[allFaces.length];
		
		for (int i=0; i<allFaces.length; i++) {
			features[i] = makeFeature(allFaces[i]);
			System.err.println(i +"/" + allFaces.length);
		}
		
		return features;
	}

	private static DoGSIFTFeature makeFeature(File file) throws IOException {
		DetectedFace face = loadFace(file);
		
		return factory.createFeature(face, false);
	}
}
