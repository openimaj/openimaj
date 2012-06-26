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

import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.feature.local.list.FileLocalFeatureList;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.io.IOUtils;
import org.openimaj.tools.clusterquantiser.FileType;
import org.openimaj.tools.clusterquantiser.Header;

/**
 * Tests of {@link FileType}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FileTypeTest {
	File asciiKeys;
	File binaryKeys;
	File ellipse;
	private File binaryColourKeys;
	private File binaryAsiftEnrichedKeys;
	private File binaryAsiftKeys;
	
	/**
	 * Setup
	 */
	@Before
	public void setup() {
		try
		{
			asciiKeys = new File(new URI(this.getClass().getResource("test.key").toString()).getPath());
			binaryKeys = new File(new URI(this.getClass().getResource("test.bkey").toString()).getPath());
			binaryColourKeys = new File(new URI(this.getClass().getResource("testColour.bkey").toString()).getPath());
			binaryAsiftEnrichedKeys = new File(new URI(this.getClass().getResource("testAsiftEnriched.bkey").toString()).getPath());
			binaryAsiftKeys = new File(new URI(this.getClass().getResource("testAsift.bkey").toString()).getPath());
			ellipse = new File(new URI(this.getClass().getResource("picture.haraff.spin").toString()).getPath());
		}
		catch( URISyntaxException e )
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * test colour binary keypoint header
	 * @throws IOException
	 */
	@Test
	public void test_readHeader_colourBinaryKeypoint() throws IOException {
		Header h = FileType.BINARY_KEYPOINT.readHeader(binaryColourKeys);
		
		assertEquals(h.ndims, 384);
		assertEquals(h.nfeatures, 4419);
	}
	
	/**
	 * test asift enriched binary header
	 * @throws IOException
	 */
	@Test
	public void test_readHeader_asiftEnrichedBinaryKeypoint() throws IOException {
		Header h = FileType.ASIFTENRICHED_BINARY.readHeader(binaryAsiftEnrichedKeys);
		
		assertEquals(h.ndims, 128);
		assertEquals(h.nfeatures, 5869);
	}
	
	/**
	 * Test binary keypoint header
	 * @throws IOException
	 */
	@Test
	public void test_readHeader_binaryKeypoint() throws IOException {
		Header h = FileType.BINARY_KEYPOINT.readHeader(binaryKeys);
		
		assertEquals(h.ndims, 128);
		assertEquals(h.nfeatures, 958);
	}
	
	/**
	 * Test binary keypoint
	 * @throws IOException
	 */
	@Test
	public void test_readData_binaryKeypoint() throws IOException {
		FileType.BINARY_KEYPOINT.readHeader(binaryKeys);
		byte[][] alldata = FileType.BINARY_KEYPOINT.readFeatures(binaryKeys);
		
		assertEquals(alldata[0][0], (byte)(2-128));
		assertEquals(alldata[0][1], (byte)(0-128));
		assertEquals(alldata[1][0], (byte)(10-128));
		assertEquals(alldata[1][1], (byte)(50-128));
		
		byte[][] line100 = FileType.BINARY_KEYPOINT.readFeatures(binaryKeys, 99,100);
		assertArrayEquals(line100[1], alldata[100]);
		
		byte[][] line957 = FileType.BINARY_KEYPOINT.readFeatures(binaryKeys, 957);
		assertArrayEquals(line957[0], alldata[957]);
		
		
		assertEquals(line957[0][127], (byte)(4-128));
		assertEquals(line957[0][126], (byte)(4-128));
		assertEquals(line957[0][125], (byte)(3-128));
	}
	
	/**
	 * Test colour binary keypoint
	 * @throws IOException
	 */
	@Test
	public void test_readData_colourBinaryKeypoint() throws IOException {
		FileType.BINARY_KEYPOINT.readHeader(binaryColourKeys);
		byte[][] alldata = FileType.BINARY_KEYPOINT.readFeatures(binaryColourKeys);
		
		assertTrue(alldata[0].length == 384);
//		assertEquals(alldata[0][0], (byte)(2-128));
//		assertEquals(alldata[0][1], (byte)(0-128));
//		assertEquals(alldata[1][0], (byte)(10-128));
//		assertEquals(alldata[1][1], (byte)(50-128));
//		
//		byte[][] line100 = FileType.BINARY_KEYPOINT.readFeatures(binaryKeys, 99,100);
//		assertArrayEquals(line100[1], alldata[100]);
//		
//		byte[][] line957 = FileType.BINARY_KEYPOINT.readFeatures(binaryKeys, 957);
//		assertArrayEquals(line957[0], alldata[957]);
//		
//		
//		assertEquals(line957[0][127], (byte)(4-128));
//		assertEquals(line957[0][126], (byte)(4-128));
//		assertEquals(line957[0][125], (byte)(3-128));
	}
	
	/**
	 * test asift enriched keypoint
	 * @throws IOException
	 */
	@Test
	public void test_readData_asiftEnrichedBinaryKeypoint() throws IOException {
		FileType.BINARY_KEYPOINT.readHeader(binaryAsiftKeys);
		
		FileType.ASIFTENRICHED_BINARY.readHeader(binaryAsiftEnrichedKeys);
		byte[][] alldata = FileType.ASIFTENRICHED_BINARY.readFeatures(binaryAsiftEnrichedKeys);
		byte[][] olddata = FileType.BINARY_KEYPOINT.readFeatures(binaryAsiftKeys);
		
		for(int i = 0 ; i < alldata.length; i++){
			assertTrue(Arrays.equals(alldata[i],olddata[i]));
		}
	}
	
	/**
	 * Test ascii keypoint header
	 * @throws IOException
	 */
	@Test
	public void test_readHeader_asciiKeypoint() throws IOException {
		Header h = FileType.LOWE_KEYPOINT_ASCII.readHeader(asciiKeys);
		assertEquals(h.ndims, 128);
		assertEquals(h.nfeatures, 958);
	}
	
	/**
	 * Test ascii keypoint
	 * @throws IOException
	 */
	@Test
	public void test_readData_asciiKeypoint() throws IOException {
		byte[][] alldata = FileType.LOWE_KEYPOINT_ASCII.readFeatures(asciiKeys);
		LocalFeatureList<Keypoint> kpl = FileLocalFeatureList.read(asciiKeys, Keypoint.class);
		IOUtils.writeBinary(binaryKeys, kpl);
		assertEquals(alldata[0][0], (byte)(2-128));
		assertEquals(alldata[0][1], (byte)(0-128));
		assertEquals(alldata[1][0], (byte)(10-128));
		assertEquals(alldata[1][1], (byte)(50-128));
		
		byte[][] line100 = FileType.LOWE_KEYPOINT_ASCII.readFeatures(asciiKeys, 100);
		assertArrayEquals(line100[0], alldata[100]);
		
		byte[][] line957 = FileType.LOWE_KEYPOINT_ASCII.readFeatures(asciiKeys, 957);
		assertArrayEquals(line957[0], alldata[957]);
		
		assertEquals(line957[0][127], (byte)(4-128));
		assertEquals(line957[0][126], (byte)(4-128));
		assertEquals(line957[0][125], (byte)(3-128));
	}
	
	/**
	 * Test ascii ellipse header
	 * @throws IOException
	 */
	@Test
	public void test_readHeader_asciiEllipse() throws IOException {
		Header h = FileType.ELLIPSE_ASCII.readHeader(ellipse);
		
		assertEquals(h.ndims, 50);
		assertEquals(h.nfeatures, 2375);
	}
	
	/**
	 * Test ascii ellipse
	 * @throws IOException
	 */
	@Test
	public void test_readData_asciiEllipse() throws IOException {
		byte[][] alldata = FileType.ELLIPSE_ASCII.readFeatures(ellipse);
		
		assertEquals(alldata[0][0], (byte)(0-128));
		assertEquals(alldata[0][1], (byte)(0-128));
		assertEquals(alldata[7][0], (byte)(123-128));
		assertEquals(alldata[7][1], (byte)(244-128));
		
		byte[][] line100 = FileType.ELLIPSE_ASCII.readFeatures(ellipse, 100);
		assertArrayEquals(line100[0], alldata[100]);
		
		byte[][] line2374 = FileType.ELLIPSE_ASCII.readFeatures(ellipse, 2374);
		assertArrayEquals(line2374[0], alldata[2374]);
		
		assertEquals(line2374[0][49], (byte)(0-128));
		assertEquals(line2374[0][48], (byte)(24-128));
		assertEquals(line2374[0][47], (byte)(93-128));
//		assertEquals(line2374[0][49], 0);
//		assertEquals(line2374[0][48], 24);
//		assertEquals(line2374[0][47], 93);
	}
}
