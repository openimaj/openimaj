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
package org.openimaj.util.tree;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * @author Jonathan Hare
 *
 * @param <T> The type of node the tree can hold
 */
public interface TreeNode<T> extends Iterable<TreeNode<T>>, Serializable {
	/***
	 * @return The value held at the node
	 */
	public abstract T getValue();

	/***
	 * Set the node value
	 * @param obj new value
	 */
	public abstract void setValue(T obj);

	/***
	 * Add a child node.
	 * @param tn the child node
	 */
	public abstract void addChild(TreeNode<T> tn);
	/***
	 * Remove a child node (if it exists)
	 * @param tn the child node to remove
	 */
	public abstract void removeChild(TreeNode<T> tn);

	/***
	 * Get all children
	 * @return list of tree nodes
	 */
	public abstract List<TreeNode<T>> getChildren();

	@Override
	public abstract Iterator<TreeNode<T>> iterator();

	/***
	 * Does this tree node contain no children
	 * @return contains children
	 */
	public abstract boolean isLeaf();
}
