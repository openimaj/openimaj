package org.openimaj.demos.sandbox.slideshow;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.openimaj.image.DisplayUtilities.ScalingImageComponent;

public class PictureSlide implements Slide {
	private static final long serialVersionUID = 1L;
	URL url;

	public PictureSlide(URL picture) throws IOException {
		this.url = picture;
	}

	@Override
	public Component getComponent(int width, int height) throws IOException {
		ScalingImageComponent ic = new ScalingImageComponent();
		BufferedImage image = ImageIO.read(url);
		ic.setImage(image);
		ic.setSize(width, height);
		ic.setPreferredSize(new Dimension(width, height));
		return ic;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
}
