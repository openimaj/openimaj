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

import org.openimaj.feature.local.keypoints.face.FacialKeypoint;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.pyramid.SimplePyramid;
import org.openimaj.math.geometry.point.Point2dImpl;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class FacialDescriptor {
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
	
	public static Matrix estimateAffineTransform(Pixel[] pts) {
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
					B.set(j, 0, pts[i].x);
					B.set(j, 1, pts[i].y);
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

			//			e=sum(sqrt(sum(D.*D)));
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
	
	public static FacialKeypoint extdesc(FImage I, Pixel[] pts) {
		int r = 7;
		float scl = 1;
		
		// The keypoint we are constrcuting
		FacialKeypoint keypoint = new FacialKeypoint();
		keypoint.featurePoints = new ArrayList<Pixel>();
		keypoint.featureRadius = r;
		
		keypoint.nFeatures = VP.length;

		Matrix T = estimateAffineTransform(pts);
		
		//		T=T(1:2,1:2);
		T = T.getMatrix(0, 1, 0, 1);

		//		[U,S,V]=svd(T);
		SingularValueDecomposition svd = T.svd();

		//		s=mean(diag(S));
		double sv[] = svd.getSingularValues();
		float s = (float) (sv[0]+sv[1] / 2);

		int lev = (int) (Math.max(Math.floor(Math.log(s)/Math.log(1.5)),0));
		float ps = (float) Math.pow(1.5, lev);
		s=s/ps;
		//		T=[1/ps 0 ; 0 1/ps]*T;
		T = new Matrix(new double[][]{{1/ps,0}, {0,1/ps}}).times(T);
		
		keypoint.transform = T;

//		FImage J = null;
//		if(lev == 0){
//			J = I;
//		}
//		else{
//			FImage [] PYR = pyramid15(I,lev);
//			J = PYR[lev-1];
//		}
		FImage J = I.process(new SimplePyramid<FImage>(1.5f,lev));
		

		//		P0=zeros(2,size(VP,2));
		//		for (j=1:size(VP,2)) {
		//			P0(:,j)=mean(P(:,    VP( VP(:,j)>0 , j )    ),2);            
		//		}
		//		P0=(P0-1)/ps+1;
		Point2dImpl[] P0 = new Point2dImpl[VP.length];
		for (int j=0; j<P0.length; j++) {
			int [] vp = VP[j];
			int vp0 = vp[0];
			
			P0[j] = new Point2dImpl(0, 0);
			if (vp.length == 1) {
				P0[j].x = pts[vp0].x / ps;
				P0[j].y = pts[vp0].y / ps;				
				
				keypoint.featurePoints.add(pts[vp0].clone());
			} else {
				int vp1 = vp[1];
				P0[j].x = ((pts[vp0].x + pts[vp1].x) / 2.0f) / ps;
				P0[j].y = ((pts[vp0].y + pts[vp1].y) / 2.0f) / ps;
				
				keypoint.featurePoints.add(new Pixel((pts[vp0].x + pts[vp1].x) / 2,(pts[vp0].y + pts[vp1].y) / 2));
			}
		}

		//Prebuild transform
		List<Point2dImpl> transformed = new ArrayList<Point2dImpl>();
//		List<Pixel> nontransformed = new ArrayList<Pixel>();
		for (int rr=-r; rr<=r; rr++) {
			for (int cc=-r; cc<=r; cc++) {
				float r2 = rr*rr + cc*cc;
				if (r2<=r*r) { //inside circle
					Matrix XY0 = new Matrix(new double[][] {{cc*scl}, {rr*scl}});
					Matrix XYt = T.times(XY0);

					transformed.add(new Point2dImpl((float)XYt.get(0, 0), (float)XYt.get(1, 0)));
//					nontransformed.add(new Pixel(cc,rr));
				}
			}
		}
		
		keypoint.featureLength = transformed.size();
		
		float[] feature = new float[transformed.size()*VP.length];
		int last = 0;
		for (int j=0; j<VP.length; j++) {
//			FImage fdj = new FImage(2*r+1,2*r+1);
			int n = 0;
			float mean = 0;
			float m2 = 0;
			
			// 
			
			for (Point2dImpl XYt : transformed) {
				double xt = XYt.x + P0[j].x;
				double yt = XYt.y + P0[j].y;

//				int cc = nontransformed.get(n).x;
//				int rr = nontransformed.get(n).y;
//				fdj.pixels[rr+r][cc+r] = J.getPixelInterp(xt, yt);
				feature[n + last] = J.getPixelInterp(xt, yt);

				n++;
				float delta = feature[n + last - 1] - mean;
				mean = mean + delta / n;
				m2 = m2 + delta*(feature[n + last - 1] - mean);						
			}
//			DisplayUtilities.display(fdj.doubleSize().doubleSize().doubleSize());
			
			float std = (float) Math.sqrt(m2/(n-1));
			if (std<=0) std = 1;
			
			for (int i=last; i<last+n; i++) {
				feature[i] = (feature[i] - mean) / std;
			}
			
			last += n;
		}
		
		keypoint.featureVector = feature;
		return keypoint;
	}
}
