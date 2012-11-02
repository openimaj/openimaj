package org.openimaj.rdf.storm.sparql.topology.builder.group;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.rdf.storm.sparql.topology.builder.group.GroupTree.GroupNode;

/**
 * This class holds a tree of groups which in turn can hold more groups or
 * filters. This is used to construct a join plan.
 * 
 * Group trees are constructed as a walk. New groups are started and added to
 * the previously
 * started group. When a group is ended the current group becomes its parent.
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T>
 *            The type held in the tree
 * @param <G>
 *            The type of the groupnode
 * 
 */
public class GroupTree<T, G extends GroupNode<T, G>> {
	/**
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
	 * 
	 * @param <T>
	 * @param <G>
	 */
	public abstract static class GroupNode<T, G extends GroupNode<T, G>> extends ArrayList<G> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8314628985809097934L;
		G parent = null;
		List<T> leaves = new ArrayList<T>();

		public List<T> getChildren() {
			return leaves;
		}
	}

	public static interface GroupNodeCreator<G> {
		public G createGroupNode();
	}

	public static interface GroupWalker<G> {
		public void visit(G group);
	}

	G root;
	G current;
	private GroupNodeCreator<G> creator;

	/**
	 * root and current set to null.
	 */
	public GroupTree(GroupNodeCreator<G> creator) {
		this.creator = creator;
	}

	/**
	 * A new group has started
	 */
	public void startGroup() {
		if (this.current == null) {
			// just started!
			this.root = creator.createGroupNode();
			this.current = root;
		}
		else {
			G newCurrent = creator.createGroupNode();
			this.current.add(newCurrent);
			newCurrent.parent = current;
			this.current = newCurrent;
		}
	}

	/**
	 * end a group
	 */
	public void endGroup() {
		this.current = current.parent;
	}

	/**
	 * Add a leaf node to the current group
	 * 
	 * @param child
	 */
	public void addLeaf(T child) {
		this.current.leaves.add(child);
	}

	/**
	 * Visit groups with no sub-groups first
	 * 
	 * @param groupWalker
	 */
	public void depthFirstGroups(GroupWalker<G> groupWalker) {
		depthFirstGroups(groupWalker, this.root);
	}

	private void depthFirstGroups(GroupWalker<G> groupWalker, G node) {
		if (node.size() > 0) {
			for (G g : node) {
				depthFirstGroups(groupWalker, g);
			}
		}
		groupWalker.visit(node);
	}

	public G getRootGroupNode() {
		return this.root;
	}
}
