package org.openimaj.demos.video.videosift;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.feature.local.engine.InterestPointImageExtractorProperties;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;

public class FeatureClickListener<S,T extends Image<S,T> & SinglebandImageProcessor.Processable<Float,FImage,T> > implements MouseListener {

	private List<InterestPointKeypoint> points = null;
	private T image;
	private JFrame frame = null;
	private ResizeProcessor r = new ResizeProcessor(100,100);
	
	@Override
	public synchronized void mouseClicked(MouseEvent e) {
		if(this.points == null) return;
		double dist = Double.MAX_VALUE;
		Ellipse foundShape = null;
		InterestPointKeypoint foundPoint = null;
		Point2dImpl clickPoint = new Point2dImpl(e.getPoint().x,e.getPoint().y);
		for(InterestPointKeypoint point : points){
			Ellipse ellipse = EllipseUtilities.ellipseFromSecondMoments(point.x,point.y,point.location.secondMoments, point.scale);
			if(ellipse.isInside(clickPoint)){
//				double pdist = Math.sqrt(clickPoint.x * clickPoint.x + clickPoint.y * clickPoint.y);
				double pdist = point.scale;
				if(pdist < dist){
					foundShape = ellipse;
					foundPoint = point;
					dist = pdist;
				}
			}
		}
		if(foundShape!=null){
//			PolygonExtractionProcessor<S, T> ext = new PolygonExtractionProcessor<S,T>(foundShape, image.newInstance(1, 1).getPixel(0,0));
			FGaussianConvolve blur = new FGaussianConvolve (foundPoint.scale);
			InterestPointImageExtractorProperties<S, T> extract = new InterestPointImageExtractorProperties<S,T>(image.process(blur),foundPoint.location);
			if(frame== null){
				frame = DisplayUtilities.display(extract.image.process(r));
			}
			else{
				frame.dispose();
				frame = DisplayUtilities.display(extract.image.process(r));
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	public List<InterestPointKeypoint> getPoints() {
		return points;
	}

	public synchronized void setImage(List<InterestPointKeypoint> points,T image) {
		this.image = image;
		this.points = points;
	}

	public T getImage() {
		return image;
	}

}
