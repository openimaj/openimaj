package org.openimaj.image.typography.hershey;

import org.openimaj.image.Image;
import org.openimaj.image.typography.FontRenderer;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Renderer for the Hershey vector font set.
 * Based on HersheyFont.java by James P. Buzbee, which carried the 
 * following copyright statement:
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
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
final class HersheyFontRenderer<T> implements FontRenderer<T, HersheyFont, HersheyFontStyle<T>> {
	@Override
	public void renderText(Image<T, ?> image, String text, int x, int y, HersheyFont font, HersheyFontStyle<T> style) {
		drawText(text, font.data, style, x, y, style.width, style.height, style.horizontalAlignment, style.verticalAlignment, style.angle, true, new Rectangle(), image, style.lineColour );
	}

	@Override
	public Rectangle getBounds(String string, HersheyFontStyle<T> style) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected int drawText(String text, HersheyFontData fnt, HersheyFontStyle<T> sty, int xc, int yc, 
			float width, float height, int Horizontal_Alignment, int Vertical_Alignment,
			double theta, boolean Draw, Rectangle r, Image<T,?> image, T col) {
		int character;
		int len;
		int rotpx = 0, rotpy = 0;
		int xp, yp;
		boolean rotate = false;
		float cosTheta = 0, sinTheta = 0;
		float verticalOffsetFactor = 0;

		// set the flag to true if the angle is not 0.0
		rotate = (theta != 0.0) ? true : false;

		// if we are to do a rotation
		if (rotate) {
			// set up the rotation variables
			theta = -Math.PI / 180.0 * theta;
			cosTheta = (float) Math.cos(theta);
			sinTheta = (float) Math.sin(theta);

			// set the position to do all rotations about
			rotpx = xc;
			rotpy = yc;
		}

		// starting position
		xp = xc;

		yp = yc;

		// if we are not going to actually draw the string
		if (!Draw) {
			// set up to initialize the bounding rectangle
			r.x = xp;
			r.y = yp;
			r.width = xp;
			r.height = yp;
		}

		switch (Vertical_Alignment) {
		case HersheyFontStyle.VERTICAL_TOP:
			verticalOffsetFactor = 0;
			break;

		case HersheyFontStyle.VERTICAL_HALF:
			verticalOffsetFactor = 0.5f;
			break;

		case HersheyFontStyle.VERTICAL_NORMAL: // also VERTICAL_BOTTOM

			verticalOffsetFactor = 1;
			break;

		case HersheyFontStyle.VERTICAL_CAP:
			verticalOffsetFactor = 0.25f;
			break;

		}

		// move the y position based on the vertical alignment
		yp = yp
				- (int) (verticalOffsetFactor * (height * (fnt.characterSetMaxY - fnt.characterSetMinY)));

		// if we have a non-standard horizontal alignment
		if ((Horizontal_Alignment != HersheyFontStyle.HORIZONTAL_LEFT)
				&& (Horizontal_Alignment != HersheyFontStyle.HORIZONTAL_NORMAL)) {
			// find the length of the string in pixels ...
			len = 0;

			for (int j = 0; j < text.length(); j++) {
				// the character's number in the array ...
				character = text.charAt(j) - ' ';

				len += (fnt.characterMaxX[character] - fnt.characterMinX[character]) * width;
			}

			// if we are center aligned
			if (Horizontal_Alignment == HersheyFontStyle.HORIZONTAL_CENTER) {
				// move the starting point half to the left
				xp -= len / 2;
			} else {
				// alignment is right, move the start all the way to the left
				xp -= len;
			}
		}

		// loop through each character in the string ...
		for (int j = 0; j < text.length(); j++) {
			// the character's number in the array ...
			character = text.charAt(j) - ' ';

			// render this character
			drawCharacter(xp, yp, rotpx, rotpy, width, height, rotate,
					sinTheta, cosTheta, Draw, r, fnt.characterVectors[character],
					fnt.numberOfPoints[character], fnt.characterMinX[character],
					fnt.characterSetMinY, sty.lineWidth, sty.italic, sty.italicSlant, image,
					col);

			// advance the starting coordinate
			xp += (int) ((fnt.characterMaxX[character] - fnt.characterMinX[character]) * width);

		} // end for each character

		return (0);
	}

	protected void drawFontLine(int x1, int y1, int x2, int y2, int width, Image<T,?> image, T col) {
		// if the width is greater than one
		if (width > 1) {
			Polygon filledPolygon = new Polygon();

			int offset = width / 2;

			// this does not generate a true "wide line" but it seems to
			// look OK for font lines

			filledPolygon.addVertex(x1 - offset, y1 + offset);
			filledPolygon.addVertex(x1 + offset, y1 - offset);
			filledPolygon.addVertex(x2 + offset, y2 - offset);
			filledPolygon.addVertex(x2 - offset, y2 + offset);

			// draw a polygon
			image.drawPolygonFilled(filledPolygon, col);
		} else {
			// draw a line
			image.drawLine(x1, y1, x2, y2, col);
			// System.out.println("" + x1 + " " + x2 );
		}

		return;
	}

	protected int fontAdjustment(String fontname) {
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

		return (xadjust);

	}

	protected void drawCharacter(int xp, int yp, int rotpx, int rotpy,
			float width, float height, boolean rotate, float sinTheta,
			float cosTheta, boolean Draw, Rectangle r, char Vectors[][],
			int numberOfPoints, int minX, int characterSetMinY, int lineWidth,
			boolean italics, float slant, Image<T,?> g, T col) {
		float xd, yd, xd2, yd2;
		int oldx = 0, oldy = 0, x, y, i;
		boolean skip = true;
		float finalSlant = height * (-slant);

		// loop through each vertex in the character
		for (i = 1; i < numberOfPoints; i++) {
			// System.out.print("" + Vectors[X][i] + Vectors[Y][i] );
			// if this is a "skip"
			if (Vectors[HersheyFontData.X][i] == ' ') {
				// set the skip flag
				skip = true;
			} else {
				// calculate italics offset if necessary
				x = (int) ((italics) ? ((Vectors[HersheyFontData.Y][i] - characterSetMinY) * finalSlant)
						: 0)
						+
						// add italics offset to the "normal" point
						// transformation
						transformX(xp, Vectors[HersheyFontData.X][i], minX, width);

				// calculate the y coordinate
				y = transformY(yp, Vectors[HersheyFontData.Y][i], characterSetMinY, height);

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

				if (!Draw) {
					// System.out.println("x,y" + x + ", " + y );

					// we just want the bounding box of the string
					if (x < r.x) {
						r.x = x;
					}
					if (y < r.y) {
						r.y = y;
					}

					if (x > r.width) {
						r.width = x;
					}
					if (y > r.height) {
						r.height = y;
					}
				}

				if (!skip) {
					// if we are to draw the string
					if (Draw) {
						drawFontLine(oldx, oldy, x, y, lineWidth, g, col);
					}
				} // end if not skip

				skip = false;

				oldx = x;
				oldy = y;

			} // end if skip
		} // end for each vertex in the character
	}

	protected final int transformX(int xoffset, int px, int minx, float mag) {
		return ((int) (xoffset + (px - minx) * mag));
	}

	protected final int transformY(int yoffset, int py, int miny, float mag) {
		return ((int) (yoffset + (py - miny) * mag));
	}
}
