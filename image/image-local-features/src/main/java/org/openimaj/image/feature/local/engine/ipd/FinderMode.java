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
package org.openimaj.image.feature.local.engine.ipd;

import org.openimaj.image.feature.local.detector.ipd.finder.CharacteristicOctaveInterestPointFinder;

import org.openimaj.image.feature.local.detector.ipd.finder.LoggingOctaveInterestPointFinder;
import org.openimaj.image.feature.local.detector.ipd.finder.OctaveInterestPointFinder;
import org.openimaj.image.feature.local.interest.IPDSelectionMode;
import org.openimaj.image.feature.local.interest.InterestPointData;
import org.openimaj.image.feature.local.interest.InterestPointDetector;

/**
 * The type of finder to use
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public interface FinderMode<T extends InterestPointData> {
	/**
	 * Given a detector and the selection pmode
	 * @param detector
	 * @param selectionMode
	 * @return the finder instance
	 */
	public OctaveInterestPointFinder<T> finder(InterestPointDetector<T> detector, IPDSelectionMode selectionMode);
	/**
	 * An {@link OctaveInterestPointFinder} is considered the most basic.
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 * @param <T>
	 */
	static class Basic<T extends InterestPointData> implements FinderMode<T>{
		@Override
		public OctaveInterestPointFinder<T> finder(InterestPointDetector<T> detector,IPDSelectionMode selectionMode) {
			return new OctaveInterestPointFinder<T>(detector,selectionMode);
		}
	}
	/**
	 * A logging logs as well as finding points. Mainly used to debug
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 * @param <T>
	 */
	static class Logging<T extends InterestPointData> implements FinderMode<T>{
		@Override
		public OctaveInterestPointFinder<T> finder(InterestPointDetector<T> detector,IPDSelectionMode selectionMode) {
			return new LoggingOctaveInterestPointFinder<T>(detector,selectionMode);
		}
	}
	/**
	 * The characteristic finder throws away ellipses that are basically the same, keeping the strongest one.
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 * @param <T>
	 */
	static class Characteristic<T extends InterestPointData> implements FinderMode<T>{
		private CharacteristicOctaveInterestPointFinder<T> settings;

		public Characteristic(){
			settings = new CharacteristicOctaveInterestPointFinder<T>(null,null);
		}
		
		public Characteristic(int maxDistance, double maxRotation, double maxAxisRatio){
			settings = new CharacteristicOctaveInterestPointFinder<T>(null,null);
			settings.maxDistance = maxDistance;
			settings.maxRotation = maxRotation;
			settings.maxAxisRatio = maxAxisRatio;
		}
		
		public Characteristic(int maxDistance){
			settings = new CharacteristicOctaveInterestPointFinder<T>(null,null);
			settings.maxDistance = maxDistance;
		}
		
		@Override
		public OctaveInterestPointFinder<T> finder(InterestPointDetector<T> detector,IPDSelectionMode selectionMode) {
			CharacteristicOctaveInterestPointFinder<T> n = new CharacteristicOctaveInterestPointFinder<T>(detector,selectionMode);
			n.maxDistance = settings.maxDistance;
			n.maxRotation = settings.maxRotation;
			n.maxAxisRatio = settings.maxAxisRatio;
			return n;
		}
	}
}