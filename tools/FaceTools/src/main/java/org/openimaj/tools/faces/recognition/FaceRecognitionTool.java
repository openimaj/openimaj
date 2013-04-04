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
package org.openimaj.tools.faces.recognition;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.util.pair.IndependentPair;

/**
 * A tool for using pre-trained face recognisers
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FaceRecognitionTool {

	/**
	 * The main method of the tool.
	 * 
	 * @param <T>
	 *            Type of DetectedFace
	 * 
	 * @param args
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static <T extends DetectedFace> void main(String[] args) throws IOException, ClassNotFoundException {
		final FaceRecognitionToolOptions options = new FaceRecognitionToolOptions();
		final CmdLineParser parser = new CmdLineParser(options);

		try {
			parser.parseArgument(args);
		} catch (final CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("java FaceRecogniserTrainingTool [options...] IMAGE-FILES-OR-DIRECTORIES");
			parser.printUsage(System.err);
			return;
		}

		final FaceRecognitionEngine<T, String> engine = options.getEngine();

		if (options.files == null) {
			System.err.println("No images to test");
			return;
		}

		for (final File f : options.files) {
			System.out.println(f);

			final List<IndependentPair<T, ScoredAnnotation<String>>> res = engine.recogniseBest(ImageUtilities.readF(f));
			for (int i = 0; i < res.size(); i++) {
				System.out.println("Face " + i + ": " + res.get(i).firstObject().getBounds() + " -> "
						+ res.get(i).secondObject());
			}

			System.out.println();
		}
	}

}
