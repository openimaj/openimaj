package org.openimaj.text.geo;

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class TestWorldPolygons {

	private WorldPolygons worldPoly;

	@Before
	public void setup() {
		this.worldPoly = new WorldPolygons();
	}

	@Test
	public void testPolys() {
		final Collection<WorldPlace> shapes = this.worldPoly.getShapes();
		System.out.println(shapes.size());

		final WorldPlace afghan = this.worldPoly.byCountryCode("af");
		final WorldPlace belgium = this.worldPoly.byCountryCode("be");

		assertTrue(afghan.calculateArea() > belgium.calculateArea());
	}
}
