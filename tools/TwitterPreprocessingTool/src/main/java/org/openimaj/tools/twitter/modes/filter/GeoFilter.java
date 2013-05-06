/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.tools.twitter.modes.filter;

import java.util.ArrayList;
import java.util.List;
import org.kohsuke.args4j.Option;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.twitter.USMFStatus;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class GeoFilter extends TwitterPreprocessingPredicate {
	
	@Option(name="--bounding-box", aliases="-bb", required=false, usage="The bounding box to filter against.", metaVar="STRING")
	String boundBox = "51.28,-0.489,51.686,0.236";
	private Rectangle location = null;
	
	public GeoFilter(){
		
	}
	
	@Override
	public boolean test(USMFStatus twitterStatus) {
		if(twitterStatus.geo == null){
			return false;
		}
		
		
		//if(geomap == null) geomap = (Map<String, Object>) twitterStatus.coordinates;
//		"geo":{"type":"Point","coordinates":[51.55047862,-0.29938507]}
		List<Double> geolist = new ArrayList<Double>();
		geolist.add(twitterStatus.geo[0]);
		geolist.add(twitterStatus.geo[1]);
		Point2d pos = new Point2dImpl(geolist.get(1).floatValue(),geolist.get(0).floatValue());
		return location.isInside(pos);
	}
	
	@Override
	public void validate() {
		getLocation();
	}
	
	private Rectangle getLocation() {
		if(this.location == null){
			double minLon,minLat,maxLon,maxLat;
			String[] parts = boundBox.split(",");
			minLat = Double.parseDouble(parts[0]);
			minLon = Double.parseDouble(parts[1]);
			maxLat = Double.parseDouble(parts[2]);
			maxLon = Double.parseDouble(parts[3]);
			
			Point2d minp = new Point2dImpl((float)minLon,(float)minLat);
			Point2d maxp = new Point2dImpl((float)maxLon,(float)maxLat);
			this.location  = new Rectangle(minp,maxp);
		}
		return location;
	}
}
