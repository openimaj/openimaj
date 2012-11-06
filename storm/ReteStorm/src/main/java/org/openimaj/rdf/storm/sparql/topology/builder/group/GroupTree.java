package org.openimaj.rdf.storm.sparql.topology.builder.group;

import java.util.ArrayList;

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

	public static interface Walkable<T,G extends GroupNode<?, G>>{
		public void walkDepthFirst(GroupWalker<G> walker);

		public T payload();
	}

	public static class NullWalker<T,G extends GroupNode<T, G>> implements Walkable<T,G>{

		private T instance;
		public NullWalker(T instance) {
			this.instance = instance;
		}
		@Override
		public void walkDepthFirst(GroupWalker<G> walker) {
		}
		@Override
		public T payload() {
			return instance;
		}

	}

	/**
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
	 *
	 * @param <T>
	 * @param <G>
	 */
	public abstract static class GroupNode<T, G extends GroupNode<T, G>> extends ArrayList<Walkable<T,G>> implements Walkable<T,G> {
		/**
		 *
		 */
		private static final long serialVersionUID = -8314628985809097934L;
		G parent = null;

		@SuppressWarnings("unchecked")
		@Override
		public void walkDepthFirst(GroupWalker<G> walker) {
			if (this.size() > 0) {
				for (Walkable<T,G> g : this) {
					g.walkDepthFirst(walker);
				}
			}
			walker.visit((G) this);
		}

		/**
		 * @param child
		 */
		public void addChild(T child){
			this.add(new NullWalker<T,G>(child));
		}
	}

	/**
	 * Can create the custom GroupNode class
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 * @param <G>
	 */
	public static interface GroupNodeCreator<G> {
		/**
		 * @return a new group
		 */
		public G createGroupNode();
	}

	/**
	 * Can visit groups
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 * @param <G>
	 */
	public static interface GroupWalker<G> {
		/**
		 * @param group the group to visit
		 */
		public void visit(G group);
	}

	G root;
	G current;
	private GroupNodeCreator<G> creator;

	/**
	 * root and current set to null.
	 * @param creator the creator of new nodes
	 */
	public GroupTree(GroupNodeCreator<G> creator) {
		this.creator = creator;
	}

	/**
	 * Starts recording nodes to involve in a union. If a union has
	 * started and remains unfinished, starting a new one has no effect?
	 * Equivilant of A UNION B UNION D? is this even allowed?
	 */
	public void startUnion() {
	}

	/**
	 * Finishes a union node
	 */
	public void endUnion() {
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
		this.current.addChild(child);
	}

	/**
	 * Visit groups with no sub-groups first
	 *
	 * @param groupWalker
	 */
	public void depthFirstGroups(GroupWalker<G> groupWalker) {
		this.root.walkDepthFirst(groupWalker);
	}

	/**
	 * @return the root group node
	 */
	public G getRootGroupNode() {
		return this.root;
	}


}
