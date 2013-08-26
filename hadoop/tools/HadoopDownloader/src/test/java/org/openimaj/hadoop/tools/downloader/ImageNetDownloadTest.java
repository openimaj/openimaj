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
 * Test the image-net format
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ImageNetDownloadTest {
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
		String exampleList = "n00007846_41	http://static.flickr.com/3423/3788747850_c9653099c2.jpg" + "\n" 
//		+
//		"t3st	http://farm5.staticflickr.com/4013/4214727934_0ff14790f8.jpg" + "\n" +
//		"test	http://farm3.static.flickr.com/2412/2364526179_6d19772ac4_b.jpg" + "\n" +
//		"n00007846_383	http://secrets-of-flirting.com/girlfriend.jpg" + "\n" + 
//		"n00007846_499	http://z.about.com/d/kidstvmovies/1/0/a/8/sm3005.jpg" + "\n" + 
//		"n00007846_543	http://static.flickr.com/3455/3372482944_244c25c45f.jpg" + "\n" + 
//		"n00007846_612	http://static.flickr.com/3592/3376795744_e89f42f5c5.jpg" + "\n" + 
//		"n00007846_658	http://static.flickr.com/122/286394792_9232f00db3.jpg" + "\n" + 
//		"n00007846_709	http://static.flickr.com/3299/3621111660_bcb5907cb0.jpg" + "\n" + 
//		"n00007846_839	http://static.flickr.com/3628/3376796820_a1dd3e2ed7.jpg" + "\n" + 
//		"n00007846_846	http://static.flickr.com/3383/3653165852_8f8a06eaa6.jpg" + "\n" + 
//		"Special	designedToFail.com/image.wang" + "\n" + 
//		"Failcakes	http://www.wave.co.nz/~bodyline/pages/catalogue/wetsuits/mens_summer/images/long-sleeve-inferno-L.jpg" + "\n" + 
//		"n00007846_991	http://www.cinema.bg/sff/images-person/David-Lanzmann.gif"
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
		HadoopDownloader.main(new String[]{
				"-i", exampleFile.getAbsolutePath(),
				"-o", exampleOut.getAbsolutePath(),
				"-m", "IMAGE_NET"});
	}
}
