package org.openimaj.demos.sandbox.image;

import java.io.File;
import java.io.IOException;

import org.openimaj.feature.MultidimensionalFloatFV;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.aggregate.VLAD;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.assignment.hard.ExactByteAssigner;

public class VLADDemo {
	public static void main(String[] args) throws IOException {
		final FImage image = ImageUtilities.readF(new File("/Users/jsh2/Data/ukbench/full/ukbench01000.jpg"));
		final DoGSIFTEngine engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(false);

		final LocalFeatureList<Keypoint> features = engine.findFeatures(image);
		final ByteCentroidsResult centroids = IOUtils.read(new File("/Users/jsh2/Desktop/ukbench16.voc"),
				ByteCentroidsResult.class);

		final ExactByteAssigner assigner = new ExactByteAssigner(centroids);

		final VLAD<byte[]> vlad = new VLAD<byte[]>(assigner, centroids.centroids);
		final MultidimensionalFloatFV agg = vlad.aggregate(features);

		System.out.println(agg);

		DisplayUtilities.display(VLAD.drawDescriptor(agg.values, 16, 4, 8));
	}
}
