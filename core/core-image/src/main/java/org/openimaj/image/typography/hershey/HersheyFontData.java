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

import java.io.IOException;
import java.io.InputStream;

/**
 * Internal data for the Hershey vector font set.
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
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
final class HersheyFontData {
	private final static int MAX_CHARACTERS = 256;
	private final static int MAX_POINTS = 150;
	protected final static int X = 0;
	protected final static int Y = 1;

	protected String name;
	protected char characterVectors[][][] = new char[MAX_CHARACTERS][2][MAX_POINTS];
	protected int numberOfPoints[] = new int[MAX_CHARACTERS];
	protected int characterMinX[];
	protected int characterMaxX[];
	protected int characterSetMinY;
	protected int characterSetMaxY;
	protected int charactersInSet;

	/**
	 * Construct the HersheyFontData by reading from the resource
	 * with the given name.
	 * @param name the font name
	 * @throws IOException if error loading font
	 */
	public HersheyFontData(String name) throws IOException {
		InputStream fontStream = null;
		this.name = name;
		
		try {
			// open the font file
			fontStream = this.getClass().getResourceAsStream("/org/openimaj/image/typography/font/hershey/" + name);

			// load the font file
			loadHersheyFont(name, fontStream);
		} finally {
			// close the font file
			if (fontStream != null) try { fontStream.close(); } catch (IOException e) {}
		}
	}

	private void loadHersheyFont(String fontname, InputStream fontStream) throws IOException {
		int character, n;
		int c;
		int xadjust = fontAdjustment(fontname);

		// loop through the characters in the file ...
		character = 0;

		// while we have not processed all of the characters
		while (true) {
			// if we cannot read the next field
			if (getInt(fontStream, 5) < 1) {
				// we are done, set the font specification for num chars
				charactersInSet = character;

				// break the read loop
				break;
			} else {
				// get the number of vertices in this character
				n = getInt(fontStream, 3);

				// save it
				numberOfPoints[character] = n;

				// read in the vertice coordinates ...
				for (int i = 0; i < n; i++) {
					// if we are at the end of the line
					if ((i == 32) || (i == 68) || (i == 104) || (i == 140)) {
						// skip the carriage return
						fontStream.read();
					}

					// get the next character
					c = fontStream.read();

					// if this is a return ( we have a DOS style file )
					if (c == '\n') {
						// throw it away and get another
						c = fontStream.read();
					}

					// get the x coordinate
					characterVectors[character][X][i] = (char) c;

					// read the y coordinate
					characterVectors[character][Y][i] = (char) fontStream.read();
				}

				// skip the carriage return
				fontStream.read();

				// increment the character counter
				character++;
			}
		}
		// determine the size of each character ...

		characterMinX = new int[charactersInSet];
		characterMaxX = new int[charactersInSet];

		// initialize ...
		characterSetMinY = 1000;
		characterSetMaxY = -1000;

		// loop through each character ( except the space character )
		for (int j = 1; j < charactersInSet; j++) {
			// calculate the size
			calculateCharacterSize(j, xadjust);
		}

		// handle the space character - if the 'a' character is defined
		if (('a' - ' ') <= charactersInSet) {
			// make the space character the same size as the 'a'
			characterMinX[0] = characterMinX['a' - ' '];
			characterMaxX[0] = characterMaxX['a' - ' '];
		} else {
			// make the space char the same size as the last char
			characterMinX[0] = characterMinX[charactersInSet - 1];
			characterMaxX[0] = characterMaxX[charactersInSet - 1];
		}
	}

	private void calculateCharacterSize(int j, int xadj) {
		int cx, cy;
		characterMinX[j] = 1000;
		characterMaxX[j] = -1000;

		// for all the vertices in the character
		for (int i = 1; i < numberOfPoints[j]; i++) {
			cx = characterVectors[j][X][i];
			cy = characterVectors[j][Y][i];

			// if this is not a "skip"
			if (cx != ' ') {
				// if this is less than our current minimum
				if (cx < characterMinX[j]) {
					// save it
					characterMinX[j] = cx;
				}

				// if this is greater than our current maximum
				if (cx > characterMaxX[j]) {
					// save it
					characterMaxX[j] = cx;
				}

				// if this is less than our current minimum
				if (cy < characterSetMinY) {
					// save it
					characterSetMinY = cy;
				}

				// if this is greater than our current maximum
				if (cy > characterSetMaxY) {
					// save it
					characterSetMaxY = cy;
				}
			}
		}

		characterMinX[j] -= xadj;
		characterMaxX[j] += xadj;
	}

	private int fontAdjustment(String fontname) {
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

	@Override
	public String toString() {
		return name;
	}

	private int getInt(InputStream file, int n) throws IOException {
		char[] buf;
		int c;
		int j = 0;

		buf = new char[n];

		// for the specified number of characters
		for (int i = 0; i < n; i++) {
			c = file.read();

			// get character and discard spare newlines
			while ((c == '\n') || (c == '\r')) {
				c = file.read();
			}

			// if we hit end of file
			if (c == -1) {
				// return an error
				return (c);
			}

			// if this is not a blank
			if ((char) c != ' ') {
				// save the character
				buf[j++] = (char) c;
			}
		}

		// return the decimal equivalent of the string
		return Integer.parseInt(String.copyValueOf(buf, 0, j));
	}
}
