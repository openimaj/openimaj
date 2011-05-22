package org.openimaj.demos;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.interest.HarrisIPD;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class SecondMomentVisualiser extends JFrame implements MouseListener, MouseMotionListener {
	
	public static void main(String args[]) throws IOException{
		SecondMomentVisualiser vis = new SecondMomentVisualiser ();
	}

	private MBFImage image;
	private HarrisIPD ipd;
	private Point2d drawPoint = null;
	private MBFImage toDraw;
	
	public SecondMomentVisualiser () throws IOException{
		image = ImageUtilities.readMBF(
			SecondMomentVisualiser.class.getResourceAsStream("/org/openimaj/image/data/square_rot.png")
		);
		
		ipd = new HarrisIPD();
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image));
		
		
		this.getContentPane().addMouseListener(this);
		this.getContentPane().addMouseMotionListener(this);
		this.setBounds(0, 0, image.getWidth(), image.getHeight());
		this.setVisible(true);
		
		class Updater implements Runnable{
			
			private SecondMomentVisualiser frame;
			Updater(SecondMomentVisualiser frame){
				this.frame =frame;
			}
			@Override
			public void run() {
				while(true)
				{
					frame.draw();
					try {
						Thread.sleep(1000/30);
					} catch (InterruptedException e) {
					}
				}
			}
		}
		toDraw = image.clone();
		Thread t = new Thread(new Updater(this));
		t.start();
		

	}
	
	
	public void draw() {
		
		DisplayUtilities.display(toDraw,this);
	}


	private void drawEBowl(MBFImage toDraw) {
		Matrix secondMoments = ipd.getSecondMomentsAt((int)this.drawPoint.getX(), (int)this.drawPoint.getY());
//		System.out.println(secondMoments.det());
//		secondMoments = secondMoments.times(1/secondMoments.det());
//		System.out.println(secondMoments.det());
		try{
			List<Ellipse> ellipses = getBowlEllipse(secondMoments);
			for(Ellipse ellipse : ellipses){
				toDraw.drawPolygon(ellipse, 1,RGBColour.GREEN);
			}
		}
		catch(Exception e){
			
		}
	}


	private List<Ellipse> getBowlEllipse(Matrix secondMoments) {
		List<Ellipse> ellipses = new ArrayList<Ellipse>();
		for(double E = 0.0001f; E < 0.001; E+=0.0001){
			double d1,d2,r1,r2,r3,r4;
			
			//	If [u v] M [u v]' = E(u,v)
			//	THEN
			//	[u v] (M / E(u,v)) [u v]' = 1
			//	THEN you can go ahead and do the eigen decomp s.t.
			//	(M / E(u,v)) = R' D R
			//	where R is the rotation and D is the size of the ellipse
			
			EigenvalueDecomposition rdr = secondMoments.times(1.0f/E).eig();
			
			d1 = Math.sqrt(rdr.getD().get(0,0));
			d2 = Math.sqrt(rdr.getD().get(1,1));
			Matrix rotation = rdr.getV().transpose();
			r1 = Math.acos(rotation.get(0, 0));
			r2 = Math.asin(-rotation.get(0, 1));
			r3 = Math.asin(rotation.get(1, 0));
			r4 = Math.acos(rotation.get(1, 1));
			
//			System.out.printf("Rotations: %.2f %.2f %.2f %.2f\n",r1,r2,r3,r4);
		
			ellipses.add(Ellipse.ellipseFromEquation(
					this.drawPoint.getX(), // center x
					this.drawPoint.getY(), // center y
					d1, // semi-major axis
					d2, // semi-minor axis
					r1 // rotation
			));
		}
		
		return ellipses;
	}


	private void drawClick(MBFImage toDraw) {
		toDraw.drawPoint(this.drawPoint, RGBColour.RED, 3);
	}


	@Override
	public void mouseClicked(MouseEvent event) {
		drawPoint  = new Point2dImpl(event.getX(),event.getY());
		toDraw = image.clone();
		if(this.drawPoint!=null){
			drawClick(toDraw);
			drawEBowl(toDraw);
		}
	}

	@Override
	public void mouseEntered(MouseEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseMoved(MouseEvent e) {
		drawPoint  = new Point2dImpl(e.getX(),e.getY());
		toDraw = image.clone();
		if(this.drawPoint!=null){
			drawClick(toDraw);
			drawEBowl(toDraw);
		}
	}
}
