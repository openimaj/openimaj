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
package org.openimaj.demos.video.utils;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 * Listen for numeric key presses and seek the video
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class NumberKeySeekListener implements KeyListener {

	private final VideoDisplay<MBFImage> display;
	private final long duration;

	/**
	 * Construct with the given display
	 * @param videoDisplay
	 */
	public NumberKeySeekListener(final VideoDisplay<MBFImage> videoDisplay) {
		this.display = videoDisplay;
		if(!(this.display.getVideo() instanceof XuggleVideo)){
			throw new UnsupportedOperationException("You can only seek in xuggle videos");
		}
		final XuggleVideo v = (XuggleVideo) this.display.getVideo();
		this.duration = v.getDuration();
//		System.out.println("Video duration is: " + duration);
	}

	@Override
	public void keyPressed(final KeyEvent keyEvent) {
		if(keyEvent.getKeyCode() >= KeyEvent.VK_0 && keyEvent.getKeyCode() <= KeyEvent.VK_9 ){
			final float number = keyEvent.getKeyCode() - KeyEvent.VK_0;
			final double toSeek =  (this.duration * (number/10.f));
//			System.out.println(this.display.getVideo().getFPS());
//			System.out.println("Seeking to: " + toSeek);
			this.display.seek((long)toSeek);
		}
	}

	@Override
	public void keyReleased(final KeyEvent keyEvent) {}

	@Override
	public void keyTyped(final KeyEvent keyEvent) {this.keyPressed(keyEvent);}

}
