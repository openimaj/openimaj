package org.openimaj.image.contour;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestMooreBorderFollowing {
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testSimple() throws Exception {
		float[][] pixels = new float[][]{
			new float[]{0,0,0,0,0,0},
			new float[]{0,0,1,1,1,0},
			new float[]{0,1,0,0,1,0},
			new float[]{0,0,1,1,1,0},
			new float[]{0,0,0,0,0,0}
		};
		
		FImage img = new FImage(pixels);
		
		MooreNeighborStrategy strat = new MooreNeighborStrategy();
		Pixel start = new Pixel(1,2);
		Pixel from  = new Pixel(1,3);
		List<Pixel> border = strat.border(img, start, from);
		assertTrue(border.size() == img.sum());
		System.out.println(border);
		from  = new Pixel(2,2);
		border = strat.border(img, start, from);
		assertTrue(border.size() == img.sum()-2);
		System.out.println(border);
		
	}
	/**
	 * @throws Exception
	 */
	@Test
	public void testOpenLoop() throws Exception {
		float[][] pixels = new float[][]{
			new float[]{0,0,0,0,0,0,0,0},
			new float[]{0,0,0,0,0,0,0,0},
			new float[]{0,0,0,0,0,1,1,0},
			new float[]{0,0,0,1,0,0,1,0},
			new float[]{0,0,0,1,0,1,1,0},
			new float[]{0,0,0,0,1,1,0,0},
			new float[]{0,0,0,0,0,0,0,0}
		};
		
		FImage img = new FImage(pixels);
		
		MooreNeighborStrategy strat = new MooreNeighborStrategy();
		Pixel start = new Pixel(3,4);
		Pixel from  = new Pixel(3,5);
		List<Pixel> border = strat.border(img, start, from);
		System.out.println(border);
		
	}

}
