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
package org.openimaj.image.analysis.colour;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests for CIE DeltaE
 * 
 * see http://www.ece.rochester.edu/~gsharma/ciede2000/ for details
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class CIEDE2000Test {
	private static final double[][] testData = {
		{50.0000,2.6772,-79.7751,50.0000,0.0000,-82.7485,2.0425},
		{50.0000,3.1571,-77.2803,50.0000,0.0000,-82.7485,2.8615},
		{50.0000,2.8361,-74.0200,50.0000,0.0000,-82.7485,3.4412},
		{50.0000,-1.3802,-84.2814,50.0000,0.0000,-82.7485,1.0000},
		{50.0000,-1.1848,-84.8006,50.0000,0.0000,-82.7485,1.0000},
		{50.0000,-0.9009,-85.5211,50.0000,0.0000,-82.7485,1.0000},
		{50.0000,0.0000,0.0000,50.0000,-1.0000,2.0000,2.3669},
		{50.0000,-1.0000,2.0000,50.0000,0.0000,0.0000,2.3669},
		{50.0000,2.4900,-0.0010,50.0000,-2.4900,0.0009,7.1792},
		{50.0000,2.4900,-0.0010,50.0000,-2.4900,0.0010,7.1792},
		{50.0000,2.4900,-0.0010,50.0000,-2.4900,0.0011,7.2195},
		{50.0000,2.4900,-0.0010,50.0000,-2.4900,0.0012,7.2195},
		{50.0000,-0.0010,2.4900,50.0000,0.0009,-2.4900,4.8045},
		{50.0000,-0.0010,2.4900,50.0000,0.0010,-2.4900,4.8045},
		{50.0000,-0.0010,2.4900,50.0000,0.0011,-2.4900,4.7461},
		{50.0000,2.5000,0.0000,50.0000,0.0000,-2.5000,4.3065},
		{50.0000,2.5000,0.0000,73.0000,25.0000,-18.0000,27.1492},
		{50.0000,2.5000,0.0000,61.0000,-5.0000,29.0000,22.8977},
		{50.0000,2.5000,0.0000,56.0000,-27.0000,-3.0000,31.9030},
		{50.0000,2.5000,0.0000,58.0000,24.0000,15.0000,19.4535},
		{50.0000,2.5000,0.0000,50.0000,3.1736,0.5854,1.0000},
		{50.0000,2.5000,0.0000,50.0000,3.2972,0.0000,1.0000},
		{50.0000,2.5000,0.0000,50.0000,1.8634,0.5757,1.0000},
		{50.0000,2.5000,0.0000,50.0000,3.2592,0.3350,1.0000},
		{60.2574,-34.0099,36.2677,60.4626,-34.1751,39.4387,1.2644},
		{63.0109,-31.0961,-5.8663,62.8187,-29.7946,-4.0864,1.2630},
		{61.2901,3.7196,-5.3901,61.4292,2.2480,-4.9620,1.8731},
		{35.0831,-44.1164,3.7933,35.0232,-40.0716,1.5901,1.8645},
		{22.7233,20.0904,-46.6940,23.0331,14.9730,-42.5619,2.0373},
		{36.4612,47.8580,18.3852,36.2715,50.5065,21.2231,1.4146},
		{90.8027,-2.0831,1.4410,91.1528,-1.6435,0.0447,1.4441},
		{90.9257,-0.5406,-0.9208,88.6381,-0.8985,-0.7239,1.5381},
		{6.7747,-0.2908,-2.4247,5.8714,-0.0985,-2.2286,0.6377},
		{2.0776,0.0795,-1.1350,0.9033,-0.0636,-0.5514,0.9082}
	};
	
	/**
	 * forward test
	 */
	@Test
	public void test() {
		for (double[] data : testData) {
			double L1 = data[0];
			double a1 = data[1];
			double b1 = data[2];
			double L2 = data[3];
			double a2 = data[4];
			double b2 = data[5];
			double de = data[6];
			
			double cde = CIEDE2000.calculateDeltaE(L1, a1, b1, L2, a2, b2);
			
			assertEquals(de, cde, 5e-5);
		}
	}

	/**
	 * reverse test
	 */
	@Test
	public void testReverse() {
		for (double[] data : testData) {
			double L1 = data[3];
			double a1 = data[4];
			double b1 = data[5];
			double L2 = data[0];
			double a2 = data[1];
			double b2 = data[2];
			double de = data[6];
			
			double cde = CIEDE2000.calculateDeltaE(L1, a1, b1, L2, a2, b2);
			
			assertEquals(de, cde, 5e-5);
		}
	}
}
