package org.openimaj.demos.sandbox.image.vlad;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.demos.sandbox.vlad.VLADIndexer;
import org.openimaj.feature.MultidimensionalFloatFV;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.normalisation.HellingerNormaliser;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.aggregate.VLAD;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.FloatKeypoint;
import org.openimaj.image.feature.local.keypoints.Keypoint;

public class VLADDemo {
	public static void main(String[] args) throws IOException {
		final FImage image = ImageUtilities.readF(new File("/Users/jsh2/Data/ukbench/full/ukbench01001.jpg"));
		final DoGSIFTEngine engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(false);

		final LocalFeatureList<Keypoint> features = engine.findFeatures(image);
		final List<FloatKeypoint> fkeys = FloatKeypoint.convert(features);

		for (final FloatKeypoint k : fkeys)
			HellingerNormaliser.normalise(k.vector, 0);

		final VLADIndexer indexer = VLADIndexer.read(new File("/Users/jsh2/vlad-indexer-ukbench-2x.dat"));

		// final ByteCentroidsResult centroids = IOUtils.read(new
		// File("/Users/jsh2/Desktop/ukbench16.voc"),
		// ByteCentroidsResult.class);
		// final ExactByteAssigner assigner = new ExactByteAssigner(centroids);
		// final VLAD<byte[]> vlad = new VLAD<byte[]>(assigner,
		// centroids.centroids, true);
		// final MultidimensionalFloatFV agg = vlad.aggregate(features);

		final MultidimensionalFloatFV agg = indexer.vlad.aggregate(fkeys);

		System.out.println(agg);

		DisplayUtilities.display(VLAD.drawDescriptor(agg.values, 64, 4, 8));
	}
}
