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
/**
 *
 */
package org.openimaj.image.feature.local.detector.mser;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.watershed.Component;
import org.openimaj.image.analysis.watershed.MergeTreeBuilder;
import org.openimaj.image.analysis.watershed.WatershedProcessor;
import org.openimaj.image.analysis.watershed.feature.ComponentFeature;
import org.openimaj.util.tree.TreeNode;

/**
 * Detector for MSER features.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "J Matas", "O Chum", "M Urban", "T Pajdla" },
		title = "Robust wide-baseline stereo from maximally stable extremal regions",
		year = "2004",
		journal = "Image and Vision Computing",
		pages = { "761 ", " 767" },
		url = "http://www.sciencedirect.com/science/article/pii/S0262885604000435",
		number = "10",
		volume = "22",
		customData = {
				"issn", "0262-8856",
				"doi", "10.1016/j.imavis.2004.02.006",
				"keywords", "Robust metric"
		})
public class MSERFeatureGenerator {
	/**
	 * A way of representing how the MSER should be processed.
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *
	 */
	public enum MSERDirection {
		/**
		 * Upwards detection
		 */
		Up,
		/**
		 * Downwards detection
		 */
		Down,
		/**
		 * Upwards and Downwards detection
		 */
		UpAndDown
	}

	private int delta = 10;
	private int maxArea = Integer.MAX_VALUE;
	private int minArea = 1;
	private float maxVariation = Float.MAX_VALUE;
	private float minDiversity = 0;
	private Class<? extends ComponentFeature>[] featureClasses;

	/**
	 * Default constructor
	 *
	 * @param featureClasses
	 *            features to generate for each mser
	 */
	@SafeVarargs
	public MSERFeatureGenerator(Class<? extends ComponentFeature>... featureClasses) {
		this.featureClasses = featureClasses;
	}

	/**
	 * Constructor that takes all the parameters for the MSER process.
	 *
	 * @param delta
	 * @param maxArea
	 * @param minArea
	 * @param maxVariation
	 * @param minDiversity
	 * @param featureClasses
	 *            features to generate for each mser
	 */
	@SafeVarargs
	public MSERFeatureGenerator(int delta, int maxArea, int minArea, float maxVariation, float minDiversity,
			Class<? extends ComponentFeature>... featureClasses)
	{
		this(featureClasses);

		this.delta = delta;
		this.maxArea = maxArea;
		this.minArea = minArea;
		this.maxVariation = maxVariation;
		this.minDiversity = minDiversity;
	}

	/**
	 * Performs a watershed then an MSER detection on the given image and
	 * returns the MSERs.
	 *
	 * @param img
	 *            The image to analyse.
	 * @return A list of {@link Component}s
	 */
	public List<Component> generateMSERs(FImage img) {
		return generateMSERs(img, MSERDirection.UpAndDown);
	}

	/**
	 * Performs a watershed then an MSER detection on the given image and
	 * returns the MSERs.
	 *
	 * @param img
	 *            The image to analyse.#
	 * @param dir
	 *            The direction which to process the MSERS
	 * @return A list of {@link Component}s
	 */
	public List<Component> generateMSERs(FImage img, MSERDirection dir) {
		final List<MergeTreeBuilder> mtb = performWatershed(img);
		final List<Component> regions = performMSERDetection(mtb, dir);
		return regions;
	}

	/**
	 * Perform the watershed algorithm on the given image.
	 *
	 * @param img
	 *            The image to perform the watershed on
	 * @return A tuple of {@link MergeTreeBuilder}s (down first, up second)
	 */
	public List<MergeTreeBuilder> performWatershed(FImage img) {
		// Create the image analysis object
		final WatershedProcessor watershedUp = new WatershedProcessor(featureClasses);
		final WatershedProcessor watershedDown = new WatershedProcessor(featureClasses);
		final MergeTreeBuilder treeBuilderUp = new MergeTreeBuilder();
		watershedUp.addComponentStackMergeListener(treeBuilderUp);
		final MergeTreeBuilder treeBuilderDown = new MergeTreeBuilder();
		watershedDown.addComponentStackMergeListener(treeBuilderDown);

		// -----------------------------------------------------------------
		// Watershed the image to get the tree
		// -----------------------------------------------------------------
		// bottom-up watershed
		watershedUp.processImage(img);

		// Invert the image, as we must detect MSERs from both top-down
		// and bottom-up.
		img = img.inverse();
		// top-down watershed
		watershedDown.processImage(img);

		// Return the image to its original state.
		img = img.inverse();

		final List<MergeTreeBuilder> mtb = new ArrayList<MergeTreeBuilder>();
		mtb.add(treeBuilderDown);
		mtb.add(treeBuilderUp);
		return mtb;
	}

	/**
	 * Performs MSER detection on the trees provided. The input list must be a
	 * list containing {@link MergeTreeBuilder}s, the first being the downward
	 * watershed, the second being the upward watershed.
	 *
	 * @param mtbs
	 *            The list of {@link MergeTreeBuilder}s
	 * @param dir
	 *            The direction to detect MSERs from
	 * @return A list of {@link Component}s
	 */
	public List<Component> performMSERDetection(List<MergeTreeBuilder> mtbs, MSERDirection dir) {
		// Remove the MSER component flags in the trees (in case they're being
		// reused)
		clearTree(mtbs.get(0).getTree());
		clearTree(mtbs.get(1).getTree());

		// -----------------------------------------------------------------
		// Now run the MSER detector on it
		// -----------------------------------------------------------------
		// bottom up detection
		// System.out.println( mtbs.get(1).getTree() );
		List<Component> regionsUp = null;
		if (mtbs.get(1).getTree() != null && (dir == MSERDirection.Up || dir == MSERDirection.UpAndDown)) {
			final MSERDetector mser = new MSERDetector(mtbs.get(1).getTree());
			mser.setDelta(this.delta);
			mser.setMaxArea(this.maxArea);
			mser.setMinArea(this.minArea);
			mser.setMaxVariation(this.maxVariation);
			mser.setMinDiversity(this.minDiversity);
			regionsUp = mser.detect();
			// System.out.println( "Top-down detected: "+regionsUp );
		}

		// top-down detection
		List<Component> regionsDown = null;
		if (mtbs.get(0).getTree() != null && (dir == MSERDirection.Down || dir == MSERDirection.UpAndDown)) {
			final MSERDetector mser2 = new MSERDetector(mtbs.get(0).getTree());
			mser2.setDelta(this.delta);
			mser2.setMaxArea(this.maxArea);
			mser2.setMinArea(this.minArea);
			mser2.setMaxVariation(this.maxVariation);
			mser2.setMinDiversity(this.minDiversity);
			regionsDown = mser2.detect();
			// System.out.println( "Bottom-up detected: "+regionsDown );
		}

		final List<Component> regions = new ArrayList<Component>();
		if (regionsUp != null)
			regions.addAll(regionsUp);
		if (regionsDown != null)
			regions.addAll(regionsDown);

		// System.out.println( "Detected "+regions.size()+" regions ");
		// System.out.println( "Detected "+countMSERs( mtbs.get(0).getTree()
		// )+" in down tree" );
		// System.out.println( "Detected "+countMSERs( mtbs.get(1).getTree()
		// )+" in up tree" );
		return regions;
	}

	/**
	 * Removes all the MSER flags from the components in the tree
	 *
	 * @param tree
	 *            The tree to clear MSER flags
	 */
	private void clearTree(TreeNode<Component> tree) {
		if (tree == null)
			return;
		final Component c = tree.getValue();
		if (c != null)
			c.isMSER = false;
		if (tree.getChildren() != null)
			for (final TreeNode<Component> child : tree.getChildren())
				clearTree(child);
	}

	/**
	 * Returns a count of the number of components in the tree that are marked
	 * as MSERs.
	 *
	 * @param tree
	 *            The tree to count MSERs in
	 * @return the count
	 */
	public int countMSERs(TreeNode<Component> tree) {
		if (tree == null)
			return 0;
		int retVal = 0;
		final Component c = tree.getValue();
		if (c != null && c.isMSER)
			retVal++;
		if (tree.getChildren() != null)
			for (final TreeNode<Component> child : tree.getChildren())
				retVal += countMSERs(child);
		return retVal;

	}

	/**
	 * @return the delta
	 */
	public int getDelta() {
		return delta;
	}

	/**
	 * @param delta
	 *            the delta to set
	 */
	public void setDelta(int delta) {
		this.delta = delta;
	}

	/**
	 * @return the maxArea
	 */
	public int getMaxArea() {
		return maxArea;
	}

	/**
	 * @param maxArea
	 *            the maxArea to set
	 */
	public void setMaxArea(int maxArea) {
		this.maxArea = maxArea;
	}

	/**
	 * @return the minArea
	 */
	public int getMinArea() {
		return minArea;
	}

	/**
	 * @param minArea
	 *            the minArea to set
	 */
	public void setMinArea(int minArea) {
		this.minArea = minArea;
	}

	/**
	 * @return the maxVariation
	 */
	public float getMaxVariation() {
		return maxVariation;
	}

	/**
	 * @param maxVariation
	 *            the maxVariation to set
	 */
	public void setMaxVariation(float maxVariation) {
		this.maxVariation = maxVariation;
	}

	/**
	 * @return the minDiversity
	 */
	public float getMinDiversity() {
		return minDiversity;
	}

	/**
	 * @param minDiversity
	 *            the minDiversity to set
	 */
	public void setMinDiversity(float minDiversity) {
		this.minDiversity = minDiversity;
	}

}
