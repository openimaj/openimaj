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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Utility functions for reading lowe-style keypoint files and the (similar) files produced by the MSER and oxford tools
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class AsciiInterestPoint {
	/**
	 * Number of location elements for ellipse files
	 */
	public static final int NUM_ELLIPSE_LOC_FEATS = 5;
	/**
	 * Number of location elements for circle files
	 */
	public static final int NUM_CIRCLE_LOC_FEATS = 4;
	/**
	 * Number of location features for ASIFT files
	 */
	public static final int NUM_ASIFT_LOC_FEATS = 7;
	
	/**
	 * Read the header
	 * 
	 * @param file
	 * @param reverse
	 * @return the header info
	 * @throws IOException
	 */
	public static Header readHeader(File file, boolean reverse) throws IOException {
		Scanner scanner = null;
		try {
			scanner = new Scanner(file);
			return readHeader(scanner, reverse);
		} finally {
			scanner.close();
		}
	}
	
	/**
	 * Read the data
	 * 
	 * @param file
	 * @param reverse
	 * @param nLocationFeatures
	 * @return the data
	 * @throws IOException
	 */
	public static byte [][] readData(File file, boolean reverse, int nLocationFeatures) throws IOException {
		Scanner scanner = null;
		
		try {
			scanner = new Scanner(file);
			Header header = readHeader(scanner, reverse);			
			return readData(scanner, header, nLocationFeatures);
		} finally {
			scanner.close();
		}
	}
	
	/**
	 * Read the data
	 * @param file
	 * @param indices
	 * @param reverse
	 * @param nLocationFeatures
	 * @return the data
	 * @throws IOException
	 */
	public static byte  [][] readData(File file, int [] indices, boolean reverse, int nLocationFeatures) throws IOException {
		Scanner scanner = null;

		try {
			byte [][] data = new byte[indices.length][];
			
			scanner = new Scanner(file);
			
			Arrays.sort(indices);
		
			Header header = readHeader(scanner, reverse);
		
			if (indices[indices.length-1] >= header.nfeatures)
				throw new IllegalArgumentException("Invalid index");
		
			int currentIdx = 0;
			for (int i=0; i<=indices[indices.length-1]; i++) {
				if (i == indices[currentIdx]) {
					data[currentIdx] = readLine(scanner, header, nLocationFeatures);
					currentIdx++;
				} else {
					skipLine(scanner, header, nLocationFeatures);
				}
			}
			return data;
		} finally {
			scanner.close();
		}
	}
	
	/**
	 * Read the file
	 * @param source
	 * @param reverse
	 * @param nLocationFeatures
	 * @return the data
	 * @throws IOException
	 */
	public static FeatureFile read(InputStream source, boolean reverse,int nLocationFeatures) throws IOException {
		
		Scanner scanner = null;
		try {
			scanner = new Scanner(source);
			Header header = readHeader(scanner, reverse);
			
		
			
			byte[][] data = new byte[header.nfeatures][header.ndims];
			String[] locations = new String[header.nfeatures];
		
			for (int i=0; i<header.nfeatures; i++) {
				String location = "";
				for (int j=0; j<nLocationFeatures; j++)
					location += scanner.nextFloat() + " ";
				locations[i] = location;
			
				for (int j=0; j<header.ndims; j++)
					data[i][j] = (byte)(scanner.nextInt()-128);
			}
			FeatureFile ff = new MemoryFeatureFile(data,locations);
			return ff;
		} finally {
			scanner.close();
		}
	}
	
	/**
	 * Read the file
	 * @param file
	 * @param reverse
	 * @param nLocationFeatures
	 * @return the data
	 * @throws IOException
	 */
	public static FeatureFile read(File file, boolean reverse, int nLocationFeatures) throws IOException {
		Scanner scanner = null;
		
		try {
			scanner = new Scanner(file);
			Header header = readHeader(scanner, reverse);
		
			
			byte[][] data = new byte[header.nfeatures][header.ndims];
			String[] locations = new String[header.nfeatures];
		
			for (int i=0; i<header.nfeatures; i++) {
				String location = "";
				for (int j=0; j<nLocationFeatures; j++)
					location += scanner.nextFloat() + " ";
				locations[i] = location;
			
				for (int j=0; j<header.ndims; j++)
					data[i][j] = (byte) (scanner.nextInt()-128);
			}
			FeatureFile ff = new MemoryFeatureFile(data,locations);
			return ff;
		} finally {
			scanner.close();
		}
	}
	
	protected static Header readHeader(Scanner scanner, boolean reverse) throws IOException {
		Header header = new Header();
		
		if (reverse) {
			header.ndims = scanner.nextInt();
			header.nfeatures = scanner.nextInt();
		} else {
			header.nfeatures = scanner.nextInt();
			header.ndims = scanner.nextInt();
		}
		
		return header;
	}
	
	protected static byte [][] readData(Scanner scanner, Header header, int nLocationFeatures) {
		byte [][] data = new byte[header.nfeatures][header.ndims];
		
		for (int i=0; i<header.nfeatures; i++) {	
			data[i] = readLine(scanner, header, nLocationFeatures);
		}
		
		return data;
	}
	
	protected static byte [] readLine(Scanner scanner, Header header, int nLocationFeatures) {
		byte [] data = new byte[header.ndims];
		
		//skip the location data
		for (int j=0; j<nLocationFeatures; j++)
			scanner.nextFloat();
			
		for (int j=0; j<header.ndims; j++)
			data[j] = (byte) (scanner.nextInt() - 128);
		
		return data;
	}
	
	protected static void skipLine(Scanner scanner, Header header, int nLocationFeatures) {
		//skip the location data
		for (int j=0; j<nLocationFeatures; j++)
			scanner.next();
//			scanner.nextFloat();

		for (int j=0; j<header.ndims; j++)
			scanner.next();
//			scanner.nextInt();
	}

	
}
