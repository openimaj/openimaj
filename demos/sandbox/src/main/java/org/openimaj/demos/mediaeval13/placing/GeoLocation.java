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
package org.openimaj.demos.mediaeval13.placing;

/**
 * A geolocation consisting of a latitude and longitude
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class GeoLocation {
	public static final double EARTH_RADIUS_KM = 6371;// In kilometers

	/**
	 * The latitude
	 */
	public double latitude;

	/**
	 * The longitude
	 */
	public double longitude;

	public GeoLocation(double lat, double lng) {
		this.latitude = lat;
		this.longitude = lng;
	}

	/**
	 * Compute the haversine distance between two points on the Earth's surface
	 * in kilometers.
	 * 
	 * @param actual
	 * @return
	 */
	public double haversine(GeoLocation actual) {
		return haversine(this.latitude, this.longitude, actual.latitude, actual.longitude);
	}

	static double haversine(double lat1, double lon1, double lat2, double lon2) {
		final double dLat = Math.toRadians(lat2 - lat1);
		final double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2)
				* Math.cos(lat1) * Math.cos(lat2);
		final double c = 2 * Math.asin(Math.sqrt(a));
		return EARTH_RADIUS_KM * c;
	}

	@Override
	public String toString() {
		return latitude + " " + longitude;
	}
}
