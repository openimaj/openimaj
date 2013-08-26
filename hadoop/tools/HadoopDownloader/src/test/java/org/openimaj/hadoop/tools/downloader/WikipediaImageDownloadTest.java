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
package org.openimaj.hadoop.tools.downloader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.hadoop.tools.downloader.HadoopDownloader;

/**
 * Test wikipedia image downloads
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class WikipediaImageDownloadTest {
	/**
	 * Temporary folder for output
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	private File exampleFile;
	private File exampleOut;

	/**
	 * Setup input data
	 * @throws IOException
	 */
	@Before public void setup() throws IOException{
		String exampleList = "File:0002MAN-Hermes.jpg" + "\n" 
//		+ 
//		"File:0002s9rg.jpg" + "\n" + 
//		"File:00-02 Saturn L-Series sedan.jpg" + "\n" + 
//		"File:0002+Sucres+Ecuador+1944.jpg" + "\n" + 
//		"File:00-02 Toyota Echo coupe.jpg" + "\n" + 
//		"File:00031-Trindade-Leal----Gine.jpg" + "\n" + 
//		"File:00-03 BMW X5 3.0si.jpg" + "\n" + 
//		"File:00-03 Chevrolet Malibu.jpg" + "\n" + 
//		"File:0003LON2008HY.JPG" + "\n" + 
//		"File:00-03 Mercury Sable wagon front.jpg" + "\n" + 
//		"File:00040m.jpg"
		;
		
		exampleFile = folder.newFile("images.txt");
		
		PrintWriter pw = new PrintWriter(new FileWriter(exampleFile));
		pw.println(exampleList);
		pw.flush();
		pw.close();
		
		exampleOut = folder.newFile("example.images");
		exampleOut.delete();
	}
	
	/**
	 * Test download
	 * @throws Exception
	 */
	@Test public void testDownload() throws Exception{
		HadoopDownloader .main(new String[]{
			"-i",exampleFile.getAbsolutePath(),
			"-o",exampleOut.getAbsolutePath(),
			"-m","WIKIPEDIA_IMAGES_DUMP",
			"-s","1000"
		});
	}
}
