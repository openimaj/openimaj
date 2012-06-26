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
package org.openimaj.image.processing.transform;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.resize.ResizeProcessor;

import Jama.Matrix;

/**
 * 
 * Test the projection processor
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ProjectionProcessorTest {

	MBFImage image = null;
	/**
	 * Create an mbf image
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException{
		image = ImageUtilities.readMBF(this.getClass().getResourceAsStream("/org/openimaj/image/data/sinaface.jpg"));
		
	}
	
	/**
	 * See if we can project a single image into the correct location
	 */
	@Test
	public void testSingleImage(){
		double rot = 90 * (Math.PI/180);
		Matrix rotationMatrix = Matrix.constructWithCopy(new double[][]{
				{Math.cos(rot),-Math.sin(rot),0},
				{Math.sin(rot),Math.cos(rot),0},
				{0,0,1},
		});
		
		ProjectionProcessor<Float[],MBFImage> process = new ProjectionProcessor<Float[],MBFImage>();
		process.setMatrix(rotationMatrix);
		image.accumulateWith(process);
		DisplayUtilities.display(process.performProjection().process(new ResizeProcessor(300,300)));
	}
	
	/**
	 * run the test as an app
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{
		ProjectionProcessorTest t = new ProjectionProcessorTest ();
		t.setup();
		t.testSingleImage();
	}
}
