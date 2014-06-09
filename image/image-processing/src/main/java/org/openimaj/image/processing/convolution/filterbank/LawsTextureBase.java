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
package org.openimaj.image.processing.convolution.filterbank;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.processing.convolution.FConvolution;

/**
 * Base {@link FilterBank} that provides the 16 raw kernels used in Laws texture
 * classification approach. These are used by the {@link LawsTexture} class to
 * make 9 filters (by averaging the directional ones).
 * <p>
 * This class is provided as a convenience for extensions to Laws technique; in
 * direct applications of Law's technique, you'll want to use
 * {@link LawsTexture} instead.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Laws, K. I." },
		title = "{Rapid Texture Identification}",
		year = "1980",
		booktitle = "Proc. SPIE Conf. Image Processing for Missile Guidance",
		pages = { "376", "", "380" },
		customData = {
				"citeulike-article-id", "2335645",
				"keywords", "bibtex-import",
				"posted-at", "2008-02-05 15:32:50",
				"priority", "2"
		})
public class LawsTextureBase extends FilterBank {
	private static final float[] L5 = { 1, 4, 6, 4, 1 };
	private static final float[] E5 = { -1, -2, 0, 2, 1 };
	private static final float[] S5 = { -1, 0, 2, 0, -1 };
	private static final float[] R5 = { 1, -4, 6, -4, 1 };

	protected final static int L5E5 = 0;
	protected final static int E5L5 = 1;
	protected final static int L5R5 = 2;
	protected final static int R5L5 = 3;
	protected final static int E5S5 = 4;
	protected final static int S5E5 = 5;
	protected final static int S5S5 = 6;
	protected final static int R5R5 = 7;
	protected final static int L5S5 = 8;
	protected final static int S5L5 = 9;
	protected final static int E5E5 = 10;
	protected final static int E5R5 = 11;
	protected final static int R5E5 = 12;
	protected final static int S5R5 = 13;
	protected final static int R5S5 = 14;

	/**
	 * Default constructor
	 */
	public LawsTextureBase() {
		super(makeFilters());
	}

	private static FConvolution[] makeFilters() {
		final FConvolution[] filters = new FConvolution[15];

		filters[L5E5] = makeFilter(L5, E5);
		filters[E5L5] = makeFilter(E5, L5);
		filters[L5R5] = makeFilter(L5, R5);
		filters[R5L5] = makeFilter(R5, L5);
		filters[E5S5] = makeFilter(E5, S5);
		filters[S5E5] = makeFilter(S5, E5);
		filters[S5S5] = makeFilter(S5, S5);
		filters[R5R5] = makeFilter(R5, R5);
		filters[L5S5] = makeFilter(L5, S5);
		filters[S5L5] = makeFilter(S5, L5);
		filters[E5E5] = makeFilter(E5, E5);
		filters[E5R5] = makeFilter(E5, R5);
		filters[R5E5] = makeFilter(R5, E5);
		filters[S5R5] = makeFilter(S5, R5);
		filters[R5S5] = makeFilter(R5, S5);

		return filters;
	}

	private static FConvolution makeFilter(float[] l, float[] r) {
		final float[][] f = new float[l.length][r.length];

		for (int i = 0; i < l.length; i++)
			for (int j = 0; j < r.length; j++)
				f[i][j] = l[i] * r[j];

		return new FConvolution(f);
	}
}
