package org.openimaj.math.geometry.shape.util.polygon;

/**
 * Edge intersection classes
 */
public class VertexType
{
	public static final int NUL = 0; /* Empty non-intersection */

	public static final int EMX = 1; /* External maximum */

	public static final int ELI = 2; /* External left intermediate */

	public static final int TED = 3; /* Top edge */

	public static final int ERI = 4; /* External right intermediate */

	public static final int RED = 5; /* Right edge */

	public static final int IMM = 6; /* Internal maximum and minimum */

	public static final int IMN = 7; /* Internal minimum */

	public static final int EMN = 8; /* External minimum */

	public static final int EMM = 9; /* External maximum and minimum */

	public static final int LED = 10; /* Left edge */

	public static final int ILI = 11; /* Internal left intermediate */

	public static final int BED = 12; /* Bottom edge */

	public static final int IRI = 13; /* Internal right intermediate */

	public static final int IMX = 14; /* Internal maximum */

	public static final int FUL = 15; /* Full non-intersection */

	public static int getType( int tr, int tl, int br, int bl )
	{
		return tr + (tl << 1) + (br << 2) + (bl << 3);
	}
}
