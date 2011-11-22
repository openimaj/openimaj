package org.openimaj.demos.utils.slideshowframework;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.swing.JPanel;

import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.processing.transform.MBFProjectionProcessor;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.xuggle.XuggleVideo;

import Jama.Matrix;

public class VideoSlide implements Slide, VideoDisplayListener<MBFImage> {
	private static final long serialVersionUID = 1L;
	URL url;
	VideoDisplay<MBFImage> display;
	private URL background;
	private PictureSlide pictureSlide;
	private Matrix transform;
	private ImageComponent panel;
	private BufferedImage bimg;
	private MBFImage mbfImage;
	
	public VideoSlide(URL video, URL background, Matrix transform) throws IOException {
		this.url = video;
		this.background = background;
		this.pictureSlide = new PictureSlide(this.background);
		this.transform = transform;
	}

	public VideoSlide(URL video, Matrix transform) throws IOException {
		this.url = video;
		this.transform = transform;
	}
	
	@Override
	public Component getComponent(int width, int height) throws IOException {
		if(this.pictureSlide == null){
			this.mbfImage = new MBFImage(width,height,3);
			panel = (ImageComponent) new PictureSlide(mbfImage).getComponent(width, height);
		}
		else{
			panel = (ImageComponent) pictureSlide.getComponent(width, height);
			this.mbfImage = pictureSlide.mbfImage.clone();
		}
		
		panel.setSize(width, height);
		panel.setPreferredSize(new Dimension(width, height));

		XuggleVideo video = new XuggleVideo(url,true);
		display = VideoDisplay.createOffscreenVideoDisplay(video);
		display.setStopOnVideoEnd(false);
		display.addVideoListener(this);
		
		return panel;
	}

	@Override
	public void close() {
		display.close();
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		MBFImage bgCopy = mbfImage.clone();
		MBFProjectionProcessor proj = new MBFProjectionProcessor();
		proj.setMatrix(transform);
		proj.processImage(frame);
		proj.performProjection(0, 0, bgCopy);
		panel.setImage(bimg = ImageUtilities.createBufferedImageForDisplay( bgCopy, bimg ));
	}
}
