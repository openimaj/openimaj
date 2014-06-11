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
package org.openimaj.image.feature.local.interest.experiment;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.interest.EllipticInterestPointData;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import cern.colt.Arrays;

import Jama.Matrix;

public class RepeatabilityExperiment {
	private IPDRepeatability<EllipticInterestPointData> rep;

	RepeatabilityExperiment(MBFImage img1, MBFImage img2, List<Ellipse> e1, List<Ellipse> e2, Matrix transform, double d){
		this.rep = IPDRepeatability.repeatability(img1, img2, e1, e2, transform, d);
	}
	
	public double[] doExperiment(){
		return new double[]{this.rep.repeatability(0.5)};
	}
	
	public static void main(String args[]){
		int centerx = 200;
		int centery = 200;
		Ellipse e1 = new Ellipse(centerx,centery,30,20,Math.PI/4f);
		Polygon e1Box = e1.calculateOrientedBoundingBox();
		
		MBFImage e1Image = new MBFImage(400,400,ColourSpace.RGB);
		MBFImage e2Image = new MBFImage(400,400,ColourSpace.RGB);
		
		e1Image.drawShape(e1, RGBColour.RED);
		e1Image.drawShape(e1Box, RGBColour.BLUE);
		
		Matrix rot = TransformUtilities.rotationMatrixAboutPoint(Math.PI/2f,centerx,centery);
		Matrix scale = TransformUtilities.scaleMatrixAboutPoint(2.0, 3.0,centerx,centery);
		Matrix move = TransformUtilities.translateMatrix(100,100);
		Matrix transform = Matrix.identity(3,3);
		transform = transform.times(move);
		transform = transform.times(scale);
		transform = transform.times(rot);
		
		Point2d e2Pos = e1.transform(transform).calculateCentroid();
		e2Image.drawPoint(e2Pos, RGBColour.GREEN, 3);
		Matrix mrot = TransformUtilities.rotationMatrixAboutPoint(0.4, (int)(e2Pos.getX()), (int)(e2Pos.getY()));
		Matrix mscale = TransformUtilities.scaleMatrixAboutPoint(1.02, 1.01,(int)e2Pos.getX(),(int)e2Pos.getY());
		Matrix mmove = TransformUtilities.translateMatrix(-2,-2);
		
		Matrix pert = Matrix.identity(3, 3);
		pert = pert.times(mmove);
		pert = pert.times(mrot);
		pert = pert.times(mscale);
		
		e2Image.drawShape(e1Box.transform(pert.times(transform)), RGBColour.BLUE);
		e2Image.drawShape(e1.transform(pert.times(transform)), RGBColour.RED);
		
		e2Image.drawShape(e1Box.transform(transform), RGBColour.GREEN);
		e2Image.drawShape(e1.transform(transform), RGBColour.ORANGE);
		
		Shape e2Untransform = e1.transform(pert.times(transform)).transform(transform.inverse());
		e1Image.drawShape(e2Untransform, RGBColour.GREEN);
		e1Image.drawPoint(e2Untransform.calculateCentroid(), RGBColour.GREEN, 3);
		
		ArrayList<Ellipse> e1List = new ArrayList<Ellipse>();
		e1List.add(e1);
		
		ArrayList<Ellipse> e2List = new ArrayList<Ellipse>();
		e2List.add(e1.transformAffine(pert.times(transform)));
		
		RepeatabilityExperiment exp = new RepeatabilityExperiment(e1Image,e2Image,e1List,e2List,transform,1);
		System.out.println(Arrays.toString(exp.doExperiment()));
		DisplayUtilities.display(e1Image, "Untransformed image");
		JFrame w2 = DisplayUtilities.display(e2Image, "Transformed image");
		w2.setBounds(400, 0, 400, 400);
		
	}
}
