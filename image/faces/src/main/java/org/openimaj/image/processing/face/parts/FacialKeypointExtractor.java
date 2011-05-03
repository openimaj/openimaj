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
package org.openimaj.image.processing.face.parts;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.algorithm.EuclideanDistanceTransform;
import org.openimaj.image.processing.algorithm.SummedAreaTable;

public class FacialKeypointExtractor {
	protected Model model;
	
	public FacialKeypointExtractor() {
		model = Model.DEFAULT_MODEL;
	}
	
	public FacialKeypointExtractor(Model model) {
		this.model = model;
	}
	
	public Pixel[] extractFeatures(FImage image) {
		SummedAreaTable sat = new SummedAreaTable(image);

		FImage [] AC = new FImage[9];
		for (int i=0; i<9; i++) {
			FImage map = MaskedHaarCascade.maskedHaarCascade(sat, model.winsize, model.winsize, model.part[i].HCas, model.part[i].talpha, model.part[i].M);

			AC[i] = map.multiplyInline(-(float)model.appwt);
		}
		
		return findParts(AC);
	}
	
	protected Pixel[] findParts(FImage [] AC) {
		float maxconf=Float.NEGATIVE_INFINITY;
		float conf = 0;
		Pixel [] Pbest = null;
		
		for (int t=0; t< model.tree.length; t++) {
			FImage [] B = new FImage[model.part.length];
			
			int [][][] argB = new int[model.part.length][model.imgsize][model.imgsize];
			
			Pixel [] P = new Pixel[model.part.length];
			for (int i=0; i<model.part.length; i++) P[i] = new Pixel(0,0); 

			for (int ci=model.tree[t].depthorder.length-1; ci>=0; ci--) {
				int c = model.tree[t].depthorder[ci];

				int [] off = model.tree[t].MU[c];
				int [] bb = new int[4];
				if (off[0]>=0) {
					bb[0]=0;
					bb[1]=model.imgsize-off[0];
				} else {
					bb[0]=-off[0];
					bb[1]=model.imgsize;
				}
				if (off[1]>=0) {
					bb[2]=0;
					bb[3]=model.imgsize-off[1];
				} else {
					bb[2]=-off[1];
					bb[3]=model.imgsize;
				}

				FImage C = new FImage(model.imgsize, model.imgsize);
				C.fill(Float.MAX_VALUE);

				for (int rr=bb[2]; rr<bb[3]; rr++) {
					for (int cc=bb[0]; cc<bb[1]; cc++) {
						int tc = cc + off[0];
						int tr = rr + off[1];
						
						C.pixels[rr][cc] = AC[c].pixels[tr][tc];
						
						for (int g : model.tree[t].children[c]) {
							C.pixels[rr][cc] += B[g].pixels[tr][tc];
						}
					}
				}
				
				if (model.tree[t].parent[c] != -1) {
					C.divideInline((float)model.tree[t].scale[c]);
					
					FImage D = new FImage(C.width, C.height);
					int [][] L = new int[C.height][C.width];
					EuclideanDistanceTransform.squaredEuclideanDistance(C, D, L);

					B[c] = D.multiplyInline((float)model.tree[t].scale[c]);

					for (int rr=0; rr<L.length; rr++)
						for (int cc=0; cc<L[0].length; cc++)
							argB[c][rr][cc] = L[rr][cc] + off[1] * model.imgsize + off[0];
				} else {
					FValuePixel min = C.minPixel();
					P[c].x = min.x;
					P[c].y = min.y;

					conf=-min.value;
				}
			}

			for (int ci=1; ci<model.tree[t].depthorder.length; ci++) {
				int c = model.tree[t].depthorder[ci];

				int p = model.tree[t].parent[c];
				int mini = argB[c][P[p].y][P[p].x];

				P[c].y = mini / model.imgsize;
				P[c].x = mini - P[c].y * model.imgsize;
			}

			conf = (float) (conf + Math.log(model.tree[t].mix));

			if (conf>maxconf) {
				maxconf=conf;
				Pbest=P;
			}
		}
		
		return Pbest;
	}

	public static void main(String [] args) throws IOException {		
		//FImage image = ImageUtilities.readF(new File("/Users/jsh2/Desktop/buffy_face.png"));
		FImage image = ImageUtilities.readF(new File("/Users/ss/Desktop/sinaface.jpg"));
		
		FacialKeypointExtractor fe = new FacialKeypointExtractor();
		
		long t1 = System.currentTimeMillis();
		Pixel[] pts = fe.extractFeatures(image);
		long t2 = System.currentTimeMillis();
		
		System.out.println((t2-t1)/1000.0);
		
		for (Pixel pt : pts) {
			image.drawPoint(pt, 1f, 3);
		}
		
		ImageUtilities.write(image, "png", new File("/Users/ss/Desktop/sinaface_feats.png"));
	}
}
