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
package org.openimaj.demos.sandbox.image.gif;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.imageio.IIOException;

import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.resize.ResizeProcessor;

import scala.actors.threadpool.Arrays;

public class GifWriterPayground {
	private static List<Image<?, ?>> frames;

	public static void main(String[] args) throws IIOException, FileNotFoundException, IOException {
		// VideoCapture vc = new VideoCapture( 640,480 );
		createGif("/Users/ss/Pictures/emmawedding/lillygif", "2012-08-13 18.55.32-");
		createGif("/Users/ss/Pictures/emmawedding/andygif", "2012-08-13 20.06.20-");
	}

	private static void createGif(String pathname, final String filepattern) throws IIOException, FileNotFoundException,
			IOException
	{
		System.out.println("Creating " + pathname);
		final File[] files = new File(pathname).listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				final String name = pathname.getName();
				return name.startsWith(filepattern);
			}
		});
		final List<File> flist = Arrays.asList(files);
		final GifSequenceWriter gifWriter = GifSequenceWriter.createWriter(200, true, new File(pathname + "out.gif"));
		File f;
		for (int i = 0; i < files.length; i++) {
			f = flist.get(i);
			final MBFImage mbfImage = ImageUtilities.readMBF(f);
			System.out.println("Writing frame!");
			gifWriter.writeToSequence(mbfImage.process(new ResizeProcessor(640, 480)));
		}
		for (int i = files.length - 2; i > 0; i--) {
			f = flist.get(i);
			final MBFImage mbfImage = ImageUtilities.readMBF(f);
			System.out.println("Writing frame!");
			gifWriter.writeToSequence(mbfImage.process(new ResizeProcessor(640, 480)));
		}
		gifWriter.close();
	}
}
