package org.openimaj.demos;

import javax.swing.JFrame;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.interest.HarrisIPD;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

public class AnimatedInterestPointVisualiser {
	private Rectangle rectangle;
	private Point2dImpl point;
	private Matrix transform;
	private MBFImage image;
	private int derivscale;
	private HarrisIPD ipd;
	private Ellipse ellipseToDraw;
	private JFrame jframe;
	private float rotation;

	public AnimatedInterestPointVisualiser(){
		this.rectangle = new Rectangle(100,100,200,200);
		this.point = new Point2dImpl(110,110);
		this.rotation = 0f;
		this.transform = TransformUtilities.rotationMatrixAboutPoint(this.rotation , 200, 200);
		derivscale = 1;
		ipd = new HarrisIPD((float)derivscale,3);
		this.image = new MBFImage(400,400,ColourSpace.RGB);
		this.jframe = DisplayUtilities.display(this.image);
		drawShape();
		updateEllipse();
		
		
		class Updater implements Runnable{
			
			private AnimatedInterestPointVisualiser frame;
			Updater(AnimatedInterestPointVisualiser frame){
				this.frame =frame;
			}
			@Override
			public void run() {
				while(true)
				{
					frame.drawShape();
					frame.updateEllipse();
					frame.drawFrame();
					frame.updateTransform();
					try {
						Thread.sleep(1000/30);
					} catch (InterruptedException e) {
					}
				}
			}
		}
		Thread t = new Thread(new Updater(this));
		t.start();
	}

	public void updateTransform() {
		this.rotation+=Math.PI/100f;
		this.transform = TransformUtilities.rotationMatrixAboutPoint(this.rotation , 200, 200);
	}

	public void drawFrame() {
		this.image.drawShape(this.ellipseToDraw, RGBColour.RED);
		DisplayUtilities.display(this.image,this.jframe);
	}

	private void updateEllipse() {
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(this.image));
		Point2dImpl np = this.point.transform(transform);
		Matrix sm = ipd.getSecondMomentsAt((int)np.x,(int)np.y);
		ellipseToDraw = EllipseUtilities.ellipseFromSecondMoments(np.x,np.y, sm, 5*2);
	}

	public void drawShape() {
		this.image.fill(RGBColour.WHITE);
		this.image.drawShapeFilled(this.rectangle.transform(transform),  RGBColour.BLACK);
		this.image = image.process(new FGaussianConvolve(5));
		this.image.drawPoint(this.point.transform(transform), RGBColour.RED, 1);
	}
	
	public static void main(String args[]){
		new AnimatedInterestPointVisualiser();
	}
}
