package org.openimaj.demos.acmmm11.presentation;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.openimaj.image.DisplayUtilities.ImageComponent;

public class MovingPictureSlide extends PictureSlide implements Slide, Runnable {
	private static final long serialVersionUID = 1L;
	ImageComponent ic;
	volatile boolean stop;
	
	public MovingPictureSlide(URL picture) throws IOException {
		super(picture);
	}

	@Override
	public Component getComponent(int width, int height) throws IOException {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		
		panel.setSize(width, height);
		panel.setPreferredSize(new Dimension(width, height));

		ic = new ImageComponent(ImageIO.read(url));
		panel.add(ic);
		ic.setLocation(100, 100);
		panel.validate();
		
		stop = false;
		new Thread(this).start();
		
		return panel;
	}

	@Override
	public void close() {
		stop = true;
	}

	@Override
	public void run() {
		while (!stop) {
			Point oldL = ic.getLocation();
			int newx = oldL.x + 1;
			int newy = oldL.y + 1;
			
			ic.setLocation(newx, newy);
			
			try {
				Thread.sleep(1000/30);
			} catch (InterruptedException e) {}
		}
	}
}
