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

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.lang.reflect.Method;

import javax.swing.JFrame;

import org.apache.commons.lang.SystemUtils;

/**
 * Utility class for dealing with fullscreen Swing applications.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FullscreenUtility {
	protected JFrame window;
	protected boolean fullscreen = false;

	/**
	 * Construct with the given JFrame. The utility will allow the frame to be
	 * toggled between windowed and fullscreen mode.
	 *
	 * @param frame
	 *            The frame.
	 */
	public FullscreenUtility(JFrame frame) {
		this.window = frame;

		if (SystemUtils.IS_OS_MAC_OSX) {
			// if we're on a mac, we'll add the fullscreen hint to the window so
			// the standard controls work
			try {
				final Class<?> util = Class.forName("com.apple.eawt.FullScreenUtilities");
				final Class<?> params[] = new Class[] { Window.class, Boolean.TYPE };
				final Method method = util.getMethod("setWindowCanFullScreen", params);

				method.invoke(util, window, true);
			} catch (final Exception e) {
				// ignore
			}
		}
	}

	/**
	 * Method allows changing whether this window is displayed in fullscreen or
	 * windowed mode.
	 *
	 * @param fullscreen
	 *            true = change to fullscreen, false = change to windowed
	 */
	public void setFullscreen(boolean fullscreen) {
		if (this.fullscreen != fullscreen) {
			if (SystemUtils.IS_OS_MAC_OSX) {
				setFullscreenOSX(fullscreen);
			} else {
				setFullscreenAWT(fullscreen);
			}
		}
	}

	// See https://bugs.openjdk.java.net/browse/JDK-8013547
	// basically AWT fullscreen has big problems (with comboboxes and
	// keylisteners) on OSX with Java 7 & 8 (and maybe later versions of 6).
	// We'll try and use the EAWT fullscreen stuff if possible and fall back if
	// not...
	private void setFullscreenOSX(boolean fullscreen) {
		// change modes
		this.fullscreen = fullscreen;

		try {
			final Class<?> appClz = Class.forName("com.apple.eawt.Application");
			final Method getApp = appClz.getMethod("getApplication");
			final Object app = getApp.invoke(appClz);
			final Class<?> params[] = new Class[] { Window.class };
			final Method reqFS = appClz.getMethod("requestToggleFullScreen", params);
			reqFS.invoke(app, window);
		} catch (final ClassNotFoundException e1) {
		} catch (final Exception e) {
			// revert mode change
			this.fullscreen = !fullscreen;
			setFullscreenAWT(fullscreen);
		}
	}

	private void setFullscreenAWT(boolean fullscreen)
	{
		// get a reference to the device.
		final GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

		// change modes.
		this.fullscreen = fullscreen;
		// toggle fullscreen mode
		if (!fullscreen)
		{
			// hide the frame so we can change it.
			window.setVisible(false);
			// remove the frame from being displayable.
			window.dispose();
			// put the borders back on the frame.
			window.setUndecorated(false);
			// needed to unset this window as the fullscreen window.
			device.setFullScreenWindow(null);
			// recenter window
			window.setLocationRelativeTo(null);
			window.setResizable(true);

			// reset the display mode to what it was before
			// we changed it.
			window.setVisible(true);
		}
		else
		{ // change to fullscreen.
			// hide everything
			window.setVisible(false);
			// remove the frame from being displayable.
			window.dispose();
			// remove borders around the frame
			window.setUndecorated(true);
			// attempt to change the screen resolution.
			window.setResizable(false);
			window.setAlwaysOnTop(false);
			// make the window fullscreen.
			device.setFullScreenWindow(window);

			if (SystemUtils.IS_JAVA_1_7 && SystemUtils.IS_OS_MAC_OSX) {
				System.err.println("Applying first responder fix");
				// OSX first responder bug:
				// http://mail.openjdk.java.net/pipermail/macosx-port-dev/2012-November/005109.html
				// unfortunately this might not be a complete fix...
				window.setVisible(false);
			}
			// show the frame
			window.setVisible(true);
		}
		// make sure that the screen is refreshed.
		window.repaint();
	}
}
