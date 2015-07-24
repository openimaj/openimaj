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
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.transform.MBFProjectionProcessor;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplay.EndAction;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.xuggle.XuggleVideo;

import Jama.Matrix;

/**
 * Slide that shows a video. Number keys are bound to seek to different points
 * in the video.
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class VideoSlide implements Slide, VideoDisplayListener<MBFImage>, KeyListener {
	URL url;
	VideoDisplay<MBFImage> display;
	private URL background;
	protected PictureSlide pictureSlide;
	private final Matrix transform;
	private ImageComponent panel;
	private BufferedImage bimg;
	private MBFImage mbfImage;
	private XuggleVideo video;
	private EndAction endAction = EndAction.LOOP;

	/**
	 * Default constructor.
	 *
	 * @param video
	 * @param background
	 * @param transform
	 * @param endAction
	 * @throws IOException
	 */
	public VideoSlide(final URL video, final URL background, final Matrix transform, EndAction endAction)
			throws IOException
	{
		this.url = makeURL(video);
		this.background = background;
		this.pictureSlide = new PictureSlide(this.background);
		this.transform = transform;
		this.endAction = endAction;
	}

	/**
	 * Default constructor.
	 *
	 * @param video
	 * @param transform
	 * @param endAction
	 * @throws IOException
	 */
	public VideoSlide(final URL video, final Matrix transform, EndAction endAction) throws IOException {
		this.url = makeURL(video);
		this.transform = transform;
		this.endAction = endAction;
	}

	private URL makeURL(URL url) throws IOException {
		if (url.getProtocol().startsWith("jar")) {
			final File tmp = File.createTempFile("movie", ".tmp");
			tmp.deleteOnExit();
			FileUtils.copyURLToFile(url, tmp);
			url = tmp.toURI().toURL();
		}
		return url;
	}

	/**
	 * Default constructor.
	 *
	 * @param video
	 * @param background
	 * @param endAction
	 * @throws IOException
	 */
	public VideoSlide(final URL video, final URL background, EndAction endAction) throws IOException {
		this(video, background, null, endAction);
	}

	@Override
	public Component getComponent(final int width, final int height) throws IOException {
		if (this.pictureSlide == null) {
			this.mbfImage = new MBFImage(width, height, 3);
			this.panel = (ImageComponent) new PictureSlide(this.mbfImage).getComponent(width, height);
		}
		else {
			this.panel = (ImageComponent) this.pictureSlide.getComponent(width, height);
			this.mbfImage = this.pictureSlide.mbfImage.clone();
		}

		this.panel.setSize(width, height);
		this.panel.setPreferredSize(new Dimension(width, height));

		this.video = new XuggleVideo(this.url, false);
		this.display = VideoDisplay.createOffscreenVideoDisplay(this.video);
		this.display.setEndAction(this.endAction);

		this.display.addVideoListener(this);

		return this.panel;
	}

	@Override
	public void close() {
		this.display.close();
	}

	@Override
	public void afterUpdate(final VideoDisplay<MBFImage> display) {
		// do nothing
	}

	@Override
	public void beforeUpdate(final MBFImage frame) {
		if (this.transform != null) {
			final MBFImage bgCopy = this.mbfImage.clone();
			final MBFProjectionProcessor proj = new MBFProjectionProcessor();
			proj.setMatrix(this.transform);
			proj.accumulate(frame);
			proj.performProjection(0, 0, bgCopy);
			this.panel.setImage(this.bimg = ImageUtilities.createBufferedImageForDisplay(bgCopy, this.bimg));
		}
		else
			this.panel.setImage(this.bimg = ImageUtilities.createBufferedImageForDisplay(frame, this.bimg));
	}

	@Override
	public void keyPressed(final KeyEvent key) {
		final int code = key.getKeyCode();
		if (code >= KeyEvent.VK_0 && code <= KeyEvent.VK_9) {
			final double prop = (code - KeyEvent.VK_0) / 10.0;
			final long dur = this.video.getDuration();
			this.display.seek((long) (dur * prop));
		}
		if (code == KeyEvent.VK_SPACE) {
			this.display.togglePause();
		}
	}

	@Override
	public void keyReleased(final KeyEvent arg0) {
		// do nothing
	}

	@Override
	public void keyTyped(final KeyEvent arg0) {
		// do nothing
	}
}
