package org.openimaj.demos.sandbox.asm.landmark;

import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.TemplateMatcher;
import org.openimaj.image.analysis.algorithm.TemplateMatcher.TemplateMatcherMode;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.ObjectFloatPair;

public class BlockLandmarkModel implements LandmarkModel<FImage> {
	public static class Factory implements LandmarkModelFactory<FImage> {
		@Override
		public LandmarkModel<FImage> createLandmarkModel() {
			return new BlockLandmarkModel();
		}

		@Override
		public LandmarkModel<FImage> createLandmarkModel(float scaleFactor) {
			return new BlockLandmarkModel();
		}
	}
	
	
	int blockSize = 11;
	int searchSize = 31;
	
	FImage average;
	int n = 0;
	TemplateMatcher matcher;
	
	protected Rectangle getROI(int x, int y, int w, int h) {
		if(w % 2 == 0 ) w+=1;
		if(h % 2 == 0 ) h+=1;
		
		int roiX = Math.max(0,x-(int)(w/2.0));
		int roiY = Math.max(0,y-(int)(h/2.0));
		
		int newWidth = (int)(w / 2.0) + 1 + (x - roiX);
		int newHeight = (int)(h / 2.0) + 1 + (y - roiY);
		
		return new Rectangle(roiX, roiY, newWidth, newHeight);
	}
	
	protected FImage extractBlock(FImage image, Point2d pt, int sz) {
		return image.extractROI(getROI((int)pt.getX(), (int)pt.getY(), sz, sz));
	}

	@Override
	public void updateModel(FImage image, Point2d point, PointList pointList) {
		FImage extracted = extractBlock(image, point, blockSize);
		
		n++;
		if (average == null) {
			average = extracted; 
		} else {
			average.addInline( extracted.subtractInline(average).divide((float)n) );
		}
		matcher = null;
	}

	@Override
	public float computeCost(FImage image, Point2d point, PointList pointList) {
		FImage extracted = extractBlock(image, point, blockSize);
		
		if (matcher == null)
			matcher = new TemplateMatcher(average, TemplateMatcherMode.NORM_SUM_SQUARED_DIFFERENCE);
		
		matcher.setSearchBounds(null);
		extracted.analyseWith(matcher);
		
		return matcher.getResponseMap().pixels[0][0];
	}

	@Override
	public ObjectFloatPair<Point2d> updatePosition(FImage image, Point2d initial, PointList pointList) {
		Rectangle roi = getROI((int)initial.getX(), (int)initial.getY(), searchSize, searchSize);
		
		if (matcher == null)
			matcher = new TemplateMatcher(average, TemplateMatcherMode.NORM_SUM_SQUARED_DIFFERENCE);
		
		matcher.setSearchBounds(roi);
		image.analyseWith(matcher);
		FValuePixel p = matcher.getBestResponses(1)[0];
		
		return new ObjectFloatPair<Point2d>(p, 0);
	}

}
