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
import org.openimaj.image.FImage;
import org.openimaj.image.processing.convolution.SumBoxFilter;

/**
 * Implementation of Laws texture energy measures, based on the description in
 * <a href="https://courses.cs.washington.edu/courses/cse576/book/ch7.pdf">
 * Shapiro and Stockman Section 7.3.4</a>.
 * <p>
 * Nine texture energy images are created by convolving with the 16 base 5x5
 * Laws filters, and then computing the energy for each pixel by summing the
 * absolute pixel values in a macro window about that pixel.
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
public class LawsTexture extends LawsTextureBase {

	private int macroWidth = 15;
	private int macroHeight = 15;

	/**
	 * Construct using 15*15 macro windows for the energy computation
	 */
	public LawsTexture() {
	}

	/**
	 * Construct using the given sized macro windows for the energy computation
	 * 
	 * @param macro
	 *            the the window width and height
	 */
	public LawsTexture(int macro) {
		this.macroWidth = macro;
		this.macroHeight = macro;
	}

	/**
	 * Construct using the given sized macro windows for the energy computation
	 * 
	 * @param macroWidth
	 *            the window width
	 * @param macroHeight
	 *            the window height
	 */
	public LawsTexture(int macroWidth, int macroHeight) {
		this.macroWidth = macroWidth;
		this.macroHeight = macroHeight;
	}

	@Override
	public void analyseImage(FImage in) {
		super.analyseImage(in);

		final FImage[] tmpResp = responses;
		responses = new FImage[9];

		responses[0] = absAverage(tmpResp[L5E5], tmpResp[E5L5]);
		responses[1] = absAverage(tmpResp[L5R5], tmpResp[R5L5]);
		responses[2] = absAverage(tmpResp[E5S5], tmpResp[S5E5]);
		responses[3] = tmpResp[S5S5].abs();
		responses[4] = tmpResp[R5R5].abs();
		responses[5] = absAverage(tmpResp[L5S5], tmpResp[S5L5]);
		responses[6] = tmpResp[E5E5].abs();
		responses[7] = absAverage(tmpResp[E5R5], tmpResp[R5E5]);
		responses[8] = absAverage(tmpResp[S5R5], tmpResp[R5S5]);

		for (int i = 0; i < 9; i++) {
			responses[i] = responses[i].processInplace(new SumBoxFilter(macroWidth, macroHeight));
		}
	}

	private FImage absAverage(FImage i1, FImage i2) {
		final FImage img = new FImage(i1.width, i1.height);

		for (int y = 0; y < img.height; y++)
			for (int x = 0; x < img.width; x++)
				img.pixels[y][x] = Math.abs(i1.pixels[y][x] + i2.pixels[y][x]) / 2;

		return img;
	}
}
