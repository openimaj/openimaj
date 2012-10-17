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
package org.openimaj.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.util.processes.JavaProcess;

/**
 * Tests for the {@link ReferencesTool}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class ReferencesToolTests {
	private static final String FAT_JAR_NAME = "ReferenceTest-1.0-SNAPSHOT-jar-with-dependencies.jar";
	private static final String THIN_JAR_NAME = "ReferenceTest-1.0-SNAPSHOT.jar";

	/**
	 * Temporary folder
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	File thinJar;
	File fatJar;

	/**
	 * Setup tests
	 * 
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException {
		thinJar = folder.newFile(THIN_JAR_NAME);

		FileOutputStream thinJarFos = null;
		try {
			thinJarFos = new FileOutputStream(thinJar);
			IOUtils.copy(this.getClass().getResourceAsStream(THIN_JAR_NAME), thinJarFos);
		} finally {
			if (thinJarFos != null)
				thinJarFos.close();
		}

		fatJar = folder.newFile(FAT_JAR_NAME);

		FileOutputStream fatJarFos = null;
		try {
			fatJarFos = new FileOutputStream(fatJar);
			IOUtils.copy(this.getClass().getResourceAsStream(FAT_JAR_NAME), fatJarFos);
		} finally {
			if (fatJarFos != null)
				fatJarFos.close();
		}
	}

	/**
	 * Test that the -jar option works with a valid jar file
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testJar() throws Throwable {
		final File txt = folder.newFile("refs.txt");
		txt.delete();

		final String args = "-wt " + txt.getAbsolutePath() + " -jar " + fatJar.getAbsolutePath();

		JavaProcess.runProcess(ReferencesTool.class, args);

		final List<String> lines = IOUtils.readLines(new FileInputStream(txt));

		assertEquals(5, lines.size());
	}

	/**
	 * Test that the -jar option fails with an invalid jar file
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testJarInvalid() throws Throwable {
		final File txt = folder.newFile("refs.txt");
		txt.delete();

		final String args = "-wt " + txt.getAbsolutePath() + " -jar " + thinJar.getAbsolutePath();

		JavaProcess.runProcess(ReferencesTool.class, args);

		assertTrue(!txt.exists());
	}

	/**
	 * Test that the -cp option works with a valid jar file and mainclass
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testClasspath() throws Throwable {
		final File txt = folder.newFile("refs.txt");
		txt.delete();

		final String args = "-wt " + txt.getAbsolutePath() + " -cp " + thinJar.getAbsolutePath()
				+ " org.openimaj.tests.App";

		JavaProcess.runProcess(ReferencesTool.class, args);

		final List<String> lines = IOUtils.readLines(new FileInputStream(txt));

		assertEquals(5, lines.size());
	}
}
