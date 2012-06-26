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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openimaj.demos.Demo;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.renderer.FImageRenderer;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Triangle;
import org.openimaj.math.geometry.triangulation.DelaunayTriangulator;

/**
 * 	Demonstrates the OpenIMAJ implementation of Delaunay 
 * 	triangulation. Displays the triangulation of a set of pre-defined
 * 	points.
 * 
 *  @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *	
 *	@created 15 Feb 2012
 */
@Demo(
	author = "Jonathon Hare", 
	description = "Demonstrates the OpenIMAJ implementation of Delaunay " +
			"triangulation. Displays the triangulation of a set of pre-defined " +
			"points.", 
	keywords = { "delaunay", "triangulation", "math", "geometry" }, 
	title = "Delaunay Triangulation",
	icon = "/org/openimaj/demos/icons/core/delaunay-icon.png"
)
public class Triangulation {
	/**
	 * 	Default main
	 *  @param args Command-line arguments
	 */
	public static void main(String [] args) {
		Point2d[] pixels = {
				new Point2dImpl(0,0),
				new Point2dImpl(100,0),
				new Point2dImpl(20,20),
				new Point2dImpl(40,20),
				new Point2dImpl(60,20),
				new Point2dImpl(80,20),
				new Point2dImpl(45,55),
				new Point2dImpl(55,55),
				new Point2dImpl(50,60),
				new Point2dImpl(30,65),
				new Point2dImpl(70,65),
				new Point2dImpl(0,100),
				new Point2dImpl(100,100),
		};
		
		List<Triangle> tris = DelaunayTriangulator.triangulate(new ArrayList<Point2d>(Arrays.asList(pixels)));
		
		FImage image = new FImage(101, 101);
		FImageRenderer renderer = image.createRenderer();
		
		for (Triangle t : tris) {
			renderer.drawShape(t, 1f);
		}
		System.out.println(tris);
		DisplayUtilities.display(image);
	}
}
