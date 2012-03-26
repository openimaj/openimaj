package org.openimaj.demos.sandbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.point.Point3d;
import org.openimaj.math.geometry.point.Point3dImpl;
import org.openimaj.math.geometry.transforms.AffineTransformModel3d;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.math.model.fit.RANSAC.StoppingCondition;
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
		AffineTransformModel3d model = new AffineTransformModel3d(0.1);
		//RANSAC<Point3d, Point3d> ransac = new RANSAC<Point3d, Point3d>(model, 1000, new RANSAC.BestFitStoppingCondition(), true);
		//ransac.fitData(pts);
		model.estimate(pts);
		
		return model.getTransform();
	}
	
	
	public static void main(String[] args) throws IOException {
		Matrix tf = Matrix.identity(4, 4);
		List<Point3d> pc = new ArrayList<Point3d>(); 
		
		PointCloudAlign align = new PointCloudAlign(new File("/Volumes/Untitled/features/feature_info7.di"));
		tf = align.computeAlignment();
//		for (Pair<Point3d> ptpr : align.pts) {
//			pc.add(ptpr.firstObject().transform(tf));
//		}
		tf.print(5, 5);
		
//		for (int i=1; i<8; i++) {
//			PointCloudAlign align = new PointCloudAlign(new File("/Volumes/Untitled/features/feature_info"+i+".di"));
//			Matrix t = align.computeAlignment().inverse(); //from p2 to p1
//		
//			t = tf.times(t);
//			for (Pair<Point3d> ptpr : align.pts) {
//				pc.add(ptpr.firstObject().transform(tf));
//				pc.add(ptpr.secondObject().transform(t));
//			}
//			tf = t;
//		}

		for (Point3d pt : pc) {
			System.out.println(pt.toString().replace("(", "").replace(")", ""));
		}
	}
}
