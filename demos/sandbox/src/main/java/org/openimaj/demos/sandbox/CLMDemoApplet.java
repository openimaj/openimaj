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
package org.openimaj.demos.sandbox;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

import org.openimaj.demos.faces.MultiPuppeteer;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;

public class CLMDemoApplet extends JApplet {
	private static final long serialVersionUID = 1L;

	@Override
	public void init() {
		try {
			this.setSize(640, 480);

			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					try {
						final MultiPuppeteer vs = new MultiPuppeteer();

						final VideoCapture vc = new VideoCapture(640, 480);

						final ImageComponent ic = new ImageComponent(true);
						CLMDemoApplet.this.add(ic);

						final VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay(vc, ic);
						vd.addVideoListener(vs);

						CLMDemoApplet.this.doLayout();
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
