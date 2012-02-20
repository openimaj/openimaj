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
package org.openimaj.math.geometry.shape.util;

import java.util.Hashtable;

import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.util.polygon.AetTree;
import org.openimaj.math.geometry.shape.util.polygon.BundleState;
import org.openimaj.math.geometry.shape.util.polygon.EdgeNode;
import org.openimaj.math.geometry.shape.util.polygon.EdgeTable;
import org.openimaj.math.geometry.shape.util.polygon.HState;
import org.openimaj.math.geometry.shape.util.polygon.ItNode;
import org.openimaj.math.geometry.shape.util.polygon.LmtNode;
import org.openimaj.math.geometry.shape.util.polygon.LmtTable;
import org.openimaj.math.geometry.shape.util.polygon.OperationType;
import org.openimaj.math.geometry.shape.util.polygon.PolygonNode;
import org.openimaj.math.geometry.shape.util.polygon.ScanBeamTree;
import org.openimaj.math.geometry.shape.util.polygon.ScanBeamTreeEntries;
import org.openimaj.math.geometry.shape.util.polygon.StNode;
import org.openimaj.math.geometry.shape.util.polygon.TopPolygonNode;
import org.openimaj.math.geometry.shape.util.polygon.VertexType;

/**
 * <code>PolygonUtils</code> is a Java version of the <i>General Polygon Clipper</i>
 * algorithm developed by Alan Murta (gpc@cs.man.ac.uk). The home page for the
 * original source can be found at <a
 * href="http://www.cs.man.ac.uk/aig/staff/alan/software/" target="_blank">
 * http://www.cs.man.ac.uk/aig/staff/alan/software/</a>.
 * <p>
 * <b><code>polyClass:</code></b> Some of the public methods below take a
 * <code>polyClass</code> argument. This <code>java.lang.Class</code> object is
 * assumed to implement the <code>Polygon</code> interface and have a no
 * argument constructor. This was done so that the user of the algorithm could
 * create their own classes that implement the <code>Polygon</code> interface
 * and still uses this algorithm.
 * <p>
 * <strong>Implementation Note:</strong> The converted algorithm does support
 * the <i>difference</i> operation, but a public method has not been provided
 * and it has not been tested. To do so, simply follow what has been done for
 * <i>intersection</i>.
 * 
 * @author Dan Bridenbecker, Solution Engineering, Inc.
 */
public class PolygonUtils
{
	// -----------------
	// --- Constants ---
	// -----------------
	private static final boolean DEBUG = false;

	private static final double GPC_EPSILON = 2.2204460492503131e-016;

	// private static final String GPC_VERSION = "2.31" ;

	/** LEFT */
	public static final int LEFT = 0;

	/** RIGHT */
	public static final int RIGHT = 1;

	/** ABOVE */
	public static final int ABOVE = 0;

	/** BELOW */
	public static final int BELOW = 1;

	/** CLIP */
	public static final int CLIP = 0;

	/** SUBJ */
	public static final int SUBJ = 1;

	// ------------------------
	// --- Member Variables ---
	// ------------------------
	private Hashtable<Polygon, Boolean> contributions = new Hashtable<Polygon, Boolean>();

	// --------------------
	// --- Constructors ---
	// --------------------
	/** Creates a new instance of PolygonUtils */
	public PolygonUtils()
	{
	}

	/**
	 * Return the intersection of <code>p1</code> and <code>p2</code> where the
	 * return type is of <code>polyClass</code>. See the note in the class
	 * description for more on <ocde>polyClass</code>.
	 * 
	 * @param p1 One of the polygons to perform the intersection with
	 * @param p2 One of the polygons to perform the intersection with
	 * @param polyClass The type of <code>Polygon</code> to return
	 * @return the resultant polygon
	 */
	public Polygon intersection( Polygon p1, Polygon p2,
			Class<Polygon> polyClass )
	{
		return clip( OperationType.GPC_INT, p1, p2, polyClass );
	}

	/**
	 * Return the union of <code>p1</code> and <code>p2</code> where the return
	 * type is of <code>polyClass</code>. See the note in the class description
	 * for more on <ocde>polyClass</code>.
	 * 
	 * @param p1 One of the polygons to perform the union with
	 * @param p2 One of the polygons to perform the union with
	 * @param polyClass The type of <code>Polygon</code> to return
	 * @return the resultant polygon
	 */
	public Polygon union( Polygon p1, Polygon p2, Class<Polygon> polyClass )
	{
		return clip( OperationType.GPC_UNION, p1, p2, polyClass );
	}

	/**
	 * Return the xor of <code>p1</code> and <code>p2</code> where the return
	 * type is of <code>polyClass</code>. See the note in the class description
	 * for more on <code>polyClass</code>.
	 * 
	 * @param p1 One of the polygons to perform the xor with
	 * @param p2 One of the polygons to perform the xor with
	 * @param polyClass The type of <code>Polygon</code> to return
	 * @return the resultant polygon
	 */
	public Polygon xor( Polygon p1, Polygon p2, Class<Polygon> polyClass )
	{
		return clip( OperationType.GPC_XOR, p1, p2, polyClass );
	}

	/**
	 * Return the intersection of <code>p1</code> and <code>p2</code> where the
	 * return type is of <code>PolyDefault</code>.
	 * 
	 * @param p1 One of the polygons to perform the intersection with
	 * @param p2 One of the polygons to perform the intersection with
	 * @return the resultant polygon
	 */
	public Polygon intersection( Polygon p1, Polygon p2 )
	{
		return clip( OperationType.GPC_INT, p1, p2, Polygon.class );
	}

	/**
	 * Return the union of <code>p1</code> and <code>p2</code> where the return
	 * type is of <code>PolyDefault</code>.
	 * 
	 * @param p1 One of the polygons to performt he union with
	 * @param p2 One of the polygons to performt he union with
	 * @return the resultant polygon
	 */
	public Polygon union( Polygon p1, Polygon p2 )
	{
		return clip( OperationType.GPC_UNION, p1, p2, Polygon.class );
	}

	/**
	 * Return the xor of <code>p1</code> and <code>p2</code> where the return
	 * type is of <code>PolyDefault</code>.
	 * 
	 * @param p1 One of the polygons to perform the xor with
	 * @param p2 One of the polygons to perform the xor with
	 * @return the resultant polygon
	 */
	public Polygon xor( Polygon p1, Polygon p2 )
	{
		return clip( OperationType.GPC_XOR, p1, p2, Polygon.class );
	}

	// -----------------------
	// --- Private Methods ---
	// -----------------------

	/**
	 * Create a new <code>Polygon</code> type object using
	 * <code>polyClass</code>.
	 */
	private Polygon createNewPoly( Class<Polygon> polyClass )
	{
		try
		{
			return (Polygon) polyClass.newInstance();
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}

	/**
	 * <code>clip()</code> is the main method of the clipper algorithm. This is
	 * where the conversion from really begins.
	 */
	private Polygon clip( OperationType op, Polygon subj, Polygon clip,
			Class<Polygon> polyClass )
	{
		HState hs = new HState();
		Polygon result = createNewPoly( polyClass );

		/* Test for trivial NULL result cases */
		if( (subj.isEmpty() && clip.isEmpty())
				|| (subj.isEmpty() && ((op == OperationType.GPC_INT) || 
						(op == OperationType.GPC_DIFF)))
				|| (clip.isEmpty() && (op == OperationType.GPC_INT)) )
		{
			System.err.println( "Subject or clip is empty" );
			return result;
		}

		/* Identify potentialy contributing contours */
		if( ((op == OperationType.GPC_INT) || (op == OperationType.GPC_DIFF))
				&& !subj.isEmpty() && !clip.isEmpty() )
		{
			minimax_test( subj, clip, op );
		}

		/* Build LMT */
		LmtTable lmt_table = new LmtTable();
		ScanBeamTreeEntries sbte = new ScanBeamTreeEntries();
		// EdgeTable s_heap = null ;
		// EdgeTable c_heap = null ;
		if( !subj.isEmpty() )
		{
			/* s_heap = */build_lmt( lmt_table, sbte, subj, SUBJ, op );
		}
		if( DEBUG )
		{
			System.out.println( "" );
			System.out.println( " ------------ After build_lmt for subj ---------" );
			lmt_table.print();
		}
		if( !clip.isEmpty() )
		{
			/* c_heap = */build_lmt( lmt_table, sbte, clip, CLIP, op );
		}
		if( DEBUG )
		{
			System.out.println( "" );
			System.out.println( " ------------ After build_lmt for clip ---------" );
			lmt_table.print();
		}

		/* Return a NULL result if no contours contribute */
		if( lmt_table.top_node == null )
		{
			return result;
		}

		/* Build scanbeam table from scanbeam tree */
		double[] sbt = sbte.build_sbt();

		int[] parity = new int[2];
		parity[0] = LEFT;
		parity[1] = LEFT;

		/* Invert clip polygon for difference operation */
		if( op == OperationType.GPC_DIFF )
		{
			parity[CLIP] = RIGHT;
		}

		if( DEBUG )
		{
			print_sbt( sbt );
		}

		LmtNode local_min = lmt_table.top_node;

		TopPolygonNode out_poly = new TopPolygonNode(); // used to create
														// resulting Polygon

		AetTree aet = new AetTree();
		int scanbeam = 0;

		/* Process each scanbeam */
		while( scanbeam < sbt.length )
		{
			/* Set yb and yt to the bottom and top of the scanbeam */
			double yb = sbt[scanbeam++];
			double yt = 0.0;
			double dy = 0.0;
			if( scanbeam < sbt.length )
			{
				yt = sbt[scanbeam];
				dy = yt - yb;
			}

			/* === SCANBEAM BOUNDARY PROCESSING ================================ */

			/* If LMT node corresponding to yb exists */
			if( local_min != null )
			{
				if( local_min.y == yb )
				{
					/* Add edges starting at this local minimum to the AET */
					for( EdgeNode edge = local_min.first_bound; (edge != null); edge = edge.next_bound )
					{
						add_edge_to_aet( aet, edge );
					}

					local_min = local_min.next;
				}
			}

			if( DEBUG )
			{
				aet.print();
			}
			/* Set dummy previous x value */
			double px = -Double.MAX_VALUE;

			/* Create bundles within AET */
			EdgeNode e0 = aet.top_node;
			EdgeNode e1 = aet.top_node;

			/* Set up bundle fields of first edge */
			aet.top_node.bundle[ABOVE][aet.top_node.type] = (aet.top_node.top.y != yb) ? 1
					: 0;
			aet.top_node.bundle[ABOVE][((aet.top_node.type == 0) ? 1 : 0)] = 0;
			aet.top_node.bstate[ABOVE] = BundleState.UNBUNDLED;

			for( EdgeNode next_edge = aet.top_node.next; (next_edge != null); next_edge = next_edge.next )
			{
				int ne_type = next_edge.type;
				int ne_type_opp = ((next_edge.type == 0) ? 1 : 0); // next edge
																	// type
																	// opposite

				/* Set up bundle fields of next edge */
				next_edge.bundle[ABOVE][ne_type] = (next_edge.top.y != yb) ? 1
						: 0;
				next_edge.bundle[ABOVE][ne_type_opp] = 0;
				next_edge.bstate[ABOVE] = BundleState.UNBUNDLED;

				/* Bundle edges above the scanbeam boundary if they coincide */
				if( next_edge.bundle[ABOVE][ne_type] == 1 )
				{
					if( EQ( e0.xb, next_edge.xb ) && EQ( e0.dx, next_edge.dx )
							&& (e0.top.y != yb) )
					{
						next_edge.bundle[ABOVE][ne_type] ^= e0.bundle[ABOVE][ne_type];
						next_edge.bundle[ABOVE][ne_type_opp] = e0.bundle[ABOVE][ne_type_opp];
						next_edge.bstate[ABOVE] = BundleState.BUNDLE_HEAD;
						e0.bundle[ABOVE][CLIP] = 0;
						e0.bundle[ABOVE][SUBJ] = 0;
						e0.bstate[ABOVE] = BundleState.BUNDLE_TAIL;
					}
					e0 = next_edge;
				}
			}

			int[] horiz = new int[2];
			horiz[CLIP] = HState.NH;
			horiz[SUBJ] = HState.NH;

			int[] exists = new int[2];
			exists[CLIP] = 0;
			exists[SUBJ] = 0;

			PolygonNode cf = null;

			/* Process each edge at this scanbeam boundary */
			for( EdgeNode edge = aet.top_node; (edge != null); edge = edge.next )
			{
				exists[CLIP] = edge.bundle[ABOVE][CLIP]
						+ (edge.bundle[BELOW][CLIP] << 1);
				exists[SUBJ] = edge.bundle[ABOVE][SUBJ]
						+ (edge.bundle[BELOW][SUBJ] << 1);

				if( (exists[CLIP] != 0) || (exists[SUBJ] != 0) )
				{
					/* Set bundle side */
					edge.bside[CLIP] = parity[CLIP];
					edge.bside[SUBJ] = parity[SUBJ];

					boolean contributing = false;
					int br = 0, bl = 0, tr = 0, tl = 0;
					/* Determine contributing status and quadrant occupancies */
					if( (op == OperationType.GPC_DIFF)
							|| (op == OperationType.GPC_INT) )
					{
						contributing = ((exists[CLIP] != 0) && ((parity[SUBJ] != 0) || (horiz[SUBJ] != 0)))
								|| ((exists[SUBJ] != 0) && ((parity[CLIP] != 0) || (horiz[CLIP] != 0)))
								|| ((exists[CLIP] != 0) && (exists[SUBJ] != 0) && (parity[CLIP] == parity[SUBJ]));
						br = ((parity[CLIP] != 0) && (parity[SUBJ] != 0)) ? 1
								: 0;
						bl = (((parity[CLIP] ^ edge.bundle[ABOVE][CLIP]) != 0) && ((parity[SUBJ] ^ edge.bundle[ABOVE][SUBJ]) != 0)) ? 1
								: 0;
						tr = (((parity[CLIP] ^ ((horiz[CLIP] != HState.NH) ? 1
								: 0)) != 0) && ((parity[SUBJ] ^ ((horiz[SUBJ] != HState.NH) ? 1
								: 0)) != 0)) ? 1 : 0;
						tl = (((parity[CLIP]
								^ ((horiz[CLIP] != HState.NH) ? 1 : 0) ^ edge.bundle[BELOW][CLIP]) != 0) && ((parity[SUBJ]
								^ ((horiz[SUBJ] != HState.NH) ? 1 : 0) ^ edge.bundle[BELOW][SUBJ]) != 0)) ? 1
								: 0;
					}
					else if( op == OperationType.GPC_XOR )
					{
						contributing = (exists[CLIP] != 0)
								|| (exists[SUBJ] != 0);
						br = (parity[CLIP]) ^ (parity[SUBJ]);
						bl = (parity[CLIP] ^ edge.bundle[ABOVE][CLIP])
								^ (parity[SUBJ] ^ edge.bundle[ABOVE][SUBJ]);
						tr = (parity[CLIP] ^ ((horiz[CLIP] != HState.NH) ? 1
								: 0))
								^ (parity[SUBJ] ^ ((horiz[SUBJ] != HState.NH) ? 1
										: 0));
						tl = (parity[CLIP]
								^ ((horiz[CLIP] != HState.NH) ? 1 : 0) ^ edge.bundle[BELOW][CLIP])
								^ (parity[SUBJ]
										^ ((horiz[SUBJ] != HState.NH) ? 1 : 0) ^ edge.bundle[BELOW][SUBJ]);
					}
					else if( op == OperationType.GPC_UNION )
					{
						contributing = ((exists[CLIP] != 0) && (!(parity[SUBJ] != 0) || (horiz[SUBJ] != 0)))
								|| ((exists[SUBJ] != 0) && (!(parity[CLIP] != 0) || (horiz[CLIP] != 0)))
								|| ((exists[CLIP] != 0) && (exists[SUBJ] != 0) && (parity[CLIP] == parity[SUBJ]));
						br = ((parity[CLIP] != 0) || (parity[SUBJ] != 0)) ? 1
								: 0;
						bl = (((parity[CLIP] ^ edge.bundle[ABOVE][CLIP]) != 0) || ((parity[SUBJ] ^ edge.bundle[ABOVE][SUBJ]) != 0)) ? 1
								: 0;
						tr = (((parity[CLIP] ^ ((horiz[CLIP] != HState.NH) ? 1
								: 0)) != 0) || ((parity[SUBJ] ^ ((horiz[SUBJ] != HState.NH) ? 1
								: 0)) != 0)) ? 1 : 0;
						tl = (((parity[CLIP]
								^ ((horiz[CLIP] != HState.NH) ? 1 : 0) ^ edge.bundle[BELOW][CLIP]) != 0) || ((parity[SUBJ]
								^ ((horiz[SUBJ] != HState.NH) ? 1 : 0) ^ edge.bundle[BELOW][SUBJ]) != 0)) ? 1
								: 0;
					}
					else
					{
						throw new IllegalStateException( "Unknown op" );
					}

					/* Update parity */
					parity[CLIP] ^= edge.bundle[ABOVE][CLIP];
					parity[SUBJ] ^= edge.bundle[ABOVE][SUBJ];

					/* Update horizontal state */
					if( exists[CLIP] != 0 )
					{
						horiz[CLIP] = hs.next_h_state[horiz[CLIP]][((exists[CLIP] - 1) << 1)
								+ parity[CLIP]];
					}
					if( exists[SUBJ] != 0 )
					{
						horiz[SUBJ] = hs.next_h_state[horiz[SUBJ]][((exists[SUBJ] - 1) << 1)
								+ parity[SUBJ]];
					}

					if( contributing )
					{
						double xb = edge.xb;

						int vclass = VertexType.getType( tr, tl, br, bl );
						switch (vclass)
						{
							case VertexType.EMN:
							case VertexType.IMN:
								edge.outp[ABOVE] = out_poly.add_local_min( xb,
										yb );
								px = xb;
								cf = edge.outp[ABOVE];
								break;
							case VertexType.ERI:
								if( xb != px )
								{
									cf.add_right( xb, yb );
									px = xb;
								}
								edge.outp[ABOVE] = cf;
								cf = null;
								break;
							case VertexType.ELI:
								edge.outp[BELOW].add_left( xb, yb );
								px = xb;
								cf = edge.outp[BELOW];
								break;
							case VertexType.EMX:
								if( xb != px )
								{
									cf.add_left( xb, yb );
									px = xb;
								}
								out_poly.merge_right( cf, edge.outp[BELOW] );
								cf = null;
								break;
							case VertexType.ILI:
								if( xb != px )
								{
									cf.add_left( xb, yb );
									px = xb;
								}
								edge.outp[ABOVE] = cf;
								cf = null;
								break;
							case VertexType.IRI:
								edge.outp[BELOW].add_right( xb, yb );
								px = xb;
								cf = edge.outp[BELOW];
								edge.outp[BELOW] = null;
								break;
							case VertexType.IMX:
								if( xb != px )
								{
									cf.add_right( xb, yb );
									px = xb;
								}
								out_poly.merge_left( cf, edge.outp[BELOW] );
								cf = null;
								edge.outp[BELOW] = null;
								break;
							case VertexType.IMM:
								if( xb != px )
								{
									cf.add_right( xb, yb );
									px = xb;
								}
								out_poly.merge_left( cf, edge.outp[BELOW] );
								edge.outp[BELOW] = null;
								edge.outp[ABOVE] = out_poly.add_local_min( xb,
										yb );
								cf = edge.outp[ABOVE];
								break;
							case VertexType.EMM:
								if( xb != px )
								{
									cf.add_left( xb, yb );
									px = xb;
								}
								out_poly.merge_right( cf, edge.outp[BELOW] );
								edge.outp[BELOW] = null;
								edge.outp[ABOVE] = out_poly.add_local_min( xb,
										yb );
								cf = edge.outp[ABOVE];
								break;
							case VertexType.LED:
								if( edge.bot.y == yb )
									edge.outp[BELOW].add_left( xb, yb );
								edge.outp[ABOVE] = edge.outp[BELOW];
								px = xb;
								break;
							case VertexType.RED:
								if( edge.bot.y == yb )
									edge.outp[BELOW].add_right( xb, yb );
								edge.outp[ABOVE] = edge.outp[BELOW];
								px = xb;
								break;
							default:
								break;
						} /* End of switch */
					} /* End of contributing conditional */
				} /* End of edge exists conditional */
				if( DEBUG )
				{
					out_poly.print();
				}
			} /* End of AET loop */

			/* Delete terminating edges from the AET, otherwise compute xt */
			for( EdgeNode edge = aet.top_node; (edge != null); edge = edge.next )
			{
				if( edge.top.y == yb )
				{
					EdgeNode prev_edge = edge.prev;
					EdgeNode next_edge = edge.next;

					if( prev_edge != null )
						prev_edge.next = next_edge;
					else
						aet.top_node = next_edge;

					if( next_edge != null ) next_edge.prev = prev_edge;

					/*
					 * Copy bundle head state to the adjacent tail edge if
					 * required
					 */
					if( (edge.bstate[BELOW] == BundleState.BUNDLE_HEAD)
							&& (prev_edge != null) )
					{
						if( prev_edge.bstate[BELOW] == BundleState.BUNDLE_TAIL )
						{
							prev_edge.outp[BELOW] = edge.outp[BELOW];
							prev_edge.bstate[BELOW] = BundleState.UNBUNDLED;
							if( prev_edge.prev != null )
							{
								if( prev_edge.prev.bstate[BELOW] == BundleState.BUNDLE_TAIL )
								{
									prev_edge.bstate[BELOW] = BundleState.BUNDLE_HEAD;
								}
							}
						}
					}
				}
				else
				{
					if( edge.top.y == yt )
						edge.xt = edge.top.x;
					else
						edge.xt = edge.bot.x + edge.dx * (yt - edge.bot.y);
				}
			}

			if( scanbeam < sbte.sbt_entries )
			{
				/*
				 * === SCANBEAM INTERIOR PROCESSING
				 * ==============================
				 */

				/* Build intersection table for the current scanbeam */
				ItNodeTable it_table = new ItNodeTable();
				it_table.build_intersection_table( aet, dy );

				/* Process each node in the intersection table */
				for( ItNode intersect = it_table.top_node; (intersect != null); intersect = intersect.next )
				{
					e0 = intersect.ie[0];
					e1 = intersect.ie[1];

					/* Only generate output for contributing intersections */
					if( ((e0.bundle[ABOVE][CLIP] != 0) || (e0.bundle[ABOVE][SUBJ] != 0))
							&& ((e1.bundle[ABOVE][CLIP] != 0) || (e1.bundle[ABOVE][SUBJ] != 0)) )
					{
						PolygonNode p = e0.outp[ABOVE];
						PolygonNode q = e1.outp[ABOVE];
						double ix = intersect.point.x;
						double iy = intersect.point.y + yb;

						int in_clip = (((e0.bundle[ABOVE][CLIP] != 0) && !(e0.bside[CLIP] != 0))
								|| ((e1.bundle[ABOVE][CLIP] != 0) && (e1.bside[CLIP] != 0)) || (!(e0.bundle[ABOVE][CLIP] != 0)
								&& !(e1.bundle[ABOVE][CLIP] != 0)
								&& (e0.bside[CLIP] != 0) && (e1.bside[CLIP] != 0))) ? 1
								: 0;

						int in_subj = (((e0.bundle[ABOVE][SUBJ] != 0) && !(e0.bside[SUBJ] != 0))
								|| ((e1.bundle[ABOVE][SUBJ] != 0) && (e1.bside[SUBJ] != 0)) || (!(e0.bundle[ABOVE][SUBJ] != 0)
								&& !(e1.bundle[ABOVE][SUBJ] != 0)
								&& (e0.bside[SUBJ] != 0) && (e1.bside[SUBJ] != 0))) ? 1
								: 0;

						int tr = 0, tl = 0, br = 0, bl = 0;
						/* Determine quadrant occupancies */
						if( (op == OperationType.GPC_DIFF)
								|| (op == OperationType.GPC_INT) )
						{
							tr = ((in_clip != 0) && (in_subj != 0)) ? 1 : 0;
							tl = (((in_clip ^ e1.bundle[ABOVE][CLIP]) != 0) && ((in_subj ^ e1.bundle[ABOVE][SUBJ]) != 0)) ? 1
									: 0;
							br = (((in_clip ^ e0.bundle[ABOVE][CLIP]) != 0) && ((in_subj ^ e0.bundle[ABOVE][SUBJ]) != 0)) ? 1
									: 0;
							bl = (((in_clip ^ e1.bundle[ABOVE][CLIP] ^ e0.bundle[ABOVE][CLIP]) != 0) && ((in_subj
									^ e1.bundle[ABOVE][SUBJ] ^ e0.bundle[ABOVE][SUBJ]) != 0)) ? 1
									: 0;
						}
						else if( op == OperationType.GPC_XOR )
						{
							tr = (in_clip) ^ (in_subj);
							tl = (in_clip ^ e1.bundle[ABOVE][CLIP])
									^ (in_subj ^ e1.bundle[ABOVE][SUBJ]);
							br = (in_clip ^ e0.bundle[ABOVE][CLIP])
									^ (in_subj ^ e0.bundle[ABOVE][SUBJ]);
							bl = (in_clip ^ e1.bundle[ABOVE][CLIP] ^ e0.bundle[ABOVE][CLIP])
									^ (in_subj ^ e1.bundle[ABOVE][SUBJ] ^ e0.bundle[ABOVE][SUBJ]);
						}
						else if( op == OperationType.GPC_UNION )
						{
							tr = ((in_clip != 0) || (in_subj != 0)) ? 1 : 0;
							tl = (((in_clip ^ e1.bundle[ABOVE][CLIP]) != 0) || ((in_subj ^ e1.bundle[ABOVE][SUBJ]) != 0)) ? 1
									: 0;
							br = (((in_clip ^ e0.bundle[ABOVE][CLIP]) != 0) || ((in_subj ^ e0.bundle[ABOVE][SUBJ]) != 0)) ? 1
									: 0;
							bl = (((in_clip ^ e1.bundle[ABOVE][CLIP] ^ e0.bundle[ABOVE][CLIP]) != 0) || ((in_subj
									^ e1.bundle[ABOVE][SUBJ] ^ e0.bundle[ABOVE][SUBJ]) != 0)) ? 1
									: 0;
						}
						else
						{
							throw new IllegalStateException(
									"Unknown op type, " + op );
						}

						int vclass = VertexType.getType( tr, tl, br, bl );
						switch (vclass)
						{
							case VertexType.EMN:
								e0.outp[ABOVE] = out_poly
										.add_local_min( ix, iy );
								e1.outp[ABOVE] = e0.outp[ABOVE];
								break;
							case VertexType.ERI:
								if( p != null )
								{
									p.add_right( ix, iy );
									e1.outp[ABOVE] = p;
									e0.outp[ABOVE] = null;
								}
								break;
							case VertexType.ELI:
								if( q != null )
								{
									q.add_left( ix, iy );
									e0.outp[ABOVE] = q;
									e1.outp[ABOVE] = null;
								}
								break;
							case VertexType.EMX:
								if( (p != null) && (q != null) )
								{
									p.add_left( ix, iy );
									out_poly.merge_right( p, q );
									e0.outp[ABOVE] = null;
									e1.outp[ABOVE] = null;
								}
								break;
							case VertexType.IMN:
								e0.outp[ABOVE] = out_poly
										.add_local_min( ix, iy );
								e1.outp[ABOVE] = e0.outp[ABOVE];
								break;
							case VertexType.ILI:
								if( p != null )
								{
									p.add_left( ix, iy );
									e1.outp[ABOVE] = p;
									e0.outp[ABOVE] = null;
								}
								break;
							case VertexType.IRI:
								if( q != null )
								{
									q.add_right( ix, iy );
									e0.outp[ABOVE] = q;
									e1.outp[ABOVE] = null;
								}
								break;
							case VertexType.IMX:
								if( (p != null) && (q != null) )
								{
									p.add_right( ix, iy );
									out_poly.merge_left( p, q );
									e0.outp[ABOVE] = null;
									e1.outp[ABOVE] = null;
								}
								break;
							case VertexType.IMM:
								if( (p != null) && (q != null) )
								{
									p.add_right( ix, iy );
									out_poly.merge_left( p, q );
									e0.outp[ABOVE] = out_poly.add_local_min(
											ix, iy );
									e1.outp[ABOVE] = e0.outp[ABOVE];
								}
								break;
							case VertexType.EMM:
								if( (p != null) && (q != null) )
								{
									p.add_left( ix, iy );
									out_poly.merge_right( p, q );
									e0.outp[ABOVE] = out_poly.add_local_min(
											ix, iy );
									e1.outp[ABOVE] = e0.outp[ABOVE];
								}
								break;
							default:
								break;
						} /* End of switch */
					} /* End of contributing intersection conditional */

					/* Swap bundle sides in response to edge crossing */
					if( e0.bundle[ABOVE][CLIP] != 0 )
						e1.bside[CLIP] = (e1.bside[CLIP] == 0) ? 1 : 0;
					if( e1.bundle[ABOVE][CLIP] != 0 )
						e0.bside[CLIP] = (e0.bside[CLIP] == 0) ? 1 : 0;
					if( e0.bundle[ABOVE][SUBJ] != 0 )
						e1.bside[SUBJ] = (e1.bside[SUBJ] == 0) ? 1 : 0;
					if( e1.bundle[ABOVE][SUBJ] != 0 )
						e0.bside[SUBJ] = (e0.bside[SUBJ] == 0) ? 1 : 0;

					/* Swap e0 and e1 bundles in the AET */
					EdgeNode prev_edge = e0.prev;
					EdgeNode next_edge = e1.next;
					if( next_edge != null )
					{
						next_edge.prev = e0;
					}

					if( e0.bstate[ABOVE] == BundleState.BUNDLE_HEAD )
					{
						boolean search = true;
						while( search )
						{
							prev_edge = prev_edge.prev;
							if( prev_edge != null )
							{
								if( prev_edge.bstate[ABOVE] != BundleState.BUNDLE_TAIL )
								{
									search = false;
								}
							}
							else
							{
								search = false;
							}
						}
					}
					if( prev_edge == null )
					{
						aet.top_node.prev = e1;
						e1.next = aet.top_node;
						aet.top_node = e0.next;
					}
					else
					{
						prev_edge.next.prev = e1;
						e1.next = prev_edge.next;
						prev_edge.next = e0.next;
					}
					e0.next.prev = prev_edge;
					e1.next.prev = e1;
					e0.next = next_edge;
					if( DEBUG )
					{
						out_poly.print();
					}
				} /* End of IT loop */

				/* Prepare for next scanbeam */
				for( EdgeNode edge = aet.top_node; (edge != null); edge = edge.next )
				{
					EdgeNode next_edge = edge.next;
					EdgeNode succ_edge = edge.succ;
					if( (edge.top.y == yt) && (succ_edge != null) )
					{
						/* Replace AET edge by its successor */
						succ_edge.outp[BELOW] = edge.outp[ABOVE];
						succ_edge.bstate[BELOW] = edge.bstate[ABOVE];
						succ_edge.bundle[BELOW][CLIP] = edge.bundle[ABOVE][CLIP];
						succ_edge.bundle[BELOW][SUBJ] = edge.bundle[ABOVE][SUBJ];
						EdgeNode prev_edge = edge.prev;
						if( prev_edge != null )
							prev_edge.next = succ_edge;
						else
							aet.top_node = succ_edge;
						if( next_edge != null ) next_edge.prev = succ_edge;
						succ_edge.prev = prev_edge;
						succ_edge.next = next_edge;
					}
					else
					{
						/* Update this edge */
						edge.outp[BELOW] = edge.outp[ABOVE];
						edge.bstate[BELOW] = edge.bstate[ABOVE];
						edge.bundle[BELOW][CLIP] = edge.bundle[ABOVE][CLIP];
						edge.bundle[BELOW][SUBJ] = edge.bundle[ABOVE][SUBJ];
						edge.xb = edge.xt;
					}
					edge.outp[ABOVE] = null;
				}
			}
		} /* === END OF SCANBEAM PROCESSING ================================== */

		/* Generate result polygon from out_poly */
		result = out_poly.getResult( polyClass );

		return result;
	}

	private static boolean EQ( double a, double b )
	{
		return (Math.abs( a - b ) <= GPC_EPSILON);
	}

	/** 
	 * @param i
	 * @param n
	 * @return previous index
	 */
	public static int PREV_INDEX( int i, int n )
	{
		return ((i - 1 + n) % n);
	}

	/**
	 * @param i
	 * @param n
	 * @return next index
	 */
	public static int NEXT_INDEX( int i, int n )
	{
		return ((i + 1) % n);
	}

	private boolean OPTIMAL( Polygon p, int i )
	{
		return (p.getInnerPoly( 0 ).points
				.get( PREV_INDEX( i, p.nVertices() ) ).getY() != p
				.getInnerPoly( 0 ).points.get( i ).getY())
				|| (p.getInnerPoly( 0 ).points.get(
						NEXT_INDEX( i, p.nVertices() ) ).getY() != p
						.getInnerPoly( 0 ).points.get( i ).getY());
	}

	private static Rectangle[] create_contour_bboxes( Polygon p )
	{
		Rectangle[] box = new Rectangle[p.getNumInnerPoly()];

		/* Construct contour bounding boxes */
		for( int c = 0; c < p.getNumInnerPoly(); c++ )
		{
			Polygon inner_poly = p.getInnerPoly( c );
			box[c] = inner_poly.calculateRegularBoundingBox();
		}
		return box;
	}

	private void minimax_test( Polygon subj, Polygon clip, OperationType op )
	{
		Rectangle[] s_bbox = create_contour_bboxes( subj );
		Rectangle[] c_bbox = create_contour_bboxes( clip );

		int subj_num_poly = subj.getNumInnerPoly();
		int clip_num_poly = clip.getNumInnerPoly();
		boolean[][] o_table = new boolean[subj_num_poly][clip_num_poly];

		/* Check all subject contour bounding boxes against clip boxes */
		for( int s = 0; s < subj_num_poly; s++ )
		{
			for( int c = 0; c < clip_num_poly; c++ )
			{
				o_table[s][c] = (!((s_bbox[s].maxX() < c_bbox[c].minX()) || (s_bbox[s]
						.minX() > c_bbox[c].maxX())))
						&& (!((s_bbox[s].maxY() < c_bbox[c].minY()) || (s_bbox[s]
								.minY() > c_bbox[c].maxY())));
			}
		}

		/* For each clip contour, search for any subject contour overlaps */
		for( int c = 0; c < clip_num_poly; c++ )
		{
			boolean overlap = false;
			for( int s = 0; !overlap && (s < subj_num_poly); s++ )
			{
				overlap = o_table[s][c];
			}
			if( !overlap )
			{
				contributions.put( clip, false );
				// clip.setContributing( c, false ); // Flag non contributing
				// status
			}
		}

		if( op == OperationType.GPC_INT )
		{
			/* For each subject contour, search for any clip contour overlaps */
			for( int s = 0; s < subj_num_poly; s++ )
			{
				boolean overlap = false;
				for( int c = 0; !overlap && (c < clip_num_poly); c++ )
				{
					overlap = o_table[s][c];
				}
				if( !overlap )
				{
					contributions.put( subj, false );
					// subj.setContributing( s, false ); // Flag non
					// contributing status
				}
			}
		}
	}

	private static LmtNode bound_list( LmtTable lmt_table, double y )
	{
		if( lmt_table.top_node == null )
		{
			lmt_table.top_node = new LmtNode( y );
			return lmt_table.top_node;
		}
		else
		{
			LmtNode prev = null;
			LmtNode node = lmt_table.top_node;
			boolean done = false;
			while( !done )
			{
				if( y < node.y )
				{
					/* Insert a new LMT node before the current node */
					LmtNode existing_node = node;
					node = new LmtNode( y );
					node.next = existing_node;
					if( prev == null )
					{
						lmt_table.top_node = node;
					}
					else
					{
						prev.next = node;
					}
					if( existing_node == lmt_table.top_node )
					{
						lmt_table.top_node = node;
					}
					done = true;
				}
				else if( y > node.y )
				{
					/* Head further up the LMT */
					if( node.next == null )
					{
						node.next = new LmtNode( y );
						node = node.next;
						done = true;
					}
					else
					{
						prev = node;
						node = node.next;
					}
				}
				else
				{
					/* Use this existing LMT node */
					done = true;
				}
			}
			return node;
		}
	}

	private static void insert_bound( LmtNode lmt_node, EdgeNode e )
	{
		if( lmt_node.first_bound == null )
		{
			/* Link node e to the tail of the list */
			lmt_node.first_bound = e;
		}
		else
		{
			boolean done = false;
			EdgeNode prev_bound = null;
			EdgeNode current_bound = lmt_node.first_bound;
			while( !done )
			{
				/* Do primary sort on the x field */
				if( e.bot.x < current_bound.bot.x )
				{
					/* Insert a new node mid-list */
					if( prev_bound == null )
					{
						lmt_node.first_bound = e;
					}
					else
					{
						prev_bound.next_bound = e;
					}
					e.next_bound = current_bound;

					EdgeNode existing_bound = current_bound;
					current_bound = e;
					current_bound.next_bound = existing_bound;
					if( lmt_node.first_bound == existing_bound )
					{
						lmt_node.first_bound = current_bound;
					}
					done = true;
				}
				else if( e.bot.x == current_bound.bot.x )
				{
					/* Do secondary sort on the dx field */
					if( e.dx < current_bound.dx )
					{
						/* Insert a new node mid-list */
						if( prev_bound == null )
						{
							lmt_node.first_bound = e;
						}
						else
						{
							prev_bound.next_bound = e;
						}
						e.next_bound = current_bound;
						EdgeNode existing_bound = current_bound;
						current_bound = e;
						current_bound.next_bound = existing_bound;
						if( lmt_node.first_bound == existing_bound )
						{
							lmt_node.first_bound = current_bound;
						}
						done = true;
					}
					else
					{
						/* Head further down the list */
						if( current_bound.next_bound == null )
						{
							current_bound.next_bound = e;
							done = true;
						}
						else
						{
							prev_bound = current_bound;
							current_bound = current_bound.next_bound;
						}
					}
				}
				else
				{
					/* Head further down the list */
					if( current_bound.next_bound == null )
					{
						current_bound.next_bound = e;
						done = true;
					}
					else
					{
						prev_bound = current_bound;
						current_bound = current_bound.next_bound;
					}
				}
			}
		}
	}

	private void add_edge_to_aet( AetTree aet, EdgeNode edge )
	{
		if( aet.top_node == null )
		{
			/* Append edge onto the tail end of the AET */
			aet.top_node = edge;
			edge.prev = null;
			edge.next = null;
		}
		else
		{
			EdgeNode current_edge = aet.top_node;
			EdgeNode prev = null;
			boolean done = false;
			while( !done )
			{
				/* Do primary sort on the xb field */
				if( edge.xb < current_edge.xb )
				{
					/* Insert edge here (before the AET edge) */
					edge.prev = prev;
					edge.next = current_edge;
					current_edge.prev = edge;
					if( prev == null )
					{
						aet.top_node = edge;
					}
					else
					{
						prev.next = edge;
					}
					if( current_edge == aet.top_node )
					{
						aet.top_node = edge;
					}
					current_edge = edge;
					done = true;
				}
				else if( edge.xb == current_edge.xb )
				{
					/* Do secondary sort on the dx field */
					if( edge.dx < current_edge.dx )
					{
						/* Insert edge here (before the AET edge) */
						edge.prev = prev;
						edge.next = current_edge;
						current_edge.prev = edge;
						if( prev == null )
						{
							aet.top_node = edge;
						}
						else
						{
							prev.next = edge;
						}
						// if( current_edge == aet.top_node )
						// {
						// aet.top_node = edge ;
						// }
						// current_edge = edge ;
						done = true;
					}
					else
					{
						/* Head further into the AET */
						prev = current_edge;
						if( current_edge.next == null )
						{
							current_edge.next = edge;
							edge.prev = current_edge;
							edge.next = null;
							done = true;
						}
						else
						{
							current_edge = current_edge.next;
						}
					}
				}
				else
				{
					/* Head further into the AET */
					prev = current_edge;
					if( current_edge.next == null )
					{
						current_edge.next = edge;
						edge.prev = current_edge;
						edge.next = null;
						done = true;
					}
					else
					{
						current_edge = current_edge.next;
					}
				}
			}
		}
	}

	private static void add_to_sbtree( ScanBeamTreeEntries sbte, double y )
	{
		if( sbte.sb_tree == null )
		{
			/* Add a new tree node here */
			sbte.sb_tree = new ScanBeamTree( y );
			sbte.sbt_entries++;
			return;
		}
		ScanBeamTree tree_node = sbte.sb_tree;
		boolean done = false;
		while( !done )
		{
			if( tree_node.y > y )
			{
				if( tree_node.less == null )
				{
					tree_node.less = new ScanBeamTree( y );
					sbte.sbt_entries++;
					done = true;
				}
				else
				{
					tree_node = tree_node.less;
				}
			}
			else if( tree_node.y < y )
			{
				if( tree_node.more == null )
				{
					tree_node.more = new ScanBeamTree( y );
					sbte.sbt_entries++;
					done = true;
				}
				else
				{
					tree_node = tree_node.more;
				}
			}
			else
			{
				done = true;
			}
		}
	}

	private EdgeTable build_lmt( LmtTable lmt_table, ScanBeamTreeEntries sbte,
			Polygon p, int type, // Polygon
									// type
									// SUBJ/CLIP
			OperationType op )
	{
		/* Create the entire input polygon edge table in one go */
		EdgeTable edge_table = new EdgeTable();

		for( int c = 0; c < p.getNumInnerPoly(); c++ )
		{
			Polygon ip = p.getInnerPoly( c );
			Boolean cc;
			if( (cc = contributions.get( ip )) != null && !cc )
			{
				/* Ignore the non-contributing contour */
				contributions.put( ip, true );
				// ip.setContributing(0, true);
			}
			else
			{
				/* Perform contour optimisation */
				int num_vertices = 0;
				int e_index = 0;
				edge_table = new EdgeTable();
				for( int i = 0; i < ip.nVertices(); i++ )
				{
					if( OPTIMAL( ip, i ) )
					{
						double x = ip.getInnerPoly( 0 ).points.get( i )
								.getX();
						double y = ip.getInnerPoly( 0 ).points.get( i )
								.getY();
						edge_table.addNode( x, y );

						/* Record vertex in the scanbeam table */
						add_to_sbtree( sbte, y );

						num_vertices++;
					}
				}

				/* Do the contour forward pass */
				for( int min = 0; min < num_vertices; min++ )
				{
					/* If a forward local minimum... */
					if( edge_table.FWD_MIN( min ) )
					{
						/* Search for the next local maximum... */
						int num_edges = 1;
						int max = NEXT_INDEX( min, num_vertices );
						while( edge_table.NOT_FMAX( max ) )
						{
							num_edges++;
							max = NEXT_INDEX( max, num_vertices );
						}

						/* Build the next edge list */
						int v = min;
						EdgeNode e = edge_table.getNode( e_index );
						e.bstate[BELOW] = BundleState.UNBUNDLED;
						e.bundle[BELOW][CLIP] = 0;
						e.bundle[BELOW][SUBJ] = 0;

						for( int i = 0; i < num_edges; i++ )
						{
							EdgeNode ei = edge_table.getNode( e_index + i );
							EdgeNode ev = edge_table.getNode( v );

							ei.xb = ev.vertex.x;
							ei.bot.x = ev.vertex.x;
							ei.bot.y = ev.vertex.y;

							v = NEXT_INDEX( v, num_vertices );
							ev = edge_table.getNode( v );

							ei.top.x = ev.vertex.x;
							ei.top.y = ev.vertex.y;
							ei.dx = (ev.vertex.x - ei.bot.x)
									/ (ei.top.y - ei.bot.y);
							ei.type = type;
							ei.outp[ABOVE] = null;
							ei.outp[BELOW] = null;
							ei.next = null;
							ei.prev = null;
							ei.succ = ((num_edges > 1) && (i < (num_edges - 1))) ? edge_table
									.getNode( e_index + i + 1 ) : null;
							// ei.pred = ((num_edges > 1) && (i > 0)) ?
							// edge_table.getNode(e_index+i-1) : null ;
							ei.next_bound = null;
							ei.bside[CLIP] = (op == OperationType.GPC_DIFF) ? RIGHT
									: LEFT;
							ei.bside[SUBJ] = LEFT;
						}
						insert_bound(
								bound_list( lmt_table,
										edge_table.getNode( min ).vertex.y ), e );
						if( DEBUG )
						{
							System.out.println( "fwd" );
							lmt_table.print();
						}
						e_index += num_edges;
					}
				}

				/* Do the contour reverse pass */
				for( int min = 0; min < num_vertices; min++ )
				{
					/* If a reverse local minimum... */
					if( edge_table.REV_MIN( min ) )
					{
						/* Search for the previous local maximum... */
						int num_edges = 1;
						int max = PREV_INDEX( min, num_vertices );
						while( edge_table.NOT_RMAX( max ) )
						{
							num_edges++;
							max = PREV_INDEX( max, num_vertices );
						}

						/* Build the previous edge list */
						int v = min;
						EdgeNode e = edge_table.getNode( e_index );
						e.bstate[BELOW] = BundleState.UNBUNDLED;
						e.bundle[BELOW][CLIP] = 0;
						e.bundle[BELOW][SUBJ] = 0;

						for( int i = 0; i < num_edges; i++ )
						{
							EdgeNode ei = edge_table.getNode( e_index + i );
							EdgeNode ev = edge_table.getNode( v );

							ei.xb = ev.vertex.x;
							ei.bot.x = ev.vertex.x;
							ei.bot.y = ev.vertex.y;

							v = PREV_INDEX( v, num_vertices );
							ev = edge_table.getNode( v );

							ei.top.x = ev.vertex.x;
							ei.top.y = ev.vertex.y;
							ei.dx = (ev.vertex.x - ei.bot.x)
									/ (ei.top.y - ei.bot.y);
							ei.type = type;
							ei.outp[ABOVE] = null;
							ei.outp[BELOW] = null;
							ei.next = null;
							ei.prev = null;
							ei.succ = ((num_edges > 1) && (i < (num_edges - 1))) ? edge_table
									.getNode( e_index + i + 1 ) : null;
							// ei.pred = ((num_edges > 1) && (i > 0)) ?
							// edge_table.getNode(e_index+i-1) : null ;
							ei.next_bound = null;
							ei.bside[CLIP] = (op == OperationType.GPC_DIFF) ? RIGHT
									: LEFT;
							ei.bside[SUBJ] = LEFT;
						}
						insert_bound(
								bound_list( lmt_table,
										edge_table.getNode( min ).vertex.y ), e );
						if( DEBUG )
						{
							System.out.println( "rev" );
							lmt_table.print();
						}
						e_index += num_edges;
					}
				}
			}
		}
		return edge_table;
	}

	private StNode add_st_edge( StNode st, ItNodeTable it, EdgeNode edge,
			double dy )
	{
		if( st == null )
		{
			/* Append edge onto the tail end of the ST */
			st = new StNode( edge, null );
		}
		else
		{
			double den = (st.xt - st.xb) - (edge.xt - edge.xb);

			/* If new edge and ST edge don't cross */
			if( (edge.xt >= st.xt) || (edge.dx == st.dx)
					|| (Math.abs( den ) <= GPC_EPSILON) )
			{
				/* No intersection - insert edge here (before the ST edge) */
				StNode existing_node = st;
				st = new StNode( edge, existing_node );
			}
			else
			{
				/* Compute intersection between new edge and ST edge */
				double r = (edge.xb - st.xb) / den;
				double x = st.xb + r * (st.xt - st.xb);
				double y = r * dy;

				/* Insert the edge pointers and the intersection point in the IT */
				it.top_node = add_intersection( it.top_node, st.edge, edge, x,
						y );

				/* Head further into the ST */
				st.prev = add_st_edge( st.prev, it, edge, dy );
			}
		}
		return st;
	}

	private ItNode add_intersection( ItNode it_node, EdgeNode edge0,
			EdgeNode edge1, double x, double y )
	{
		if( it_node == null )
		{
			/* Append a new node to the tail of the list */
			it_node = new ItNode( edge0, edge1, x, y, null );
		}
		else
		{
			if( it_node.point.y > y )
			{
				/* Insert a new node mid-list */
				ItNode existing_node = it_node;
				it_node = new ItNode( edge0, edge1, x, y, existing_node );
			}
			else
			{
				/* Head further down the list */
				it_node.next = add_intersection( it_node.next, edge0, edge1, x,
						y );
			}
		}
		return it_node;
	}

	class ItNodeTable
	{
		ItNode top_node;

		public void build_intersection_table( AetTree aet, double dy )
		{
			StNode st = null;

			/* Process each AET edge */
			for( EdgeNode edge = aet.top_node; (edge != null); edge = edge.next )
			{
				if( (edge.bstate[PolygonUtils.ABOVE] == BundleState.BUNDLE_HEAD)
						|| (edge.bundle[PolygonUtils.ABOVE][PolygonUtils.CLIP] != 0)
						|| (edge.bundle[PolygonUtils.ABOVE][PolygonUtils.SUBJ] != 0) )
				{
					st = add_st_edge( st, this, edge, dy );
				}
			}
		}
	}

	// -------------
	// --- DEBUG ---
	// -------------
	private static void print_sbt( double[] sbt )
	{
		System.out.println( "" );
		System.out.println( "sbt.length=" + sbt.length );
		for( int i = 0; i < sbt.length; i++ )
		{
			System.out.println( "sbt[" + i + "]=" + sbt[i] );
		}
	}
}
