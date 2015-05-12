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
package org.openimaj.image.analysis.watershed;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.image.analysis.watershed.event.ComponentStackMergeListener;
import org.openimaj.util.tree.TreeNode;
import org.openimaj.util.tree.TreeNodeImpl;

/**
 * A listener that listens to the watershed algorithm progress and creates a
 * region tree as the processing takes place.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class MergeTreeBuilder implements ComponentStackMergeListener
{
	Logger logger = Logger.getLogger(MergeTreeBuilder.class);

	/** This is the tree we build */
	private TreeNode<Component> tree = null;

	/**
	 * Because we're creating components to store the history, the components
	 * that are being processed by the algorithm are not the same as those in
	 * our tree, so we must provide a map so that we can join up the tree
	 * afterwards.
	 */
	private Map<Component, TreeNode<Component>> map = null;

	/**
	 * Default constructor
	 */
	public MergeTreeBuilder()
	{
		map = new HashMap<Component, TreeNode<Component>>();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.analysis.watershed.event.ComponentStackMergeListener#componentsMerged(org.openimaj.image.analysis.watershed.Component,
	 *      org.openimaj.image.analysis.watershed.Component)
	 */
	@Override
	public void componentsMerged(Component c1, Component c2)
	{
		// logger.debug( "Map: "+map );
		logger.debug("Component c1: " + c1);
		logger.debug("Component c2: " + c2);

		// Create a tree node for the component if it doesn't
		// exist already
		TreeNode<Component> c1xtn = map.get(c1);
		if (c1xtn == null)
		{
			logger.debug("c1 not found");
			c1xtn = new TreeNodeImpl<Component>();
			final Component c1x = c1.clone();
			c1xtn.setValue(c1x);
			map.put(c1, c1xtn);
		}

		// Add all the pixels from c2 into our copy of c1
		c1xtn.getValue().merge(c2);

		// Create a tree node for the second component
		// if it doesn't exist already
		TreeNode<Component> c2xtn = map.get(c2);
		if (c2xtn == null)
		{
			logger.debug("c2 not found");
			c2xtn = new TreeNodeImpl<Component>();
			final Component c2x = c2.clone();
			c2xtn.setValue(c2x);
			map.put(c2, c2xtn);
		}

		// logger.debug("Linking " + c1xtn + " and " + c2xtn);

		// Link the tree nodes
		c1xtn.addChild(c2xtn);
		this.tree = c1xtn;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.analysis.watershed.event.ComponentStackMergeListener#componentPromoted(org.openimaj.image.analysis.watershed.Component)
	 */
	@Override
	public void componentPromoted(Component c1)
	{
		final TreeNode<Component> c1xtn_old = map.get(c1);

		final Component c1x = c1.clone();

		final TreeNode<Component> c1xtn = new TreeNodeImpl<Component>();
		c1xtn.setValue(c1x);

		map.put(c1, c1xtn);

		if (c1xtn_old != null)
			c1xtn.addChild(c1xtn_old);
	}

	/**
	 * Return the tree that has been built.
	 *
	 * @return the tree
	 */
	public TreeNode<Component> getTree()
	{
		return tree;
	}
}
