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
