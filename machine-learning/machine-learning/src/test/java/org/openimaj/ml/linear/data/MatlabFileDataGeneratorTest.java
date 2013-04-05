package org.openimaj.ml.linear.data;

import static org.junit.Assert.*;
import gov.sandia.cognition.math.matrix.Matrix;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.io.FileUtils;
import org.openimaj.util.pair.Pair;

public class MatlabFileDataGeneratorTest {
	
	/**
	 * the output folder
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	private File matfile;
	@Before
	public void before() throws IOException{
		matfile = folder.newFile("tmp.mat");
		InputStream stream = MatlabFileDataGeneratorTest.class.getResourceAsStream("/org/openimaj/ml/linear/data/XYs.mat");
		FileUtils.copyStreamToFileBinary(stream, matfile);
		System.out.println(matfile);
		System.out.println("Done!");
		
	}
	@Test
	public void testMatlabFile() throws IOException{
		MatlabFileDataGenerator gen = new MatlabFileDataGenerator(matfile);
		int nusers = -1;
		int nwords = -1;
		int ntasks = -1;
		for (int i = 0; i < gen.size(); i++) {
			Pair<Matrix> XY = gen.generate();
			Matrix X = XY.firstObject();
			Matrix Y = XY.secondObject();
			
			if(nusers == -1){
				nusers = X.getNumColumns();
				nwords = X.getNumRows();
				ntasks = Y.getNumColumns();
			}
			else{
				assertTrue(nusers == X.getNumColumns());
				assertTrue(nwords == X.getNumRows());
				assertTrue(ntasks == Y.getNumColumns());
			}
		}
	}
}
