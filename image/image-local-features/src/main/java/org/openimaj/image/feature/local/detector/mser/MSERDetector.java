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
import java.util.LinkedList;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.analysis.watershed.Component;
import org.openimaj.util.tree.TreeNode;

/**
 * Takes a merge tree from the watershed algorithm and detects MSERs. The
 * {@link #detect()} method returns a list of MSER components. The components
 * are also marked as MSERs in the merge tree, such that the tree can be used to
 * determine the hierarchical nature of the MSERs. Use the
 * {@link Component#isMSER} to determine if the component in the tree is an
 * MSER.
 * 
 * <blockquote> (From http://www.vlfeat.org/overview/mser.html) The stability of
 * an extremal region R is the inverse of the relative area variation of the
 * region R when the intensity level is increased by d. Formally, the variation
 * is defined as:
 * 
 * <pre>
 * <code>
 * |R(+d) - R|
 * -----------
 *      |R|
 *  </code>
 * </pre>
 * 
 * where |R| denotes the area of the extremal region R, R(+d) is the extremal
 * region +d levels up which contains R and |R(+d) - R| is the area difference
 * of the two regions.
 * 
 * A stable region has a small variation. The algorithm finds regions which are
 * "maximally stable", meaning that they have a lower variation than the regions
 * one level below or above. Note that due to the discrete nature of the image,
 * the region below / above may be coincident with the actual region, in which
 * case the region is still deemed maximal.
 * 
 * However, even if an extremal region is maximally stable, it might be rejected
 * if:
 * 
 * it is too big (see the parameter maxArea); it is too small (see the parameter
 * minArea); it is too unstable (see the parameter maxVariation); it is too
 * similar to its parent MSER (see the parameter minDiversity). </blockquote>
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 
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
public class MSERDetector {
	/** The maximum area an MSER can take, in pixels */
	private int maxArea = Integer.MAX_VALUE;

	/** The minimum area an MSER can be, in pixels */
	private int minArea = 1;

	/** The minimum stability an MSER must have */
	private float maxVariation = 1;

	/** The minimum diversity with its parent than an MSER must have */
	private float minDiversity = 0;

	/** The stability delta */
	private int delta = 10;

	/** The tree to be processed */
	private TreeNode<Component> mergeTree = null;

	/**
	 * Constructor that takes the merge tree from the watershed algorithm.
	 * 
	 * @param mergeTree
	 *            The merge tree
	 */
	public MSERDetector(TreeNode<Component> mergeTree) {
		this.mergeTree = mergeTree;
	}

	/**
	 * Detect MSERs in the merge tree provided in the constructor.
	 * 
	 * @return a list of detected regions.
	 */
	public List<Component> detect() {
		final List<Component> detectedRegions = processTree(mergeTree);
		return detectedRegions;
	}

	/**
	 * Traverses the tree and attempts to find the minima (in terms of the rate
	 * of change of size over intensity) in the chains that form from the
	 * branches of the tree.
	 * 
	 * @param mergeTree
	 *            The merge tree.
	 * @return A list of stable regions
	 */
	private List<Component> processTree(TreeNode<Component> mergeTree) {
		final List<Component> detectedRegions = new ArrayList<Component>();
		if (mergeTree != null)
			processTreeAux(mergeTree, detectedRegions, new LinkedList<TreeNode<Component>>());
		return detectedRegions;
	}

	/**
	 * Recursive tree-traversal method for the processTree function.
	 * 
	 * @param treeNode
	 *            The current tree node being processed
	 * @param components
	 *            The list of stable regions so far detected
	 * @param path
	 *            The path to treeNode that has been taken so far
	 */
	private void processTreeAux(TreeNode<Component> treeNode, List<Component> components,
			LinkedList<TreeNode<Component>> path)
	{
		// Is there a branch or a chain?
		// Also no need to iterate down the tree if the intensity level of
		// this component is already less than the delta, because no regions
		// below this point couldn't possibly be considered stable.
		if (treeNode.getChildren() != null && treeNode.getChildren().size() > 0 &&
				treeNode.getValue().pivot.value >= delta)
		{
			path.add(treeNode);

			// Recursive traversal of tree
			for (final TreeNode<Component> node : treeNode.getChildren())
				processTreeAux(node, components, path);
		}

		// Remove this node, if present
		// (it won't be present for leaf nodes)
		path.remove(treeNode);
		final Component child = treeNode.getValue();

		// The size of the component must be within the defined limits
		if (child.size() < maxArea && child.size() > minArea) {
			// Now calculate whether this component could be an MSER
			final Component parent = getAppropriateParent(path, child.pivot.value, delta);

			// If there's no appropriate parent node (above delta) then
			// this node cannot be an MSER
			if (parent != null) {
				final int intensityDifference = Math.abs(parent.pivot.value - child.pivot.value);

				// MSERs can only occur if the difference is greater than the
				// delta.
				if (intensityDifference >= delta) {
					float variation;

					if (intensityDifference == delta)
						variation = Math.abs(child.size() - parent.size()) / (float) parent.size();
					else
						variation = 0; // the virtual node at level-delta has
										// the same size as the current

					// The variation must be less than the defined maximum
					if (variation < maxVariation) {
						child.isMSER = true;
						components.add(child);

						// duplicate detection: if any children are mser of
						// similar size, then remove them
						for (final TreeNode<Component> childNode : treeNode.getChildren()) {
							if (childNode.getValue().isMSER) {
								final float div = (float) (child.size() - childNode.getValue().size())
										/ (float) child.size();

								if (div < minDiversity) {
									childNode.getValue().isMSER = false;
									components.remove(childNode.getValue());
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the next appropriate parent where the parent component has a
	 * greylevel that is at least delta more than the current greylevel
	 * 
	 * @param path
	 *            The path to search
	 * @param currentGL
	 *            The current grey level of the node
	 * @param delta
	 *            The delta to use
	 * @return The next component up the tree path that has an appropriate
	 *         greylevel, or NULL if there isn't one
	 */
	private Component getAppropriateParent(LinkedList<TreeNode<Component>> path, int currentGL, int delta) {
		// Iterate through the path backwards
		for (int i = path.size() - 1; i >= 0; i--) {
			if (Math.abs(path.get(i).getValue().pivot.value - currentGL) >= delta)
				return path.get(i).getValue();
		}

		return null;
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

	/**
	 * @return the mergeTree
	 */
	public TreeNode<Component> getMergeTree() {
		return mergeTree;
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
}
