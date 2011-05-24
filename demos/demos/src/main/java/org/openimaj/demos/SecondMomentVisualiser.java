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
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.Pair;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class SecondMomentVisualiser implements MouseListener, MouseMotionListener {
	
	public static void main(String args[]) throws IOException{
		SecondMomentVisualiser vis = new SecondMomentVisualiser ();
	}

	private MBFImage image;
	private HarrisIPD ipd;
	private Point2d drawPoint = null;
	private double derivscale;
	private JFrame projectFrame;
	private ResizeProcessor resizeProject;
	private List<Ellipse> ellipses;
	private List<Pair<Line2d>> lines;
	private Matrix transformMatrix;
	private JFrame mouseFrame;
	private int windowSize;
	
	public SecondMomentVisualiser () throws IOException{
		image = ImageUtilities.readMBF(
			SecondMomentVisualiser.class.getResourceAsStream("/org/openimaj/image/data/square_rot.png")
		);
		derivscale = 5;
		ipd = new HarrisIPD((float)derivscale,15);
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image));
		
		
		
		
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
		image = image.process(new FGaussianConvolve(5));
		
		this.mouseFrame = DisplayUtilities.display(image.clone());
		this.mouseFrame.getContentPane().addMouseListener(this);
		this.mouseFrame.getContentPane().addMouseMotionListener(this);
		
		projectFrame = DisplayUtilities.display(image.clone());
		projectFrame.setBounds(image.getWidth(),0 , image.getWidth(), image.getHeight());
		ellipses = new ArrayList<Ellipse>();
		lines = new ArrayList<Pair<Line2d>>();
		resizeProject = new ResizeProcessor(256,256);
		Thread t = new Thread(new Updater(this));
		t.start();
		
		
		

	}
	
	
	public synchronized void draw() {
			MBFImage toDraw = image.clone(); 
			if(this.drawPoint!=null)
				toDraw.drawPoint(this.drawPoint, RGBColour.RED, 3);
			
			for(Ellipse ellipse : ellipses){
				toDraw.drawPolygon(ellipse, 1,RGBColour.GREEN);
			}
			for(Pair<Line2d> line : lines){
				toDraw.drawLine(line.firstObject(),3, RGBColour.BLUE);
				toDraw.drawLine(line.secondObject(),3, RGBColour.RED);
			}
			if(this.transformMatrix!=null){
				try{
					
					ProjectionProcessor<Float[],MBFImage> pp = new ProjectionProcessor<Float[],MBFImage>();
					pp.setMatrix(this.transformMatrix);
					this.image.process(pp);
					MBFImage patch = pp.performProjection((int)-windowSize,(int)windowSize,(int)-windowSize,(int)windowSize,RGBColour.RED).process(this.resizeProject);
					DisplayUtilities.display(patch,this.projectFrame);
				}
				catch(Exception e){
					e.printStackTrace();
				}
				
			}
			
			
			DisplayUtilities.display(toDraw.clone(),this.mouseFrame);
	}


	private synchronized  void setEBowl() {
		Matrix secondMoments = ipd.getSecondMomentsAt((int)this.drawPoint.getX(), (int)this.drawPoint.getY());
//		System.out.println(secondMoments.det());
//		secondMoments = secondMoments.times(1/secondMoments.det());
//		System.out.println(secondMoments.det());
		this.ellipses.clear();
		this.lines.clear();
		try{
			getBowlEllipse(secondMoments);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	
	private void getBowlEllipse(Matrix secondMoments) {
		double rotation = 0;
		double d1=0,d2=0;
			if(secondMoments.det() == 0)return;
			//	If [u v] M [u v]' = E(u,v)
			//	THEN
			//	[u v] (M / E(u,v)) [u v]' = 1
			//	THEN you can go ahead and do the eigen decomp s.t.
			//	(M / E(u,v)) = R' D R
			//	where R is the rotation and D is the size of the ellipse
//			double divFactor = 1/E;
			Matrix noblur = new Matrix(new double[][]{
					{ipd.lxmx.getPixel((int)this.drawPoint.getX(), (int)this.drawPoint.getY()),ipd.lxmy.getPixel((int)this.drawPoint.getX(), (int)this.drawPoint.getY())},
					{ipd.lxmy.getPixel((int)this.drawPoint.getX(), (int)this.drawPoint.getY()),ipd.lxmx.getPixel((int)this.drawPoint.getX(), (int)this.drawPoint.getY())}
			});
			System.out.println("NO BLUR SECOND MOMENTS MATRIX");
			noblur.print(5, 5);
			System.out.println("det is: " + noblur.det());
			
			double divFactor = 1/Math.sqrt(secondMoments.det());
			double scaleFctor = 4 * derivscale;
			EigenvalueDecomposition rdr = secondMoments.times(divFactor).eig();
			secondMoments.times(divFactor).print(5, 5);
			
			System.out.println("D1(before)= " + rdr.getD().get(0,0));
			System.out.println("D2(before) = " + rdr.getD().get(1,1));
			
			if(rdr.getD().get(0,0) == 0)
				d1 = 0;
			else
				d1 = 1.0/Math.sqrt(rdr.getD().get(0,0));
//				d1 = Math.sqrt(rdr.getD().get(0,0));
			if(rdr.getD().get(1,1) == 0)
				d2 = 0;
			else
				d2 = 1.0/Math.sqrt(rdr.getD().get(1,1));
//				d2 = Math.sqrt(rdr.getD().get(1,1));
			
			double scaleCorrectedD1 = d1 * scaleFctor;
			double scaleCorrectedD2 = d2 *scaleFctor;
			
			Matrix eigenMatrix = rdr.getV();
			System.out.println("D1 = " + d1);
			System.out.println("D2 = " + d2);
			eigenMatrix.print(5, 5);
			
			rotation = Math.atan2(eigenMatrix.get(0,1),eigenMatrix.get(1,1));
			if(d1!=0 && d2!=0){
				Matrix translate = TransformUtilities.translateMatrix(-this.drawPoint.getX(),-this.drawPoint.getY());
//				Matrix rotate = TransformUtilities.rotationMatrix(rotation);
				Matrix scale = TransformUtilities.scaleMatrix(d2, d1).inverse();
				this.transformMatrix = translate.times(scale);
				this.windowSize = (int) (scaleFctor * d1/d2);
				if(this.windowSize > 256) this.windowSize = 256;
//				this.transformMatrix = eigenMatrix.times(new Matrix(new double[][]{{d1,0},{0,d2}}));
//				this.transformMatrix = this.transformMatrix.inverse();
//				this.transformMatrix = new Matrix(new double[][]{
//					{eigenMatrix.get(0, 0),eigenMatrix.get(0, 1),-this.drawPoint.getX()},
//					{eigenMatrix.get(1, 0),eigenMatrix.get(1, 1),-this.drawPoint.getY()},
//					{0,0,1},
//				});
				for(double d : transformMatrix.getRowPackedCopy()) 
					if(d==Double.NaN){
						this.transformMatrix = null;
						break;
					}
			}
			else{
				transformMatrix = null;
			}
			if(transformMatrix!=null){
				System.out.println("Transform matrix:");
				transformMatrix.print(5, 5);
			}
			ellipses.add(Ellipse.ellipseFromEquation(
					this.drawPoint.getX(), // center x
					this.drawPoint.getY(), // center y
					scaleCorrectedD1, // semi-major axis
					scaleCorrectedD2, // semi-minor axis
					rotation// rotation
			));
			
			Line2d major = Line2d.lineFromRotation((int)this.drawPoint.getX(), (int)this.drawPoint.getY(), (float)rotation- Math.PI, (int)scaleCorrectedD1);
			Line2d minor = Line2d.lineFromRotation((int)this.drawPoint.getX(), (int)this.drawPoint.getY(), (float)(rotation- Math.PI/2 ), (int)scaleCorrectedD2);
			lines.add(new Pair<Line2d>(major,minor));
	}




	@Override
	public void mouseClicked(MouseEvent event) {
		drawPoint  = new Point2dImpl(event.getX(),event.getY());
		if(this.drawPoint!=null){
			setEBowl();
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
		if(this.drawPoint!=null){
			setEBowl();
		}
	}
}
