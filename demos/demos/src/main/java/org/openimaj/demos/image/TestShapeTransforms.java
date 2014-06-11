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
package org.openimaj.demos.image;

import javax.swing.JFrame;

import org.openimaj.demos.Demo;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

/**
 * 	Demonstrates affine transforms for shapes.	
 * 
 *  @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *	
 *	@created 15 Feb 2012
 */
@Demo(
	author = "Sina Samangooei", 
	description = "Demonstrates affine transforms for shapes.", 
	keywords = { "shape", "affine", "transform" }, 
	title = "Affine Shape Transforms",
	icon = "/org/openimaj/demos/icons/image/affine-icon.png"
)
public class TestShapeTransforms {
	private static Runnable displayUpdater;
	private static JFrame frame;
	private static double rotation = Math.PI*2/4;
	private static Ellipse ellipse;
	private static MBFImage image;

	/**
	 * 	Default main
	 *  @param args Command-line arguments
	 */
	public static void main(String args[]){
		ellipse = new Ellipse(400,400,100,50,0);
		image = new MBFImage(800,800,ColourSpace.RGB);
		frame = DisplayUtilities.display(image);
		displayUpdater = new Runnable(){
			@Override
			public void run() {
				while(true){
					DisplayUtilities.display(image,frame);
					update();
					try {
						Thread.sleep(1000/30);
					} catch (InterruptedException e) {
					}
				}
			}
			
		};
		Thread t = new Thread(displayUpdater);
		t.start();
	}
	
	private static void update() {
		rotation += Math.PI/30;
//		int dx = 100;
//		int dy = 100;
//		float x = (float) (Math.cos(rotation) * dx + Math.sin(rotation) * dy);
//		float y = (float) (-Math.sin(rotation) * dx + Math.cos(rotation) * dy);
		Matrix rotMat = TransformUtilities.rotationMatrixAboutPoint(rotation, ellipse.calculateCentroid().getX(), ellipse.calculateCentroid().getY());
//		Matrix transMat = TransformUtilities.translateMatrix(x, y);
		Matrix scaleMat = TransformUtilities.scaleMatrix(Math.abs(0.5 * Math.cos(rotation)) + 1, Math.abs(0.5 * Math.sin(rotation))+ 1);
		Matrix scaledTrans = scaleMat.times(TransformUtilities.translateMatrix(-ellipse.calculateCentroid().getX(), -ellipse.calculateCentroid().getY()));
		scaledTrans = TransformUtilities.translateMatrix(ellipse.calculateCentroid().getX(), ellipse.calculateCentroid().getY()).times(scaledTrans);
		Matrix transform = Matrix.identity(3, 3);
		transform = rotMat.times(transform);
//		transform = transMat.times(transform);
//		transform = scaledTrans.times(transform);
		image.fill(RGBColour.BLACK);
		image.createRenderer().drawShapeFilled(ellipse.transformAffine(transform), RGBColour.RED);
	}
	
}
