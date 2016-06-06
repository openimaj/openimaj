package org.openimaj.workinprogress;

import java.io.File;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator;
import org.openimaj.video.capture.VideoCapture;

public class Snap {
	MemoryLocalFeatureList<Keypoint> mapData = new MemoryLocalFeatureList<Keypoint>();
	File[] mapKeypointFiles = {
			new File("/Users/jon/Consulting/MapSnapper/map_data/MAP_DATA_2K/NY42.key"),
			new File("/Users/jon/Consulting/MapSnapper/map_data/MAP_DATA_2K/NY44.key"),
			new File("/Users/jon/Consulting/MapSnapper/map_data/MAP_DATA_2K/NY46.key")
	};
	int[] baseNorthing = { 200, 400, 600 };
	int[] baseEasting = { 400, 400, 400 };

	// scale_factor determines how many pixels correspond to one grid square
	int scaleFactor = 100;

	// map_dimension is the height/width of each map tile (they must all be the
	// same size)
	int mapDimension = 2000;
	private DoGSIFTEngine engine;
	private ConsistentLocalFeatureMatcher2d<Keypoint> matcher;

	public Snap() {
		loadMapData();

		engine = new DoGSIFTEngine();
		// engine.getOptions().setDoubleInitialImage(false);

		final FastBasicKeypointMatcher<Keypoint> innerMatcher = new FastBasicKeypointMatcher<Keypoint>(8);
		matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(innerMatcher);
		final RobustAffineTransformEstimator estimator = new RobustAffineTransformEstimator(0.5);
		matcher.setFittingModel(estimator);
		matcher.setModelFeatures(mapData);

	}

	protected void loadMapData() {
		for (int i = 0; i < mapKeypointFiles.length; i++) {
			try {
				// read keypoints for current tile
				final MemoryLocalFeatureList<Keypoint> map = MemoryLocalFeatureList.read(mapKeypointFiles[i],
						Keypoint.class);

				// loop through each keypoint in the tile and
				// adjust its position, so that the keypoints
				// x and y position is its true OSGB grid ref!
				for (final Keypoint k : map) {
					final int east = baseEasting[i] + Math.round(10.0f * (k.getX() / scaleFactor));
					final int north = baseNorthing[i]
							+ Math.round(10.0f * ((mapDimension - k.getY()) / scaleFactor)); // different
					// coord systems!!

					k.setX(east);
					k.setY(north);
				}

				mapData.addAll(map);
			} catch (final Exception ex) {
				System.out.println(ex);
				System.exit(1);
			}
		}
	}

	/**
	 * Estimate a grid reference from an image using the MapSnapper algorithm.
	 * 
	 * @param image
	 *
	 * @return grid reference string
	 * @throws Exception
	 */
	public String getGridRef(FImage image) throws Exception {
		final LocalFeatureList<Keypoint> keys = engine.findFeatures(image);
		if (matcher.findMatches(keys)) {
			System.out.println("Done! -- Found Match");

			final Point2d coords = matcher.getModel().predict(
					new Point2dImpl(image.width / 2.0f, image.height / 2.0f));

			final int east = Math.round(coords.getX());
			final int north = Math.round(coords.getY());

			final Object[] gr = { "NY", east, north };
			return String.format("%2s%3d%3d\n", gr);
		} else {
			return "Match Not Found";
		}
	}

	public static void main(String[] args) throws Exception {
		final Snap snap = new Snap();
		final VideoCapture vc = new VideoCapture(640, 480);

		while (true) {
			final FImage img = vc.getNextFrame().flatten();
			DisplayUtilities.displayName(img, "Live Video");
			final FImage patch = ResizeProcessor.resample(img, 160, 120);// .extractCenter(120,
			// 120);
			final String res = snap.getGridRef(patch);
			if (!res.contains("Not"))
				System.out.println(res);
		}
	}
}
