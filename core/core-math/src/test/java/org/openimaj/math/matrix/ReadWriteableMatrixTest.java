package org.openimaj.math.matrix;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.io.IOUtils;

import Jama.Matrix;

/**
 * Tests for ReadWriteableMatrix
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class ReadWriteableMatrixTest {
	ReadWriteableMatrix [] rndMats;
	
	@Before
	public void setup() {
		rndMats = new ReadWriteableMatrix[] { 
				new ReadWriteableMatrix(Matrix.random(5, 8)), 
				new ReadWriteableMatrix(Matrix.random(5, 5))
		};
	}
	
	protected static void compareMatrices(Matrix m1, Matrix m2) {
		final int rows = m1.getRowDimension();
		final int cols = m1.getColumnDimension();
		
		assertEquals(rows, m2.getRowDimension());
		assertEquals(cols, m2.getColumnDimension());
		
		double[][] data1 = m1.getArray();
		double[][] data2 = m2.getArray();
		for (int r=0; r<rows; r++)
			assertArrayEquals(data1[r], data2[r], 0.00001);
	}
	
	@Test
	public void testBinaryIO() throws IOException {
		for (ReadWriteableMatrix m : rndMats) {
			File tmp = File.createTempFile("openimaj", "mat");
			IOUtils.writeBinary(tmp, m);
			
			ReadWriteableMatrix m2 = IOUtils.read(tmp, ReadWriteableMatrix.class);
			
			compareMatrices(m, m2);
			
			tmp.delete();
		}
	}
	
	@Test
	public void testAsciiIO() throws IOException {
		for (ReadWriteableMatrix m : rndMats) {
			File tmp = File.createTempFile("openimaj", "mat");
			IOUtils.writeASCII(tmp, m);
			
			ReadWriteableMatrix m2 = IOUtils.read(tmp, ReadWriteableMatrix.class);
			
			compareMatrices(m, m2);
			
			tmp.delete();
		}		
	}
}
