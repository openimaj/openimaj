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
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
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
	
	/**
	 * Default constructor.
	 * @param video
	 * @param background
	 * @param transform
	 * @throws IOException
	 */
	public VideoSlide(URL video, URL background, Matrix transform) throws IOException {
		this.url = video;
		this.background = background;
		this.pictureSlide = new PictureSlide(this.background);
		this.transform = transform;
	}

	/**
	 * Default constructor.
	 * @param video
	 * @param transform
	 * @throws IOException
	 */
	public VideoSlide(URL video, Matrix transform) throws IOException {
		this.url = video;
		this.transform = transform;
	}
	
	/**
	 * Default constructor.
	 * @param video
	 * @param background
	 * @throws IOException
	 */
	public VideoSlide(URL video, URL background) throws IOException {
		this(video, background, null);
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
			proj.accumulate(frame);
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
