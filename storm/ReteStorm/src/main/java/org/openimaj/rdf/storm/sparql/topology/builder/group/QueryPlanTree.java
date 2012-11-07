package org.openimaj.rdf.storm.sparql.topology.builder.group;

import java.util.ArrayList;

import org.openimaj.rdf.storm.sparql.topology.builder.group.QueryPlanTree.Group;

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
public class QueryPlanTree<T, G extends Group<T, G>> {

	/**
	 * A node of the query plan. Nodes contain pauloads and can be visited by a
	 * walker
	 * 
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
	 * 
	 * @param <T>
	 * @param <G>
	 */
	public static interface QueryPlanNode<T, G extends Group<T, G>> {
		/**
		 * @param walker
		 *            walk down the query plan depth first
		 */
		public void walkDepthFirst(QueryPlanWalker<T, G> walker);

		/**
		 * @return This walkable query node's payload.
		 */
		public T payload();
	}

	/**
	 * The leaf node of a query plan.
	 * 
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
	 * 
	 * @param <T>
	 * @param <G>
	 */
	public static class LeafNode<T, G extends Group<T, G>> implements QueryPlanNode<T, G> {

		private T instance;

		/**
		 * @param instance
		 *            the instance held in the leaf node
		 */
		public LeafNode(T instance) {
			this.instance = instance;
		}

		@Override
		public void walkDepthFirst(QueryPlanWalker<T, G> walker) {
		}

		@Override
		public T payload() {
			return instance;
		}

	}

	/**
	 * A Union is backed by a {@link Group}.
	 * 
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
	 * 
	 * @param <T>
	 * @param <G>
	 */
	public abstract static class Union<T, G extends Group<T, G>> implements QueryPlanNode<T, G> {
		G groupNode;

		/**
		 * @param groupNode
		 */
		public Union(G groupNode) {
			this.groupNode = groupNode;
		}

		@Override
		public void walkDepthFirst(QueryPlanWalker<T, G> walker) {
			if (groupNode.size() > 0) {
				for (QueryPlanNode<T, G> g : groupNode) {
					g.walkDepthFirst(walker); // go down each union node
				}
			}
			walker.visitUnion(this);
		}

	}

	/**
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
	 * 
	 * @param <T>
	 * @param <G>
	 */
	public abstract static class Group<T, G extends Group<T, G>> extends
			ArrayList<QueryPlanNode<T, G>> implements QueryPlanNode<T, G> {
		/**
		 *
		 */
		private static final long serialVersionUID = -8314628985809097934L;
		G parent = null;

		@SuppressWarnings("unchecked")
		@Override
		public void walkDepthFirst(QueryPlanWalker<T, G> walker) {
			if (this.size() > 0) {
				for (QueryPlanNode<T, G> g : this) {
					g.walkDepthFirst(walker);
				}
			}
			walker.visitGroup((G) this);
		}

		/**
		 * @param child
		 */
		public void addChild(T child) {
			this.add(new LeafNode<T, G>(child));
		}
	}

	/**
	 * Can create the custom GroupNode class
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * @param <T>
	 *            the type of the payload
	 * @param <G>
	 *            the type of the groupnodes
	 */
	public static interface QueryPlanNodeCreator<T, G extends Group<T, G>> {
		/**
		 * @return a new group
		 */
		public G createGroupNode();

		/**
		 * @return a union node
		 */
		public Union<T, G> createUnion();
	}

	/**
	 * Can visit groups
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 * @param <T>
	 *            The type of the payload
	 * @param <G>
	 *            The type of the group
	 */
	public static interface QueryPlanWalker<T, G extends Group<T, G>> {
		/**
		 * @param group
		 *            the group to visit
		 */
		public void visitGroup(G group);

		/**
		 * @param union
		 *            the union to visit
		 */
		public void visitUnion(Union<T, G> union);
	}

	G root;
	G current;
	private QueryPlanNodeCreator<T, G> creator;

	/**
	 * root and current set to null.
	 * 
	 * @param creator
	 *            the creator of new nodes
	 */
	public QueryPlanTree(QueryPlanNodeCreator<T, G> creator) {
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
	public void depthFirstGroups(QueryPlanWalker<T, G> groupWalker) {
		this.root.walkDepthFirst(groupWalker);
	}

	/**
	 * @return the root group node
	 */
	public G getRootGroupNode() {
		return this.root;
	}

}
