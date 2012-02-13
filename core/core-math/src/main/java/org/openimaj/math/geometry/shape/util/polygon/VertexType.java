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
package org.openimaj.math.geometry.shape.util.polygon;

/**
 * Edge intersection classes
 */
public class VertexType
{
	/** Empty non-intersection */
	public static final int NUL = 0;

	/** External maximum */
	public static final int EMX = 1;

	/** External left intermediate */
	public static final int ELI = 2;

	/** Top edge */
	public static final int TED = 3;

	/** External right intermediate */
	public static final int ERI = 4;

	/** Right edge */
	public static final int RED = 5;

	/** Internal maximum and minimum */
	public static final int IMM = 6;

	/** Internal minimum */
	public static final int IMN = 7;

	/** External minimum */
	public static final int EMN = 8;

	/** External maximum and minimum */
	public static final int EMM = 9;

	/** Left edge */
	public static final int LED = 10;

	/** Internal left intermediate */
	public static final int ILI = 11;

	/** Bottom edge */
	public static final int BED = 12;

	/** Internal right intermediate */
	public static final int IRI = 13;

	/** Internal maximum */
	public static final int IMX = 14;

	/** Full non-intersection */
	public static final int FUL = 15;

	/**
	 * @param tr
	 * @param tl
	 * @param br
	 * @param bl
	 * @return type
	 */
	public static int getType( int tr, int tl, int br, int bl )
	{
		return tr + (tl << 1) + (br << 2) + (bl << 3);
	}
}
