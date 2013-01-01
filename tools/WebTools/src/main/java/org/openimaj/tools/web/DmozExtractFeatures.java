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
package org.openimaj.tools.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.web.layout.ElementInfo;
import org.openimaj.web.layout.LayoutExtractor;

/**
 * Extract features from the webpages listed in files created by
 * {@link Dmoz2CSV}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class DmozExtractFeatures {
	final static String csvregex = ",(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))";

	/**
	 * Main method. First arg is the csv; second is the output directory.
	 * 
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final File inputCSV = new File(args[0]);
		final File outputDirBase = new File(args[1]);

		System.setOut(new PrintStream(System.out, true, "UTF-8"));

		final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputCSV), "UTF-8"));

		String it;
		while ((it = br.readLine()) != null) {
			final String[] parts = it.split(csvregex);

			final String url = parts[2];
			System.out.println(url);

			final File dir = new File(outputDirBase, parts[0].replace("\"", "") + "/" + parts[1] + "/"
					+ url.replace(":", "|").replace("/", "_"));
			final File layoutfile = new File(dir, "layout.csv");
			final File imagefile = new File(dir, "render.png");

			if (dir.exists())
				continue;
			if (!dir.mkdirs())
				continue;

			final LayoutExtractor le = new LayoutExtractor(30000L); // timeout
																	// after 30s
			if (le.load(url)) {
				final PrintWriter layoutfilePW = new PrintWriter(new FileWriter(layoutfile));

				final List<ElementInfo> info = le.getLayoutInfo();
				layoutfilePW.println(ElementInfo.getCSVHeader());
				for (final ElementInfo ei : info) {
					layoutfilePW.println(ei.toCSVString());
				}

				layoutfilePW.close();

				final MBFImage image = le.render(1024, 768);
				if (image != null)
					ImageUtilities.write(image, imagefile);
			}
		}

		br.close();
	}
}
