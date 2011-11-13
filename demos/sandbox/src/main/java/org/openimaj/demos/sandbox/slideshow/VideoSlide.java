package org.openimaj.demos.sandbox.slideshow;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.net.URL;

import javax.swing.JPanel;

import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.xuggle.XuggleVideo;

public class VideoSlide implements Slide {
	private static final long serialVersionUID = 1L;
	URL url;
	VideoDisplay<MBFImage> display;
	
	public VideoSlide(URL picture) throws IOException {
		this.url = picture;
	}

	@Override
	public Component getComponent(int width, int height) throws IOException {
		JPanel panel = new JPanel();
		
		panel.setSize(width, height);
		panel.setPreferredSize(new Dimension(width, height));

		XuggleVideo video = new XuggleVideo(url);
		display = VideoDisplay.createVideoDisplay(video, panel);
		
		return panel;
	}

	@Override
	public void close() {
		((XuggleVideo)display.getVideo()).close();
		display.close();
	}
}
