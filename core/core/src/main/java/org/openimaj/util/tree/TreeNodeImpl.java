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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * Tree node implementation
 * @param <T> Type of the value object stored in the node.
 */
public class TreeNodeImpl<T> implements TreeNode<T> {
	private static final long serialVersionUID = 1L;
	
	/**
	 * The value of the node.
	 */
	public T value;
	
	/**
     * The list of child nodes.
     */
    public List<TreeNode<T>> children = new ArrayList<TreeNode<T>>();
    
    /**
     * Construct an empty node.
     */
    public TreeNodeImpl() {}
    
    /**
     * Construct a node holding the given value.
     * @param value the value.
     */
    public TreeNodeImpl(T value) {
    	this.value = value;
    }
    
    @Override
	public T getValue() {
    	return value;
    }
    
    @Override
	public void setValue(T value) {
    	this.value = value;
    }
    
    @Override
	public List<TreeNode<T>> getChildren() {
    	return children;
    }
    
    @Override
	public void addChild(TreeNode<T> tn) {
    	children.add(tn);
    }
    
    @Override
	public void removeChild(TreeNode<T> tn) {
    	children.remove(tn);
    }
    
    @Override
	public Iterator<TreeNode<T>> iterator() {
    	return children.iterator();
    }
    
    @Override
	public boolean isLeaf() {
    	return children.size() == 0;
    }
    
    @Override
	public String toString() {
    	String v = "Node("+value+")[";
    	for (TreeNode<T> ch : children)
    		v += ch.toString() +", ";
    	return v + "]";
    }
}
