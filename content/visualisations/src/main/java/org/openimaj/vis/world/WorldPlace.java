/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
