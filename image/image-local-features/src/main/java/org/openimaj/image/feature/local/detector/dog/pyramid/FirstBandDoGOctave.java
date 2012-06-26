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
package org.openimaj.image.feature.local.detector.dog.pyramid;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.pyramid.OctaveProcessor;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramid;


/**
 * A {@link FirstBandDoGOctave} works like a {@link DoGOctave},
 * but with an {@link MBFImage}, however, only the first band of 
 * the {@link MBFImage} is used to build the DoG pyramid.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FirstBandDoGOctave 
	extends
		GaussianOctave<FImage> implements OctaveProcessor<GaussianOctave<MBFImage>, MBFImage> 
{ 
	/**
	 * Construct a Difference of Gaussian octave with the provided parent Pyramid
	 * and octaveSize. The octaveSize parameter is the size of 
	 * the octave's images compared to the original image used
	 * to construct the pyramid. An octaveSize of 1 means the 
	 * same size as the original, 2 means half size, 4 means 
	 * quarter size, etc.
	 * 
	 * @param parent the pyramid that this octave belongs to
	 * @param octSize the size of the octave relative to
	 * 			the original image.
	 */
	public FirstBandDoGOctave(GaussianPyramid<FImage> parent, float octSize) {
		super(parent, octSize);
	}
	
	@Override
	public void process(GaussianOctave<MBFImage> octave) {
		images = new FImage[options.getScales() + options.getExtraScaleSteps()];
		
		//compute DoG by subtracting adjacent levels 
		for (int i = 0; i < images.length; i++) {
			images[i] = octave.images[i].bands.get(0).clone();
			images[i].subtractInplace(octave.images[i + 1].bands.get(0));
		}
	}
}
