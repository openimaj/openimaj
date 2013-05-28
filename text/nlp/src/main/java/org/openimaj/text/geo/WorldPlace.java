package org.openimaj.text.geo;

import java.util.List;

import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Shape;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class WorldPlace {

	private String countryCode;
	private String name;
	private List<Shape> geoms;
	private float lat;
	private float lon;

	/**
	 * @param name
	 * @param desc
	 * @param lat
	 * @param lon
	 * @param shape
	 */
	public WorldPlace(String name, String countryCode, float lat, float lon, List<Shape> shape) {
		this.name = name;
		this.countryCode = countryCode;
		this.lat = lat;
		this.lon = lon;
		this.geoms = shape;
	}
	
	public String toString(){
		return String.format("{%s: (%2.2f,%2.2f), Area: %s}",name, lat, lon, calculateArea());
	}

	public double calculateArea() {
		double total = 0;
		for (Shape geom : this.geoms) {
			total += geom.calculateArea();
		}
		return total;
	}

	/**
	 * @return the shapes of this place
	 */
	public List<Shape> getShapes() {
		return this.geoms;
	}

	/**
	 * @return the ISOA2 country code
	 */
	public String getISOA2() {
		return this.countryCode;
	}

}
