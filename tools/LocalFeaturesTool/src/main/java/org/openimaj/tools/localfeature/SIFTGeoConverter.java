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
package org.openimaj.tools.localfeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.SIFTGeoKeypoint;
import org.openimaj.io.IOUtils;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.Parallel;

/**
 * Simple tool to batch convert files in siftgeo format to lowe's sift format.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class SIFTGeoConverter {
	private static void getInputs(File file, List<File> files) {
		if (file.isDirectory()) {
			for (final File f : file.listFiles()) {
				getInputs(f, files);
			}
		} else {
			if (file.getName().endsWith(".siftgeo")) {
				files.add(file);
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// final File inDir = new
		// File("/Volumes/My Book/Data/ukbench/features/hesaff");
		// final File outDir = new
		// File("/Volumes/My Book/Data/ukbench/features/hesaff-sift");
		final File inDir = new File(args[0]);
		final File outDir = new File(args[1]);

		outDir.mkdirs();

		final List<File> input = new ArrayList<File>();
		getInputs(inDir, input);

		Parallel.forEach(input, new Operation<File>() {
			@Override
			public void perform(File file) {
				try {
					System.out.println(file);

					final LocalFeatureList<SIFTGeoKeypoint> sgkeys = SIFTGeoKeypoint.read(file);
					final LocalFeatureList<Keypoint> keys = new MemoryLocalFeatureList<Keypoint>(128, sgkeys.size());

					for (final SIFTGeoKeypoint sg : sgkeys) {
						final Keypoint k = new Keypoint();
						k.ivec = sg.descriptor;
						k.x = sg.location.x;
						k.y = sg.location.y;
						k.ori = sg.location.orientation;
						k.scale = sg.location.scale;
						keys.add(k);
					}

					final File path = new File(file.getAbsolutePath()
							.replace(inDir.getAbsolutePath(), outDir.getAbsolutePath())
							.replace(".siftgeo", ".sift"));
					path.getParentFile().mkdirs();
					IOUtils.writeBinary(path, keys);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
