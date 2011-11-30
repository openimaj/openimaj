package org.openimaj.content.slideshow;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.transform.MBFProjectionProcessor;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.xuggle.XuggleVideo;

import Jama.Matrix;

/**
 * Slide that shows a video. Number keys are bound to seek to
 * different points in the video.
 * 
 * @author Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class VideoSlide implements Slide, VideoDisplayListener<MBFImage>, KeyListener {
	private static final long serialVersionUID = 1L;
	URL url;
	VideoDisplay<MBFImage> display;
	private URL background;
	private PictureSlide pictureSlide;
	private Matrix transform;
	private ImageComponent panel;
	private BufferedImage bimg;
	private MBFImage mbfImage;
	private XuggleVideo video;
	
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
	
	public VideoSlide(URL video, URL background) throws IOException {
		this(video,background,null);
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

		video = new XuggleVideo(url,true);
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
		//do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		if(transform != null){
			MBFImage bgCopy = mbfImage.clone();
			MBFProjectionProcessor proj = new MBFProjectionProcessor();
			proj.setMatrix(transform);
			proj.processImage(frame);
			proj.performProjection(0, 0, bgCopy);
			panel.setImage(bimg = ImageUtilities.createBufferedImageForDisplay( bgCopy, bimg ));
		}
		else
			panel.setImage(bimg = ImageUtilities.createBufferedImageForDisplay( frame, bimg ));
	}

	@Override
	public void keyPressed(KeyEvent key) {
		int code = key.getKeyCode();
		if(code >= KeyEvent.VK_0 && code <= KeyEvent.VK_9){
			double prop = (code - KeyEvent.VK_0)/10.0;
			long dur = this.video.getDuration();
			this.display.seek(dur * prop);
		}
		if(code == KeyEvent.VK_SPACE){
			this.display.togglePause();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		//do nothing
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		//do nothing
	}
}
