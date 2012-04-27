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
package org.openimaj.hadoop.tools.fastkmeans;


import java.io.File;
import java.util.ArrayList;

import org.apache.hadoop.util.ToolRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.hadoop.tools.fastkmeans.HadoopFastKMeans;
import org.openimaj.hadoop.tools.fastkmeans.HadoopFastKMeansOptions;


public class HadoopFastKMeansTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	File imageSeqFile = new File("data/images.seq");
	File featureSeqFile = new File("data/features.seq");
	File tmpOut = null;
	
	@Before
	public void setUp() throws Exception {
		tmpOut = folder.newFile("tmp.out");
		tmpOut.delete();
	}
	
	@Test
	public void testRandomInit() throws Exception{
		HadoopFastKMeans hfkm = new HadoopFastKMeans();
		HadoopFastKMeansOptions hfkmo = new HadoopFastKMeansOptions(null);
		hfkmo.inputs = new ArrayList<String>();
		hfkmo.inputs.add(featureSeqFile.getAbsolutePath());
		hfkmo.output = tmpOut.getAbsolutePath();
		hfkmo.forceRM = true;
		hfkmo.nsamples = 1000;
		hfkm.setOptions(hfkmo);
		ToolRunner.run(hfkm, new String[]{});
	}
	
	@Test
	public void testRandomInitAll() throws Exception{
		HadoopFastKMeans hfkm = new HadoopFastKMeans();
		HadoopFastKMeansOptions hfkmo = new HadoopFastKMeansOptions(null);
		hfkmo.inputs = new ArrayList<String>();
		hfkmo.inputs.add(featureSeqFile.getAbsolutePath());
		hfkmo.output = tmpOut.getAbsolutePath();
		hfkmo.forceRM = true;
		hfkmo.nsamples = -1;
		hfkm.setOptions(hfkmo);
		ToolRunner.run(hfkm, new String[]{});
	}
	
	public static void main(String args[]) throws Exception{
		HadoopFastKMeansTest test = new HadoopFastKMeansTest();
		test.setUp();
		test.testRandomInit();
	}
}
