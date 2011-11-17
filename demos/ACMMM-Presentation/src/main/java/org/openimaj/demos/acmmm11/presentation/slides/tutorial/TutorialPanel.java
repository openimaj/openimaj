package org.openimaj.demos.acmmm11.presentation.slides.tutorial;

import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.transform.MBFProjectionProcessor;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import Jama.Matrix;

public abstract class TutorialPanel extends JPanel implements VideoDisplayListener<MBFImage>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2105054613577879944L;
	private MBFImage toDraw;
	private BufferedImage bimg;
	private ImageComponent comp;
	private Matrix scaleMat;
	
	public TutorialPanel(String name, Video<MBFImage> capture, int width, int height){
		this.setBorder( BorderFactory.createTitledBorder( name ) );
		this.comp = new ImageComponent(true);
		this.add(comp);
		toDraw = new MBFImage(width,height,3);
		
		float scaleW = (float)width / (float)capture.getWidth();
		float scaleH = (float)height / (float)capture.getHeight();
		
		this.scaleMat = TransformUtilities.scaleMatrix(scaleW, scaleH);
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
//		MBFProjectionProcessor pp = new MBFProjectionProcessor();
//		pp.setMatrix(scaleMat);
//		pp.processImage(frame);
//		pp.performProjection(0, 0, toDraw);
		toDraw.internalCopy(frame);
		doTutorial(toDraw);
		this.comp.setImage(bimg = ImageUtilities.createBufferedImageForDisplay( toDraw, bimg ));
	}

	public abstract void doTutorial(MBFImage toDraw);
}
