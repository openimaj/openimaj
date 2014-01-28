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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

/**
 * A slideshow that can be used in a Java Swing application. The slideshow is
 * created in a new window (JFrame).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class SlideshowApplication extends Slideshow {
	protected FullscreenUtility fsutil;

	/**
	 * Default constructor.
	 * 
	 * @param slides
	 *            The slides to display.
	 * @param slideWidth
	 *            The slide width.
	 * @param slideHeight
	 *            The slide height.
	 * @param background
	 *            The background image.
	 * @throws IOException
	 */
	public SlideshowApplication(List<Slide> slides, int slideWidth, int slideHeight, BufferedImage background)
			throws IOException
	{
		super(new JFrame(), slides, slideWidth, slideHeight, background);
		fsutil = new FullscreenUtility((JFrame) container);
	}

	/**
	 * Default constructor.
	 * 
	 * @param slide
	 *            The slide to display.
	 * @param slideWidth
	 *            The slide width.
	 * @param slideHeight
	 *            The slide height.
	 * @param background
	 *            The background image.
	 * @throws IOException
	 */
	public SlideshowApplication(Slide slide, int slideWidth, int slideHeight, BufferedImage background)
			throws IOException
	{
		super(new JFrame(), createList(slide), slideWidth, slideHeight, background);
		fsutil = new FullscreenUtility((JFrame) container);
	}

	/**
	 * Default constructor.
	 * 
	 * @param slide
	 *            The slide to display.
	 * @param slideWidth
	 *            The slide width.
	 * @param slideHeight
	 *            The slide height.
	 * @throws IOException
	 */
	public SlideshowApplication(Slide slide, int slideWidth, int slideHeight)
			throws IOException
	{
		super(new JFrame(), createList(slide), slideWidth, slideHeight, null);
		fsutil = new FullscreenUtility((JFrame) container);
	}

	private static List<Slide> createList(Slide... slides) {
		final List<Slide> slidesl = new ArrayList<Slide>();

		for (final Slide s : slides)
			slidesl.add(s);

		return slidesl;
	}

	@Override
	protected void pack() {
		((JFrame) container).pack();
	}

	@Override
	public void setFullscreen(boolean fullscreen) {
		fsutil.setFullscreen(fullscreen);
	}

	@Override
	protected boolean isFullscreen() {
		return fsutil.fullscreen;
	}
}
