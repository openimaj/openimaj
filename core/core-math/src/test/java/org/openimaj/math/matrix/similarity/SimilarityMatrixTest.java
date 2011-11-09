package org.openimaj.math.matrix.similarity;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.io.IOUtils;
import org.openimaj.math.matrix.ReadWriteableMatrixTest;

import Jama.Matrix;

/**
 * Tests for ReadWriteableMatrix
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class SimilarityMatrixTest {
	SimilarityMatrix mat;

	@Before
	public void setup() {
		mat = new SimilarityMatrix(new String[]{"a","b","c","d","e"}, Matrix.random(5, 5));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNonSquare() {
		mat = new SimilarityMatrix(new String[]{"a","b","c","d","e"}, Matrix.random(5, 6));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testBadIndex() {
		mat = new SimilarityMatrix(new String[]{"a","b","c","d"}, Matrix.random(5, 5));
	}

	@Test
	public void testBinaryIO() throws IOException {
		File tmp = File.createTempFile("openimaj", "mat");
		IOUtils.writeBinary(tmp, mat);

		SimilarityMatrix m2 = IOUtils.read(tmp, SimilarityMatrix.class);

		ReadWriteableMatrixTest.compareMatrices(mat, m2);

		tmp.delete();
	}

	@Test
	public void testAsciiIO() throws IOException {
		File tmp = File.createTempFile("openimaj", "mat");
		IOUtils.writeASCII(tmp, mat);

		SimilarityMatrix m2 = IOUtils.read(tmp, SimilarityMatrix.class);

		ReadWriteableMatrixTest.compareMatrices(mat, m2);

		tmp.delete();
	}
}
