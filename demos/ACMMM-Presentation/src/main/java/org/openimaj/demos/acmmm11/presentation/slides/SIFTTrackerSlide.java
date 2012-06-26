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
package org.openimaj.demos.acmmm11.presentation.slides;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.JPanel;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.demos.video.VideoSIFT;


/**
 * Slide showing SIFT tracking
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class SIFTTrackerSlide implements Slide, KeyListener {
	VideoSIFT vs;
	
	@Override
	public Component getComponent(int width, int height) throws IOException {
		JPanel c = new JPanel();
		c.setOpaque(false);
		c.setPreferredSize(new Dimension(width, height));
		c.setLayout(new GridBagLayout());
		
		try {
			vs = new VideoSIFT(c);
			
			for (Component cc : c.getComponents()) {
				if (cc instanceof JPanel) {
					((JPanel)cc).setOpaque( false );
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return c;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (vs != null) vs.keyTyped(e);
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (vs != null) vs.keyPressed(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (vs != null) vs.keyReleased(e);
	}

	@Override
	public void close() {
		if (vs != null) vs.stop();
	}

}
