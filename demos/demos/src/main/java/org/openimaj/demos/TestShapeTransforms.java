package org.openimaj.demos;

import javax.swing.JFrame;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

public class TestShapeTransforms {
	private static Runnable displayUpdater;
	private static JFrame frame;
	private static double rotation = Math.PI*2/4;
	private static Ellipse ellipse;
	private static MBFImage image;

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
		int dx = 100;
		int dy = 100;
		float x = (float) (Math.cos(-rotation) * dx - Math.sin(-rotation) * dy);
		float y = (float) (Math.sin(-rotation) * dx + Math.cos(-rotation) * dy);
		Matrix rotMat = TransformUtilities.rotationMatrixAboutPoint(rotation, ellipse.getCOG().getX(), ellipse.getCOG().getY());
		Matrix transMat = TransformUtilities.translateMatrix(x, y);
		Matrix scaleMat = TransformUtilities.scaleMatrix(Math.abs(2.0 * Math.cos(rotation)), Math.abs(2.0 * Math.sin(rotation)));
		Matrix scaledTrans = scaleMat.times(TransformUtilities.translateMatrix(-ellipse.getCOG().getX(), -ellipse.getCOG().getY()));
		scaledTrans = TransformUtilities.translateMatrix(ellipse.getCOG().getX(), ellipse.getCOG().getY()).times(scaledTrans);
		Matrix transform = Matrix.identity(3, 3);
//		transform = rotMat.times(transform);
		transform = transMat.times(transform);
		transform = scaledTrans.times(transform);
		image.fill(RGBColour.BLACK);
		image.drawShapeFilled(ellipse.transformAffine(transform), RGBColour.RED);
	}
	
}
