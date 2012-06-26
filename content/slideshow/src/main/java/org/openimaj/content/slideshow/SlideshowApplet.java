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
import java.util.List;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * A slideshow implementation that can be run as an applet.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class SlideshowApplet extends Slideshow {
	protected FullscreenUtility fsutil;
	
	/**
	 * Default constructor.
	 * @param applet The applet class to attach to.
	 * @param slides The slides to display.
	 * @param slideWidth The slide width.
	 * @param slideHeight The slide height.
	 * @param background The background image.
	 * @throws IOException
	 */
	public SlideshowApplet(JApplet applet, List<Slide> slides, int slideWidth, int slideHeight, BufferedImage background) throws IOException {
		super(applet, slides, slideWidth, slideHeight, background);
	}

	@Override
	protected void pack() {
		((JApplet)container).doLayout();
	}

	@Override
	public void setFullscreen(final boolean fullscreen) {
		boolean currentlyFullscreen = isFullscreen();
		
		if (currentlyFullscreen != fullscreen) {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					if (!fullscreen) { 
						fsutil.window.setVisible(false);
						((JApplet)container).setContentPane(fsutil.window.getContentPane());
						fsutil.window.dispose();
						fsutil.window = null;
						fsutil = null;
					} else {
						fsutil = new FullscreenUtility(new JFrame());
						fsutil.window.setContentPane(((JApplet)container).getContentPane());
						fsutil.window.addKeyListener(SlideshowApplet.this);
						fsutil.window.pack();
						fsutil.window.setVisible(true);
						fsutil.setFullscreen(true);
					}
				}
			});
		}
	}

	@Override
	protected boolean isFullscreen() {
		return fsutil == null ? false : fsutil.fullscreen;
	}
}
