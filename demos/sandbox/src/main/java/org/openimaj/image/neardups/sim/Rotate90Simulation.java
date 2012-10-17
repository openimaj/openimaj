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
package org.openimaj.image.neardups.sim;

import org.openimaj.image.MBFImage;

/**
 * Simulate rotations by 90,180,270,0 degrees
 * 
 * @author jsh2
 *
 */
public class Rotate90Simulation extends Simulation {
	public Rotate90Simulation(int seed) {
		super(seed);
	}

	@Override
	public MBFImage applySimulation(MBFImage input) {
		int angle = random.nextInt(4);
		MBFImage output = null;
		
		switch (angle) {
		case 0:
			output = input.clone();
			break;
		case 1:
			output = new MBFImage(input.getHeight(), input.getWidth(), input.numBands());
			for (int j=0; j<output.getHeight(); j++) {
				for (int i=0; i<output.getWidth(); i++) {
					output.setPixel(i, j, input.getPixel(j, i));
				}
			}
			break;
		case 2:
			output = new MBFImage(input.getWidth(), input.getHeight(), input.numBands());
			for (int j=0; j<output.getHeight(); j++) {
				for (int i=0; i<output.getWidth(); i++) {
					output.setPixel(i, j, input.getPixel(i, input.getHeight() - j - 1));
				}
			}
			break;
		case 3:
			output = new MBFImage(input.getHeight(), input.getWidth(), input.numBands());
			for (int j=0; j<output.getHeight(); j++) {
				for (int i=0; i<output.getWidth(); i++) {
					output.setPixel(i, j, input.getPixel(input.getWidth() - j - 1, i));
				}
			}
			break;
		}
		
		return output;
	}

}
