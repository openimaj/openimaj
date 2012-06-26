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
package org.openimaj.image.feature.local.detector.ipd.finder;

import java.util.TreeSet;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.feature.local.interest.IPDSelectionMode;
import org.openimaj.image.feature.local.interest.InterestPointData;
import org.openimaj.image.feature.local.interest.InterestPointDetector;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;
import org.openimaj.math.geometry.shape.Ellipse;

/**
 * A characteristic octave interest point finder throws {@link InterestPointData} away if two instances are similar. 
 * Similarity is defined by the position, rotation and axis ratio of the two interest points.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class CharacteristicOctaveInterestPointFinder<T extends InterestPointData> extends OctaveInterestPointFinder<T> {

	private static final double DEFAULT_MAX_DISTANCE = 4;
	private static final double DEFAULT_MAX_ROTATION = (Math.PI / 180.0) * 15.0;
	private static final double DEFAULT_MAX_AXIS_RATIO = 0.1;
	/**
	 * The maximum distance before two keypoints are considered "similar"
	 */
	public double maxDistance = DEFAULT_MAX_DISTANCE;
	/**
	 * The maximum rotation difference before two keypoints are considered "similar"
	 */
	public double maxRotation = DEFAULT_MAX_ROTATION;
	/**
	 * The maximum axis ratio difference before two keypoints are considered similar
	 */
	public double maxAxisRatio = DEFAULT_MAX_AXIS_RATIO;

	/**
	 * construct this finder with the detector and selection mode
	 * @param detector
	 * @param selectionMode
	 */
	public CharacteristicOctaveInterestPointFinder(InterestPointDetector<T> detector, IPDSelectionMode selectionMode) {
		super(detector, selectionMode);
	}
	
	@Override
	public void finish() {
		LocalFeatureList<InterestPointKeypoint<T>> locatedFeatures = this.listener.getFeatures();
		TreeSet<Integer> toRemove = new TreeSet<Integer>();
		for (int i = 0; i < locatedFeatures.size(); i++) {
			InterestPointKeypoint<T> kp1 = locatedFeatures.get(i);
			for (int j = i+1; j < locatedFeatures.size(); j++) {
				InterestPointKeypoint<T> kp2 = locatedFeatures.get(j);
				if(similarTo(kp1,kp2)){
					if(kp1.location.score >= kp2.location.score){
						toRemove.add(j);
					}
					else{
						toRemove.add(i);
					}
				}
			}	
		}
		int nRemove = 0;
		for(int index : toRemove){
			locatedFeatures.remove(index - nRemove++);
		}
	}

	private boolean similarTo(InterestPointKeypoint<T> kp1,InterestPointKeypoint<T> kp2) {
		boolean similar = true;
		// Similar position
		similar = Math.sqrt(Math.pow(kp1.x -kp2.x,2) +  Math.pow(kp1.y -kp2.y,2)) < maxDistance ; 
		if(!similar) return false;
		Ellipse e1 = kp1.location.getEllipse();
		Ellipse e2 = kp2.location.getEllipse();
		// Ellipse with a similar rotation
		similar = Math.abs(e1.getRotation() - e2.getRotation()) < maxRotation; 
		if(!similar) return false;
		
		// Similar semi-major and semi-minor axis ratio
		similar = Math.abs((e1.getMinor()/e1.getMajor()) - (e2.getMinor()/e2.getMajor())) < maxAxisRatio ;
		if(!similar) return false;
		
		return true;
	}

}
