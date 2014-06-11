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
package org.openimaj.demos.touchtable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;

public class ReallyBasicTouchTracker {
	private static final long TIME_TO_DIE = 500;
	private long currentId = 0; 
	private List<Touch> lastPoints = new ArrayList<Touch>();
	private double threshold;
	
	public ReallyBasicTouchTracker(double threshold) {
		this.threshold = threshold;
	}
	
	public List<Touch> trackPoints(Collection<Touch> pts) {
		List<Touch> newPoints = new ArrayList<Touch>(pts.size());
		for (Touch pt : pts) {
			Touch matched = matchPoint(pt);
			
			if (matched == null) {
				newPoints.add(new Touch(pt, currentId++, null));
			} else {
				
				lastPoints.remove(matched);
				
				Point2dImpl mv = new Point2dImpl(pt.calculateCentroid());
				mv.x -= matched.getX();
				mv.y -= matched.getY();
				
				newPoints.add(new Touch(pt, matched.touchID, mv));
			}
		}
		for (Touch touch : lastPoints) {
			if(System.currentTimeMillis() - touch.createdTime < TIME_TO_DIE){
				newPoints.add(touch);
			}
		}
		lastPoints = newPoints;
		
		return lastPoints;
	}

	private Touch matchPoint(Circle query) {
		double minDist = Double.MAX_VALUE;
		Touch best = null;
		
		for (Touch pt : lastPoints) {
			double dist = Line2d.distance(query.calculateCentroid(), pt.calculateCentroid());
			
			if (dist < minDist) {
				minDist = dist;
				best = pt;
			}
		}
		
		if (minDist > threshold) 
			return null;
		if(System.currentTimeMillis() - best.createdTime > TIME_TO_DIE){
			best=null;
		}
		return best;
	}
}
