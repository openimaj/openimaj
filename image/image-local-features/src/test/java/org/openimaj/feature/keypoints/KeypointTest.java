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
package org.openimaj.feature.keypoints;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.KeypointVisualizer;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.io.IOUtils;

/**
 * Test the keypoints generated using a keypoint engine
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class KeypointTest {
	/**
	 * Temp folder
	 */
	@Rule
    public TemporaryFolder folder = new TemporaryFolder();
	
	static float FLOAT_EPS = 0.01f;
	
	DoGSIFTEngine engine;
	FImage im1;
	FImage im2;
	LocalFeatureList<Keypoint> k1;
	LocalFeatureList<Keypoint> k2;


	
	/**
	 * Load a couple images, extract their keypoints
	 * 
	 * @throws IOException
	 */
	@Before public void setup() throws IOException {
		engine = new DoGSIFTEngine();
		im1 = ImageUtilities.readF(this.getClass().getResourceAsStream("/org/openimaj/image/data/sinaface.jpg"));
		im2 = ImageUtilities.readF(this.getClass().getResourceAsStream("/org/openimaj/image/data/cat.jpg"));
		
		k1 = engine.findFeatures(im1);
		k2 = engine.findFeatures(im2);
		
		
	}
	
	private void displayCatKeys() throws IOException{
		FImage cat = ImageUtilities.readF(this.getClass().getResourceAsStream("/org/openimaj/image/data/cat.jpg"));
		cat = ResizeProcessor.doubleSize(cat);
		LocalFeatureList<Keypoint> catKeys = engine.findFeatures(cat);
		double minScale = 2.5;
		double minCol = cat.width/2.0;
		double minRow = cat.height/2.0;
		double maxCol = cat.width/2.0 + 40;
		double maxRow = cat.height/2.0 + 40;
		Keypoint selectedK = null;
		for(Keypoint k : catKeys){
			selectedK = k;
			if(k.scale>minScale && k.x > minCol &&  k.y > minRow && k.y < maxRow && k.x < maxCol ){
				catKeys = new MemoryLocalFeatureList<Keypoint>();
//				k.ori = (float) (k.ori - Math.PI/2.0);
				catKeys.add(k);
				break;
			}
		}
		
		FImage catInv = cat.clone().inverse();
		LocalFeatureList<Keypoint> catInvKeys = engine.findFeatures(catInv);
		for(Keypoint k : catInvKeys){
			selectedK = k;
			if(k.scale>minScale && k.x > minCol &&  k.y > minRow && k.y < maxRow && k.x < maxCol ){
				catInvKeys = new MemoryLocalFeatureList<Keypoint>();
//				k.ori = (float) (k.ori - Math.PI/2.0);
				catInvKeys.add(k);
				break;
			}
		}
		
		KeypointVisualizer<Float[],MBFImage> kpv = new KeypointVisualizer<Float[],MBFImage>(new MBFImage(cat,cat,cat), catKeys);
		MBFImage left = kpv.drawPatches(new Float[]{1.0f,0.0f,0.0f}, new Float[]{0.0f,1.0f,0.0f});
		left = left.extractCenter((int)selectedK.x, (int)selectedK.y, (int)(selectedK.scale * 3 * 3 * 2 +10), (int)(selectedK.scale * 3 * 3  * 2 +10));
		
		KeypointVisualizer<Float[],MBFImage> kpvRight = new KeypointVisualizer<Float[],MBFImage>(new MBFImage(catInv,catInv,catInv), catInvKeys);
		MBFImage right = kpvRight.drawPatches(new Float[]{1.0f,0.0f,0.0f}, new Float[]{0.0f,1.0f,0.0f});
		right = right.extractCenter((int)selectedK.x, (int)selectedK.y, (int)(selectedK.scale * 3 * 3 * 2 +10), (int)(selectedK.scale * 3 * 3  * 2 +10));
		
		MBFImage combined = new MBFImage(left.getWidth() + right.getWidth(), Math.max(left.getHeight(), right.getHeight()), 3);
		MBFImageRenderer renderer = combined.createRenderer();
		renderer.drawImage(left, 0, 0);
		renderer.drawImage(right, left.getWidth(), 0);
		
//		DisplayUtilities.display(combined);
		String base = "/Users/ss06r/Development/LiveMemories/trunk/publications/ACM ICMR 2011/ImprQuant/images";
		ImageUtilities.write(left, "png", new File(base,"normalKeypoint.png"));
		ImageUtilities.write(right, "png", new File(base,"invertedKeypoint.png"));
		ImageUtilities.write(cat, "png", new File(base,"normal.png"));
		ImageUtilities.write(catInv, "png", new File(base,"inverted.png"));
	}
	
	/**
	 * Save and load a keypoint, check to see if it is consistent
	 * 
	 * @throws IOException
	 */
	@Test public void testIO() throws IOException {
		File ascii = null;
		File binary = null;
		
		
		try {
			ascii = folder.newFile("kpttest.ascii");
			binary = folder.newFile("kpttest.bin");
			
			IOUtils.writeASCII(ascii, k1);
			IOUtils.writeBinary(binary, k1);
			
			//test ascii read
			List<Keypoint> asciiKeys = MemoryLocalFeatureList.read(ascii, Keypoint.class);
			List<Keypoint> asciiKeys2 = MemoryLocalFeatureList.read(ascii, Keypoint.class);
			
			assertEquals(k1.size(), asciiKeys.size());
			assertEquals(k1.size(), asciiKeys2.size());
			for (int i=0; i<k1.size(); i++) {
				assertEquals(k1.get(i).x, asciiKeys.get(i).x, FLOAT_EPS);
				assertEquals(k1.get(i).x, asciiKeys2.get(i).x, FLOAT_EPS);
				
				assertEquals(k1.get(i).y, asciiKeys.get(i).y, FLOAT_EPS);
				assertEquals(k1.get(i).y, asciiKeys2.get(i).y, FLOAT_EPS);
				
				assertEquals(k1.get(i).scale, asciiKeys.get(i).scale, FLOAT_EPS);
				assertEquals(k1.get(i).scale, asciiKeys2.get(i).scale, FLOAT_EPS);
				
				assertEquals(k1.get(i).ori, asciiKeys.get(i).ori, FLOAT_EPS);
				assertEquals(k1.get(i).ori, asciiKeys2.get(i).ori, FLOAT_EPS);
				
				assertArrayEquals(k1.get(i).ivec, asciiKeys.get(i).ivec);
				assertArrayEquals(k1.get(i).ivec, asciiKeys2.get(i).ivec);
				
				for (int v : k1.get(i).ivec) {
					assert(v+128 >= 0);
				}
			}
			
			//test binary read
			List<Keypoint> binaryKeys = MemoryLocalFeatureList.read(binary, Keypoint.class);
			
			assertEquals(k1.size(), binaryKeys.size());
			for (int i=0; i<k1.size(); i++) {
				assertEquals(k1.get(i).x, binaryKeys.get(i).x, FLOAT_EPS);
				
				assertEquals(k1.get(i).y, binaryKeys.get(i).y, FLOAT_EPS);
				
				assertEquals(k1.get(i).scale, binaryKeys.get(i).scale, FLOAT_EPS);
				
				assertEquals(k1.get(i).ori, binaryKeys.get(i).ori, FLOAT_EPS);
				
				assertArrayEquals(k1.get(i).ivec, binaryKeys.get(i).ivec);
			}
		} finally {
			if (ascii != null) ascii.delete();
			if (binary != null) binary.delete();
		}
	}
	/**
	 * Run the test as a java application
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String args[]) throws IOException{
		KeypointTest test = new KeypointTest ();
		test.setup();
		test .displayCatKeys();
	}
}
