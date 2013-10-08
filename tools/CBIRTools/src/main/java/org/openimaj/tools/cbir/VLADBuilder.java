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
package org.openimaj.tools.cbir;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.LocalFeatureExtractor;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.local.aggregate.VLAD;
import org.openimaj.image.indexing.vlad.VLADIndexerDataBuilder;
import org.openimaj.io.IOUtils;

/**
 * Tool to build a {@link VLAD} object and serialise it to a file.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class VLADBuilder {
	@Option(
			name = "--extractor-file",
			aliases = "-ef",
			required = true,
			usage = "the local feature extractor used to generate the input features")
	protected File extractorFile;

	@Option(
			name = "--features-dir",
			required = true,
			usage = "directory of the files containing the input local features (one per image)")
	protected File localFeaturesDir;

	@Option(
			name = "--features-regex",
			required = false,
			usage = "regular expression to match the feature filenames against")
	protected String regex;

	@Option(
			name = "--normalise",
			aliases = "-norm",
			required = false,
			usage = "should the resultant VLAD features be l2 normalised?")
	protected boolean normalise = false;

	@Option(
			name = "--sample-proportion", required = false,
			usage = "the proportion of features to sample for the clustering the VLAD centroids")
	protected float sampleProp = 0.1f;

	@Option(
			name = "--num-vlad-centroids",
			aliases = "-n",
			required = false,
			usage = "the number of centroids for VLAD (16~64). Defaults to 64.")
	protected int numVladCentroids = 64;

	@Option(
			name = "--num-iterations",
			aliases = "-ni",
			required = false,
			usage = "the number of clustering iterations (~100). Defaults to 100.")
	protected int numIterations = 100;

	@Option(
			name = "--output",
			aliases = "-o",
			required = true,
			usage = "the output file")
	protected File output;

	/**
	 * Main method
	 * 
	 * @param args
	 *            arguments
	 * @throws IOException
	 *             if an error occurs during reading or writing
	 */
	public static void main(String[] args) throws IOException {
		final VLADBuilder builder = new VLADBuilder();
		final CmdLineParser parser = new CmdLineParser(builder);

		try {
			parser.parseArgument(args);
		} catch (final CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: java -jar CBIRTool.jar VLADBuilder [options]");
			parser.printUsage(System.err);
			return;
		}

		final LocalFeatureExtractor<LocalFeature<?, ?>, MBFImage> extractor = IOUtils.readFromFile(builder.extractorFile);

		final List<File> localFeatures = new ArrayList<File>();
		getInputFiles(localFeatures, builder.localFeaturesDir,
				builder.regex == null ? null : Pattern.compile(builder.regex));

		final VLADIndexerDataBuilder vladBuilder = new VLADIndexerDataBuilder(extractor, localFeatures,
				builder.normalise,
				builder.numVladCentroids, builder.numIterations, 0, 0, 0, builder.sampleProp, 1, null);

		final VLAD<float[]> vlad = vladBuilder.buildVLAD();

		IOUtils.writeToFile(vlad, builder.output);
	}

	protected static void getInputFiles(List<File> localFeatures, File dir, Pattern pattern) {
		for (final File f : dir.listFiles()) {
			if (f.isDirectory()) {
				getInputFiles(localFeatures, f, pattern);
			} else {
				if (pattern == null || pattern.matcher(f.getName()).matches()) {
					localFeatures.add(f);
				}
			}
		}
	}
}
