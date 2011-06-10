package org.openimaj.demos;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.transform.NonLinearWarp;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.shape.Triangle;
import org.openimaj.util.pair.Pair;

public class NonLinearWarpDemo implements MouseMotionListener {
	private JFrame frame;
	private MBFImage img;

	public NonLinearWarpDemo() throws IOException {
		img = ImageUtilities.readMBF(new File("/Users/jsh2/IMG_4469.JPG"));
		frame = DisplayUtilities.display(img);
		frame.addMouseMotionListener(this);
	}

	protected void updateImage(Pixel newCentre) {
		Pixel p1 = new Pixel(0, 0);
		Pixel p2 = new Pixel(img.getWidth(), 0);
		Pixel p3 = new Pixel(img.getWidth(), img.getHeight());
		Pixel p4 = new Pixel(0, img.getHeight());
		Pixel p5 = new Pixel(img.getWidth()/2, img.getHeight()/2);

		Pixel np1 = new Pixel(0, 0);
		Pixel np2 = new Pixel(img.getWidth(), 0);
		Pixel np3 = new Pixel(img.getWidth(), img.getHeight());
		Pixel np4 = new Pixel(0, img.getHeight());
		Pixel np5 = newCentre;

		List<Pair<Shape>> matchingRegions = new ArrayList<Pair<Shape>>();
		matchingRegions.add( new Pair<Shape>(new Triangle(p1, p2, p5).asPolygon(), new Triangle(np1, np2, np5).asPolygon()) );
		matchingRegions.add( new Pair<Shape>(new Triangle(p2, p3, p5).asPolygon(), new Triangle(np2, np3, np5).asPolygon()) );
		matchingRegions.add( new Pair<Shape>(new Triangle(p3, p4, p5).asPolygon(), new Triangle(np3, np4, np5).asPolygon()) );
		matchingRegions.add( new Pair<Shape>(new Triangle(p4, p1, p5).asPolygon(), new Triangle(np4, np1, np5).asPolygon()) );

		DisplayUtilities.display(img.process(new NonLinearWarp<Float[], MBFImage>(matchingRegions)), frame);
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		Pixel p = new Pixel(e.getX(), e.getY());
		updateImage(p);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		
	}
	
	public static void main(String [] args) throws IOException {
		new NonLinearWarpDemo();
	}
}
