package org.openimaj.demos;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openimaj.feature.FloatFV;
import org.openimaj.io.IOUtils;
import org.openimaj.util.iterator.TextLineIterable;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLSingle;

public class FVFWExtractMatlabVectors {

	public static void main(String[] args) throws IOException {
		final Map<Integer, String> database = readDatabase();

		final File outbase = new File("/Users/jon/Data/lfw/matlab-fvs/");

		for (int i = 1; i <= 128; i++) {
			final File chunk = new File(
					"/Users/jon/Downloads/data/lfw_aligned/SIFT_1pix_PCA64_GMM512/features/poolfv/1/", String.format(
							"feat_%d-v6.mat", i));

			System.out.println(chunk);

			final MatFileReader reader = new MatFileReader(chunk);
			final MLSingle feats = (MLSingle) reader.getMLArray("chunk");
			final MLDouble index = (MLDouble) reader.getMLArray("index");

			for (int j = 0; j < index.getN(); j++) {
				final int id = (int) (double) index.get(0, j);

				final File outfile = new File(outbase, database.get(id).replace(".jpg", ".bin"));
				outfile.getParentFile().mkdirs();

				final float[] vec = new float[feats.getM()];
				for (int k = 0; k < feats.getM(); k++) {
					vec[k] = feats.get(k, j);
				}

				final FloatFV fv = new FloatFV(vec);
				IOUtils.writeBinary(outfile, fv);
			}
		}

	}

	private static Map<Integer, String> readDatabase() {
		final HashMap<Integer, String> map = new HashMap<Integer, String>();
		for (final String line : new TextLineIterable(new File("/Users/jon/Downloads/data/shared/info/database.csv"))) {
			final String[] parts = line.split(",");

			final String value = parts[0].trim();
			final int key = Integer.parseInt(parts[1].trim());

			map.put(key, value);
		}

		return map;
	}
}
