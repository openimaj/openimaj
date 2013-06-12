package org.openimaj.vis.world;

import java.util.List;

import org.openimaj.math.geometry.shape.Shape;

/**
 * 	Represents a single place in the world and it's bounds and shape.
 * 	Also includes various metadata about the country.
 *
 * 	@author Sina Samangooei (ss@ecs.soton.ac.uk)
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 */
public class WorldPlace
{
	/** The country's international code (ISOA2) */
	private final String countryCode;

	/** Full name of the country */
	private final String name;

	/** The shape of the country */
	private final List<Shape> geoms;

	/** The latitude position */
	private final float latitude;

	/** The longitude position */
	private final float longitude;

	/**
	 * 	Construct a new world place.
	 *
	 * 	@param name The country's name
	 * 	@param countryCode The country's international code (ISOA2)
	 * 	@param lat The latitude
	 * 	@param lon The longitude
	 * 	@param shape The shape of the country
	 */
	public WorldPlace( final String name, final String countryCode,
			final float lat, final float lon, final List<Shape> shape )
	{
		this.name = name;
		this.countryCode = countryCode;
		this.latitude = lat;
		this.longitude = lon;
		this.geoms = shape;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return String.format( "{%s: (%2.2f,%2.2f), Area: %s}", this.name,
				this.latitude, this.longitude, this.calculateArea() );
	}

	/**
	 *	Calculates the area of the country
	 *	@return The area of the country
	 */
	public double calculateArea()
	{
		double total = 0;
		for( final Shape geom : this.geoms )
			total += geom.calculateArea();
		return total;
	}

	/**
	 * @return the shapes of this place
	 */
	public List<Shape> getShapes()
	{
		return this.geoms;
	}

	/**
	 * @return the ISOA2 country code
	 */
	public String getISOA2()
	{
		return this.countryCode;
	}


	/**
	 *	@return the countryCode
	 */
	public String getCountryCode()
	{
		return this.countryCode;
	}

	/**
	 *	@return the name
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 *	@return the latitude
	 */
	public float getLatitude()
	{
		return this.latitude;
	}

	/**
	 *	@return the longitude
	 */
	public float getLongitude()
	{
		return this.longitude;
	}
}
