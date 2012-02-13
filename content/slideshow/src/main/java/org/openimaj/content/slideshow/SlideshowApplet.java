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
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
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
