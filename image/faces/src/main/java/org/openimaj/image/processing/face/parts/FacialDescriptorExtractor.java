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

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.face.parts.DetectedFace.DetectedFacePart;
import org.openimaj.image.processing.face.parts.FacialKeypoint.FacialKeypointType;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

/**
 * Class to extract different types of feature from a face
 * described by a set of facial key-points.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class FacialDescriptorExtractor {
	final static int [][] VP={{0},
			{1},
			{2},
			{3},
			{4},
			{5},
			{6},
			{7},
			{8},
			{0, 1},
			{2, 3},
			{1, 2},
			{7, 8}};
	
	/**
	 * Normalised positions of facial parts
	 */
	final static float [][] Pmu = {{25.0347f, 34.1802f, 44.1943f, 53.4623f, 34.1208f, 39.3564f, 44.9156f, 31.1454f, 47.8747f},
			  					   {34.1580f, 34.1659f, 34.0936f, 33.8063f, 45.4179f, 47.0043f, 45.3628f, 53.0275f, 52.7999f},
			  					   {1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f}};
	
	final static int CANONICAL_SIZE = 80;

	/** The radius of the descriptor samples about each point */
	int radius = 7;
	
	/** The scale of the descriptor samples about each point */
	float scl = 1;
	
	int facePatchSize = 100;
	float facePatchBorderPercentage = 0.225f;
	
	public FacialDescriptorExtractor() {
		
	}
	
	protected Matrix estimateAffineTransform(FacialKeypoint[] pts) {
		float emin=Float.POSITIVE_INFINITY;
		Matrix T = null;

		//for (c=1:size(NC8,2)) {
		for (int c=0; c<9; c++) {
			//A=Pmu(:,NC8(:,c))';
			//B=P(:,NC8(:,c))';
			Matrix A = new Matrix(8,3);
			Matrix B = new Matrix(8,3);
			for (int i=0, j=0; i<9; i++) {
				if (i!=8-c) {
					A.set(j, 0, Pmu[0][i]);
					A.set(j, 1, Pmu[1][i]);
					A.set(j, 2, Pmu[2][i]);
					B.set(j, 0, pts[i].imagePosition.x);
					B.set(j, 1, pts[i].imagePosition.y);
					B.set(j, 2, 1);
					j++;
				}
			}

			//Tc=(A\B)'; // essentially solve AX=B for X and transpose
			Matrix Tc = A.solve(B).transpose();

			//P1=Tc*Pmu(:,NC8(:,c));
			Matrix P1 = Tc.times(A.transpose());
			//D=P1-P(:,NC8(:,c));
			Matrix D = P1.minus(B.transpose());

			float e = 0;
			for (int cc=0; cc<D.getColumnDimension(); cc++) {
				float colsum = 0;
				for (int rr=0; rr<D.getRowDimension(); rr++) {
					colsum += D.get(rr, cc) * D.get(rr, cc);;
				}
				e += Math.sqrt(colsum);
			}

			if (e<emin) {
				emin=e;
				T=Tc;
			}
		}

		return T;
	}
	
	protected void extractAffineFacePatch(FImage image, FacialKeypoint[] pts, DetectedFace descriptor) {
		double size = facePatchSize + 2.0 * facePatchSize * facePatchBorderPercentage;
		double sc = (double)CANONICAL_SIZE / size;
		
		//do the scaling to everything but the translation!!!
		Matrix T = descriptor.transform.copy();
		T.set(0, 0, T.get(0, 0) * sc);
		T.set(1, 1, T.get(1, 1) * sc);
		T.set(0, 1, T.get(0, 1) * sc);
		T.set(1, 0, T.get(1, 0) * sc);
		
		FImage J = FacePipeline.pyramidResize(image, T);	
		descriptor.affineFacePatch = FacePipeline.extractPatch(J, T, (int) size, (int) (size*facePatchBorderPercentage));
	}
	
	protected void extractFacePatch(FImage image, FacialKeypoint[] pts, DetectedFace descriptor) {
		FacialKeypoint lefteye = FacialKeypoint.getKeypoint(pts, FacialKeypointType.EYE_LEFT_LEFT);
		FacialKeypoint righteye = FacialKeypoint.getKeypoint(pts, FacialKeypointType.EYE_RIGHT_RIGHT);
		
		int eyeDist = 50;
		int eyePaddingLeftRight = 15;
		int eyePaddingTop = 20;
		
		float dx = righteye.imagePosition.x - lefteye.imagePosition.x;
		float dy = righteye.imagePosition.y - lefteye.imagePosition.y;
		
		float rotation = (float) Math.atan2(dy, dx);
		float scaling = (float) (eyeDist / Math.sqrt(dx*dx + dy*dy));
		
		float tx = lefteye.imagePosition.x - eyePaddingLeftRight / scaling;
		float ty = lefteye.imagePosition.y - eyePaddingTop / scaling;
		
		Matrix tf = TransformUtilities.scaleMatrix(scaling, scaling).times(TransformUtilities.translateMatrix(-tx, -ty)).times(TransformUtilities.rotationMatrixAboutPoint(-rotation, lefteye.imagePosition.x, lefteye.imagePosition.y));
	
		tf = tf.inverse();
		
		FImage J = FacePipeline.pyramidResize(image, tf);
		descriptor.facePatch = FacePipeline.extractPatch(J, tf, 80, 0);
	}
	
	public DetectedFace extractDescriptor(FImage image, FacialKeypoint[] pts, Rectangle bounds) {
		DetectedFace descr = new DetectedFace();
		descr.bounds = bounds;
		descr.featureRadius = radius;
		
		descr.transform = estimateAffineTransform(pts);
	
		extractFeatures(image, pts, descr);
		extractAffineFacePatch(image, pts, descr);
		extractFacePatch(image, pts, descr);
		
		return descr;
	}
	
	protected void extractFeatures(FImage image, FacialKeypoint[] pts, DetectedFace descr) {
		Matrix T0 = descr.transform;
		Matrix T = T0.copy();
		FImage J = FacePipeline.pyramidResize(image, T);
		
		float pyrScale = (float) (T0.get(0,2) / T.get(0, 2));
		
		//build a list of the center of each patch wrt image J
		Point2dImpl[] P0 = new Point2dImpl[VP.length];
		for (int j=0; j<P0.length; j++) {
			int [] vp = VP[j];
			int vp0 = vp[0];
			
			P0[j] = new Point2dImpl(0, 0);
			if (vp.length == 1) {
				P0[j].x = pts[vp0].imagePosition.x / pyrScale;
				P0[j].y = pts[vp0].imagePosition.y / pyrScale;
			} else {
				int vp1 = vp[1];
				P0[j].x = ((pts[vp0].imagePosition.x + pts[vp1].imagePosition.x) / 2.0f) / pyrScale;
				P0[j].y = ((pts[vp0].imagePosition.y + pts[vp1].imagePosition.y) / 2.0f) / pyrScale;
			}
		}
		
		//Prebuild transform
		List<Point2dImpl> transformed = new ArrayList<Point2dImpl>();
		List<Pixel> nontransformed = new ArrayList<Pixel>();
		for (int rr=-radius; rr<=radius; rr++) {
			for (int cc=-radius; cc<=radius; cc++) {
				float r2 = rr*rr + cc*cc;
				if (r2<=radius*radius) { //inside circle
					//Note: do transform without the translation!!!
					float px = (float) (cc*scl* T.get(0, 0) + rr*scl*T.get(0, 1));
					float py = (float) (cc*scl* T.get(1, 0) + rr*scl*T.get(1, 1));
					
					transformed.add(new Point2dImpl(px, py));
					nontransformed.add(new Pixel(cc,rr));
				}
			}
		}
		
		for (int j=0; j<VP.length; j++) {
			DetectedFacePart pd = descr.new DetectedFacePart(FacialKeypointType.valueOf(j), new Point2dImpl(P0[j].x * pyrScale, P0[j].y * pyrScale));
			descr.faceParts.add(pd);
			pd.featureVector = new float[transformed.size()];
			
			int n = 0;
			float mean = 0;
			float m2 = 0;
			
			for (int i=0; i<transformed.size(); i++) {
				Point2dImpl XYt = transformed.get(i);
				
				double xt = XYt.x + P0[j].x;
				double yt = XYt.y + P0[j].y;
				float val = J.getPixelInterp(xt, yt);
				
				pd.featureVector[i] = val;
				
				n++;
				float delta = val - mean;
				mean = mean + delta / n;
				m2 = m2 + delta*(val - mean);
			}
			
			float std = (float) Math.sqrt(m2 / (n-1));
			if (std <= 0) std = 1;
			
			for (int i=0; i<transformed.size(); i++) {
				pd.featureVector[i] = (pd.featureVector[i] - mean) / std;
			}
		}
	}
}
