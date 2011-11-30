package org.openimaj.content.slideshow;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import org.openimaj.image.DisplayUtilities.ScalingImageComponent;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

/**
 * A slide that displays a picture, scaled to the size of the
 * slide.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class PictureSlide implements Slide {
	private static final long serialVersionUID = 1L;
	
	protected URL url;
	protected ScalingImageComponent ic;
	protected MBFImage mbfImage;

	/**
	 * Create a picture slide
	 * @param picture the url of the picture 
	 * @throws IOException
	 */
	public PictureSlide(URL picture) throws IOException {
		this.url = picture;
		this.mbfImage = ImageUtilities.readMBF(this.url);
	}

	/**
	 * Create a picture slide
	 * @param mbfImage the picture
	 */
	public PictureSlide(MBFImage mbfImage) {
		this.mbfImage = mbfImage;
	}

	@Override
	public Component getComponent(int width, int height) throws IOException {
		BufferedImage image = ImageUtilities.createBufferedImageForDisplay( mbfImage, null);
		
		ic = new ScalingImageComponent();
		ic.setImage(image);
		ic.setSize(width, height);
		ic.setPreferredSize(new Dimension(width, height));
		
		return ic;
	}

	@Override
	public void close() {
		ic = null;
	}
}
