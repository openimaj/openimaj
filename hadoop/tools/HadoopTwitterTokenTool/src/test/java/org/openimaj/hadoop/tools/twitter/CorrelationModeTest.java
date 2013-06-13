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
package org.openimaj.hadoop.tools.twitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class CorrelationModeTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private String hadoopCommand;
	private File dest;
	private File output;

	/**
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException {
		hadoopCommand = "-i %s -om %s -ro %s";
		final TarInputStream tin = new TarInputStream(new GZIPInputStream(
				CorrelationModeTest.class.getResourceAsStream("/org/openimaj/hadoop/tools/twitter/dfidf.out.tar.gz")));
		TarEntry entry = null;
		output = folder.newFile("results.out");
		output.delete();
		output.mkdir();
		dest = folder.newFile("DFIDF.out");
		dest.delete();
		dest.mkdir();

		while ((entry = tin.getNextEntry()) != null) {
			final File tdst = new File(dest.toString(), entry.getName());
			if (entry.isDirectory()) {
				tdst.mkdir();
			}
			else {
				final FileOutputStream fout = new FileOutputStream(tdst);
				tin.copyEntryContents(fout);
				fout.close();
			}
		}
		tin.close();
	}

	@Test
	public void testWordIDFTimeSeries() throws Exception {
		// String command = String.format(
		// hadoopCommand,
		// dest.getAbsolutePath(),
		// "CSV",
		// output
		// );
		// String[] args = command.split(" ");
		// HadoopTwitterTokenTool.main(args);
		//
		// WordTimeValue wordTimeSeries = new
		// WordTimeValue(output.getAbsolutePath());
		// long[] timePeriods = new long[]{
		// 1285887600000l, // 1285974000000l, 1286060400000l,
		// 1286146800000l, // 1286233200000l, 1286319600000l,
		// 1286406000000l, // 1286492400000l, 1286578800000l
		// };
		// WordDFIDFTimeSeries wts = wordTimeSeries.values.get("#lol");
		// System.out.println(wts);
		// IntervalSummationProcessor<WordDFIDF[],WordDFIDF,
		// WordDFIDFTimeSeries> isp = new
		// IntervalSummationProcessor<WordDFIDF[],WordDFIDF,
		// WordDFIDFTimeSeries>(timePeriods);
		// isp.process(wts);
		// System.out.println(wts);
	}

	@Test
	public void testCorrelation() throws Exception {
		System.out.println("Reading DFIDF from: " + dest.getAbsolutePath());
		String command = String.format(
				hadoopCommand,
				dest.getAbsolutePath(),
				"CORRELATION",
				output
				);
		command += " -maxp 0.1";
		final String[] args = command.split(" ");
		HadoopTwitterTokenTool.main(args);
		System.out.println(output);
	}
}
