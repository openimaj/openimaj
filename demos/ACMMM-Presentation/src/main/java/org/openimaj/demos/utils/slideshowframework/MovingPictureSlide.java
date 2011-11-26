package org.openimaj.demos.utils.slideshowframework;

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
	int vx;
	int vy;
	int width;
	int height;
	
	public MovingPictureSlide(URL picture) throws IOException {
		super(picture);
	}

	@Override
	public Component getComponent(int width, int height) throws IOException {
		this.width = width;
		this.height = height;
		
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(null);
		
		panel.setSize(width, height);
		panel.setPreferredSize(new Dimension(width, height));

		ic = new ImageComponent(ImageIO.read(url));
		panel.add(ic);
		ic.setLocation(100, 100);
		panel.validate();
		
		vx = (int) (Math.random()*4) - 2;
		vy = (int) (Math.random()*4) - 2;
		
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
			int newx = oldL.x + vx;
			int newy = oldL.y + vy;
			
			if (newx < 0) { 
				vx*=-1;
				newx += vx;
			}
			
			if (newy < 0) { 
				vy*=-1;
				newx += vy;
			}
			
			if (newx >= width - ic.getWidth()) { 
				vx*=-1;
				newx += vx;
			}
			
			if (newy >= height - ic.getHeight()) { 
				vy*=-1;
				newx += vy;
			}
			
			ic.setLocation(newx, newy);
			
			try {
				Thread.sleep(1000/60);
			} catch (InterruptedException e) {}
		}
	}
}
