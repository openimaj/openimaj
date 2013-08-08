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
