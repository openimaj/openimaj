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
package org.openimaj.demos.video.utils;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.feature.local.engine.ipd.InterestPointImageExtractorProperties;
import org.openimaj.image.feature.local.interest.InterestPointData;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;

/**
 * Click on features and draw them
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <S>
 * @param <T>
 */
public class FeatureClickListener<S,T extends Image<S,T> & SinglebandImageProcessor.Processable<Float,FImage,T> > implements MouseListener {

	private LocalFeatureList<InterestPointKeypoint<InterestPointData>> points = null;
	private T image;
	private JFrame frame = null;
	private ResizeProcessor r = new ResizeProcessor(100,100);
	
	@Override
	public synchronized void mouseClicked(MouseEvent e) {
		if(this.points == null) return;
		double dist = Double.MAX_VALUE;
		Circle foundShape = null;
		InterestPointKeypoint<InterestPointData> foundPoint = null;
		Point2dImpl clickPoint = new Point2dImpl(e.getPoint().x,e.getPoint().y);
		for(InterestPointKeypoint<InterestPointData> point : points){
			Circle ellipse = new Circle(point.location.x, point.location.y, point.scale);
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


	/**
	 * @return the clicked on
	 */
	public LocalFeatureList<InterestPointKeypoint<InterestPointData>> getPoints() {
		return points;
	}

	/**
	 * the image and keypoints to draw
	 * @param kpl
	 * @param image
	 */
	public synchronized void setImage(LocalFeatureList<InterestPointKeypoint<InterestPointData>> kpl,T image) {
		this.image = image;
		this.points = kpl;
	}

	/**
	 * @return the underlying image being clicked on
	 */
	public T getImage() {
		return image;
	}

}
