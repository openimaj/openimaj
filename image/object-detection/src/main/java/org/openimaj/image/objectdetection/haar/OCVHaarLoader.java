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
package org.openimaj.image.objectdetection.haar;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Support for reading OpenCV Haar Cascade XML files. Currently only supports
 * the old-style format.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class OCVHaarLoader {
	private static final float ICV_STAGE_THRESHOLD_BIAS = 0.0001f;

	private static final String NEXT_NODE = "next";
	private static final String PARENT_NODE = "parent";
	private static final String STAGE_THRESHOLD_NODE = "stage_threshold";
	private static final String ANONYMOUS_NODE = "_";
	private static final String RIGHT_NODE_NODE = "right_node";
	private static final String RIGHT_VAL_NODE = "right_val";
	private static final String LEFT_NODE_NODE = "left_node";
	private static final String LEFT_VAL_NODE = "left_val";
	private static final String THRESHOLD_NODE = "threshold";
	private static final String TILTED_NODE = "tilted";
	private static final String RECTS_NODE = "rects";
	private static final String FEATURE_NODE = "feature";
	private static final String TREES_NODE = "trees";
	private static final String STAGES_NODE = "stages";
	private static final String SIZE_NODE = "size";
	private static final String OCV_STORAGE_NODE = "opencv_storage";

	static class TreeNode {
		HaarFeature feature;
		float threshold;
		float left_val;
		float right_val;
		int left_node = -1;
		int right_node = -1;
	}

	static class StageNode {
		private int parent = -1;
		private int next = -1;
		private float threshold;
		private List<List<TreeNode>> trees = new ArrayList<List<TreeNode>>();
	}

	static class OCVHaarClassifierNode {
		int width;
		int height;
		String name;
		boolean hasTiltedFeatures = false;
		List<StageNode> stages = new ArrayList<StageNode>();
	}

	/**
	 * Read using an XML Pull Parser. This requires the exact format of the xml
	 * is consistent (i.e. element order is consistent). Checks are made at each
	 * node to ensure that we're reading the correct data.
	 * 
	 * @param in
	 *            the InputStream to consume
	 * @return the parsed cascade
	 * @throws IOException
	 */
	static OCVHaarClassifierNode readXPP(InputStream in) throws IOException {
		try {
			final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			final XmlPullParser reader = factory.newPullParser();

			reader.setInput(in, null);

			reader.nextTag(); // opencv_storage
			checkNode(reader, OCV_STORAGE_NODE);

			reader.nextTag(); // haarcascade_{type}
			if (!"opencv-haar-classifier".equals(reader.getAttributeValue(null, "type_id"))) {
				throw new IOException("Unsupported format: " + reader.getAttributeValue(null, "type_id"));
			}

			final OCVHaarClassifierNode root = new OCVHaarClassifierNode();
			root.name = reader.getName();

			reader.nextTag(); // <size>
			checkNode(reader, SIZE_NODE);

			final String sizeStr = reader.nextText();
			final String[] widthHeight = sizeStr.trim().split(" ");
			if (widthHeight.length != 2) {
				throw new IOException("expecting 'w h' for size element, got: " + sizeStr);
			}

			root.width = Integer.parseInt(widthHeight[0]);
			root.height = Integer.parseInt(widthHeight[1]);

			reader.nextTag(); // <stages>
			checkNode(reader, STAGES_NODE);

			// parse stage tags
			while (reader.nextTag() == XmlPullParser.START_TAG) { // <_>
				checkNode(reader, ANONYMOUS_NODE);

				final StageNode currentStage = new StageNode();
				root.stages.add(currentStage);

				reader.nextTag(); // <trees>
				checkNode(reader, TREES_NODE);

				while (reader.nextTag() == XmlPullParser.START_TAG) { // <_>
					checkNode(reader, ANONYMOUS_NODE);

					final List<TreeNode> currentTree = new ArrayList<TreeNode>();
					currentStage.trees.add(currentTree);

					while (reader.nextTag() == XmlPullParser.START_TAG) { // <_>
						checkNode(reader, ANONYMOUS_NODE);

						final List<WeightedRectangle> regions = new ArrayList<WeightedRectangle>(3);

						reader.nextTag(); // <feature>
						checkNode(reader, FEATURE_NODE);

						reader.nextTag(); // <rects>
						checkNode(reader, RECTS_NODE);

						while (reader.nextTag() == XmlPullParser.START_TAG) { // <_>
							checkNode(reader, ANONYMOUS_NODE);
							regions.add(WeightedRectangle.parse(reader.nextText()));
						}

						reader.nextTag(); // <tilted>
						checkNode(reader, TILTED_NODE);
						final boolean tilted = "1".equals(reader.nextText());

						if (tilted)
							root.hasTiltedFeatures = true;

						reader.nextTag(); // </feature>
						checkNode(reader, FEATURE_NODE);

						final HaarFeature currentFeature = HaarFeature.create(regions, tilted);

						reader.nextTag(); // <threshold>
						checkNode(reader, THRESHOLD_NODE);
						final float threshold = (float) Double.parseDouble(reader.nextText());

						final TreeNode treeNode = new TreeNode();
						treeNode.threshold = threshold;
						treeNode.feature = currentFeature;

						reader.nextTag(); // <left_val> || <left_node>
						checkNode(reader, LEFT_VAL_NODE, LEFT_NODE_NODE);
						final String leftText = reader.nextText();
						if ("left_val".equals(reader.getName())) {
							treeNode.left_val = Float.parseFloat(leftText);
						} else {
							// find leftIndexed classifier
							treeNode.left_node = Integer.parseInt(leftText);
						}
						reader.nextTag(); // <right_val> || <right_node>
						checkNode(reader, RIGHT_VAL_NODE, RIGHT_NODE_NODE);
						final String rightText = reader.nextText();
						if ("right_val".equals(reader.getName())) {
							treeNode.right_val = Float.parseFloat(rightText);
						} else {
							// find right indexed classifier (put off the lookup
							// until later)
							treeNode.right_node = Integer.parseInt(rightText);
						}

						reader.nextTag(); // </_>
						checkNode(reader, ANONYMOUS_NODE);
						currentTree.add(treeNode);
					}
				}

				reader.nextTag(); // <stage_threshold>
				checkNode(reader, STAGE_THRESHOLD_NODE);
				currentStage.threshold = Float.parseFloat(reader.nextText()) - ICV_STAGE_THRESHOLD_BIAS;

				reader.nextTag(); // <parent>
				checkNode(reader, PARENT_NODE);
				currentStage.parent = Integer.parseInt(reader.nextText());

				reader.nextTag(); // <next>
				checkNode(reader, NEXT_NODE);
				currentStage.next = Integer.parseInt(reader.nextText());

				reader.nextTag(); // </_>
				checkNode(reader, ANONYMOUS_NODE);
			}

			return root;
		} catch (final XmlPullParserException ex) {
			throw new IOException(ex);
		}
	}

	/**
	 * Read the cascade from an OpenCV xml serialisation. Currently this only
	 * supports the old-style cascade xml.
	 * 
	 * @param is
	 *            the stream to read from
	 * @return the cascade object
	 * @throws IOException
	 */
	public static StageTreeClassifier read(InputStream is) throws IOException {
		final OCVHaarClassifierNode root = readXPP(is);

		return buildCascade(root);
	}

	private static StageTreeClassifier buildCascade(OCVHaarClassifierNode root) throws IOException {
		return new StageTreeClassifier(root.width, root.height, root.name, root.hasTiltedFeatures,
				buildStages(root.stages));
	}

	private static Stage buildStages(List<StageNode> stageNodes) throws IOException {
		final Stage[] stages = new Stage[stageNodes.size()];
		for (int i = 0; i < stages.length; i++) {
			final StageNode node = stageNodes.get(i);

			stages[i] = new Stage(node.threshold, buildClassifiers(node.trees), null, null);
		}

		Stage root = null;
		boolean isCascade = true;
		for (int i = 0; i < stages.length; i++) {
			final StageNode node = stageNodes.get(i);

			if (node.parent == -1 && node.next == -1) {
				if (root == null) {
					root = stages[i];
				} else {
					throw new IOException("Inconsistent cascade/tree: multiple roots found");
				}
			}

			if (node.parent != -1) {
				// if it's a tree, multiple nodes might have the same parent,
				// but the first one we see should set the successStage
				if (stages[node.parent].successStage == null) {
					stages[node.parent].successStage = stages[i];
				}
			}

			if (node.next != -1) {
				isCascade = false; // it's a tree
				stages[i].failureStage = stages[node.next];
			}
		}

		if (!isCascade) {
			optimiseTree(root);
		}

		return root;
	}

	/**
	 * Any failure along a success branch after a node that has a failure node
	 * should result in that failure nodes branch being executed. In order to
	 * simplify the iteration through the tree, we link all failure nodes of
	 * children of the success branch to the appropriate failure node. For
	 * example,
	 * 
	 * <pre>
	 * 	3 -> 4 -> 5 -> 7 -> 9
	 *            |
	 *            | (failure) 
	 *           \ /
	 *            6 -> 8 -> 10
	 * </pre>
	 * 
	 * becomes:
	 * 
	 * <pre>
	 * 	3 -> 4 -> 5 -> 7 -> 9
	 *            |    |     |
	 *            +----/-----/
	 *            |
	 *           \ /
	 *            6 -> 8 -> 10
	 * </pre>
	 * 
	 * Note: implementation based on Matt Nathan's Java port of the OpenCV Haar
	 * code.
	 * 
	 * @param root
	 *            the root of the tree
	 */
	private static void optimiseTree(Stage root) {
		final Deque<Stage> stack = new ArrayDeque<Stage>();
		stack.push(root);

		Stage failureStage = null;
		while (!stack.isEmpty()) {
			final Stage stage = stack.pop();

			if (stage.failureStage == null) {
				// child of failure branch
				stage.failureStage = failureStage;

				if (stage.successStage != null) {
					stack.push(stage.successStage);
				}
			} else if (stage.failureStage != failureStage) {
				// new failure branch
				stack.push(stage);

				failureStage = stage.failureStage;

				if (stage.successStage != null) {
					stack.push(stage.successStage);
				}
			} else {
				// old failure branch
				stack.push(stage.failureStage);

				failureStage = null;
			}
		}
	}

	private static Classifier[] buildClassifiers(final List<List<TreeNode>> trees) {
		final Classifier[] classifiers = new Classifier[trees.size()];

		for (int i = 0; i < classifiers.length; i++) {
			classifiers[i] = buildClassifier(trees.get(i));
		}

		return classifiers;
	}

	private static Classifier buildClassifier(final List<TreeNode> tree) {
		return buildClassifier(tree, tree.get(0));
	}

	private static Classifier buildClassifier(final List<TreeNode> tree, TreeNode current) {
		final HaarFeatureClassifier fc = new HaarFeatureClassifier(current.feature, current.threshold, null, null);

		if (current.left_node == -1) {
			fc.left = new ValueClassifier(current.left_val);
		} else {
			fc.left = buildClassifier(tree, tree.get(current.left_node));
		}

		if (current.right_node == -1) {
			fc.right = new ValueClassifier(current.right_val);
		} else {
			fc.right = buildClassifier(tree, tree.get(current.right_node));
		}

		return fc;
	}

	private static void checkNode(XmlPullParser reader, String... expected) throws IOException {
		for (final String e : expected)
			if (e.equals(reader.getName()))
				return;

		throw new IOException("Unexpected tag: " + reader.getName() + " (expected: " + Arrays.toString(expected) + ")");
	}
}
