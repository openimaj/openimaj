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
package org.openimaj.image.feature.local.interest;

import org.openimaj.image.FImage;

public class HarrisIPD extends AbstractStructureTensorIPD {
	protected float eigenRatio = 0.04f;
	
	public HarrisIPD(float detectionScale, float integrationScale) {
		this(detectionScale, integrationScale, 0.04f);
	}
	
	public HarrisIPD(float detectionScale, float integrationScale, float eigenRatio) {
		super(detectionScale, integrationScale);
		this.eigenRatio = eigenRatio;
	}
	
	public HarrisIPD() {
		this((0.6f*2.5f)*(0.6f*2.5f), 2.5f*2.5f, 0.01f);
	}

	public HarrisIPD(float f) {
		super(f);
	}

	@Override
	public FImage createInterestPointMap() {
		FImage det = lxmxblur.multiply(lymyblur).subtract(lxmyblur.multiply(lxmyblur));
		FImage trace = lxmxblur.add(lymyblur);
		FImage traceSq = trace.multiply(trace);
		
		return det.subtract(traceSq.multiply(eigenRatio)); 
	}

	@Override
	public HarrisIPD clone() {
		HarrisIPD a = (HarrisIPD) super.clone();
		a.eigenRatio = this.eigenRatio;
		return a;
	}
}
