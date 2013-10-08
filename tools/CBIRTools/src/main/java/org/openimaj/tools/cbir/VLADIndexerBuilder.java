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
import org.openimaj.image.indexing.vlad.VLADIndexerData;
import org.openimaj.image.indexing.vlad.VLADIndexerDataBuilder;
import org.openimaj.image.indexing.vlad.VLADIndexerDataBuilder.StandardPostProcesses;
import org.openimaj.io.IOUtils;

/**
 * Tool to build a {@link VLADIndexerData} which can be used to build efficient
 * Product-quantised PCA-VLAD indexes.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class VLADIndexerBuilder extends VLADBuilder {
	@Option(
			name = "--num-pca-dims",
			aliases = "-npca",
			usage = "the number of dimensions to project down to using PCA (~128 for normal SIFT)")
	protected int numPcaDims = 128;

	@Option(
			name = "--pca-sample-proportion",
			usage = "the percentage of images to use for computing the PCA basis")
	protected float pcaSampleProp = 1.0f;

	@Option(
			name = "--num-pq-iterations",
			aliases = "-npqi",
			usage = "the number of iterations for clustering the product quantisers (~100)")
	protected int numPqIterations = 100;

	@Option(name = "--num-pq-assigners", aliases = "-na", usage = "the number of product quantiser assigners (~16)")
	protected int numPqAssigners = 16;

	@Option(
			name = "--post-process",
			aliases = "-pp",
			usage = "the post-processing to apply to the raw features before input to VLAD")
	protected StandardPostProcesses postProcess = StandardPostProcesses.NONE;

	/**
	 * Main method
	 * 
	 * @param args
	 *            arguments
	 * @throws IOException
	 *             if an error occurs during reading or writing
	 */
	public static void main(String[] args) throws IOException {
		final VLADIndexerBuilder builder = new VLADIndexerBuilder();
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
				builder.normalise, builder.numVladCentroids, builder.numIterations, builder.numPcaDims,
				builder.numPqIterations, builder.numPqAssigners, builder.sampleProp, builder.pcaSampleProp,
				builder.postProcess);

		final VLADIndexerData vlad = vladBuilder.buildIndexerData();

		IOUtils.writeToFile(vlad, builder.output);
	}
}
