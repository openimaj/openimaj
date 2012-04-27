package org.openimaj.tools.twitter.modes.filter;

import java.util.List;
import java.util.Map;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.kohsuke.args4j.Option;
import org.openimaj.twitter.TwitterStatus;

/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class GeoFilter extends TwitterPreprocessingFilter {
	
	@Option(name="--bounding-box", aliases="-bb", required=false, usage="The bounding box to filter against.", metaVar="STRING")
	String boundBox = "51.28,-0.489,51.686,0.236";
	private Envelope2D location = null;
	
	public GeoFilter(){
		
	}
	
	@Override
	public boolean filter(TwitterStatus twitterStatus) {
		if(twitterStatus.geo == null){
			return false;
		}
		location = getLocation();
		
		Map<String,Object> geomap = (Map<String, Object>) twitterStatus.geo;
//		"geo":{"type":"Point","coordinates":[51.55047862,-0.29938507]}
		List<Double> geolist = (List<Double>) geomap.get("coordinates");
		DirectPosition2D pos = new DirectPosition2D(geolist.get(0),geolist.get(1));
		return location.contains(pos);
	}

	private Envelope2D getLocation() {
		if(this.location == null){
			double minLon,minLat,maxLon,maxLat;
			String[] parts = boundBox.split(",");
			minLat = Double.parseDouble(parts[0]);
			minLon = Double.parseDouble(parts[1]);
			maxLat = Double.parseDouble(parts[2]);
			maxLon = Double.parseDouble(parts[3]);
			
			DirectPosition2D minp = new DirectPosition2D(minLat,minLon);
			DirectPosition2D maxp = new DirectPosition2D(maxLat,maxLon);
			this.location  = new Envelope2D(minp,maxp);
		}
		return location;
	}
}
