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
package org.openimaj.tools.similaritymatrix.modes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.kohsuke.args4j.Option;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.renderer.FImageRenderer;
import org.openimaj.image.renderer.RenderHints;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.matrix.similarity.SimilarityMatrix;
import org.openimaj.math.matrix.similarity.processor.MultidimensionalScaling;
import org.openimaj.util.pair.IndependentPair;

public class MDS implements ToolMode {
	@Option(name="--num-iterations", required=false, usage="number of iterations")
	int numIterations = 1000;

	@Option(name="--rate", required=false, usage="learning rate")
	double rate = 0.01;

	@Option(name="--output-image", aliases="-im", required=false, usage="output as image rather than text")
	boolean imageOutputMode = false;
	
	@Option(name="--output-image-size", aliases="-s", required=false, usage="set the size of the output image")
	int imageSize = 1000;

	@Override
	public void process(SimilarityMatrix matrix, File output) throws Exception {
		MultidimensionalScaling mds = new MultidimensionalScaling(numIterations, rate);
		matrix.processInplace(mds);

		if (output == null) {
			if (imageOutputMode) { 
				ImageUtilities.write(render(mds.getPoints(), imageSize), "png", System.out);
			} else {
				System.out.println(mds);
			}
		} else {
			if (imageOutputMode) {
				ImageUtilities.write(render(mds.getPoints(), imageSize), output);
			} else {
				BufferedWriter bw = new BufferedWriter(new FileWriter(output));
				bw.write(mds.toString());
				bw.close();
			}
		}
	}

	protected FImage render(List<IndependentPair<String, Point2d>> pts, int sz) {
		FImage image = new FImage(sz, sz);
		FImageRenderer renderer = image.createRenderer(RenderHints.ANTI_ALIASED);


		for (IndependentPair<String, Point2d> pair : pts) {
			double x = pair.secondObject().getX();
			double y = pair.secondObject().getY();

			int ix = (int) Math.round((x + 0.5) * sz/2);
			int iy = (int) Math.round((y + 0.5) * sz/2);

			renderer.drawShapeFilled(new Circle(ix, iy, 2), 1f);
			renderer.drawText(pair.firstObject(), ix+5, iy, HersheyFont.TIMES_MEDIUM, 20, 1f);
		}
		
		return image;
	}
}