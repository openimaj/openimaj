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
