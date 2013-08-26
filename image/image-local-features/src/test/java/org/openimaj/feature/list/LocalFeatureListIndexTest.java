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
package org.openimaj.feature.list;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map.Entry;

import org.junit.Test;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.LocalFeatureListIndex;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadWriteableInt;
import org.openimaj.io.wrappers.ReadWriteableString;


/**
 * 
 * Test the local feature list
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class LocalFeatureListIndexTest {
	/**
	 * Make sure a LocalFeatureList can be written and read
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testIO() throws IOException {
		DoGSIFTEngine engine = new DoGSIFTEngine();
		
		LocalFeatureList<Keypoint> keys1 = engine.findFeatures(ImageUtilities.readF(LocalFeatureListIndexTest.class.getResourceAsStream("/org/openimaj/image/data/cat.jpg")));
		LocalFeatureList<Keypoint> keys2 = engine.findFeatures(ImageUtilities.readF(LocalFeatureListIndexTest.class.getResourceAsStream("/org/openimaj/image/data/sinaface.jpg")));
		
		
		
		LocalFeatureListIndex<ReadWriteableInt, Keypoint> index = new LocalFeatureListIndex<ReadWriteableInt, Keypoint>();
		index.put(new ReadWriteableInt(1), keys1);
		index.put(new ReadWriteableInt(2), keys2);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.writeBinary(baos, index);
		
		LocalFeatureListIndex<ReadWriteableInt, Keypoint> index2;
		index2 = IOUtils.read(new ByteArrayInputStream(baos.toByteArray()), LocalFeatureListIndex.class);
		
		for(Entry<ReadWriteableInt, LocalFeatureList<Keypoint>> e : index2.entrySet()){
			assertTrue(index.containsKey(e.getKey()));
			for(int i = 0; i < e.getValue().size(); i++){
				assertTrue(e.getValue().get(i).equals(index.get(e.getKey()).get(i)));
			}
		}
	}
	/**
	 * Test the localfeaturelist can be read and written as ASCII
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testIOString() throws IOException {
		DoGSIFTEngine engine = new DoGSIFTEngine();
		
		LocalFeatureList<Keypoint> keys1 = engine.findFeatures(ImageUtilities.readF(LocalFeatureListIndexTest.class.getResourceAsStream("/org/openimaj/image/data/cat.jpg")));
		LocalFeatureList<Keypoint> keys2 = engine.findFeatures(ImageUtilities.readF(LocalFeatureListIndexTest.class.getResourceAsStream("/org/openimaj/image/data/sinaface.jpg")));
		
		LocalFeatureListIndex<ReadWriteableString, Keypoint> index = new LocalFeatureListIndex<ReadWriteableString, Keypoint>();
		index.put(new ReadWriteableString("" + 1), keys1);
		index.put(new ReadWriteableString("" + 2), keys2);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.writeBinary(baos, index);
		
		LocalFeatureListIndex<ReadWriteableString, Keypoint> index2;
		index2 = IOUtils.read(new ByteArrayInputStream(baos.toByteArray()), LocalFeatureListIndex.class);
		
		for(Entry<ReadWriteableString, LocalFeatureList<Keypoint>> e : index2.entrySet()){
			assertTrue(index.containsKey(e.getKey()));
			for(int i = 0; i < e.getValue().size(); i++){
				assertTrue(e.getValue().get(i).equals(index.get(e.getKey()).get(i)));
			}
		}
		
	}
}
