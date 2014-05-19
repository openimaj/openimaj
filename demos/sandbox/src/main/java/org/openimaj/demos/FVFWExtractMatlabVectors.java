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
