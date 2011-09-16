package org.openimaj.math.geometry.shape.util.polygon;

/**
 * Horizontal edge states
 */
public class HState
{
	public static final int NH = 0; /* No horizontal edge */

	public static final int BH = 1; /* Bottom horizontal edge */

	public static final int TH = 2; /* Top horizontal edge */

	/* Horizontal edge state transitions within scanbeam boundary */
	public final int[][] next_h_state =
	{
	/* ABOVE BELOW CROSS */
	/* L R L R L R */
	/* NH */{ BH, TH, TH, BH, NH, NH },
	/* BH */{ NH, NH, NH, NH, TH, TH },
	/* TH */{ NH, NH, NH, NH, BH, BH } };
}
