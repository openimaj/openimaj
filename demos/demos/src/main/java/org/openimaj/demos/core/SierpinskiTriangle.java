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
package org.openimaj.demos.core;

import java.util.Random;

import org.openimaj.demos.Demo;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.renderer.FImageRenderer;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Triangle;

/**
 * Draw a Sierpinski Triangle into an FImage using two
 * different techniques. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Demo(
	author = "Jonathon Hare", 
	description = "Demonstrates some of the core drawing tools within " +
			"OpenIMAJ by drawing a Sierpinski triangle using two different " +
			"techniques.", 
	keywords = { "sierpinski", "triangle", "render", "point", "drawing" }, 
	title = "Sierpinski Triangle",
	icon = "/org/openimaj/demos/icons/core/sierpinski-icon.png"
)
public class SierpinskiTriangle {
	/**
	 * Draw a Sierpinski Triangle by plotting random points
	 * @return image with triangle
	 */
	public static FImage randomPointTriangle() {
		FImage image = new FImage(500, 500);
		FImageRenderer renderer = image.createRenderer();
		
		Point2d [] vertices = {
			new Point2dImpl(0, 500),
			new Point2dImpl(250, 0),
			new Point2dImpl(500, 500),
		};
		
		Point2d p = new Point2dImpl(75, 450);
		
		Random random = new Random();
		
		for (int i=0; i<5000; i++) {
			int j = random.nextInt(3);
			
			p.setX((p.getX() + vertices[j].getX()) / 2);
			p.setY((p.getY() + vertices[j].getY()) / 2);
			
			renderer.drawPoint(p, 1.0f, 1);
		}
		
		return image;
	}

	protected static void divideTriangle(Point2d a, Point2d b, Point2d c, int k, FImageRenderer renderer) {
		if (k>0) {
			Point2d ab = new Point2dImpl((a.getX() + b.getX()) / 2, (a.getY() + b.getY()) / 2);
			Point2d ac = new Point2dImpl((a.getX() + c.getX()) / 2, (a.getY() + c.getY()) / 2);
			Point2d bc = new Point2dImpl((b.getX() + c.getX()) / 2, (b.getY() + c.getY()) / 2);
			
			divideTriangle(a, ab, ac, k-1, renderer);
			divideTriangle(c, ac, bc, k-1, renderer);
			divideTriangle(b, bc, ab, k-1, renderer);
		} else {
			renderer.drawShapeFilled(new Triangle(a, b, c), 1.0f);
		}
	}
	
	/**
	 * Draw a Sierpinski Triangle by recursively drawing sub-triangles
	 * @return image with triangle
	 */
	public static FImage polygonTriangle() {
		FImage image = new FImage(500, 500);
		FImageRenderer renderer = image.createRenderer();
		
		Point2d [] v = new Point2d[] {
				new Point2dImpl(0, 500),
				new Point2dImpl(500, 500),
				new Point2dImpl(250, 0),
		};
		
		divideTriangle(v[0], v[1], v[2], 4, renderer);
		
		return image;
	}
	
	/**
	 * 	Default main
	 *  @param args Command-line arguments
	 */
	public static void main(String [] args) {
		DisplayUtilities.display(randomPointTriangle());
		DisplayUtilities.display(polygonTriangle());
	}
}
