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
package org.openimaj.image.typography.hershey;

import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.image.typography.FontRenderer;
import org.openimaj.image.typography.FontStyle.HorizontalAlignment;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Renderer for the Hershey vector font set. Based on HersheyFont.java by James
 * P. Buzbee, which carried the following copyright statement:
 * 
 * <pre>
 * Copyright (c) James P. Buzbee 1996
 * House Blend Software
 * 
 * jbuzbee@nyx.net
 * Version 1.1 Dec 11 1996
 * Version 1.2 Sep 18 1997
 * Version 1.3 Feb 28 1998
 * Version 1.4 Aug 13 2000 : J++ bug workaround by  Paul Emory Sullivan
 * 
 * Permission to use, copy, modify, and distribute this software
 * for any use is hereby granted provided
 * this notice is kept intact within the source file
 * This is freeware, use it as desired !
 * 
 * Very loosly based on code with authors listed as :
 * Alan Richardson, Pete Holzmann, James Hurt
 * </pre>
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
final class HersheyFontRenderer<T> extends FontRenderer<T, HersheyFontStyle<T>> {
	protected static HersheyFontRenderer<?> INSTANCE = new HersheyFontRenderer<Object>();

	private HersheyFontRenderer() {
	}

	@Override
	public void renderText(final ImageRenderer<T, ?> renderer, final String text, final int x, final int y,
			final HersheyFontStyle<T> style)
	{
		this.drawText(text, style, x, y, true, new Rectangle(), renderer);
	}

	@Override
	public Rectangle getSize(final String text, final HersheyFontStyle<T> style) {
		final Rectangle r = new Rectangle();
		this.drawText(text, style, 0, 0, false, r, null);
		return r;
	}

	protected void drawText(final String text, final HersheyFontStyle<T> sty, final int xc, final int yc,
			final boolean Draw, final Rectangle r, final ImageRenderer<T, ?> renderer)
	{
		final HersheyFontData fnt = ((HersheyFont)sty.getFont()).data;
		int character;
		int len;
		int rotpx = 0, rotpy = 0;
		int xp, yp;
		boolean rotate = false;
		float cosTheta = 0, sinTheta = 0;
		float verticalOffsetFactor = 0;

		// set the flag to true if the angle is not 0.0
		rotate = (sty.getAngle() != 0.0) ? true : false;

		// if we are to do a rotation
		if (rotate) {
			// set up the rotation variables
			final float theta = -sty.getAngle();
			cosTheta = (float) Math.cos(theta);
			sinTheta = (float) Math.sin(theta);

			// set the position to do all rotations about
			rotpx = xc;
			rotpy = yc;
		}

		// starting position
		xp = xc;
		yp = yc;

		switch (sty.getVerticalAlignment()) {
		case VERTICAL_TOP:
			verticalOffsetFactor = 0;
			break;
		case VERTICAL_HALF:
			verticalOffsetFactor = 0.5f;
			break;
		case VERTICAL_BOTTOM:
			verticalOffsetFactor = 1;
			break;
		case VERTICAL_CAP:
			verticalOffsetFactor = 0.25f;
			break;
		}

		// move the y position based on the vertical alignment
		yp -= (int) (verticalOffsetFactor * (sty.getActualHeightScale() * (fnt.characterSetMaxY - fnt.characterSetMinY)));

		// if we have a non-standard horizontal alignment
		if ((sty.getHorizontalAlignment() != HorizontalAlignment.HORIZONTAL_LEFT)) {
			// find the length of the string in pixels ...
			len = 0;

			for (int j = 0; j < text.length(); j++) {
				// the character's number in the array ...
				character = text.charAt(j) - ' ';

				len += (fnt.characterMaxX[character] - fnt.characterMinX[character]) * sty.getActualWidthScale();
			}

			// if we are center aligned
			if (sty.getHorizontalAlignment() == HorizontalAlignment.HORIZONTAL_CENTER) {
				// move the starting point half to the left
				xp -= len / 2;
			} else {
				// alignment is right, move the start all the way to the left
				xp -= len;
			}
		}

		// loop through each character in the string ...
		r.x = r.y = Integer.MAX_VALUE;
		for (int j = 0; j < text.length(); j++) {
			// the character's number in the array ...
			character = text.charAt(j) - ' ';

			if (character < 0)
				character = ' ';

			// render this character
			this.drawCharacter(xp, yp, rotpx, rotpy, sty.getActualWidthScale(), sty.getActualHeightScale(), rotate,
					sinTheta, cosTheta, Draw, fnt.characterVectors[character],
					fnt.numberOfPoints[character], fnt.characterMinX[character],
					fnt.characterSetMinY, sty.getStrokeWidth(), sty.isItalic(), sty.getItalicSlant(), renderer,
					sty.getColour(), r);

			// advance the starting coordinate
			final int actualWidth = (int) ((fnt.characterMaxX[character] - fnt.characterMinX[character]) * sty
					.getActualWidthScale());
			xp += actualWidth;

			// r.width += actualWidth;
		} // end for each character

		// r.height =
		// sty.getActualHeightScale()*(fnt.characterSetMaxY-fnt.characterSetMinY);
		// System.out.println(
		// text+" : "+r+" -> "+fnt.characterSetMinY+","+fnt.characterSetMaxY );
	}

	protected int fontAdjustment(final String fontname) {
		int xadjust = 0;

		// if we do not have a script type font
		if (fontname.indexOf("scri") < 0) {
			// if we have a gothic font
			if (fontname.indexOf("goth") >= 0) {
				xadjust = 2;
			} else {
				xadjust = 3;
			}
		}

		return xadjust;
	}

	protected void drawCharacter(final int xp, final int yp, final int rotpx, final int rotpy,
			final float width, final float height, final boolean rotate, final float sinTheta,
			final float cosTheta, final boolean draw, final char vectors[][],
			final int numberOfPoints, final int minX, final int characterSetMinY, final int lineWidth,
			final boolean italics, final float slant, final ImageRenderer<T, ?> renderer, final T colour,
			final Rectangle bounds)
	{
		float xd, yd, xd2, yd2;
		int oldx = 0, oldy = 0, x, y, i;
		boolean skip = true;
		final float finalSlant = height * (-slant);
		int maxX = 0, maxY = 0;

		// loop through each vertex in the character
		for (i = 1; i < numberOfPoints; i++) {
			// if this is a "skip"
			if (vectors[HersheyFontData.X][i] == ' ') {
				// set the skip flag
				skip = true;
			} else {
				// calculate italics offset if necessary
				x = (int) ((italics) ? ((vectors[HersheyFontData.Y][i] - characterSetMinY) * finalSlant)
						: 0)
						+
						// add italics offset to the "normal" point
						// transformation
						this.transformX(xp, vectors[HersheyFontData.X][i], minX, width);

				// calculate the y coordinate
				y = this.transformY(yp, vectors[HersheyFontData.Y][i], characterSetMinY, height);

				// if we are doing a rotation
				if (rotate) {
					// apply the rotation matrix ...

					// transform the coordinate to the rotation center point
					xd = (x - rotpx);
					yd = (y - rotpy);

					// rotate
					xd2 = xd * cosTheta - yd * sinTheta;
					yd2 = xd * sinTheta + yd * cosTheta;

					// transform back
					x = (int) (xd2 + 0.5) + rotpx;
					y = (int) (yd2 + 0.5) + rotpy;
				}

				if (!skip) {
					// if we are to draw the string
					if (draw) {
						renderer.drawLine(oldx, oldy, x, y, lineWidth, colour);
					}

					bounds.x = Math.min(bounds.x, x);
					bounds.y = Math.min(bounds.y, y);
					maxX = Math.max(maxX, x);
					maxY = Math.max(maxY, y);
				} // end if not skip

				skip = false;

				oldx = x;
				oldy = y;
			} // end if skip
		} // end for each vertex in the character

		bounds.width = Math.max(bounds.width, maxX - bounds.x);
		bounds.height = Math.max(bounds.height, maxY - bounds.y);
	}

	protected final int transformX(final int xoffset, final int px, final int minx, final float mag) {
		return ((int) (xoffset + (px - minx) * mag));
	}

	protected final int transformY(final int yoffset, final int py, final int miny, final float mag) {
		return ((int) (yoffset + (py - miny) * mag));
	}
}
