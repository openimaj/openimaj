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
package org.openimaj.tools.clusterquantiser;

import java.util.Iterator;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.feature.local.keypoints.Keypoint;

/**
 * An iterator over keypoints
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class KeypointListArrayIterator implements Iterator<FeatureFileFeature>{

	protected Iterator<? extends Keypoint> kpli;

	/**
	 * Construct with list of keypoints
	 * @param kpl
	 */
	public KeypointListArrayIterator(LocalFeatureList<? extends Keypoint> kpl) {
		this.kpli = kpl.iterator();
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return kpli.hasNext();
	}

	@Override
	public FeatureFileFeature next() {
		// TODO Auto-generated method stub
		FeatureFileFeature f = new FeatureFileFeature ();
		Keypoint kp = kpli.next();
		f.data = kp.ivec;
		f.location = String.format("%4.2f %4.2f %4.2f %4.3f",kp.y, kp.x, kp.scale, kp.ori);
		return f;
	}

	@Override
	public void remove() {
		kpli.remove();
	}
}
