package org.openimaj.demos.utils.slideshowframework;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.openimaj.image.DisplayUtilities.ScalingImageComponent;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

public class PictureSlide implements Slide {
	private static final long serialVersionUID = 1L;
	protected URL url;
	public ScalingImageComponent ic;
	MBFImage mbfImage;

	public PictureSlide(URL picture) throws IOException {
		this.url = picture;
		this.mbfImage = ImageUtilities.readMBF(this.url);
	}

	public PictureSlide(MBFImage mbfImage) {
		this.mbfImage = mbfImage;
	}

	@Override
	public Component getComponent(int width, int height) throws IOException {
		ic = new ScalingImageComponent();
//		BufferedImage image = ImageIO.read(url);
		BufferedImage image = ImageUtilities.createBufferedImageForDisplay( mbfImage, null);
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
