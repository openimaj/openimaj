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
package org.openimaj.tools.imagecollection.collection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.tools.imagecollection.collection.video.YouTubeVideoImageCollection;
import org.openimaj.video.xuggle.XuggleVideoWriter;

public class XuggleVideoImageCollectionTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	String aVideo = "/org/openimaj/video/data/a_video.avi";
	private File videoFile;

	// private ImageCollectionConfig fileConfig;
	// private ImageCollectionConfig urlConfig;

	@Before
	public void setup() throws IOException {
		final InputStream s = XuggleVideoImageCollectionTest.class.getResourceAsStream(aVideo);
		videoFile = folder.newFile("xuggle.avi");

		final FileOutputStream fos = new FileOutputStream(videoFile);
		int read = 0;
		final byte[] buffer = new byte[1024];
		while ((read = s.read(buffer)) != -1) {
			fos.write(buffer, 0, read);
		}
		fos.close();
		// final String jsonVideoFile = String.format("{video:{file:\"%s\"}}",
		// videoFile);
		// final String jsonVideoURL = String.format("{video:{url:\"%s\"}}",
		// videoFile);

		// fileConfig = new ImageCollectionConfig(jsonVideoFile);
		// urlConfig = new ImageCollectionConfig(jsonVideoURL);
	}

	@Test
	public void testURLFileXuggleVideoImageCollection() throws ImageCollectionSetupException {
		// try{
		// FromFile fileVideo = new XuggleVideoImageCollection.FromFile();
		// fileVideo.setup(fileConfig);
		// List<ImageCollectionEntry<MBFImage>> fileFrames = fileVideo.getAll();
		// FromURL urlVideo = new XuggleVideoImageCollection.FromURL();
		// urlVideo.setup(urlConfig);
		// List<ImageCollectionEntry<MBFImage>> urlFrames = urlVideo.getAll();
		// assertTrue(urlFrames.size() > 0);
		// assertEquals(urlFrames.size(),fileFrames.size());
		// }
		// catch(UnsatisfiedLinkError e){
		//
		// }

	}

	@Test
	public void testYouTubeVideoImageCollection() throws ImageCollectionSetupException {
		// try{
		// String youtubeURLStr = "http://www.youtube.com/watch?v=QP9p_XkCR68";
		// String youtubeJSON =
		// String.format("{video:{url:\"%s\"}}",youtubeURLStr);
		// ImageCollectionConfig youtubeConfig = new
		// ImageCollectionConfig(youtubeJSON);
		//
		// YouTubeVideoImageCollection col = new YouTubeVideoImageCollection();
		// col.setup(youtubeConfig);
		//
		// int i = 0;
		// for(@SuppressWarnings("unused") ImageCollectionEntry<MBFImage> im :
		// col){
		// if(i++ > 10) return;
		//
		// }
		// }
		// catch(UnsatisfiedLinkError e){
		//
		// }
		// catch(NoClassDefFoundError e){
		//
		// }
	}

	public static void main(String[] args) throws ImageCollectionSetupException {
		final String youtubeURLStr = "http://www.youtube.com/watch?v=QP9p_XkCR68";
		final YouTubeVideoImageCollection col = new YouTubeVideoImageCollection(youtubeURLStr);
		final XuggleVideoWriter io = new XuggleVideoWriter("/Users/ss/Desktop/xuggleout.mpg", col.video.getWidth(),
				col.video.getHeight(), col.video.getFPS());
		io.process(col.video);
	}
}
