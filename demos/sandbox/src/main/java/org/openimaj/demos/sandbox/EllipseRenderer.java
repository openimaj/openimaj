package org.openimaj.demos.sandbox;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;

import Jama.Matrix;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class EllipseRenderer {
	
	public static void main(String[] args) {
		drawEllipse(1,0.3,0.5);
		
		JFrame sliders = new JFrame();
		sliders.setSize(200, 200);
		JSlider as = new JSlider();
		JSlider ds = new JSlider();
		JSlider cs = new JSlider();
		sliders.getContentPane().add(new JLabel("a"));
		sliders.getContentPane().add(as);
		sliders.getContentPane().add(new JLabel("d"));
		sliders.getContentPane().add(ds);
		sliders.getContentPane().add(new JLabel("c"));
		sliders.getContentPane().add(cs);
		sliders.setVisible(true);
	}

	private static void drawEllipse(int a, double d, double c) {
		Matrix sm = new Matrix(new double[][]{
//			{1f,1},
//			{1,1f},
			{a,c},
			{c,d},
		});
		
		Ellipse e = EllipseUtilities.ellipseFromCovariance(200, 200, sm, 50f);
		Matrix emat = EllipseUtilities.ellipseToCovariance(e);
		
		
		MBFImage img = new MBFImage(600,600, ColourSpace.RGB);
		
		img.drawShape(e, 3,RGBColour.RED);
		
		DisplayUtilities.displayName(img,"ellipses");
	}

}
