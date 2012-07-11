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
package org.openimaj.image.processing.face.feature.comparison;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.face.feature.ltp.LtpDtFeature;

/**
 * A comparator for Local Trinary Pattern Features using a
 * Euclidean distance transform.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Tan, Xiaoyang", "Triggs, Bill" },
		title = "Enhanced local texture feature sets for face recognition under difficult lighting conditions",
		year = "2010",
		journal = "Trans. Img. Proc.",
		pages = { "1635", "1650" },
		url = "http://dx.doi.org/10.1109/TIP.2010.2042645",
		month = "June",
		number = "6",
		publisher = "IEEE Press",
		volume = "19"
	)
public class LtpDtFeatureComparator implements FacialFeatureComparator<LtpDtFeature> {

	@Override
	public double compare(LtpDtFeature query, LtpDtFeature target) {
		List<List<Pixel>> slicePixels = query.ltpPixels;
		float distance = 0;
		
		FImage[] distanceMaps = target.getDistanceMaps();
		
		for (int i=0; i<distanceMaps.length; i++) {
			List<Pixel> pixels = slicePixels.get(i);
			
			if (distanceMaps[i] == null || pixels == null)
				continue;
			
			for (Pixel p : pixels) {
				distance += distanceMaps[i].pixels[p.y][p.x];
			}
		}
		
		return distance;
	}

	@Override
	public boolean isDistance() {
		return true;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		//do nothing
	}

	@Override
	public byte[] binaryHeader() {
		//do nothing
		return null;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		//do nothing
	}
	
	@Override
	public String toString() {
		return "LtpDtFeatureComparator";
	}
}
