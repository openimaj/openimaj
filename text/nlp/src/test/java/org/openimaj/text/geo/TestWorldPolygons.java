package org.openimaj.text.geo;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.math.geometry.shape.Shape;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestWorldPolygons {
	
	private WorldPolygons worldPoly;

	@Before
	public void setup(){
		this.worldPoly = new WorldPolygons();
	}
	
	@Test
	public void testPolys(){
		Collection<WorldPlace> shapes = this.worldPoly.getShapes();
		System.out.println(shapes.size());
		
		WorldPlace afghan = this.worldPoly.byCountryCode("af");
		WorldPlace belgium = this.worldPoly.byCountryCode("be");
		
		assertTrue(afghan.calculateArea() > belgium.calculateArea());
	}
}
