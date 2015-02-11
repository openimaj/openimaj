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
package org.openimaj.image.processing.face.detection.keypoints;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;


/**
 * Tests for {@link FacialKeypoint}
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FacialKeypointTest {
static float FLOAT_EPS = 0.01f;
	
	FKEFaceDetector engine;
	FImage noface;
	FImage face;
	List<KEDetectedFace> k1;
	List<KEDetectedFace> k2;

	/**
	 * Setup
	 * @throws Exception
	 */
	@Before public void setup() throws Exception {
		engine = new FKEFaceDetector();
		noface = ImageUtilities.readF(this.getClass().getResourceAsStream("/org/openimaj/image/data/cat.jpg"));
		face = ImageUtilities.readF(this.getClass().getResourceAsStream("/org/openimaj/image/data/face/ss.jpg"));
		
		k1 = engine.detectFaces(noface);
		k2 = engine.detectFaces(face);	
	}
	
	/**
	 * Test for no faces
	 */
	@Test public void testNoFaces(){
		assertTrue(k1.size() == 0);
	}
	
	
//	@Test public void testIO() throws IOException {
//		File ascii = null;
//		File binary = null;
//		
//		try {
//			ascii = File.createTempFile("facetest", "ascii");
//			binary = File.createTempFile("facetest", "bin");
//			
//			IOUtils.writeASCII(ascii, k2);
//			IOUtils.writeBinary(binary, k2);
//			
//			//test ascii read
//			List<FacialDescriptor> asciiKeys = MemoryLocalFeatureList.read(ascii, FacialDescriptor.class);
//			List<FacialDescriptor> asciiKeys2 = k2;
//			
//			assertTrue(asciiKeys.size() == 1);
//			assertTrue(asciiKeys.size() == asciiKeys2.size());
//			
//			FacialDescriptor fpk1 = asciiKeys.get(0);
//			FacialDescriptor fpk2 = asciiKeys2.get(0);
//			
//			assertEquals(fpk1.featureLength,fpk2.featureLength);
//			assertEquals(fpk1.featureRadius,fpk2.featureRadius);
//			assertEquals(fpk1.featureVector.length,fpk2.featureVector.length);
//			assertEquals(fpk1.nFeatures,fpk2.nFeatures);
//			for(int i = 0; i < fpk1.featureVector[i]; i++) assertTrue(fpk1.featureVector[i] == fpk2.featureVector[i]);
//			
//			//test binary read
//			List<FacialDescriptor> binKeys = MemoryLocalFeatureList.read(binary, FacialDescriptor.class);
//			assertTrue(asciiKeys.size() == binKeys.size());
//			fpk2 = binKeys.get(0);
//			assertEquals(fpk1.featureLength,fpk2.featureLength);
//			assertEquals(fpk1.featureRadius,fpk2.featureRadius);
//			assertEquals(fpk1.featureVector.length,fpk2.featureVector.length);
//			assertEquals(fpk1.nFeatures,fpk2.nFeatures);
//			for(int i = 0; i < fpk1.featureVector[i]; i++) assertTrue(fpk1.featureVector[i] == fpk2.featureVector[i]);
//		} finally {
//			if (ascii != null) ascii.delete();
//			if (binary != null) binary.delete();
//		}
//	}
}
