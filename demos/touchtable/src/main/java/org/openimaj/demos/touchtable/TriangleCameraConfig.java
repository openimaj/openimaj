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
package org.openimaj.demos.touchtable;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.transform.PiecewiseMeshWarp;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.shape.Triangle;
import org.openimaj.util.pair.Pair;

public class TriangleCameraConfig implements CameraConfig {

	private int gridWidth;
	private int gridHeight;
	private Rectangle visibleArea;
	private ArrayList<Point2d> touchArray;
	private ArrayList<Point2d> screenArray;
	private ArrayList<Pair<Shape>> trianglePairs;
	private PiecewiseMeshWarp<Float[], MBFImage> nlp;

	public TriangleCameraConfig(ArrayList<Point2d> touchArray, int gridx,int gridy, Rectangle visibleArea) {
		this.gridWidth = gridx;
		this.gridHeight = gridy;
		this.visibleArea = visibleArea;
		this.touchArray = touchArray;
		createScreenArray();
		createTriangles();
		createNonLinearWarp();
	}

	private void createScreenArray() {
		this.screenArray = new ArrayList<Point2d>();
		int yInc = (int) (this.visibleArea.height/gridHeight);
		int xInc = (int) (this.visibleArea.width/gridWidth);
		for(int row = 0; row < gridHeight+1; row++){
			for(int col = 0; col < gridWidth + 1; col++){
				this.screenArray.add(new Point2dImpl(col * xInc, row * yInc));
			}
		}
	}

	public TriangleCameraConfig() {
		
	}

	private void createTriangles() {
		this.trianglePairs = new ArrayList<Pair<Shape>>();
		for (int row = 0; row < gridHeight; row++) {
			for(int col = 0; col < gridWidth; col++){
				Point2d p1 = this.touchArray.get(col + row * (gridWidth+1));
				Point2d p2 = this.touchArray.get((col+1) + row * (gridWidth+1));
				Point2d p3a = this.touchArray.get(col + (row+1) * (gridWidth+1));
				Point2d p3b = this.touchArray.get((col+1) + (row+1) * (gridWidth+1));
				
				Point2d p1Screen = this.screenArray.get(col + row * (gridWidth+1));
				Point2d p2Screen = this.screenArray.get((col+1) + row * (gridWidth+1));
				Point2d p3aScreen = this.screenArray.get(col + (row+1) * (gridWidth+1));
				Point2d p3bScreen = this.screenArray.get((col+1) + (row+1) * (gridWidth+1));
				
				Triangle ct1 = new Triangle(p1,p2,p3a);
				Triangle ct2 = new Triangle(p2,p3a,p3b);
				
				Triangle st1 = new Triangle(p1Screen,p2Screen,p3aScreen);
				Triangle st2 = new Triangle(p2Screen,p3aScreen,p3bScreen);
				
				this.trianglePairs.add(new Pair<Shape>(st1,ct1));
				this.trianglePairs.add(new Pair<Shape>(st2,ct2));
			}
		}
		
	}

	@Override
	public Touch transformTouch(Touch point) {
		int matching = this.nlp.getMatchingShapeIndex(point.calculateCentroid());
		if(matching == -1) return null;
		float ptx = point.getX();
		float pty = point.getY();
		Point2d[] cameraTriangle = ((Triangle)(this.trianglePairs.get(matching).secondObject())).vertices;
		Point2d[] screenTriangle = ((Triangle)(this.trianglePairs.get(matching).firstObject())).vertices;
		Point2dImpl A = (Point2dImpl) cameraTriangle[0]; // Place camera vector triangle points
        Point2dImpl B = (Point2dImpl) cameraTriangle[1]; // into some local vectors
        Point2dImpl C = (Point2dImpl) cameraTriangle[2];
        float total_area = (A.x - B.x) * (A.y - C.y) - (A.y - B.y) * (A.x - C.x); // Calculate the total area of the triangle
        // pt,B,C
        float area_A = (ptx - B.x) * (pty - C.y) - (pty - B.y) * (ptx - C.x); // and find the area enclosed by the

        // A,pt,C
        float area_B = (A.x - ptx) * (A.y - C.y) - (A.y - pty) * (A.x - C.x); // three camera vector triangle points

        float bary_A = area_A / total_area;                                                                                             // so we can find three fractions of the total area
        float bary_B = area_B / total_area;
        float bary_C = 1.0f - bary_A - bary_B;  // bary_A + bary_B + bary_C = 1

        Point2dImpl sA = (Point2dImpl) screenTriangle[0]; // Place screen vector triangle points
        Point2dImpl sB = (Point2dImpl) screenTriangle[1]; // into some local vectors
        Point2dImpl sC = (Point2dImpl) screenTriangle[2];


        float transformedPosx = (sA.x*bary_A) + (sB.x*bary_B) + (sC.x*bary_C);
        float transformedPosy = (sA.y*bary_A) + (sB.y*bary_B) + (sC.y*bary_C);
        
        return new Touch(transformedPosx,transformedPosy,point.getRadius(), point.touchID, point.motionVector);
	}

	@Override
	public String asciiHeader() {
		return "triangleconfig";
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		this.gridWidth = in.nextInt();
		this.gridHeight = in.nextInt();
		this.visibleArea = new Rectangle(in.nextFloat(),in.nextFloat(),in.nextFloat(),in.nextFloat());
		this.touchArray = new ArrayList<Point2d>();
		while(in.hasNext()){
			this.touchArray.add(
					new Point2dImpl(in.nextFloat(),in.nextFloat())
			);
		}
		this.createScreenArray();
		this.createTriangles();
		this.createNonLinearWarp();
		
	}

	private void createNonLinearWarp() {
		this.nlp = new PiecewiseMeshWarp<Float[], MBFImage>(this.trianglePairs);		
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.format("%d %d\n",this.gridWidth,this.gridHeight);
		out.format("%f %f %f %f\n",this.visibleArea.x,this.visibleArea.y,this.visibleArea.width,this.visibleArea.height);
		for (Point2d t : this.touchArray) {
			out.format("%f %f\n",
					t.getX(),t.getY()
			);
		}
	}

	public void drawTriangles(MBFImage image) {
		for (Pair<Shape> t : this.trianglePairs) {
			image.drawShape(t.firstObject(), RGBColour.RED);
		}
	}

}
