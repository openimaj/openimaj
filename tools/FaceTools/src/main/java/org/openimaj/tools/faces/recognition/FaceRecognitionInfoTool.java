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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;

/**
 * A tool for printing out information about face recognisers.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class FaceRecognitionInfoTool {
	@Option(name = "-f", aliases = "--file", usage = "Recogniser file", required = true)
	File recogniserFile;

	/**
	 * The main method of the tool.
	 * 
	 * @param <T>
	 *            Type of DetectedFace
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static <T extends DetectedFace> void main(String[] args) throws IOException {
		final FaceRecognitionInfoTool options = new FaceRecognitionInfoTool();
		final CmdLineParser parser = new CmdLineParser(options);

		try {
			parser.parseArgument(args);
		} catch (final CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("java FaceRecognitionInfoTool [options...]");
			parser.printUsage(System.err);
			return;
		}

		final FaceRecognitionEngine<T, String> engine = options.getEngine();

		System.out.println("Detector:\n" + engine.getDetector());
		System.out.println();
		System.out.println("Recogniser:\n" + engine.getRecogniser());

		System.out.println();

		final List<String> people = new ArrayList<String>(engine.getRecogniser().listPeople());
		Collections.sort(people);
		System.out.println("The recogniser has been trained on " + people.size() + " distinct people:");
		System.out.println(people);
	}

	<FACE extends DetectedFace> FaceRecognitionEngine<FACE, String> getEngine() throws IOException
	{
		return FaceRecognitionEngine.load(recogniserFile);
	}
}
