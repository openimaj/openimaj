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
