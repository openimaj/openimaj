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
package org.openimaj.feature.keypoints.matcher;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.KeypointCorrespondenceTestHelper;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.transforms.HomographyRefinement;
import org.openimaj.math.geometry.transforms.estimation.RobustHomographyEstimator;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 *         Check whether the KeypointCorrespondence class works. This provides
 *         some basic ways of checking whether two keypoints are the same by
 *         checking the distance of an affine transform between matching pairs
 * 
 */
public class KeypointCorrespondenceTest {

	/**
	 * Simple case, no transform whatsoever, identity transform matrix.
	 */
	@Test
	public void testSimpleKeypointCorrespondence() {

		final List<Keypoint> original = new ArrayList<Keypoint>();
		final Matrix transform = Matrix.constructWithCopy(new double[][] {
				{ 1, 0, 0 },
				{ 0, 1, 0 },
				{ 0, 0, 1 }
		});

		final Keypoint a = new Keypoint();
		final Keypoint b = new Keypoint();
		final Keypoint c = new Keypoint();

		a.x = 1;
		a.y = 1;
		a.ivec = new byte[2];
		a.ori = 0.5f;
		a.scale = 1.0f;
		b.x = 3;
		b.y = 4;
		b.ivec = new byte[2];
		b.ori = 0.5f;
		b.scale = 2.0f;
		c.x = 40;
		c.y = 30;
		c.ivec = new byte[2];
		c.ori = 0.5f;
		c.scale = 3.0f;

		original.add(a);
		original.add(b);
		original.add(c);

		final List<Pair<Keypoint>> pairs = new ArrayList<Pair<Keypoint>>();
		pairs.add(new Pair<Keypoint>(a, a));
		pairs.add(new Pair<Keypoint>(b, b));
		pairs.add(new Pair<Keypoint>(c, b));

		final float result = KeypointCorrespondenceTestHelper.correspondance(pairs, transform);

		assertTrue(result == 2);
	}

	/**
	 * Check the keypoint correspondance given a small affine shift.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testRandomTransformCorrespondence() throws IOException {
		final FImage cat = ImageUtilities.readF(this.getClass().getResourceAsStream("/org/openimaj/image/data/cat.jpg"));
		final Matrix transform = KeypointCorrespondenceTestHelper.generateMildTransform(cat);
		final FImage dizzy = cat.transform(transform);

		final DoGSIFTEngine kpe = new DoGSIFTEngine();

		final List<Keypoint> catkpl = kpe.findFeatures(cat);
		final List<Keypoint> dizzykpl = kpe.findFeatures(dizzy);

		final ConsistentLocalFeatureMatcher2d<Keypoint> mat = new ConsistentLocalFeatureMatcher2d<Keypoint>(
				new FastBasicKeypointMatcher<Keypoint>(8));
		mat.setFittingModel(new RobustHomographyEstimator(10.0, 1500,
				new RANSAC.PercentageInliersStoppingCondition(0.5), HomographyRefinement.NONE));
		mat.setModelFeatures(dizzykpl);
		mat.findMatches(catkpl);
		final List<Pair<Keypoint>> matches = mat.getMatches();
		System.out.println("Total matches: " + matches.size());
		System.out.println("Total keypoints in original: " + catkpl.size());
		// MBFImage comb = new MBFImage(Math.max(cat.rows, dizzy.rows), cat.cols
		// + dizzy.cols, 3);
		// comb.drawImage(new MBFImage(dizzy,dizzy,dizzy), 0, 0);
		// comb.drawImage(new MBFImage(cat,cat,cat), dizzy.cols, 0);
		// for (Pair<Keypoint> p : matches) {
		// comb.drawLine((int)p.secondObject().col, (int)p.secondObject().row,
		// dizzy.cols + (int)p.firstObject().col, (int)p.firstObject().row, new
		// Float[] {1.0F,0F,0F});
		// }
		// DisplayUtilities.display(comb);
		final float result = KeypointCorrespondenceTestHelper.correspondance(matches, transform, 10f);

		System.out.println(result);
	}
}
