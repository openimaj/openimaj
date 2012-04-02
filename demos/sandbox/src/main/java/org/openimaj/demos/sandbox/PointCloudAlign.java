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
package org.openimaj.demos.sandbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.point.Point3d;
import org.openimaj.math.geometry.point.Point3dImpl;
import org.openimaj.math.geometry.transforms.RigidTransformModel3d;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

public class PointCloudAlign {
	List<Pair<Point3d>> pts;
	
	public PointCloudAlign(File f) throws IOException {
		readPoints(f);
	}
	
	void readPoints(File f) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(f));
		
		pts = new ArrayList<Pair<Point3d>>();
		
		String line;
		while ( (line = br.readLine()) != null) {
			String[] parts = line.split(":");
			
			pts.add(new Pair<Point3d>(parse(parts[1]), parse(parts[2])));
		}
	}
	
	private Point3d parse(String string) {
		String[] parts = string.split(",");
		
		double x = Double.parseDouble(parts[0]);
		double y = Double.parseDouble(parts[1]);
		double z = Double.parseDouble(parts[2]);
		
		return new Point3dImpl(x, y, z);
	}

	public Matrix computeAlignment() {
		//AffineTransformModel3d model = new AffineTransformModel3d(0.1);
		RigidTransformModel3d model = new RigidTransformModel3d(0.1);
		model.estimate(pts);
		return model.getTransform();
		
		//RANSAC<Point3d, Point3d> ransac = new RANSAC<Point3d, Point3d>(model, 1000, new RANSAC.BestFitStoppingCondition(), true);
		//ransac.fitData(pts);
		//return ((MatrixTransformProvider) ransac.getModel()).getTransform();		
	}
	
	public static void main(String[] args) throws IOException {
		Matrix tf = Matrix.identity(4, 4);
		List<Point3d> pc = new ArrayList<Point3d>(); 
		
//		PointCloudAlign align = new PointCloudAlign(new File("/Volumes/Untitled/features/feature_info7.di"));
//		tf = align.computeAlignment();
//		for (Pair<Point3d> ptpr : align.pts) {
//			pc.add(ptpr.firstObject().transform(tf));
//		}
//		tf.print(5, 5);
		
		for (int i=1; i<178; i++) {
			PointCloudAlign align = new PointCloudAlign(new File("/Volumes/Untitled/features/feature_info"+i+".di"));
			Matrix t = align.computeAlignment();
			//t.print(3,3);
			t = t.inverse(); //from p2 to p1
		
			t = tf.times(t);
			for (Pair<Point3d> ptpr : align.pts) {
				pc.add(ptpr.firstObject().transform(tf));
				pc.add(ptpr.secondObject().transform(t));
			}
			tf = t;
		}

		for (Point3d pt : pc) {
			System.out.println(pt.toString().replace("(", "").replace(")", ""));
		}
	}
}
