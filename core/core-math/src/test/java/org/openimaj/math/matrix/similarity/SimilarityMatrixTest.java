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
package org.openimaj.math.matrix.similarity;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.io.IOUtils;
import org.openimaj.math.matrix.ReadWriteableMatrixTest;

import Jama.Matrix;

/**
 * Tests for {@link SimilarityMatrix}
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class SimilarityMatrixTest {
	/**
	 * Temporary directory for IO tests
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	SimilarityMatrix mat;

	/**
	 * setup
	 */
	@Before
	public void setup() {
		mat = new SimilarityMatrix(new String[]{"a","b","c","d","e"}, Matrix.random(5, 5));
	}

	/**
	 * test non-square matrix
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testNonSquare() {
		mat = new SimilarityMatrix(new String[]{"a","b","c","d","e"}, Matrix.random(5, 6));
	}

	/**
	 * test bad indexing
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testBadIndex() {
		mat = new SimilarityMatrix(new String[]{"a","b","c","d"}, Matrix.random(5, 5));
	}

	/**
	 * Test binary IO
	 * @throws IOException
	 */
	@Test
	public void testBinaryIO() throws IOException {
		File tmp = folder.newFile("openimaj-testBinaryIO.mat");
		IOUtils.writeBinary(tmp, mat);

		SimilarityMatrix m2 = IOUtils.read(tmp, SimilarityMatrix.class);

		ReadWriteableMatrixTest.compareMatrices(mat, m2);

		tmp.delete();
	}

	/**
	 * Test ASCII IO
	 * @throws IOException
	 */
	@Test
	public void testAsciiIO() throws IOException {
		File tmp = folder.newFile("openimaj-testAsciiIO.mat");
		IOUtils.writeASCII(tmp, mat);

		SimilarityMatrix m2 = IOUtils.read(tmp, SimilarityMatrix.class);

		ReadWriteableMatrixTest.compareMatrices(mat, m2);

		tmp.delete();
	}
}
