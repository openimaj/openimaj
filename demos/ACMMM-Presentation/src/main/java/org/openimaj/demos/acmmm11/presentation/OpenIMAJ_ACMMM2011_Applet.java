package org.openimaj.demos.acmmm11.presentation;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JApplet;

import org.openimaj.content.slideshow.SlideshowApplet;

public class OpenIMAJ_ACMMM2011_Applet extends JApplet {
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see java.applet.Applet#init()
	 */
	@Override
	public void init() {
		try {
			new SlideshowApplet(this, OpenIMAJ_ACMMM2011.getSlides(), 1024, 768, ImageIO.read(OpenIMAJ_ACMMM2011_Applet.class.getResourceAsStream("background.png")));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
