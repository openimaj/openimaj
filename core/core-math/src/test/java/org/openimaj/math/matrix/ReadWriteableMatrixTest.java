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
package org.openimaj.math.matrix;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.io.IOUtils;

import Jama.Matrix;

/**
 * Tests for {@link ReadWriteableMatrix}
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ReadWriteableMatrixTest {
	/**
	 * Temporary directory for IO tests
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	ReadWriteableMatrix [] rndMats;
	
	/**
	 * setup
	 */
	@Before
	public void setup() {
		rndMats = new ReadWriteableMatrix[] { 
				new ReadWriteableMatrix(Matrix.random(5, 8)), 
				new ReadWriteableMatrix(Matrix.random(5, 5))
		};
	}
	
	/**
	 * Compare two matrices using assertions
	 * @param m1 first matrix
	 * @param m2 seconf matrix
	 */
	public static void compareMatrices(Matrix m1, Matrix m2) {
		final int rows = m1.getRowDimension();
		final int cols = m1.getColumnDimension();
		
		assertEquals(rows, m2.getRowDimension());
		assertEquals(cols, m2.getColumnDimension());
		
		double[][] data1 = m1.getArray();
		double[][] data2 = m2.getArray();
		for (int r=0; r<rows; r++)
			assertArrayEquals(data1[r], data2[r], 0.00001);
	}
	
	/**
	 * Test matrix Binary IO
	 * @throws IOException
	 */
	@Test
	public void testBinaryIO() throws IOException {
		for (ReadWriteableMatrix m : rndMats) {
			File tmp = folder.newFile("openimaj-testBinaryIO.mat");
			IOUtils.writeBinary(tmp, m);
			
			ReadWriteableMatrix m2 = IOUtils.read(tmp, ReadWriteableMatrix.class);
			
			compareMatrices(m, m2);
			
			tmp.delete();
		}
	}
	
	/**
	 * Test matrix ASCII IO
	 * @throws IOException
	 */
	@Test
	public void testAsciiIO() throws IOException {
		for (ReadWriteableMatrix m : rndMats) {
			File tmp = folder.newFile("openimaj-testAsciiIO.mat");
			IOUtils.writeASCII(tmp, m);
			
			ReadWriteableMatrix m2 = IOUtils.read(tmp, ReadWriteableMatrix.class);
			
			compareMatrices(m, m2);
			
			tmp.delete();
		}		
	}
}
