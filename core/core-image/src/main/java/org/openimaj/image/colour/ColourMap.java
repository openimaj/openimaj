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
package org.openimaj.image.colour;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;

/**
 * Colour maps
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public enum ColourMap {
	/**
	 * Octave Autumn colour map. The map ranges from red through orange to
	 * yellow.
	 */
	Autumn {
		@Override
		public void apply(float val, float[] out) {
			out[0] = 1;
			out[1] = val;
			out[2] = 0;
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.INTERPOLATED;
		}
	},
	/**
	 * Octave Bone colour map. The map varies from black to white with gray-blue
	 * shades.
	 */
	Bone {
		@Override
		public void apply(float x, float[] out) {
			out[0] = ((x < 3f / 4f) ? 1 : 0) * (7f / 8f * x) +
					((x >= 3f / 4f) ? 1 : 0) * (11f / 8f * x - 3f / 8f);
			out[1] = ((x < 3f / 8f) ? 1 : 0) * (7f / 8f * x) +
					((x >= 3f / 8f && x < 3f / 4f) ? 1 : 0) * (29f / 24f * x - 1f / 8f) +
					((x >= 3f / 4f) ? 1 : 0) * (7f / 8f * x + 1f / 8f);
			out[2] = ((x < 3f / 8f) ? 1 : 0) * (29f / 24f * x) +
					((x >= 3f / 8f) ? 1 : 0) * (7f / 8f * x + 1f / 8f);
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.INTERPOLATED;
		}
	},
	/**
	 * Octave Cool colour map. The map varies from cyan to magenta.
	 */
	Cool {
		@Override
		public void apply(float x, float[] out) {
			out[0] = x;
			out[1] = 1 - x;
			out[2] = 1;
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.INTERPOLATED;
		}
	},
	/**
	 * Octave Copper colour map. The map varies from black to a light copper
	 * tone.
	 */
	Copper {
		@Override
		public void apply(float x, float[] out) {
			out[0] = ((x < 4f / 5f) ? 1 : 0) * (5f / 4f * x) + ((x >= 4f / 5f) ? 1 : 0);
			out[1] = 4f / 5f * x;
			out[2] = 1f / 2f * x;
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.INTERPOLATED;
		}
	},
	/**
	 * Octave Hot colour map. The map ranges from black through dark red, red,
	 * orange, yellow, to white.
	 */
	Hot {
		@Override
		public void apply(float x, float[] out) {
			out[0] = ((x < 2f / 5f) ? 1 : 0) * (5f / 2f * x) + ((x >= 2f / 5f) ? 1 : 0);
			out[1] = ((x >= 2f / 5f && x < 4f / 5f) ? 1 : 0) * (5f / 2f * x - 1) + ((x >= 4f / 5f) ? 1 : 0);
			out[2] = ((x >= 4f / 5f) ? 1 : 0) * (5f * x - 4f);
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.INTERPOLATED;
		}
	},
	/**
	 * Octave HSV colour map. The map begins with red, changes through yellow,
	 * green, cyan, blue, and magenta, before returning to red.
	 */
	HSV {
		@Override
		public void apply(float x, float[] out) {
			final FImage h = new FImage(1, 1);
			final FImage s = new FImage(1, 1);
			final FImage v = new FImage(1, 1);

			h.pixels[0][0] = x;
			s.pixels[0][0] = 1;
			v.pixels[0][0] = 1;

			final MBFImage img = Transforms.HSV_TO_RGB(new MBFImage(ColourSpace.HSV, h, s, v));

			out[0] = img.getBand(0).pixels[0][0];
			out[1] = img.getBand(1).pixels[0][0];
			out[2] = img.getBand(2).pixels[0][0];
		}

		@Override
		public MBFImage apply(FImage img) {
			final FImage ones = new FImage(img.width, img.height);
			ones.fill(1);
			final MBFImage mbf = new MBFImage(ColourSpace.HSV, img, ones, ones);
			return Transforms.HSV_TO_RGB(mbf);
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.INTERPOLATED;
		}
	},
	/**
	 * Octave Jet colour map. The map ranges from dark blue through blue, cyan,
	 * green, yellow, red, to dark red.
	 */
	Jet {
		@Override
		public void apply(float x, float[] out) {
			out[0] = ((x >= 3f / 8f && x < 5f / 8f) ? 1 : 0) * (4f * x - 3f / 2f) +
					((x >= 5f / 8f && x < 7f / 8f) ? 1 : 0) +
					((x >= 7f / 8f) ? 1 : 0) * (-4f * x + 9f / 2f);
			out[1] = ((x >= 1f / 8f && x < 3f / 8f) ? 1 : 0) * (4f * x - 1f / 2f) +
					((x >= 3f / 8f && x < 5f / 8f) ? 1 : 0) +
					((x >= 5f / 8f && x < 7f / 8f) ? 1 : 0) * (-4f * x + 7f / 2f);
			out[2] = ((x < 1f / 8f) ? 1 : 0) * (4f * x + 1f / 2f) +
					((x >= 1f / 8f && x < 3f / 8f) ? 1 : 0) +
					((x >= 3f / 8f && x < 5f / 8f) ? 1 : 0) * (-4f * x + 5f / 2f);
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.INTERPOLATED;
		}
	},
	/**
	 * Octave Spring colour map. The map varies from magenta to yellow.
	 */
	Spring {
		@Override
		public void apply(float x, float[] out) {
			out[0] = 1;
			out[1] = x;
			out[2] = 1 - x;
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.INTERPOLATED;
		}
	},
	/**
	 * Octave Summer colour map. The map varies from green to yellow.
	 */
	Summer {
		@Override
		public void apply(float x, float[] out) {
			out[0] = x;
			out[1] = 0.5f + x / 2f;
			out[2] = 0.4f;
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.INTERPOLATED;
		}
	},
	/**
	 * Octave Rainbow colour map. The map ranges from red through orange,
	 * yellow, green, blue, to violet.
	 */
	Rainbow {
		@Override
		public void apply(float x, float[] out) {
			out[0] = ((x < 2f / 5f) ? 1 : 0) +
					((x >= 2f / 5f && x < 3f / 5f) ? 1 : 0) * (-5f * x + 3f) +
					((x >= 4f / 5f) ? 1 : 0) * (10f / 3f * x - 8f / 3f);
			out[1] = ((x < 2f / 5f) ? 1 : 0) * (5f / 2f * x) +
					((x >= 2f / 5f & x < 3f / 5f) ? 1 : 0) +
					((x >= 3f / 5f & x < 4f / 5f) ? 1 : 0) * (-5f * x + 4f);
			out[2] = ((x >= 3f / 5f & x < 4f / 5f) ? 1 : 0) * (5f * x - 3f) +
					((x >= 4f / 5f) ? 1 : 0);
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.INTERPOLATED;
		}
	},
	/**
	 * Octave Winter colour map. The map varies from blue to green.
	 */
	Winter {
		@Override
		public void apply(float x, float[] out) {
			out[0] = 0;
			out[1] = x;
			out[2] = 1f - x / 2f;
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.INTERPOLATED;
		}
	},
	/**
	 * Sepia colour map based on the Octave Pink colour map.
	 */
	Sepia {
		@Override
		public void apply(float x, float[] out) {
			out[0] = ((x < 3f / 8f) ? 1 : 0) * (14f / 9f * x) +
					((x >= 3f / 8f) ? 1 : 0) * (2f / 3f * x + 1f / 3f);
			out[1] = ((x < 3f / 8f) ? 1 : 0) * (2f / 3f * x) +
					((x >= 3f / 8f && x < 3f / 4f) ? 1 : 0) * (14f / 9f * x - 1f / 3f) +
					((x >= 3f / 4f) ? 1 : 0) * (2f / 3f * x + 1f / 3f);
			out[1] = ((x < 3f / 4f) ? 1 : 0) * (2f / 3f * x) +
					((x >= 3f / 4f) ? 1 : 0) * (2f * x - 1f);

		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.INTERPOLATED;
		}
	},
	/**
	 * Red, green, blue, yellow, magenta, cyan discrete map
	 */
	RGBYMC {
		private final float[][] cols = { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 }, { 1, 1, 0 }, { 1, 0, 1 }, { 0, 1, 1 } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * 6), 5);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},
	/**
	 * Discrete Rainbow (red, orange, yellow, green, blue, violet)
	 */
	Prism {
		private final float[][] cols = { { 1, 0, 0 }, { 1, 0.5f, 0 }, { 1, 1, 0 }, { 0, 1, 0 }, { 0, 0, 1 },
				{ 2f / 3f, 0, 1 } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * 6), 5);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},
	/**
	 * ColorBrewer "Accent" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Accent3 {
		private final float[][] cols = { { 127f / 265f, 201f / 265f, 127f / 265f },
				{ 190f / 265f, 174f / 265f, 212f / 265f }, { 253f / 265f, 192f / 265f, 134f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Accent" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Accent4 {
		private final float[][] cols = { { 127f / 265f, 201f / 265f, 127f / 265f },
				{ 190f / 265f, 174f / 265f, 212f / 265f }, { 253f / 265f, 192f / 265f, 134f / 265f },
				{ 255f / 265f, 255f / 265f, 153f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Accent" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Accent5 {
		private final float[][] cols = { { 127f / 265f, 201f / 265f, 127f / 265f },
				{ 190f / 265f, 174f / 265f, 212f / 265f }, { 253f / 265f, 192f / 265f, 134f / 265f },
				{ 255f / 265f, 255f / 265f, 153f / 265f }, { 56f / 265f, 108f / 265f, 176f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Accent" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Accent6 {
		private final float[][] cols = { { 127f / 265f, 201f / 265f, 127f / 265f },
				{ 190f / 265f, 174f / 265f, 212f / 265f }, { 253f / 265f, 192f / 265f, 134f / 265f },
				{ 255f / 265f, 255f / 265f, 153f / 265f }, { 56f / 265f, 108f / 265f, 176f / 265f },
				{ 240f / 265f, 2f / 265f, 127f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Accent" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Accent7 {
		private final float[][] cols = { { 127f / 265f, 201f / 265f, 127f / 265f },
				{ 190f / 265f, 174f / 265f, 212f / 265f }, { 253f / 265f, 192f / 265f, 134f / 265f },
				{ 255f / 265f, 255f / 265f, 153f / 265f }, { 56f / 265f, 108f / 265f, 176f / 265f },
				{ 240f / 265f, 2f / 265f, 127f / 265f }, { 191f / 265f, 91f / 265f, 23f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Accent" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Accent8 {
		private final float[][] cols = { { 127f / 265f, 201f / 265f, 127f / 265f },
				{ 190f / 265f, 174f / 265f, 212f / 265f }, { 253f / 265f, 192f / 265f, 134f / 265f },
				{ 255f / 265f, 255f / 265f, 153f / 265f }, { 56f / 265f, 108f / 265f, 176f / 265f },
				{ 240f / 265f, 2f / 265f, 127f / 265f }, { 191f / 265f, 91f / 265f, 23f / 265f },
				{ 102f / 265f, 102f / 265f, 102f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Blues" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Blues3 {
		private final float[][] cols = { { 222f / 265f, 235f / 265f, 247f / 265f },
				{ 158f / 265f, 202f / 265f, 225f / 265f }, { 49f / 265f, 130f / 265f, 189f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Blues" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Blues4 {
		private final float[][] cols = { { 239f / 265f, 243f / 265f, 255f / 265f },
				{ 189f / 265f, 215f / 265f, 231f / 265f }, { 107f / 265f, 174f / 265f, 214f / 265f },
				{ 33f / 265f, 113f / 265f, 181f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Blues" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Blues5 {
		private final float[][] cols = { { 239f / 265f, 243f / 265f, 255f / 265f },
				{ 189f / 265f, 215f / 265f, 231f / 265f }, { 107f / 265f, 174f / 265f, 214f / 265f },
				{ 49f / 265f, 130f / 265f, 189f / 265f }, { 8f / 265f, 81f / 265f, 156f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Blues" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Blues6 {
		private final float[][] cols = { { 239f / 265f, 243f / 265f, 255f / 265f },
				{ 198f / 265f, 219f / 265f, 239f / 265f }, { 158f / 265f, 202f / 265f, 225f / 265f },
				{ 107f / 265f, 174f / 265f, 214f / 265f }, { 49f / 265f, 130f / 265f, 189f / 265f },
				{ 8f / 265f, 81f / 265f, 156f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Blues" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Blues7 {
		private final float[][] cols = { { 239f / 265f, 243f / 265f, 255f / 265f },
				{ 198f / 265f, 219f / 265f, 239f / 265f }, { 158f / 265f, 202f / 265f, 225f / 265f },
				{ 107f / 265f, 174f / 265f, 214f / 265f }, { 66f / 265f, 146f / 265f, 198f / 265f },
				{ 33f / 265f, 113f / 265f, 181f / 265f }, { 8f / 265f, 69f / 265f, 148f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Blues" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Blues8 {
		private final float[][] cols = { { 247f / 265f, 251f / 265f, 255f / 265f },
				{ 222f / 265f, 235f / 265f, 247f / 265f }, { 198f / 265f, 219f / 265f, 239f / 265f },
				{ 158f / 265f, 202f / 265f, 225f / 265f }, { 107f / 265f, 174f / 265f, 214f / 265f },
				{ 66f / 265f, 146f / 265f, 198f / 265f }, { 33f / 265f, 113f / 265f, 181f / 265f },
				{ 8f / 265f, 69f / 265f, 148f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Blues" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Blues9 {
		private final float[][] cols = { { 247f / 265f, 251f / 265f, 255f / 265f },
				{ 222f / 265f, 235f / 265f, 247f / 265f }, { 198f / 265f, 219f / 265f, 239f / 265f },
				{ 158f / 265f, 202f / 265f, 225f / 265f }, { 107f / 265f, 174f / 265f, 214f / 265f },
				{ 66f / 265f, 146f / 265f, 198f / 265f }, { 33f / 265f, 113f / 265f, 181f / 265f },
				{ 8f / 265f, 81f / 265f, 156f / 265f }, { 8f / 265f, 48f / 265f, 107f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BrBG" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BrBG3 {
		private final float[][] cols = { { 216f / 265f, 179f / 265f, 101f / 265f },
				{ 245f / 265f, 245f / 265f, 245f / 265f }, { 90f / 265f, 180f / 265f, 172f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BrBG" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BrBG4 {
		private final float[][] cols = { { 166f / 265f, 97f / 265f, 26f / 265f },
				{ 223f / 265f, 194f / 265f, 125f / 265f }, { 128f / 265f, 205f / 265f, 193f / 265f },
				{ 1f / 265f, 133f / 265f, 113f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BrBG" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BrBG5 {
		private final float[][] cols = { { 166f / 265f, 97f / 265f, 26f / 265f },
				{ 223f / 265f, 194f / 265f, 125f / 265f }, { 245f / 265f, 245f / 265f, 245f / 265f },
				{ 128f / 265f, 205f / 265f, 193f / 265f }, { 1f / 265f, 133f / 265f, 113f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BrBG" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BrBG6 {
		private final float[][] cols = { { 140f / 265f, 81f / 265f, 10f / 265f },
				{ 216f / 265f, 179f / 265f, 101f / 265f }, { 246f / 265f, 232f / 265f, 195f / 265f },
				{ 199f / 265f, 234f / 265f, 229f / 265f }, { 90f / 265f, 180f / 265f, 172f / 265f },
				{ 1f / 265f, 102f / 265f, 94f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BrBG" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BrBG7 {
		private final float[][] cols = { { 140f / 265f, 81f / 265f, 10f / 265f },
				{ 216f / 265f, 179f / 265f, 101f / 265f }, { 246f / 265f, 232f / 265f, 195f / 265f },
				{ 245f / 265f, 245f / 265f, 245f / 265f }, { 199f / 265f, 234f / 265f, 229f / 265f },
				{ 90f / 265f, 180f / 265f, 172f / 265f }, { 1f / 265f, 102f / 265f, 94f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BrBG" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BrBG8 {
		private final float[][] cols = { { 140f / 265f, 81f / 265f, 10f / 265f },
				{ 191f / 265f, 129f / 265f, 45f / 265f }, { 223f / 265f, 194f / 265f, 125f / 265f },
				{ 246f / 265f, 232f / 265f, 195f / 265f }, { 199f / 265f, 234f / 265f, 229f / 265f },
				{ 128f / 265f, 205f / 265f, 193f / 265f }, { 53f / 265f, 151f / 265f, 143f / 265f },
				{ 1f / 265f, 102f / 265f, 94f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BrBG" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BrBG9 {
		private final float[][] cols = { { 140f / 265f, 81f / 265f, 10f / 265f },
				{ 191f / 265f, 129f / 265f, 45f / 265f }, { 223f / 265f, 194f / 265f, 125f / 265f },
				{ 246f / 265f, 232f / 265f, 195f / 265f }, { 245f / 265f, 245f / 265f, 245f / 265f },
				{ 199f / 265f, 234f / 265f, 229f / 265f }, { 128f / 265f, 205f / 265f, 193f / 265f },
				{ 53f / 265f, 151f / 265f, 143f / 265f }, { 1f / 265f, 102f / 265f, 94f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BrBG" colour map with 10 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BrBG10 {
		private final float[][] cols = { { 84f / 265f, 48f / 265f, 5f / 265f }, { 140f / 265f, 81f / 265f, 10f / 265f },
				{ 191f / 265f, 129f / 265f, 45f / 265f }, { 223f / 265f, 194f / 265f, 125f / 265f },
				{ 246f / 265f, 232f / 265f, 195f / 265f }, { 199f / 265f, 234f / 265f, 229f / 265f },
				{ 128f / 265f, 205f / 265f, 193f / 265f }, { 53f / 265f, 151f / 265f, 143f / 265f },
				{ 1f / 265f, 102f / 265f, 94f / 265f }, { 0f / 265f, 60f / 265f, 48f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BrBG" colour map with 11 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BrBG11 {
		private final float[][] cols = { { 84f / 265f, 48f / 265f, 5f / 265f }, { 140f / 265f, 81f / 265f, 10f / 265f },
				{ 191f / 265f, 129f / 265f, 45f / 265f }, { 223f / 265f, 194f / 265f, 125f / 265f },
				{ 246f / 265f, 232f / 265f, 195f / 265f }, { 245f / 265f, 245f / 265f, 245f / 265f },
				{ 199f / 265f, 234f / 265f, 229f / 265f }, { 128f / 265f, 205f / 265f, 193f / 265f },
				{ 53f / 265f, 151f / 265f, 143f / 265f }, { 1f / 265f, 102f / 265f, 94f / 265f },
				{ 0f / 265f, 60f / 265f, 48f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BuGn" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BuGn3 {
		private final float[][] cols = { { 229f / 265f, 245f / 265f, 249f / 265f },
				{ 153f / 265f, 216f / 265f, 201f / 265f }, { 44f / 265f, 162f / 265f, 95f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BuGn" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BuGn4 {
		private final float[][] cols = { { 237f / 265f, 248f / 265f, 251f / 265f },
				{ 178f / 265f, 226f / 265f, 226f / 265f }, { 102f / 265f, 194f / 265f, 164f / 265f },
				{ 35f / 265f, 139f / 265f, 69f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BuGn" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BuGn5 {
		private final float[][] cols = { { 237f / 265f, 248f / 265f, 251f / 265f },
				{ 178f / 265f, 226f / 265f, 226f / 265f }, { 102f / 265f, 194f / 265f, 164f / 265f },
				{ 44f / 265f, 162f / 265f, 95f / 265f }, { 0f / 265f, 109f / 265f, 44f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BuGn" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BuGn6 {
		private final float[][] cols = { { 237f / 265f, 248f / 265f, 251f / 265f },
				{ 204f / 265f, 236f / 265f, 230f / 265f }, { 153f / 265f, 216f / 265f, 201f / 265f },
				{ 102f / 265f, 194f / 265f, 164f / 265f }, { 44f / 265f, 162f / 265f, 95f / 265f },
				{ 0f / 265f, 109f / 265f, 44f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BuGn" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BuGn7 {
		private final float[][] cols = { { 237f / 265f, 248f / 265f, 251f / 265f },
				{ 204f / 265f, 236f / 265f, 230f / 265f }, { 153f / 265f, 216f / 265f, 201f / 265f },
				{ 102f / 265f, 194f / 265f, 164f / 265f }, { 65f / 265f, 174f / 265f, 118f / 265f },
				{ 35f / 265f, 139f / 265f, 69f / 265f }, { 0f / 265f, 88f / 265f, 36f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BuGn" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BuGn8 {
		private final float[][] cols = { { 247f / 265f, 252f / 265f, 253f / 265f },
				{ 229f / 265f, 245f / 265f, 249f / 265f }, { 204f / 265f, 236f / 265f, 230f / 265f },
				{ 153f / 265f, 216f / 265f, 201f / 265f }, { 102f / 265f, 194f / 265f, 164f / 265f },
				{ 65f / 265f, 174f / 265f, 118f / 265f }, { 35f / 265f, 139f / 265f, 69f / 265f },
				{ 0f / 265f, 88f / 265f, 36f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BuGn" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BuGn9 {
		private final float[][] cols = { { 247f / 265f, 252f / 265f, 253f / 265f },
				{ 229f / 265f, 245f / 265f, 249f / 265f }, { 204f / 265f, 236f / 265f, 230f / 265f },
				{ 153f / 265f, 216f / 265f, 201f / 265f }, { 102f / 265f, 194f / 265f, 164f / 265f },
				{ 65f / 265f, 174f / 265f, 118f / 265f }, { 35f / 265f, 139f / 265f, 69f / 265f },
				{ 0f / 265f, 109f / 265f, 44f / 265f }, { 0f / 265f, 68f / 265f, 27f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BuPu" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BuPu3 {
		private final float[][] cols = { { 224f / 265f, 236f / 265f, 244f / 265f },
				{ 158f / 265f, 188f / 265f, 218f / 265f }, { 136f / 265f, 86f / 265f, 167f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BuPu" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BuPu4 {
		private final float[][] cols = { { 237f / 265f, 248f / 265f, 251f / 265f },
				{ 179f / 265f, 205f / 265f, 227f / 265f }, { 140f / 265f, 150f / 265f, 198f / 265f },
				{ 136f / 265f, 65f / 265f, 157f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BuPu" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BuPu5 {
		private final float[][] cols = { { 237f / 265f, 248f / 265f, 251f / 265f },
				{ 179f / 265f, 205f / 265f, 227f / 265f }, { 140f / 265f, 150f / 265f, 198f / 265f },
				{ 136f / 265f, 86f / 265f, 167f / 265f }, { 129f / 265f, 15f / 265f, 124f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BuPu" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BuPu6 {
		private final float[][] cols = { { 237f / 265f, 248f / 265f, 251f / 265f },
				{ 191f / 265f, 211f / 265f, 230f / 265f }, { 158f / 265f, 188f / 265f, 218f / 265f },
				{ 140f / 265f, 150f / 265f, 198f / 265f }, { 136f / 265f, 86f / 265f, 167f / 265f },
				{ 129f / 265f, 15f / 265f, 124f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BuPu" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BuPu7 {
		private final float[][] cols = { { 237f / 265f, 248f / 265f, 251f / 265f },
				{ 191f / 265f, 211f / 265f, 230f / 265f }, { 158f / 265f, 188f / 265f, 218f / 265f },
				{ 140f / 265f, 150f / 265f, 198f / 265f }, { 140f / 265f, 107f / 265f, 177f / 265f },
				{ 136f / 265f, 65f / 265f, 157f / 265f }, { 110f / 265f, 1f / 265f, 107f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BuPu" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BuPu8 {
		private final float[][] cols = { { 247f / 265f, 252f / 265f, 253f / 265f },
				{ 224f / 265f, 236f / 265f, 244f / 265f }, { 191f / 265f, 211f / 265f, 230f / 265f },
				{ 158f / 265f, 188f / 265f, 218f / 265f }, { 140f / 265f, 150f / 265f, 198f / 265f },
				{ 140f / 265f, 107f / 265f, 177f / 265f }, { 136f / 265f, 65f / 265f, 157f / 265f },
				{ 110f / 265f, 1f / 265f, 107f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "BuPu" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	BuPu9 {
		private final float[][] cols = { { 247f / 265f, 252f / 265f, 253f / 265f },
				{ 224f / 265f, 236f / 265f, 244f / 265f }, { 191f / 265f, 211f / 265f, 230f / 265f },
				{ 158f / 265f, 188f / 265f, 218f / 265f }, { 140f / 265f, 150f / 265f, 198f / 265f },
				{ 140f / 265f, 107f / 265f, 177f / 265f }, { 136f / 265f, 65f / 265f, 157f / 265f },
				{ 129f / 265f, 15f / 265f, 124f / 265f }, { 77f / 265f, 0f / 265f, 75f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Dark2" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Dark23 {
		private final float[][] cols = { { 27f / 265f, 158f / 265f, 119f / 265f },
				{ 217f / 265f, 95f / 265f, 2f / 265f }, { 117f / 265f, 112f / 265f, 179f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Dark2" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Dark24 {
		private final float[][] cols = { { 27f / 265f, 158f / 265f, 119f / 265f },
				{ 217f / 265f, 95f / 265f, 2f / 265f }, { 117f / 265f, 112f / 265f, 179f / 265f },
				{ 231f / 265f, 41f / 265f, 138f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Dark2" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Dark25 {
		private final float[][] cols = { { 27f / 265f, 158f / 265f, 119f / 265f },
				{ 217f / 265f, 95f / 265f, 2f / 265f }, { 117f / 265f, 112f / 265f, 179f / 265f },
				{ 231f / 265f, 41f / 265f, 138f / 265f }, { 102f / 265f, 166f / 265f, 30f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Dark2" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Dark26 {
		private final float[][] cols = { { 27f / 265f, 158f / 265f, 119f / 265f },
				{ 217f / 265f, 95f / 265f, 2f / 265f }, { 117f / 265f, 112f / 265f, 179f / 265f },
				{ 231f / 265f, 41f / 265f, 138f / 265f }, { 102f / 265f, 166f / 265f, 30f / 265f },
				{ 230f / 265f, 171f / 265f, 2f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Dark2" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Dark27 {
		private final float[][] cols = { { 27f / 265f, 158f / 265f, 119f / 265f },
				{ 217f / 265f, 95f / 265f, 2f / 265f }, { 117f / 265f, 112f / 265f, 179f / 265f },
				{ 231f / 265f, 41f / 265f, 138f / 265f }, { 102f / 265f, 166f / 265f, 30f / 265f },
				{ 230f / 265f, 171f / 265f, 2f / 265f }, { 166f / 265f, 118f / 265f, 29f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Dark2" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Dark28 {
		private final float[][] cols = { { 27f / 265f, 158f / 265f, 119f / 265f },
				{ 217f / 265f, 95f / 265f, 2f / 265f }, { 117f / 265f, 112f / 265f, 179f / 265f },
				{ 231f / 265f, 41f / 265f, 138f / 265f }, { 102f / 265f, 166f / 265f, 30f / 265f },
				{ 230f / 265f, 171f / 265f, 2f / 265f }, { 166f / 265f, 118f / 265f, 29f / 265f },
				{ 102f / 265f, 102f / 265f, 102f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "GnBu" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	GnBu3 {
		private final float[][] cols = { { 224f / 265f, 243f / 265f, 219f / 265f },
				{ 168f / 265f, 221f / 265f, 181f / 265f }, { 67f / 265f, 162f / 265f, 202f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "GnBu" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	GnBu4 {
		private final float[][] cols = { { 240f / 265f, 249f / 265f, 232f / 265f },
				{ 186f / 265f, 228f / 265f, 188f / 265f }, { 123f / 265f, 204f / 265f, 196f / 265f },
				{ 43f / 265f, 140f / 265f, 190f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "GnBu" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	GnBu5 {
		private final float[][] cols = { { 240f / 265f, 249f / 265f, 232f / 265f },
				{ 186f / 265f, 228f / 265f, 188f / 265f }, { 123f / 265f, 204f / 265f, 196f / 265f },
				{ 67f / 265f, 162f / 265f, 202f / 265f }, { 8f / 265f, 104f / 265f, 172f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "GnBu" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	GnBu6 {
		private final float[][] cols = { { 240f / 265f, 249f / 265f, 232f / 265f },
				{ 204f / 265f, 235f / 265f, 197f / 265f }, { 168f / 265f, 221f / 265f, 181f / 265f },
				{ 123f / 265f, 204f / 265f, 196f / 265f }, { 67f / 265f, 162f / 265f, 202f / 265f },
				{ 8f / 265f, 104f / 265f, 172f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "GnBu" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	GnBu7 {
		private final float[][] cols = { { 240f / 265f, 249f / 265f, 232f / 265f },
				{ 204f / 265f, 235f / 265f, 197f / 265f }, { 168f / 265f, 221f / 265f, 181f / 265f },
				{ 123f / 265f, 204f / 265f, 196f / 265f }, { 78f / 265f, 179f / 265f, 211f / 265f },
				{ 43f / 265f, 140f / 265f, 190f / 265f }, { 8f / 265f, 88f / 265f, 158f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "GnBu" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	GnBu8 {
		private final float[][] cols = { { 247f / 265f, 252f / 265f, 240f / 265f },
				{ 224f / 265f, 243f / 265f, 219f / 265f }, { 204f / 265f, 235f / 265f, 197f / 265f },
				{ 168f / 265f, 221f / 265f, 181f / 265f }, { 123f / 265f, 204f / 265f, 196f / 265f },
				{ 78f / 265f, 179f / 265f, 211f / 265f }, { 43f / 265f, 140f / 265f, 190f / 265f },
				{ 8f / 265f, 88f / 265f, 158f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "GnBu" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	GnBu9 {
		private final float[][] cols = { { 247f / 265f, 252f / 265f, 240f / 265f },
				{ 224f / 265f, 243f / 265f, 219f / 265f }, { 204f / 265f, 235f / 265f, 197f / 265f },
				{ 168f / 265f, 221f / 265f, 181f / 265f }, { 123f / 265f, 204f / 265f, 196f / 265f },
				{ 78f / 265f, 179f / 265f, 211f / 265f }, { 43f / 265f, 140f / 265f, 190f / 265f },
				{ 8f / 265f, 104f / 265f, 172f / 265f }, { 8f / 265f, 64f / 265f, 129f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Greens" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Greens3 {
		private final float[][] cols = { { 229f / 265f, 245f / 265f, 224f / 265f },
				{ 161f / 265f, 217f / 265f, 155f / 265f }, { 49f / 265f, 163f / 265f, 84f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Greens" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Greens4 {
		private final float[][] cols = { { 237f / 265f, 248f / 265f, 233f / 265f },
				{ 186f / 265f, 228f / 265f, 179f / 265f }, { 116f / 265f, 196f / 265f, 118f / 265f },
				{ 35f / 265f, 139f / 265f, 69f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Greens" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Greens5 {
		private final float[][] cols = { { 237f / 265f, 248f / 265f, 233f / 265f },
				{ 186f / 265f, 228f / 265f, 179f / 265f }, { 116f / 265f, 196f / 265f, 118f / 265f },
				{ 49f / 265f, 163f / 265f, 84f / 265f }, { 0f / 265f, 109f / 265f, 44f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Greens" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Greens6 {
		private final float[][] cols = { { 237f / 265f, 248f / 265f, 233f / 265f },
				{ 199f / 265f, 233f / 265f, 192f / 265f }, { 161f / 265f, 217f / 265f, 155f / 265f },
				{ 116f / 265f, 196f / 265f, 118f / 265f }, { 49f / 265f, 163f / 265f, 84f / 265f },
				{ 0f / 265f, 109f / 265f, 44f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Greens" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Greens7 {
		private final float[][] cols = { { 237f / 265f, 248f / 265f, 233f / 265f },
				{ 199f / 265f, 233f / 265f, 192f / 265f }, { 161f / 265f, 217f / 265f, 155f / 265f },
				{ 116f / 265f, 196f / 265f, 118f / 265f }, { 65f / 265f, 171f / 265f, 93f / 265f },
				{ 35f / 265f, 139f / 265f, 69f / 265f }, { 0f / 265f, 90f / 265f, 50f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Greens" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Greens8 {
		private final float[][] cols = { { 247f / 265f, 252f / 265f, 245f / 265f },
				{ 229f / 265f, 245f / 265f, 224f / 265f }, { 199f / 265f, 233f / 265f, 192f / 265f },
				{ 161f / 265f, 217f / 265f, 155f / 265f }, { 116f / 265f, 196f / 265f, 118f / 265f },
				{ 65f / 265f, 171f / 265f, 93f / 265f }, { 35f / 265f, 139f / 265f, 69f / 265f },
				{ 0f / 265f, 90f / 265f, 50f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Greens" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Greens9 {
		private final float[][] cols = { { 247f / 265f, 252f / 265f, 245f / 265f },
				{ 229f / 265f, 245f / 265f, 224f / 265f }, { 199f / 265f, 233f / 265f, 192f / 265f },
				{ 161f / 265f, 217f / 265f, 155f / 265f }, { 116f / 265f, 196f / 265f, 118f / 265f },
				{ 65f / 265f, 171f / 265f, 93f / 265f }, { 35f / 265f, 139f / 265f, 69f / 265f },
				{ 0f / 265f, 109f / 265f, 44f / 265f }, { 0f / 265f, 68f / 265f, 27f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Greys" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Greys3 {
		private final float[][] cols = { { 240f / 265f, 240f / 265f, 240f / 265f },
				{ 189f / 265f, 189f / 265f, 189f / 265f }, { 99f / 265f, 99f / 265f, 99f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Greys" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Greys4 {
		private final float[][] cols = { { 247f / 265f, 247f / 265f, 247f / 265f },
				{ 204f / 265f, 204f / 265f, 204f / 265f }, { 150f / 265f, 150f / 265f, 150f / 265f },
				{ 82f / 265f, 82f / 265f, 82f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Greys" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Greys5 {
		private final float[][] cols = { { 247f / 265f, 247f / 265f, 247f / 265f },
				{ 204f / 265f, 204f / 265f, 204f / 265f }, { 150f / 265f, 150f / 265f, 150f / 265f },
				{ 99f / 265f, 99f / 265f, 99f / 265f }, { 37f / 265f, 37f / 265f, 37f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Greys" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Greys6 {
		private final float[][] cols = { { 247f / 265f, 247f / 265f, 247f / 265f },
				{ 217f / 265f, 217f / 265f, 217f / 265f }, { 189f / 265f, 189f / 265f, 189f / 265f },
				{ 150f / 265f, 150f / 265f, 150f / 265f }, { 99f / 265f, 99f / 265f, 99f / 265f },
				{ 37f / 265f, 37f / 265f, 37f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Greys" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Greys7 {
		private final float[][] cols = { { 247f / 265f, 247f / 265f, 247f / 265f },
				{ 217f / 265f, 217f / 265f, 217f / 265f }, { 189f / 265f, 189f / 265f, 189f / 265f },
				{ 150f / 265f, 150f / 265f, 150f / 265f }, { 115f / 265f, 115f / 265f, 115f / 265f },
				{ 82f / 265f, 82f / 265f, 82f / 265f }, { 37f / 265f, 37f / 265f, 37f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Greys" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Greys8 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 255f / 265f },
				{ 240f / 265f, 240f / 265f, 240f / 265f }, { 217f / 265f, 217f / 265f, 217f / 265f },
				{ 189f / 265f, 189f / 265f, 189f / 265f }, { 150f / 265f, 150f / 265f, 150f / 265f },
				{ 115f / 265f, 115f / 265f, 115f / 265f }, { 82f / 265f, 82f / 265f, 82f / 265f },
				{ 37f / 265f, 37f / 265f, 37f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Greys" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Greys9 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 255f / 265f },
				{ 240f / 265f, 240f / 265f, 240f / 265f }, { 217f / 265f, 217f / 265f, 217f / 265f },
				{ 189f / 265f, 189f / 265f, 189f / 265f }, { 150f / 265f, 150f / 265f, 150f / 265f },
				{ 115f / 265f, 115f / 265f, 115f / 265f }, { 82f / 265f, 82f / 265f, 82f / 265f },
				{ 37f / 265f, 37f / 265f, 37f / 265f }, { 0f / 265f, 0f / 265f, 0f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Oranges" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Oranges3 {
		private final float[][] cols = { { 254f / 265f, 230f / 265f, 206f / 265f },
				{ 253f / 265f, 174f / 265f, 107f / 265f }, { 230f / 265f, 85f / 265f, 13f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Oranges" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Oranges4 {
		private final float[][] cols = { { 254f / 265f, 237f / 265f, 222f / 265f },
				{ 253f / 265f, 190f / 265f, 133f / 265f }, { 253f / 265f, 141f / 265f, 60f / 265f },
				{ 217f / 265f, 71f / 265f, 1f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Oranges" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Oranges5 {
		private final float[][] cols = { { 254f / 265f, 237f / 265f, 222f / 265f },
				{ 253f / 265f, 190f / 265f, 133f / 265f }, { 253f / 265f, 141f / 265f, 60f / 265f },
				{ 230f / 265f, 85f / 265f, 13f / 265f }, { 166f / 265f, 54f / 265f, 3f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Oranges" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Oranges6 {
		private final float[][] cols = { { 254f / 265f, 237f / 265f, 222f / 265f },
				{ 253f / 265f, 208f / 265f, 162f / 265f }, { 253f / 265f, 174f / 265f, 107f / 265f },
				{ 253f / 265f, 141f / 265f, 60f / 265f }, { 230f / 265f, 85f / 265f, 13f / 265f },
				{ 166f / 265f, 54f / 265f, 3f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Oranges" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Oranges7 {
		private final float[][] cols = { { 254f / 265f, 237f / 265f, 222f / 265f },
				{ 253f / 265f, 208f / 265f, 162f / 265f }, { 253f / 265f, 174f / 265f, 107f / 265f },
				{ 253f / 265f, 141f / 265f, 60f / 265f }, { 241f / 265f, 105f / 265f, 19f / 265f },
				{ 217f / 265f, 72f / 265f, 1f / 265f }, { 140f / 265f, 45f / 265f, 4f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Oranges" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Oranges8 {
		private final float[][] cols = { { 255f / 265f, 245f / 265f, 235f / 265f },
				{ 254f / 265f, 230f / 265f, 206f / 265f }, { 253f / 265f, 208f / 265f, 162f / 265f },
				{ 253f / 265f, 174f / 265f, 107f / 265f }, { 253f / 265f, 141f / 265f, 60f / 265f },
				{ 241f / 265f, 105f / 265f, 19f / 265f }, { 217f / 265f, 72f / 265f, 1f / 265f },
				{ 140f / 265f, 45f / 265f, 4f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Oranges" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Oranges9 {
		private final float[][] cols = { { 255f / 265f, 245f / 265f, 235f / 265f },
				{ 254f / 265f, 230f / 265f, 206f / 265f }, { 253f / 265f, 208f / 265f, 162f / 265f },
				{ 253f / 265f, 174f / 265f, 107f / 265f }, { 253f / 265f, 141f / 265f, 60f / 265f },
				{ 241f / 265f, 105f / 265f, 19f / 265f }, { 217f / 265f, 72f / 265f, 1f / 265f },
				{ 166f / 265f, 54f / 265f, 3f / 265f }, { 127f / 265f, 39f / 265f, 4f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "OrRd" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	OrRd3 {
		private final float[][] cols = { { 254f / 265f, 232f / 265f, 200f / 265f },
				{ 253f / 265f, 187f / 265f, 132f / 265f }, { 227f / 265f, 74f / 265f, 51f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "OrRd" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	OrRd4 {
		private final float[][] cols = { { 254f / 265f, 240f / 265f, 217f / 265f },
				{ 253f / 265f, 204f / 265f, 138f / 265f }, { 252f / 265f, 141f / 265f, 89f / 265f },
				{ 215f / 265f, 48f / 265f, 31f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "OrRd" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	OrRd5 {
		private final float[][] cols = { { 254f / 265f, 240f / 265f, 217f / 265f },
				{ 253f / 265f, 204f / 265f, 138f / 265f }, { 252f / 265f, 141f / 265f, 89f / 265f },
				{ 227f / 265f, 74f / 265f, 51f / 265f }, { 179f / 265f, 0f / 265f, 0f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "OrRd" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	OrRd6 {
		private final float[][] cols = { { 254f / 265f, 240f / 265f, 217f / 265f },
				{ 253f / 265f, 212f / 265f, 158f / 265f }, { 253f / 265f, 187f / 265f, 132f / 265f },
				{ 252f / 265f, 141f / 265f, 89f / 265f }, { 227f / 265f, 74f / 265f, 51f / 265f },
				{ 179f / 265f, 0f / 265f, 0f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "OrRd" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	OrRd7 {
		private final float[][] cols = { { 254f / 265f, 240f / 265f, 217f / 265f },
				{ 253f / 265f, 212f / 265f, 158f / 265f }, { 253f / 265f, 187f / 265f, 132f / 265f },
				{ 252f / 265f, 141f / 265f, 89f / 265f }, { 239f / 265f, 101f / 265f, 72f / 265f },
				{ 215f / 265f, 48f / 265f, 31f / 265f }, { 153f / 265f, 0f / 265f, 0f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "OrRd" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	OrRd8 {
		private final float[][] cols = { { 255f / 265f, 247f / 265f, 236f / 265f },
				{ 254f / 265f, 232f / 265f, 200f / 265f }, { 253f / 265f, 212f / 265f, 158f / 265f },
				{ 253f / 265f, 187f / 265f, 132f / 265f }, { 252f / 265f, 141f / 265f, 89f / 265f },
				{ 239f / 265f, 101f / 265f, 72f / 265f }, { 215f / 265f, 48f / 265f, 31f / 265f },
				{ 153f / 265f, 0f / 265f, 0f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "OrRd" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	OrRd9 {
		private final float[][] cols = { { 255f / 265f, 247f / 265f, 236f / 265f },
				{ 254f / 265f, 232f / 265f, 200f / 265f }, { 253f / 265f, 212f / 265f, 158f / 265f },
				{ 253f / 265f, 187f / 265f, 132f / 265f }, { 252f / 265f, 141f / 265f, 89f / 265f },
				{ 239f / 265f, 101f / 265f, 72f / 265f }, { 215f / 265f, 48f / 265f, 31f / 265f },
				{ 179f / 265f, 0f / 265f, 0f / 265f }, { 127f / 265f, 0f / 265f, 0f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Paired" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Paired3 {
		private final float[][] cols = { { 166f / 265f, 206f / 265f, 227f / 265f },
				{ 31f / 265f, 120f / 265f, 180f / 265f }, { 178f / 265f, 223f / 265f, 138f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Paired" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Paired4 {
		private final float[][] cols = { { 166f / 265f, 206f / 265f, 227f / 265f },
				{ 31f / 265f, 120f / 265f, 180f / 265f }, { 178f / 265f, 223f / 265f, 138f / 265f },
				{ 51f / 265f, 160f / 265f, 44f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Paired" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Paired5 {
		private final float[][] cols = { { 166f / 265f, 206f / 265f, 227f / 265f },
				{ 31f / 265f, 120f / 265f, 180f / 265f }, { 178f / 265f, 223f / 265f, 138f / 265f },
				{ 51f / 265f, 160f / 265f, 44f / 265f }, { 251f / 265f, 154f / 265f, 153f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Paired" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Paired6 {
		private final float[][] cols = { { 166f / 265f, 206f / 265f, 227f / 265f },
				{ 31f / 265f, 120f / 265f, 180f / 265f }, { 178f / 265f, 223f / 265f, 138f / 265f },
				{ 51f / 265f, 160f / 265f, 44f / 265f }, { 251f / 265f, 154f / 265f, 153f / 265f },
				{ 227f / 265f, 26f / 265f, 28f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Paired" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Paired7 {
		private final float[][] cols = { { 166f / 265f, 206f / 265f, 227f / 265f },
				{ 31f / 265f, 120f / 265f, 180f / 265f }, { 178f / 265f, 223f / 265f, 138f / 265f },
				{ 51f / 265f, 160f / 265f, 44f / 265f }, { 251f / 265f, 154f / 265f, 153f / 265f },
				{ 227f / 265f, 26f / 265f, 28f / 265f }, { 253f / 265f, 191f / 265f, 111f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Paired" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Paired8 {
		private final float[][] cols = { { 166f / 265f, 206f / 265f, 227f / 265f },
				{ 31f / 265f, 120f / 265f, 180f / 265f }, { 178f / 265f, 223f / 265f, 138f / 265f },
				{ 51f / 265f, 160f / 265f, 44f / 265f }, { 251f / 265f, 154f / 265f, 153f / 265f },
				{ 227f / 265f, 26f / 265f, 28f / 265f }, { 253f / 265f, 191f / 265f, 111f / 265f },
				{ 255f / 265f, 127f / 265f, 0f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Paired" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Paired9 {
		private final float[][] cols = { { 166f / 265f, 206f / 265f, 227f / 265f },
				{ 31f / 265f, 120f / 265f, 180f / 265f }, { 178f / 265f, 223f / 265f, 138f / 265f },
				{ 51f / 265f, 160f / 265f, 44f / 265f }, { 251f / 265f, 154f / 265f, 153f / 265f },
				{ 227f / 265f, 26f / 265f, 28f / 265f }, { 253f / 265f, 191f / 265f, 111f / 265f },
				{ 255f / 265f, 127f / 265f, 0f / 265f }, { 202f / 265f, 178f / 265f, 214f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Paired" colour map with 10 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Paired10 {
		private final float[][] cols = { { 166f / 265f, 206f / 265f, 227f / 265f },
				{ 31f / 265f, 120f / 265f, 180f / 265f }, { 178f / 265f, 223f / 265f, 138f / 265f },
				{ 51f / 265f, 160f / 265f, 44f / 265f }, { 251f / 265f, 154f / 265f, 153f / 265f },
				{ 227f / 265f, 26f / 265f, 28f / 265f }, { 253f / 265f, 191f / 265f, 111f / 265f },
				{ 255f / 265f, 127f / 265f, 0f / 265f }, { 202f / 265f, 178f / 265f, 214f / 265f },
				{ 106f / 265f, 61f / 265f, 154f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Paired" colour map with 11 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Paired11 {
		private final float[][] cols = { { 166f / 265f, 206f / 265f, 227f / 265f },
				{ 31f / 265f, 120f / 265f, 180f / 265f }, { 178f / 265f, 223f / 265f, 138f / 265f },
				{ 51f / 265f, 160f / 265f, 44f / 265f }, { 251f / 265f, 154f / 265f, 153f / 265f },
				{ 227f / 265f, 26f / 265f, 28f / 265f }, { 253f / 265f, 191f / 265f, 111f / 265f },
				{ 255f / 265f, 127f / 265f, 0f / 265f }, { 202f / 265f, 178f / 265f, 214f / 265f },
				{ 106f / 265f, 61f / 265f, 154f / 265f }, { 255f / 265f, 255f / 265f, 153f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Paired" colour map with 12 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Paired12 {
		private final float[][] cols = { { 166f / 265f, 206f / 265f, 227f / 265f },
				{ 31f / 265f, 120f / 265f, 180f / 265f }, { 178f / 265f, 223f / 265f, 138f / 265f },
				{ 51f / 265f, 160f / 265f, 44f / 265f }, { 251f / 265f, 154f / 265f, 153f / 265f },
				{ 227f / 265f, 26f / 265f, 28f / 265f }, { 253f / 265f, 191f / 265f, 111f / 265f },
				{ 255f / 265f, 127f / 265f, 0f / 265f }, { 202f / 265f, 178f / 265f, 214f / 265f },
				{ 106f / 265f, 61f / 265f, 154f / 265f }, { 255f / 265f, 255f / 265f, 153f / 265f },
				{ 177f / 265f, 89f / 265f, 40f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Pastel1" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Pastel13 {
		private final float[][] cols = { { 251f / 265f, 180f / 265f, 174f / 265f },
				{ 179f / 265f, 205f / 265f, 227f / 265f }, { 204f / 265f, 235f / 265f, 197f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Pastel1" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Pastel14 {
		private final float[][] cols = { { 251f / 265f, 180f / 265f, 174f / 265f },
				{ 179f / 265f, 205f / 265f, 227f / 265f }, { 204f / 265f, 235f / 265f, 197f / 265f },
				{ 222f / 265f, 203f / 265f, 228f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Pastel1" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Pastel15 {
		private final float[][] cols = { { 251f / 265f, 180f / 265f, 174f / 265f },
				{ 179f / 265f, 205f / 265f, 227f / 265f }, { 204f / 265f, 235f / 265f, 197f / 265f },
				{ 222f / 265f, 203f / 265f, 228f / 265f }, { 254f / 265f, 217f / 265f, 166f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Pastel1" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Pastel16 {
		private final float[][] cols = { { 251f / 265f, 180f / 265f, 174f / 265f },
				{ 179f / 265f, 205f / 265f, 227f / 265f }, { 204f / 265f, 235f / 265f, 197f / 265f },
				{ 222f / 265f, 203f / 265f, 228f / 265f }, { 254f / 265f, 217f / 265f, 166f / 265f },
				{ 255f / 265f, 255f / 265f, 204f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Pastel1" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Pastel17 {
		private final float[][] cols = { { 251f / 265f, 180f / 265f, 174f / 265f },
				{ 179f / 265f, 205f / 265f, 227f / 265f }, { 204f / 265f, 235f / 265f, 197f / 265f },
				{ 222f / 265f, 203f / 265f, 228f / 265f }, { 254f / 265f, 217f / 265f, 166f / 265f },
				{ 255f / 265f, 255f / 265f, 204f / 265f }, { 229f / 265f, 216f / 265f, 189f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Pastel1" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Pastel18 {
		private final float[][] cols = { { 251f / 265f, 180f / 265f, 174f / 265f },
				{ 179f / 265f, 205f / 265f, 227f / 265f }, { 204f / 265f, 235f / 265f, 197f / 265f },
				{ 222f / 265f, 203f / 265f, 228f / 265f }, { 254f / 265f, 217f / 265f, 166f / 265f },
				{ 255f / 265f, 255f / 265f, 204f / 265f }, { 229f / 265f, 216f / 265f, 189f / 265f },
				{ 253f / 265f, 218f / 265f, 236f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Pastel1" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Pastel19 {
		private final float[][] cols = { { 251f / 265f, 180f / 265f, 174f / 265f },
				{ 179f / 265f, 205f / 265f, 227f / 265f }, { 204f / 265f, 235f / 265f, 197f / 265f },
				{ 222f / 265f, 203f / 265f, 228f / 265f }, { 254f / 265f, 217f / 265f, 166f / 265f },
				{ 255f / 265f, 255f / 265f, 204f / 265f }, { 229f / 265f, 216f / 265f, 189f / 265f },
				{ 253f / 265f, 218f / 265f, 236f / 265f }, { 242f / 265f, 242f / 265f, 242f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Pastel2" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Pastel23 {
		private final float[][] cols = { { 179f / 265f, 226f / 265f, 205f / 265f },
				{ 253f / 265f, 205f / 265f, 172f / 265f }, { 203f / 265f, 213f / 265f, 232f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Pastel2" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Pastel24 {
		private final float[][] cols = { { 179f / 265f, 226f / 265f, 205f / 265f },
				{ 253f / 265f, 205f / 265f, 172f / 265f }, { 203f / 265f, 213f / 265f, 232f / 265f },
				{ 244f / 265f, 202f / 265f, 228f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Pastel2" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Pastel25 {
		private final float[][] cols = { { 179f / 265f, 226f / 265f, 205f / 265f },
				{ 253f / 265f, 205f / 265f, 172f / 265f }, { 203f / 265f, 213f / 265f, 232f / 265f },
				{ 244f / 265f, 202f / 265f, 228f / 265f }, { 230f / 265f, 245f / 265f, 201f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Pastel2" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Pastel26 {
		private final float[][] cols = { { 179f / 265f, 226f / 265f, 205f / 265f },
				{ 253f / 265f, 205f / 265f, 172f / 265f }, { 203f / 265f, 213f / 265f, 232f / 265f },
				{ 244f / 265f, 202f / 265f, 228f / 265f }, { 230f / 265f, 245f / 265f, 201f / 265f },
				{ 255f / 265f, 242f / 265f, 174f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Pastel2" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Pastel27 {
		private final float[][] cols = { { 179f / 265f, 226f / 265f, 205f / 265f },
				{ 253f / 265f, 205f / 265f, 172f / 265f }, { 203f / 265f, 213f / 265f, 232f / 265f },
				{ 244f / 265f, 202f / 265f, 228f / 265f }, { 230f / 265f, 245f / 265f, 201f / 265f },
				{ 255f / 265f, 242f / 265f, 174f / 265f }, { 241f / 265f, 226f / 265f, 204f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Pastel2" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Pastel28 {
		private final float[][] cols = { { 179f / 265f, 226f / 265f, 205f / 265f },
				{ 253f / 265f, 205f / 265f, 172f / 265f }, { 203f / 265f, 213f / 265f, 232f / 265f },
				{ 244f / 265f, 202f / 265f, 228f / 265f }, { 230f / 265f, 245f / 265f, 201f / 265f },
				{ 255f / 265f, 242f / 265f, 174f / 265f }, { 241f / 265f, 226f / 265f, 204f / 265f },
				{ 204f / 265f, 204f / 265f, 204f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PiYG" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PiYG3 {
		private final float[][] cols = { { 233f / 265f, 163f / 265f, 201f / 265f },
				{ 247f / 265f, 247f / 265f, 247f / 265f }, { 161f / 265f, 215f / 265f, 106f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PiYG" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PiYG4 {
		private final float[][] cols = { { 208f / 265f, 28f / 265f, 139f / 265f },
				{ 241f / 265f, 182f / 265f, 218f / 265f }, { 184f / 265f, 225f / 265f, 134f / 265f },
				{ 77f / 265f, 172f / 265f, 38f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PiYG" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PiYG5 {
		private final float[][] cols = { { 208f / 265f, 28f / 265f, 139f / 265f },
				{ 241f / 265f, 182f / 265f, 218f / 265f }, { 247f / 265f, 247f / 265f, 247f / 265f },
				{ 184f / 265f, 225f / 265f, 134f / 265f }, { 77f / 265f, 172f / 265f, 38f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PiYG" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PiYG6 {
		private final float[][] cols = { { 197f / 265f, 27f / 265f, 125f / 265f },
				{ 233f / 265f, 163f / 265f, 201f / 265f }, { 253f / 265f, 224f / 265f, 239f / 265f },
				{ 230f / 265f, 245f / 265f, 208f / 265f }, { 161f / 265f, 215f / 265f, 106f / 265f },
				{ 77f / 265f, 146f / 265f, 33f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PiYG" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PiYG7 {
		private final float[][] cols = { { 197f / 265f, 27f / 265f, 125f / 265f },
				{ 233f / 265f, 163f / 265f, 201f / 265f }, { 253f / 265f, 224f / 265f, 239f / 265f },
				{ 247f / 265f, 247f / 265f, 247f / 265f }, { 230f / 265f, 245f / 265f, 208f / 265f },
				{ 161f / 265f, 215f / 265f, 106f / 265f }, { 77f / 265f, 146f / 265f, 33f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PiYG" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PiYG8 {
		private final float[][] cols = { { 197f / 265f, 27f / 265f, 125f / 265f },
				{ 222f / 265f, 119f / 265f, 174f / 265f }, { 241f / 265f, 182f / 265f, 218f / 265f },
				{ 253f / 265f, 224f / 265f, 239f / 265f }, { 230f / 265f, 245f / 265f, 208f / 265f },
				{ 184f / 265f, 225f / 265f, 134f / 265f }, { 127f / 265f, 188f / 265f, 65f / 265f },
				{ 77f / 265f, 146f / 265f, 33f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PiYG" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PiYG9 {
		private final float[][] cols = { { 197f / 265f, 27f / 265f, 125f / 265f },
				{ 222f / 265f, 119f / 265f, 174f / 265f }, { 241f / 265f, 182f / 265f, 218f / 265f },
				{ 253f / 265f, 224f / 265f, 239f / 265f }, { 247f / 265f, 247f / 265f, 247f / 265f },
				{ 230f / 265f, 245f / 265f, 208f / 265f }, { 184f / 265f, 225f / 265f, 134f / 265f },
				{ 127f / 265f, 188f / 265f, 65f / 265f }, { 77f / 265f, 146f / 265f, 33f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PiYG" colour map with 10 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PiYG10 {
		private final float[][] cols = { { 142f / 265f, 1f / 265f, 82f / 265f },
				{ 197f / 265f, 27f / 265f, 125f / 265f }, { 222f / 265f, 119f / 265f, 174f / 265f },
				{ 241f / 265f, 182f / 265f, 218f / 265f }, { 253f / 265f, 224f / 265f, 239f / 265f },
				{ 230f / 265f, 245f / 265f, 208f / 265f }, { 184f / 265f, 225f / 265f, 134f / 265f },
				{ 127f / 265f, 188f / 265f, 65f / 265f }, { 77f / 265f, 146f / 265f, 33f / 265f },
				{ 39f / 265f, 100f / 265f, 25f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PiYG" colour map with 11 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PiYG11 {
		private final float[][] cols = { { 142f / 265f, 1f / 265f, 82f / 265f },
				{ 197f / 265f, 27f / 265f, 125f / 265f }, { 222f / 265f, 119f / 265f, 174f / 265f },
				{ 241f / 265f, 182f / 265f, 218f / 265f }, { 253f / 265f, 224f / 265f, 239f / 265f },
				{ 247f / 265f, 247f / 265f, 247f / 265f }, { 230f / 265f, 245f / 265f, 208f / 265f },
				{ 184f / 265f, 225f / 265f, 134f / 265f }, { 127f / 265f, 188f / 265f, 65f / 265f },
				{ 77f / 265f, 146f / 265f, 33f / 265f }, { 39f / 265f, 100f / 265f, 25f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PRGn" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PRGn3 {
		private final float[][] cols = { { 175f / 265f, 141f / 265f, 195f / 265f },
				{ 247f / 265f, 247f / 265f, 247f / 265f }, { 127f / 265f, 191f / 265f, 123f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PRGn" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PRGn4 {
		private final float[][] cols = { { 123f / 265f, 50f / 265f, 148f / 265f },
				{ 194f / 265f, 165f / 265f, 207f / 265f }, { 166f / 265f, 219f / 265f, 160f / 265f },
				{ 0f / 265f, 136f / 265f, 55f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PRGn" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PRGn5 {
		private final float[][] cols = { { 123f / 265f, 50f / 265f, 148f / 265f },
				{ 194f / 265f, 165f / 265f, 207f / 265f }, { 247f / 265f, 247f / 265f, 247f / 265f },
				{ 166f / 265f, 219f / 265f, 160f / 265f }, { 0f / 265f, 136f / 265f, 55f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PRGn" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PRGn6 {
		private final float[][] cols = { { 118f / 265f, 42f / 265f, 131f / 265f },
				{ 175f / 265f, 141f / 265f, 195f / 265f }, { 231f / 265f, 212f / 265f, 232f / 265f },
				{ 217f / 265f, 240f / 265f, 211f / 265f }, { 127f / 265f, 191f / 265f, 123f / 265f },
				{ 27f / 265f, 120f / 265f, 55f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PRGn" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PRGn7 {
		private final float[][] cols = { { 118f / 265f, 42f / 265f, 131f / 265f },
				{ 175f / 265f, 141f / 265f, 195f / 265f }, { 231f / 265f, 212f / 265f, 232f / 265f },
				{ 247f / 265f, 247f / 265f, 247f / 265f }, { 217f / 265f, 240f / 265f, 211f / 265f },
				{ 127f / 265f, 191f / 265f, 123f / 265f }, { 27f / 265f, 120f / 265f, 55f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PRGn" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PRGn8 {
		private final float[][] cols = { { 118f / 265f, 42f / 265f, 131f / 265f },
				{ 153f / 265f, 112f / 265f, 171f / 265f }, { 194f / 265f, 165f / 265f, 207f / 265f },
				{ 231f / 265f, 212f / 265f, 232f / 265f }, { 217f / 265f, 240f / 265f, 211f / 265f },
				{ 166f / 265f, 219f / 265f, 160f / 265f }, { 90f / 265f, 174f / 265f, 97f / 265f },
				{ 27f / 265f, 120f / 265f, 55f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PRGn" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PRGn9 {
		private final float[][] cols = { { 118f / 265f, 42f / 265f, 131f / 265f },
				{ 153f / 265f, 112f / 265f, 171f / 265f }, { 194f / 265f, 165f / 265f, 207f / 265f },
				{ 231f / 265f, 212f / 265f, 232f / 265f }, { 247f / 265f, 247f / 265f, 247f / 265f },
				{ 217f / 265f, 240f / 265f, 211f / 265f }, { 166f / 265f, 219f / 265f, 160f / 265f },
				{ 90f / 265f, 174f / 265f, 97f / 265f }, { 27f / 265f, 120f / 265f, 55f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PRGn" colour map with 10 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PRGn10 {
		private final float[][] cols = { { 64f / 265f, 0f / 265f, 75f / 265f }, { 118f / 265f, 42f / 265f, 131f / 265f },
				{ 153f / 265f, 112f / 265f, 171f / 265f }, { 194f / 265f, 165f / 265f, 207f / 265f },
				{ 231f / 265f, 212f / 265f, 232f / 265f }, { 217f / 265f, 240f / 265f, 211f / 265f },
				{ 166f / 265f, 219f / 265f, 160f / 265f }, { 90f / 265f, 174f / 265f, 97f / 265f },
				{ 27f / 265f, 120f / 265f, 55f / 265f }, { 0f / 265f, 68f / 265f, 27f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PRGn" colour map with 11 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PRGn11 {
		private final float[][] cols = { { 64f / 265f, 0f / 265f, 75f / 265f }, { 118f / 265f, 42f / 265f, 131f / 265f },
				{ 153f / 265f, 112f / 265f, 171f / 265f }, { 194f / 265f, 165f / 265f, 207f / 265f },
				{ 231f / 265f, 212f / 265f, 232f / 265f }, { 247f / 265f, 247f / 265f, 247f / 265f },
				{ 217f / 265f, 240f / 265f, 211f / 265f }, { 166f / 265f, 219f / 265f, 160f / 265f },
				{ 90f / 265f, 174f / 265f, 97f / 265f }, { 27f / 265f, 120f / 265f, 55f / 265f },
				{ 0f / 265f, 68f / 265f, 27f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuBu" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuBu3 {
		private final float[][] cols = { { 236f / 265f, 231f / 265f, 242f / 265f },
				{ 166f / 265f, 189f / 265f, 219f / 265f }, { 43f / 265f, 140f / 265f, 190f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuBu" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuBu4 {
		private final float[][] cols = { { 241f / 265f, 238f / 265f, 246f / 265f },
				{ 189f / 265f, 201f / 265f, 225f / 265f }, { 116f / 265f, 169f / 265f, 207f / 265f },
				{ 5f / 265f, 112f / 265f, 176f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuBu" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuBu5 {
		private final float[][] cols = { { 241f / 265f, 238f / 265f, 246f / 265f },
				{ 189f / 265f, 201f / 265f, 225f / 265f }, { 116f / 265f, 169f / 265f, 207f / 265f },
				{ 43f / 265f, 140f / 265f, 190f / 265f }, { 4f / 265f, 90f / 265f, 141f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuBu" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuBu6 {
		private final float[][] cols = { { 241f / 265f, 238f / 265f, 246f / 265f },
				{ 208f / 265f, 209f / 265f, 230f / 265f }, { 166f / 265f, 189f / 265f, 219f / 265f },
				{ 116f / 265f, 169f / 265f, 207f / 265f }, { 43f / 265f, 140f / 265f, 190f / 265f },
				{ 4f / 265f, 90f / 265f, 141f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuBu" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuBu7 {
		private final float[][] cols = { { 241f / 265f, 238f / 265f, 246f / 265f },
				{ 208f / 265f, 209f / 265f, 230f / 265f }, { 166f / 265f, 189f / 265f, 219f / 265f },
				{ 116f / 265f, 169f / 265f, 207f / 265f }, { 54f / 265f, 144f / 265f, 192f / 265f },
				{ 5f / 265f, 112f / 265f, 176f / 265f }, { 3f / 265f, 78f / 265f, 123f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuBu" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuBu8 {
		private final float[][] cols = { { 255f / 265f, 247f / 265f, 251f / 265f },
				{ 236f / 265f, 231f / 265f, 242f / 265f }, { 208f / 265f, 209f / 265f, 230f / 265f },
				{ 166f / 265f, 189f / 265f, 219f / 265f }, { 116f / 265f, 169f / 265f, 207f / 265f },
				{ 54f / 265f, 144f / 265f, 192f / 265f }, { 5f / 265f, 112f / 265f, 176f / 265f },
				{ 3f / 265f, 78f / 265f, 123f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuBu" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuBu9 {
		private final float[][] cols = { { 255f / 265f, 247f / 265f, 251f / 265f },
				{ 236f / 265f, 231f / 265f, 242f / 265f }, { 208f / 265f, 209f / 265f, 230f / 265f },
				{ 166f / 265f, 189f / 265f, 219f / 265f }, { 116f / 265f, 169f / 265f, 207f / 265f },
				{ 54f / 265f, 144f / 265f, 192f / 265f }, { 5f / 265f, 112f / 265f, 176f / 265f },
				{ 4f / 265f, 90f / 265f, 141f / 265f }, { 2f / 265f, 56f / 265f, 88f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuBuGn" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuBuGn3 {
		private final float[][] cols = { { 236f / 265f, 226f / 265f, 240f / 265f },
				{ 166f / 265f, 189f / 265f, 219f / 265f }, { 28f / 265f, 144f / 265f, 153f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuBuGn" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuBuGn4 {
		private final float[][] cols = { { 246f / 265f, 239f / 265f, 247f / 265f },
				{ 189f / 265f, 201f / 265f, 225f / 265f }, { 103f / 265f, 169f / 265f, 207f / 265f },
				{ 2f / 265f, 129f / 265f, 138f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuBuGn" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuBuGn5 {
		private final float[][] cols = { { 246f / 265f, 239f / 265f, 247f / 265f },
				{ 189f / 265f, 201f / 265f, 225f / 265f }, { 103f / 265f, 169f / 265f, 207f / 265f },
				{ 28f / 265f, 144f / 265f, 153f / 265f }, { 1f / 265f, 108f / 265f, 89f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuBuGn" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuBuGn6 {
		private final float[][] cols = { { 246f / 265f, 239f / 265f, 247f / 265f },
				{ 208f / 265f, 209f / 265f, 230f / 265f }, { 166f / 265f, 189f / 265f, 219f / 265f },
				{ 103f / 265f, 169f / 265f, 207f / 265f }, { 28f / 265f, 144f / 265f, 153f / 265f },
				{ 1f / 265f, 108f / 265f, 89f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuBuGn" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuBuGn7 {
		private final float[][] cols = { { 246f / 265f, 239f / 265f, 247f / 265f },
				{ 208f / 265f, 209f / 265f, 230f / 265f }, { 166f / 265f, 189f / 265f, 219f / 265f },
				{ 103f / 265f, 169f / 265f, 207f / 265f }, { 54f / 265f, 144f / 265f, 192f / 265f },
				{ 2f / 265f, 129f / 265f, 138f / 265f }, { 1f / 265f, 100f / 265f, 80f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuBuGn" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuBuGn8 {
		private final float[][] cols = { { 255f / 265f, 247f / 265f, 251f / 265f },
				{ 236f / 265f, 226f / 265f, 240f / 265f }, { 208f / 265f, 209f / 265f, 230f / 265f },
				{ 166f / 265f, 189f / 265f, 219f / 265f }, { 103f / 265f, 169f / 265f, 207f / 265f },
				{ 54f / 265f, 144f / 265f, 192f / 265f }, { 2f / 265f, 129f / 265f, 138f / 265f },
				{ 1f / 265f, 100f / 265f, 80f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuBuGn" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuBuGn9 {
		private final float[][] cols = { { 255f / 265f, 247f / 265f, 251f / 265f },
				{ 236f / 265f, 226f / 265f, 240f / 265f }, { 208f / 265f, 209f / 265f, 230f / 265f },
				{ 166f / 265f, 189f / 265f, 219f / 265f }, { 103f / 265f, 169f / 265f, 207f / 265f },
				{ 54f / 265f, 144f / 265f, 192f / 265f }, { 2f / 265f, 129f / 265f, 138f / 265f },
				{ 1f / 265f, 108f / 265f, 89f / 265f }, { 1f / 265f, 70f / 265f, 54f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuOr" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuOr3 {
		private final float[][] cols = { { 241f / 265f, 163f / 265f, 64f / 265f },
				{ 247f / 265f, 247f / 265f, 247f / 265f }, { 153f / 265f, 142f / 265f, 195f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuOr" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuOr4 {
		private final float[][] cols = { { 230f / 265f, 97f / 265f, 1f / 265f },
				{ 253f / 265f, 184f / 265f, 99f / 265f }, { 178f / 265f, 171f / 265f, 210f / 265f },
				{ 94f / 265f, 60f / 265f, 153f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuOr" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuOr5 {
		private final float[][] cols = { { 230f / 265f, 97f / 265f, 1f / 265f },
				{ 253f / 265f, 184f / 265f, 99f / 265f }, { 247f / 265f, 247f / 265f, 247f / 265f },
				{ 178f / 265f, 171f / 265f, 210f / 265f }, { 94f / 265f, 60f / 265f, 153f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuOr" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuOr6 {
		private final float[][] cols = { { 179f / 265f, 88f / 265f, 6f / 265f },
				{ 241f / 265f, 163f / 265f, 64f / 265f }, { 254f / 265f, 224f / 265f, 182f / 265f },
				{ 216f / 265f, 218f / 265f, 235f / 265f }, { 153f / 265f, 142f / 265f, 195f / 265f },
				{ 84f / 265f, 39f / 265f, 136f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuOr" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuOr7 {
		private final float[][] cols = { { 179f / 265f, 88f / 265f, 6f / 265f },
				{ 241f / 265f, 163f / 265f, 64f / 265f }, { 254f / 265f, 224f / 265f, 182f / 265f },
				{ 247f / 265f, 247f / 265f, 247f / 265f }, { 216f / 265f, 218f / 265f, 235f / 265f },
				{ 153f / 265f, 142f / 265f, 195f / 265f }, { 84f / 265f, 39f / 265f, 136f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuOr" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuOr8 {
		private final float[][] cols = { { 179f / 265f, 88f / 265f, 6f / 265f },
				{ 224f / 265f, 130f / 265f, 20f / 265f }, { 253f / 265f, 184f / 265f, 99f / 265f },
				{ 254f / 265f, 224f / 265f, 182f / 265f }, { 216f / 265f, 218f / 265f, 235f / 265f },
				{ 178f / 265f, 171f / 265f, 210f / 265f }, { 128f / 265f, 115f / 265f, 172f / 265f },
				{ 84f / 265f, 39f / 265f, 136f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuOr" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuOr9 {
		private final float[][] cols = { { 179f / 265f, 88f / 265f, 6f / 265f },
				{ 224f / 265f, 130f / 265f, 20f / 265f }, { 253f / 265f, 184f / 265f, 99f / 265f },
				{ 254f / 265f, 224f / 265f, 182f / 265f }, { 247f / 265f, 247f / 265f, 247f / 265f },
				{ 216f / 265f, 218f / 265f, 235f / 265f }, { 178f / 265f, 171f / 265f, 210f / 265f },
				{ 128f / 265f, 115f / 265f, 172f / 265f }, { 84f / 265f, 39f / 265f, 136f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuOr" colour map with 10 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuOr10 {
		private final float[][] cols = { { 127f / 265f, 59f / 265f, 8f / 265f }, { 179f / 265f, 88f / 265f, 6f / 265f },
				{ 224f / 265f, 130f / 265f, 20f / 265f }, { 253f / 265f, 184f / 265f, 99f / 265f },
				{ 254f / 265f, 224f / 265f, 182f / 265f }, { 216f / 265f, 218f / 265f, 235f / 265f },
				{ 178f / 265f, 171f / 265f, 210f / 265f }, { 128f / 265f, 115f / 265f, 172f / 265f },
				{ 84f / 265f, 39f / 265f, 136f / 265f }, { 45f / 265f, 0f / 265f, 75f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuOr" colour map with 11 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuOr11 {
		private final float[][] cols = { { 127f / 265f, 59f / 265f, 8f / 265f }, { 179f / 265f, 88f / 265f, 6f / 265f },
				{ 224f / 265f, 130f / 265f, 20f / 265f }, { 253f / 265f, 184f / 265f, 99f / 265f },
				{ 254f / 265f, 224f / 265f, 182f / 265f }, { 247f / 265f, 247f / 265f, 247f / 265f },
				{ 216f / 265f, 218f / 265f, 235f / 265f }, { 178f / 265f, 171f / 265f, 210f / 265f },
				{ 128f / 265f, 115f / 265f, 172f / 265f }, { 84f / 265f, 39f / 265f, 136f / 265f },
				{ 45f / 265f, 0f / 265f, 75f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuRd" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuRd3 {
		private final float[][] cols = { { 231f / 265f, 225f / 265f, 239f / 265f },
				{ 201f / 265f, 148f / 265f, 199f / 265f }, { 221f / 265f, 28f / 265f, 119f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuRd" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuRd4 {
		private final float[][] cols = { { 241f / 265f, 238f / 265f, 246f / 265f },
				{ 215f / 265f, 181f / 265f, 216f / 265f }, { 223f / 265f, 101f / 265f, 176f / 265f },
				{ 206f / 265f, 18f / 265f, 86f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuRd" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuRd5 {
		private final float[][] cols = { { 241f / 265f, 238f / 265f, 246f / 265f },
				{ 215f / 265f, 181f / 265f, 216f / 265f }, { 223f / 265f, 101f / 265f, 176f / 265f },
				{ 221f / 265f, 28f / 265f, 119f / 265f }, { 152f / 265f, 0f / 265f, 67f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuRd" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuRd6 {
		private final float[][] cols = { { 241f / 265f, 238f / 265f, 246f / 265f },
				{ 212f / 265f, 185f / 265f, 218f / 265f }, { 201f / 265f, 148f / 265f, 199f / 265f },
				{ 223f / 265f, 101f / 265f, 176f / 265f }, { 221f / 265f, 28f / 265f, 119f / 265f },
				{ 152f / 265f, 0f / 265f, 67f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuRd" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuRd7 {
		private final float[][] cols = { { 241f / 265f, 238f / 265f, 246f / 265f },
				{ 212f / 265f, 185f / 265f, 218f / 265f }, { 201f / 265f, 148f / 265f, 199f / 265f },
				{ 223f / 265f, 101f / 265f, 176f / 265f }, { 231f / 265f, 41f / 265f, 138f / 265f },
				{ 206f / 265f, 18f / 265f, 86f / 265f }, { 145f / 265f, 0f / 265f, 63f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuRd" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuRd8 {
		private final float[][] cols = { { 247f / 265f, 244f / 265f, 249f / 265f },
				{ 231f / 265f, 225f / 265f, 239f / 265f }, { 212f / 265f, 185f / 265f, 218f / 265f },
				{ 201f / 265f, 148f / 265f, 199f / 265f }, { 223f / 265f, 101f / 265f, 176f / 265f },
				{ 231f / 265f, 41f / 265f, 138f / 265f }, { 206f / 265f, 18f / 265f, 86f / 265f },
				{ 145f / 265f, 0f / 265f, 63f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "PuRd" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	PuRd9 {
		private final float[][] cols = { { 247f / 265f, 244f / 265f, 249f / 265f },
				{ 231f / 265f, 225f / 265f, 239f / 265f }, { 212f / 265f, 185f / 265f, 218f / 265f },
				{ 201f / 265f, 148f / 265f, 199f / 265f }, { 223f / 265f, 101f / 265f, 176f / 265f },
				{ 231f / 265f, 41f / 265f, 138f / 265f }, { 206f / 265f, 18f / 265f, 86f / 265f },
				{ 152f / 265f, 0f / 265f, 67f / 265f }, { 103f / 265f, 0f / 265f, 31f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Purples" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Purples3 {
		private final float[][] cols = { { 239f / 265f, 237f / 265f, 245f / 265f },
				{ 188f / 265f, 189f / 265f, 220f / 265f }, { 117f / 265f, 107f / 265f, 177f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Purples" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Purples4 {
		private final float[][] cols = { { 242f / 265f, 240f / 265f, 247f / 265f },
				{ 203f / 265f, 201f / 265f, 226f / 265f }, { 158f / 265f, 154f / 265f, 200f / 265f },
				{ 106f / 265f, 81f / 265f, 163f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Purples" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Purples5 {
		private final float[][] cols = { { 242f / 265f, 240f / 265f, 247f / 265f },
				{ 203f / 265f, 201f / 265f, 226f / 265f }, { 158f / 265f, 154f / 265f, 200f / 265f },
				{ 117f / 265f, 107f / 265f, 177f / 265f }, { 84f / 265f, 39f / 265f, 143f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Purples" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Purples6 {
		private final float[][] cols = { { 242f / 265f, 240f / 265f, 247f / 265f },
				{ 218f / 265f, 218f / 265f, 235f / 265f }, { 188f / 265f, 189f / 265f, 220f / 265f },
				{ 158f / 265f, 154f / 265f, 200f / 265f }, { 117f / 265f, 107f / 265f, 177f / 265f },
				{ 84f / 265f, 39f / 265f, 143f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Purples" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Purples7 {
		private final float[][] cols = { { 242f / 265f, 240f / 265f, 247f / 265f },
				{ 218f / 265f, 218f / 265f, 235f / 265f }, { 188f / 265f, 189f / 265f, 220f / 265f },
				{ 158f / 265f, 154f / 265f, 200f / 265f }, { 128f / 265f, 125f / 265f, 186f / 265f },
				{ 106f / 265f, 81f / 265f, 163f / 265f }, { 74f / 265f, 20f / 265f, 134f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Purples" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Purples8 {
		private final float[][] cols = { { 252f / 265f, 251f / 265f, 253f / 265f },
				{ 239f / 265f, 237f / 265f, 245f / 265f }, { 218f / 265f, 218f / 265f, 235f / 265f },
				{ 188f / 265f, 189f / 265f, 220f / 265f }, { 158f / 265f, 154f / 265f, 200f / 265f },
				{ 128f / 265f, 125f / 265f, 186f / 265f }, { 106f / 265f, 81f / 265f, 163f / 265f },
				{ 74f / 265f, 20f / 265f, 134f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Purples" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Purples9 {
		private final float[][] cols = { { 252f / 265f, 251f / 265f, 253f / 265f },
				{ 239f / 265f, 237f / 265f, 245f / 265f }, { 218f / 265f, 218f / 265f, 235f / 265f },
				{ 188f / 265f, 189f / 265f, 220f / 265f }, { 158f / 265f, 154f / 265f, 200f / 265f },
				{ 128f / 265f, 125f / 265f, 186f / 265f }, { 106f / 265f, 81f / 265f, 163f / 265f },
				{ 84f / 265f, 39f / 265f, 143f / 265f }, { 63f / 265f, 0f / 265f, 125f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdBu" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdBu3 {
		private final float[][] cols = { { 239f / 265f, 138f / 265f, 98f / 265f },
				{ 247f / 265f, 247f / 265f, 247f / 265f }, { 103f / 265f, 169f / 265f, 207f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdBu" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdBu4 {
		private final float[][] cols = { { 202f / 265f, 0f / 265f, 32f / 265f },
				{ 244f / 265f, 165f / 265f, 130f / 265f }, { 146f / 265f, 197f / 265f, 222f / 265f },
				{ 5f / 265f, 113f / 265f, 176f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdBu" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdBu5 {
		private final float[][] cols = { { 202f / 265f, 0f / 265f, 32f / 265f },
				{ 244f / 265f, 165f / 265f, 130f / 265f }, { 247f / 265f, 247f / 265f, 247f / 265f },
				{ 146f / 265f, 197f / 265f, 222f / 265f }, { 5f / 265f, 113f / 265f, 176f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdBu" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdBu6 {
		private final float[][] cols = { { 178f / 265f, 24f / 265f, 43f / 265f },
				{ 239f / 265f, 138f / 265f, 98f / 265f }, { 253f / 265f, 219f / 265f, 199f / 265f },
				{ 209f / 265f, 229f / 265f, 240f / 265f }, { 103f / 265f, 169f / 265f, 207f / 265f },
				{ 33f / 265f, 102f / 265f, 172f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdBu" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdBu7 {
		private final float[][] cols = { { 178f / 265f, 24f / 265f, 43f / 265f },
				{ 239f / 265f, 138f / 265f, 98f / 265f }, { 253f / 265f, 219f / 265f, 199f / 265f },
				{ 247f / 265f, 247f / 265f, 247f / 265f }, { 209f / 265f, 229f / 265f, 240f / 265f },
				{ 103f / 265f, 169f / 265f, 207f / 265f }, { 33f / 265f, 102f / 265f, 172f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdBu" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdBu8 {
		private final float[][] cols = { { 178f / 265f, 24f / 265f, 43f / 265f },
				{ 214f / 265f, 96f / 265f, 77f / 265f }, { 244f / 265f, 165f / 265f, 130f / 265f },
				{ 253f / 265f, 219f / 265f, 199f / 265f }, { 209f / 265f, 229f / 265f, 240f / 265f },
				{ 146f / 265f, 197f / 265f, 222f / 265f }, { 67f / 265f, 147f / 265f, 195f / 265f },
				{ 33f / 265f, 102f / 265f, 172f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdBu" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdBu9 {
		private final float[][] cols = { { 178f / 265f, 24f / 265f, 43f / 265f },
				{ 214f / 265f, 96f / 265f, 77f / 265f }, { 244f / 265f, 165f / 265f, 130f / 265f },
				{ 253f / 265f, 219f / 265f, 199f / 265f }, { 247f / 265f, 247f / 265f, 247f / 265f },
				{ 209f / 265f, 229f / 265f, 240f / 265f }, { 146f / 265f, 197f / 265f, 222f / 265f },
				{ 67f / 265f, 147f / 265f, 195f / 265f }, { 33f / 265f, 102f / 265f, 172f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdBu" colour map with 10 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdBu10 {
		private final float[][] cols = { { 103f / 265f, 0f / 265f, 31f / 265f }, { 178f / 265f, 24f / 265f, 43f / 265f },
				{ 214f / 265f, 96f / 265f, 77f / 265f }, { 244f / 265f, 165f / 265f, 130f / 265f },
				{ 253f / 265f, 219f / 265f, 199f / 265f }, { 209f / 265f, 229f / 265f, 240f / 265f },
				{ 146f / 265f, 197f / 265f, 222f / 265f }, { 67f / 265f, 147f / 265f, 195f / 265f },
				{ 33f / 265f, 102f / 265f, 172f / 265f }, { 5f / 265f, 48f / 265f, 97f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdBu" colour map with 11 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdBu11 {
		private final float[][] cols = { { 103f / 265f, 0f / 265f, 31f / 265f }, { 178f / 265f, 24f / 265f, 43f / 265f },
				{ 214f / 265f, 96f / 265f, 77f / 265f }, { 244f / 265f, 165f / 265f, 130f / 265f },
				{ 253f / 265f, 219f / 265f, 199f / 265f }, { 247f / 265f, 247f / 265f, 247f / 265f },
				{ 209f / 265f, 229f / 265f, 240f / 265f }, { 146f / 265f, 197f / 265f, 222f / 265f },
				{ 67f / 265f, 147f / 265f, 195f / 265f }, { 33f / 265f, 102f / 265f, 172f / 265f },
				{ 5f / 265f, 48f / 265f, 97f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdGy" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdGy3 {
		private final float[][] cols = { { 239f / 265f, 138f / 265f, 98f / 265f },
				{ 255f / 265f, 255f / 265f, 255f / 265f }, { 153f / 265f, 153f / 265f, 153f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdGy" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdGy4 {
		private final float[][] cols = { { 202f / 265f, 0f / 265f, 32f / 265f },
				{ 244f / 265f, 165f / 265f, 130f / 265f }, { 186f / 265f, 186f / 265f, 186f / 265f },
				{ 64f / 265f, 64f / 265f, 64f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdGy" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdGy5 {
		private final float[][] cols = { { 202f / 265f, 0f / 265f, 32f / 265f },
				{ 244f / 265f, 165f / 265f, 130f / 265f }, { 255f / 265f, 255f / 265f, 255f / 265f },
				{ 186f / 265f, 186f / 265f, 186f / 265f }, { 64f / 265f, 64f / 265f, 64f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdGy" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdGy6 {
		private final float[][] cols = { { 178f / 265f, 24f / 265f, 43f / 265f },
				{ 239f / 265f, 138f / 265f, 98f / 265f }, { 253f / 265f, 219f / 265f, 199f / 265f },
				{ 224f / 265f, 224f / 265f, 224f / 265f }, { 153f / 265f, 153f / 265f, 153f / 265f },
				{ 77f / 265f, 77f / 265f, 77f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdGy" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdGy7 {
		private final float[][] cols = { { 178f / 265f, 24f / 265f, 43f / 265f },
				{ 239f / 265f, 138f / 265f, 98f / 265f }, { 253f / 265f, 219f / 265f, 199f / 265f },
				{ 255f / 265f, 255f / 265f, 255f / 265f }, { 224f / 265f, 224f / 265f, 224f / 265f },
				{ 153f / 265f, 153f / 265f, 153f / 265f }, { 77f / 265f, 77f / 265f, 77f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdGy" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdGy8 {
		private final float[][] cols = { { 178f / 265f, 24f / 265f, 43f / 265f },
				{ 214f / 265f, 96f / 265f, 77f / 265f }, { 244f / 265f, 165f / 265f, 130f / 265f },
				{ 253f / 265f, 219f / 265f, 199f / 265f }, { 224f / 265f, 224f / 265f, 224f / 265f },
				{ 186f / 265f, 186f / 265f, 186f / 265f }, { 135f / 265f, 135f / 265f, 135f / 265f },
				{ 77f / 265f, 77f / 265f, 77f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdGy" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdGy9 {
		private final float[][] cols = { { 178f / 265f, 24f / 265f, 43f / 265f },
				{ 214f / 265f, 96f / 265f, 77f / 265f }, { 244f / 265f, 165f / 265f, 130f / 265f },
				{ 253f / 265f, 219f / 265f, 199f / 265f }, { 255f / 265f, 255f / 265f, 255f / 265f },
				{ 224f / 265f, 224f / 265f, 224f / 265f }, { 186f / 265f, 186f / 265f, 186f / 265f },
				{ 135f / 265f, 135f / 265f, 135f / 265f }, { 77f / 265f, 77f / 265f, 77f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdGy" colour map with 10 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdGy10 {
		private final float[][] cols = { { 103f / 265f, 0f / 265f, 31f / 265f }, { 178f / 265f, 24f / 265f, 43f / 265f },
				{ 214f / 265f, 96f / 265f, 77f / 265f }, { 244f / 265f, 165f / 265f, 130f / 265f },
				{ 253f / 265f, 219f / 265f, 199f / 265f }, { 224f / 265f, 224f / 265f, 224f / 265f },
				{ 186f / 265f, 186f / 265f, 186f / 265f }, { 135f / 265f, 135f / 265f, 135f / 265f },
				{ 77f / 265f, 77f / 265f, 77f / 265f }, { 26f / 265f, 26f / 265f, 26f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdGy" colour map with 11 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdGy11 {
		private final float[][] cols = { { 103f / 265f, 0f / 265f, 31f / 265f }, { 178f / 265f, 24f / 265f, 43f / 265f },
				{ 214f / 265f, 96f / 265f, 77f / 265f }, { 244f / 265f, 165f / 265f, 130f / 265f },
				{ 253f / 265f, 219f / 265f, 199f / 265f }, { 255f / 265f, 255f / 265f, 255f / 265f },
				{ 224f / 265f, 224f / 265f, 224f / 265f }, { 186f / 265f, 186f / 265f, 186f / 265f },
				{ 135f / 265f, 135f / 265f, 135f / 265f }, { 77f / 265f, 77f / 265f, 77f / 265f },
				{ 26f / 265f, 26f / 265f, 26f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdPu" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdPu3 {
		private final float[][] cols = { { 253f / 265f, 224f / 265f, 221f / 265f },
				{ 250f / 265f, 159f / 265f, 181f / 265f }, { 197f / 265f, 27f / 265f, 138f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdPu" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdPu4 {
		private final float[][] cols = { { 254f / 265f, 235f / 265f, 226f / 265f },
				{ 251f / 265f, 180f / 265f, 185f / 265f }, { 247f / 265f, 104f / 265f, 161f / 265f },
				{ 174f / 265f, 1f / 265f, 126f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdPu" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdPu5 {
		private final float[][] cols = { { 254f / 265f, 235f / 265f, 226f / 265f },
				{ 251f / 265f, 180f / 265f, 185f / 265f }, { 247f / 265f, 104f / 265f, 161f / 265f },
				{ 197f / 265f, 27f / 265f, 138f / 265f }, { 122f / 265f, 1f / 265f, 119f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdPu" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdPu6 {
		private final float[][] cols = { { 254f / 265f, 235f / 265f, 226f / 265f },
				{ 252f / 265f, 197f / 265f, 192f / 265f }, { 250f / 265f, 159f / 265f, 181f / 265f },
				{ 247f / 265f, 104f / 265f, 161f / 265f }, { 197f / 265f, 27f / 265f, 138f / 265f },
				{ 122f / 265f, 1f / 265f, 119f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdPu" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdPu7 {
		private final float[][] cols = { { 254f / 265f, 235f / 265f, 226f / 265f },
				{ 252f / 265f, 197f / 265f, 192f / 265f }, { 250f / 265f, 159f / 265f, 181f / 265f },
				{ 247f / 265f, 104f / 265f, 161f / 265f }, { 221f / 265f, 52f / 265f, 151f / 265f },
				{ 174f / 265f, 1f / 265f, 126f / 265f }, { 122f / 265f, 1f / 265f, 119f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdPu" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdPu8 {
		private final float[][] cols = { { 255f / 265f, 247f / 265f, 243f / 265f },
				{ 253f / 265f, 224f / 265f, 221f / 265f }, { 252f / 265f, 197f / 265f, 192f / 265f },
				{ 250f / 265f, 159f / 265f, 181f / 265f }, { 247f / 265f, 104f / 265f, 161f / 265f },
				{ 221f / 265f, 52f / 265f, 151f / 265f }, { 174f / 265f, 1f / 265f, 126f / 265f },
				{ 122f / 265f, 1f / 265f, 119f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdPu" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdPu9 {
		private final float[][] cols = { { 255f / 265f, 247f / 265f, 243f / 265f },
				{ 253f / 265f, 224f / 265f, 221f / 265f }, { 252f / 265f, 197f / 265f, 192f / 265f },
				{ 250f / 265f, 159f / 265f, 181f / 265f }, { 247f / 265f, 104f / 265f, 161f / 265f },
				{ 221f / 265f, 52f / 265f, 151f / 265f }, { 174f / 265f, 1f / 265f, 126f / 265f },
				{ 122f / 265f, 1f / 265f, 119f / 265f }, { 73f / 265f, 0f / 265f, 106f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Reds" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Reds3 {
		private final float[][] cols = { { 254f / 265f, 224f / 265f, 210f / 265f },
				{ 252f / 265f, 146f / 265f, 114f / 265f }, { 222f / 265f, 45f / 265f, 38f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Reds" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Reds4 {
		private final float[][] cols = { { 254f / 265f, 229f / 265f, 217f / 265f },
				{ 252f / 265f, 174f / 265f, 145f / 265f }, { 251f / 265f, 106f / 265f, 74f / 265f },
				{ 203f / 265f, 24f / 265f, 29f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Reds" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Reds5 {
		private final float[][] cols = { { 254f / 265f, 229f / 265f, 217f / 265f },
				{ 252f / 265f, 174f / 265f, 145f / 265f }, { 251f / 265f, 106f / 265f, 74f / 265f },
				{ 222f / 265f, 45f / 265f, 38f / 265f }, { 165f / 265f, 15f / 265f, 21f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Reds" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Reds6 {
		private final float[][] cols = { { 254f / 265f, 229f / 265f, 217f / 265f },
				{ 252f / 265f, 187f / 265f, 161f / 265f }, { 252f / 265f, 146f / 265f, 114f / 265f },
				{ 251f / 265f, 106f / 265f, 74f / 265f }, { 222f / 265f, 45f / 265f, 38f / 265f },
				{ 165f / 265f, 15f / 265f, 21f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Reds" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Reds7 {
		private final float[][] cols = { { 254f / 265f, 229f / 265f, 217f / 265f },
				{ 252f / 265f, 187f / 265f, 161f / 265f }, { 252f / 265f, 146f / 265f, 114f / 265f },
				{ 251f / 265f, 106f / 265f, 74f / 265f }, { 239f / 265f, 59f / 265f, 44f / 265f },
				{ 203f / 265f, 24f / 265f, 29f / 265f }, { 153f / 265f, 0f / 265f, 13f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Reds" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Reds8 {
		private final float[][] cols = { { 255f / 265f, 245f / 265f, 240f / 265f },
				{ 254f / 265f, 224f / 265f, 210f / 265f }, { 252f / 265f, 187f / 265f, 161f / 265f },
				{ 252f / 265f, 146f / 265f, 114f / 265f }, { 251f / 265f, 106f / 265f, 74f / 265f },
				{ 239f / 265f, 59f / 265f, 44f / 265f }, { 203f / 265f, 24f / 265f, 29f / 265f },
				{ 153f / 265f, 0f / 265f, 13f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Reds" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Reds9 {
		private final float[][] cols = { { 255f / 265f, 245f / 265f, 240f / 265f },
				{ 254f / 265f, 224f / 265f, 210f / 265f }, { 252f / 265f, 187f / 265f, 161f / 265f },
				{ 252f / 265f, 146f / 265f, 114f / 265f }, { 251f / 265f, 106f / 265f, 74f / 265f },
				{ 239f / 265f, 59f / 265f, 44f / 265f }, { 203f / 265f, 24f / 265f, 29f / 265f },
				{ 165f / 265f, 15f / 265f, 21f / 265f }, { 103f / 265f, 0f / 265f, 13f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdYlBu" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdYlBu3 {
		private final float[][] cols = { { 252f / 265f, 141f / 265f, 89f / 265f },
				{ 255f / 265f, 255f / 265f, 191f / 265f }, { 145f / 265f, 191f / 265f, 219f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdYlBu" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdYlBu4 {
		private final float[][] cols = { { 215f / 265f, 25f / 265f, 28f / 265f },
				{ 253f / 265f, 174f / 265f, 97f / 265f }, { 171f / 265f, 217f / 265f, 233f / 265f },
				{ 44f / 265f, 123f / 265f, 182f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdYlBu" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdYlBu5 {
		private final float[][] cols = { { 215f / 265f, 25f / 265f, 28f / 265f },
				{ 253f / 265f, 174f / 265f, 97f / 265f }, { 255f / 265f, 255f / 265f, 191f / 265f },
				{ 171f / 265f, 217f / 265f, 233f / 265f }, { 44f / 265f, 123f / 265f, 182f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdYlBu" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdYlBu6 {
		private final float[][] cols = { { 215f / 265f, 48f / 265f, 39f / 265f },
				{ 252f / 265f, 141f / 265f, 89f / 265f }, { 254f / 265f, 224f / 265f, 144f / 265f },
				{ 224f / 265f, 243f / 265f, 248f / 265f }, { 145f / 265f, 191f / 265f, 219f / 265f },
				{ 69f / 265f, 117f / 265f, 180f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdYlBu" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdYlBu7 {
		private final float[][] cols = { { 215f / 265f, 48f / 265f, 39f / 265f },
				{ 252f / 265f, 141f / 265f, 89f / 265f }, { 254f / 265f, 224f / 265f, 144f / 265f },
				{ 255f / 265f, 255f / 265f, 191f / 265f }, { 224f / 265f, 243f / 265f, 248f / 265f },
				{ 145f / 265f, 191f / 265f, 219f / 265f }, { 69f / 265f, 117f / 265f, 180f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdYlBu" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdYlBu8 {
		private final float[][] cols = { { 215f / 265f, 48f / 265f, 39f / 265f },
				{ 244f / 265f, 109f / 265f, 67f / 265f }, { 253f / 265f, 174f / 265f, 97f / 265f },
				{ 254f / 265f, 224f / 265f, 144f / 265f }, { 224f / 265f, 243f / 265f, 248f / 265f },
				{ 171f / 265f, 217f / 265f, 233f / 265f }, { 116f / 265f, 173f / 265f, 209f / 265f },
				{ 69f / 265f, 117f / 265f, 180f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdYlBu" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdYlBu9 {
		private final float[][] cols = { { 215f / 265f, 48f / 265f, 39f / 265f },
				{ 244f / 265f, 109f / 265f, 67f / 265f }, { 253f / 265f, 174f / 265f, 97f / 265f },
				{ 254f / 265f, 224f / 265f, 144f / 265f }, { 255f / 265f, 255f / 265f, 191f / 265f },
				{ 224f / 265f, 243f / 265f, 248f / 265f }, { 171f / 265f, 217f / 265f, 233f / 265f },
				{ 116f / 265f, 173f / 265f, 209f / 265f }, { 69f / 265f, 117f / 265f, 180f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdYlBu" colour map with 10 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdYlBu10 {
		private final float[][] cols = { { 165f / 265f, 0f / 265f, 38f / 265f }, { 215f / 265f, 48f / 265f, 39f / 265f },
				{ 244f / 265f, 109f / 265f, 67f / 265f }, { 253f / 265f, 174f / 265f, 97f / 265f },
				{ 254f / 265f, 224f / 265f, 144f / 265f }, { 224f / 265f, 243f / 265f, 248f / 265f },
				{ 171f / 265f, 217f / 265f, 233f / 265f }, { 116f / 265f, 173f / 265f, 209f / 265f },
				{ 69f / 265f, 117f / 265f, 180f / 265f }, { 49f / 265f, 54f / 265f, 149f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdYlBu" colour map with 11 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdYlBu11 {
		private final float[][] cols = { { 165f / 265f, 0f / 265f, 38f / 265f }, { 215f / 265f, 48f / 265f, 39f / 265f },
				{ 244f / 265f, 109f / 265f, 67f / 265f }, { 253f / 265f, 174f / 265f, 97f / 265f },
				{ 254f / 265f, 224f / 265f, 144f / 265f }, { 255f / 265f, 255f / 265f, 191f / 265f },
				{ 224f / 265f, 243f / 265f, 248f / 265f }, { 171f / 265f, 217f / 265f, 233f / 265f },
				{ 116f / 265f, 173f / 265f, 209f / 265f }, { 69f / 265f, 117f / 265f, 180f / 265f },
				{ 49f / 265f, 54f / 265f, 149f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdYlGn" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdYlGn3 {
		private final float[][] cols = { { 252f / 265f, 141f / 265f, 89f / 265f },
				{ 255f / 265f, 255f / 265f, 191f / 265f }, { 145f / 265f, 207f / 265f, 96f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdYlGn" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdYlGn4 {
		private final float[][] cols = { { 215f / 265f, 25f / 265f, 28f / 265f },
				{ 253f / 265f, 174f / 265f, 97f / 265f }, { 166f / 265f, 217f / 265f, 106f / 265f },
				{ 26f / 265f, 150f / 265f, 65f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdYlGn" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdYlGn5 {
		private final float[][] cols = { { 215f / 265f, 25f / 265f, 28f / 265f },
				{ 253f / 265f, 174f / 265f, 97f / 265f }, { 255f / 265f, 255f / 265f, 191f / 265f },
				{ 166f / 265f, 217f / 265f, 106f / 265f }, { 26f / 265f, 150f / 265f, 65f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdYlGn" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdYlGn6 {
		private final float[][] cols = { { 215f / 265f, 48f / 265f, 39f / 265f },
				{ 252f / 265f, 141f / 265f, 89f / 265f }, { 254f / 265f, 224f / 265f, 139f / 265f },
				{ 217f / 265f, 239f / 265f, 139f / 265f }, { 145f / 265f, 207f / 265f, 96f / 265f },
				{ 26f / 265f, 152f / 265f, 80f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdYlGn" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdYlGn7 {
		private final float[][] cols = { { 215f / 265f, 48f / 265f, 39f / 265f },
				{ 252f / 265f, 141f / 265f, 89f / 265f }, { 254f / 265f, 224f / 265f, 139f / 265f },
				{ 255f / 265f, 255f / 265f, 191f / 265f }, { 217f / 265f, 239f / 265f, 139f / 265f },
				{ 145f / 265f, 207f / 265f, 96f / 265f }, { 26f / 265f, 152f / 265f, 80f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdYlGn" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdYlGn8 {
		private final float[][] cols = { { 215f / 265f, 48f / 265f, 39f / 265f },
				{ 244f / 265f, 109f / 265f, 67f / 265f }, { 253f / 265f, 174f / 265f, 97f / 265f },
				{ 254f / 265f, 224f / 265f, 139f / 265f }, { 217f / 265f, 239f / 265f, 139f / 265f },
				{ 166f / 265f, 217f / 265f, 106f / 265f }, { 102f / 265f, 189f / 265f, 99f / 265f },
				{ 26f / 265f, 152f / 265f, 80f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdYlGn" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdYlGn9 {
		private final float[][] cols = { { 215f / 265f, 48f / 265f, 39f / 265f },
				{ 244f / 265f, 109f / 265f, 67f / 265f }, { 253f / 265f, 174f / 265f, 97f / 265f },
				{ 254f / 265f, 224f / 265f, 139f / 265f }, { 255f / 265f, 255f / 265f, 191f / 265f },
				{ 217f / 265f, 239f / 265f, 139f / 265f }, { 166f / 265f, 217f / 265f, 106f / 265f },
				{ 102f / 265f, 189f / 265f, 99f / 265f }, { 26f / 265f, 152f / 265f, 80f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdYlGn" colour map with 10 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdYlGn10 {
		private final float[][] cols = { { 165f / 265f, 0f / 265f, 38f / 265f }, { 215f / 265f, 48f / 265f, 39f / 265f },
				{ 244f / 265f, 109f / 265f, 67f / 265f }, { 253f / 265f, 174f / 265f, 97f / 265f },
				{ 254f / 265f, 224f / 265f, 139f / 265f }, { 217f / 265f, 239f / 265f, 139f / 265f },
				{ 166f / 265f, 217f / 265f, 106f / 265f }, { 102f / 265f, 189f / 265f, 99f / 265f },
				{ 26f / 265f, 152f / 265f, 80f / 265f }, { 0f / 265f, 104f / 265f, 55f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "RdYlGn" colour map with 11 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	RdYlGn11 {
		private final float[][] cols = { { 165f / 265f, 0f / 265f, 38f / 265f }, { 215f / 265f, 48f / 265f, 39f / 265f },
				{ 244f / 265f, 109f / 265f, 67f / 265f }, { 253f / 265f, 174f / 265f, 97f / 265f },
				{ 254f / 265f, 224f / 265f, 139f / 265f }, { 255f / 265f, 255f / 265f, 191f / 265f },
				{ 217f / 265f, 239f / 265f, 139f / 265f }, { 166f / 265f, 217f / 265f, 106f / 265f },
				{ 102f / 265f, 189f / 265f, 99f / 265f }, { 26f / 265f, 152f / 265f, 80f / 265f },
				{ 0f / 265f, 104f / 265f, 55f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set1" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set13 {
		private final float[][] cols = { { 228f / 265f, 26f / 265f, 28f / 265f },
				{ 55f / 265f, 126f / 265f, 184f / 265f }, { 77f / 265f, 175f / 265f, 74f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set1" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set14 {
		private final float[][] cols = { { 228f / 265f, 26f / 265f, 28f / 265f },
				{ 55f / 265f, 126f / 265f, 184f / 265f }, { 77f / 265f, 175f / 265f, 74f / 265f },
				{ 152f / 265f, 78f / 265f, 163f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set1" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set15 {
		private final float[][] cols = { { 228f / 265f, 26f / 265f, 28f / 265f },
				{ 55f / 265f, 126f / 265f, 184f / 265f }, { 77f / 265f, 175f / 265f, 74f / 265f },
				{ 152f / 265f, 78f / 265f, 163f / 265f }, { 255f / 265f, 127f / 265f, 0f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set1" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set16 {
		private final float[][] cols = { { 228f / 265f, 26f / 265f, 28f / 265f },
				{ 55f / 265f, 126f / 265f, 184f / 265f }, { 77f / 265f, 175f / 265f, 74f / 265f },
				{ 152f / 265f, 78f / 265f, 163f / 265f }, { 255f / 265f, 127f / 265f, 0f / 265f },
				{ 255f / 265f, 255f / 265f, 51f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set1" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set17 {
		private final float[][] cols = { { 228f / 265f, 26f / 265f, 28f / 265f },
				{ 55f / 265f, 126f / 265f, 184f / 265f }, { 77f / 265f, 175f / 265f, 74f / 265f },
				{ 152f / 265f, 78f / 265f, 163f / 265f }, { 255f / 265f, 127f / 265f, 0f / 265f },
				{ 255f / 265f, 255f / 265f, 51f / 265f }, { 166f / 265f, 86f / 265f, 40f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set1" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set18 {
		private final float[][] cols = { { 228f / 265f, 26f / 265f, 28f / 265f },
				{ 55f / 265f, 126f / 265f, 184f / 265f }, { 77f / 265f, 175f / 265f, 74f / 265f },
				{ 152f / 265f, 78f / 265f, 163f / 265f }, { 255f / 265f, 127f / 265f, 0f / 265f },
				{ 255f / 265f, 255f / 265f, 51f / 265f }, { 166f / 265f, 86f / 265f, 40f / 265f },
				{ 247f / 265f, 129f / 265f, 191f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set1" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set19 {
		private final float[][] cols = { { 228f / 265f, 26f / 265f, 28f / 265f },
				{ 55f / 265f, 126f / 265f, 184f / 265f }, { 77f / 265f, 175f / 265f, 74f / 265f },
				{ 152f / 265f, 78f / 265f, 163f / 265f }, { 255f / 265f, 127f / 265f, 0f / 265f },
				{ 255f / 265f, 255f / 265f, 51f / 265f }, { 166f / 265f, 86f / 265f, 40f / 265f },
				{ 247f / 265f, 129f / 265f, 191f / 265f }, { 153f / 265f, 153f / 265f, 153f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set2" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set23 {
		private final float[][] cols = { { 102f / 265f, 194f / 265f, 165f / 265f },
				{ 252f / 265f, 141f / 265f, 98f / 265f }, { 141f / 265f, 160f / 265f, 203f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set2" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set24 {
		private final float[][] cols = { { 102f / 265f, 194f / 265f, 165f / 265f },
				{ 252f / 265f, 141f / 265f, 98f / 265f }, { 141f / 265f, 160f / 265f, 203f / 265f },
				{ 231f / 265f, 138f / 265f, 195f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set2" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set25 {
		private final float[][] cols = { { 102f / 265f, 194f / 265f, 165f / 265f },
				{ 252f / 265f, 141f / 265f, 98f / 265f }, { 141f / 265f, 160f / 265f, 203f / 265f },
				{ 231f / 265f, 138f / 265f, 195f / 265f }, { 166f / 265f, 216f / 265f, 84f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set2" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set26 {
		private final float[][] cols = { { 102f / 265f, 194f / 265f, 165f / 265f },
				{ 252f / 265f, 141f / 265f, 98f / 265f }, { 141f / 265f, 160f / 265f, 203f / 265f },
				{ 231f / 265f, 138f / 265f, 195f / 265f }, { 166f / 265f, 216f / 265f, 84f / 265f },
				{ 255f / 265f, 217f / 265f, 47f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set2" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set27 {
		private final float[][] cols = { { 102f / 265f, 194f / 265f, 165f / 265f },
				{ 252f / 265f, 141f / 265f, 98f / 265f }, { 141f / 265f, 160f / 265f, 203f / 265f },
				{ 231f / 265f, 138f / 265f, 195f / 265f }, { 166f / 265f, 216f / 265f, 84f / 265f },
				{ 255f / 265f, 217f / 265f, 47f / 265f }, { 229f / 265f, 196f / 265f, 148f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set2" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set28 {
		private final float[][] cols = { { 102f / 265f, 194f / 265f, 165f / 265f },
				{ 252f / 265f, 141f / 265f, 98f / 265f }, { 141f / 265f, 160f / 265f, 203f / 265f },
				{ 231f / 265f, 138f / 265f, 195f / 265f }, { 166f / 265f, 216f / 265f, 84f / 265f },
				{ 255f / 265f, 217f / 265f, 47f / 265f }, { 229f / 265f, 196f / 265f, 148f / 265f },
				{ 179f / 265f, 179f / 265f, 179f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set3" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set33 {
		private final float[][] cols = { { 141f / 265f, 211f / 265f, 199f / 265f },
				{ 255f / 265f, 255f / 265f, 179f / 265f }, { 190f / 265f, 186f / 265f, 218f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set3" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set34 {
		private final float[][] cols = { { 141f / 265f, 211f / 265f, 199f / 265f },
				{ 255f / 265f, 255f / 265f, 179f / 265f }, { 190f / 265f, 186f / 265f, 218f / 265f },
				{ 251f / 265f, 128f / 265f, 114f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set3" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set35 {
		private final float[][] cols = { { 141f / 265f, 211f / 265f, 199f / 265f },
				{ 255f / 265f, 255f / 265f, 179f / 265f }, { 190f / 265f, 186f / 265f, 218f / 265f },
				{ 251f / 265f, 128f / 265f, 114f / 265f }, { 128f / 265f, 177f / 265f, 211f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set3" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set36 {
		private final float[][] cols = { { 141f / 265f, 211f / 265f, 199f / 265f },
				{ 255f / 265f, 255f / 265f, 179f / 265f }, { 190f / 265f, 186f / 265f, 218f / 265f },
				{ 251f / 265f, 128f / 265f, 114f / 265f }, { 128f / 265f, 177f / 265f, 211f / 265f },
				{ 253f / 265f, 180f / 265f, 98f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set3" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set37 {
		private final float[][] cols = { { 141f / 265f, 211f / 265f, 199f / 265f },
				{ 255f / 265f, 255f / 265f, 179f / 265f }, { 190f / 265f, 186f / 265f, 218f / 265f },
				{ 251f / 265f, 128f / 265f, 114f / 265f }, { 128f / 265f, 177f / 265f, 211f / 265f },
				{ 253f / 265f, 180f / 265f, 98f / 265f }, { 179f / 265f, 222f / 265f, 105f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set3" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set38 {
		private final float[][] cols = { { 141f / 265f, 211f / 265f, 199f / 265f },
				{ 255f / 265f, 255f / 265f, 179f / 265f }, { 190f / 265f, 186f / 265f, 218f / 265f },
				{ 251f / 265f, 128f / 265f, 114f / 265f }, { 128f / 265f, 177f / 265f, 211f / 265f },
				{ 253f / 265f, 180f / 265f, 98f / 265f }, { 179f / 265f, 222f / 265f, 105f / 265f },
				{ 252f / 265f, 205f / 265f, 229f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set3" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set39 {
		private final float[][] cols = { { 141f / 265f, 211f / 265f, 199f / 265f },
				{ 255f / 265f, 255f / 265f, 179f / 265f }, { 190f / 265f, 186f / 265f, 218f / 265f },
				{ 251f / 265f, 128f / 265f, 114f / 265f }, { 128f / 265f, 177f / 265f, 211f / 265f },
				{ 253f / 265f, 180f / 265f, 98f / 265f }, { 179f / 265f, 222f / 265f, 105f / 265f },
				{ 252f / 265f, 205f / 265f, 229f / 265f }, { 217f / 265f, 217f / 265f, 217f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set3" colour map with 10 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set310 {
		private final float[][] cols = { { 141f / 265f, 211f / 265f, 199f / 265f },
				{ 255f / 265f, 255f / 265f, 179f / 265f }, { 190f / 265f, 186f / 265f, 218f / 265f },
				{ 251f / 265f, 128f / 265f, 114f / 265f }, { 128f / 265f, 177f / 265f, 211f / 265f },
				{ 253f / 265f, 180f / 265f, 98f / 265f }, { 179f / 265f, 222f / 265f, 105f / 265f },
				{ 252f / 265f, 205f / 265f, 229f / 265f }, { 217f / 265f, 217f / 265f, 217f / 265f },
				{ 188f / 265f, 128f / 265f, 189f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set3" colour map with 11 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set311 {
		private final float[][] cols = { { 141f / 265f, 211f / 265f, 199f / 265f },
				{ 255f / 265f, 255f / 265f, 179f / 265f }, { 190f / 265f, 186f / 265f, 218f / 265f },
				{ 251f / 265f, 128f / 265f, 114f / 265f }, { 128f / 265f, 177f / 265f, 211f / 265f },
				{ 253f / 265f, 180f / 265f, 98f / 265f }, { 179f / 265f, 222f / 265f, 105f / 265f },
				{ 252f / 265f, 205f / 265f, 229f / 265f }, { 217f / 265f, 217f / 265f, 217f / 265f },
				{ 188f / 265f, 128f / 265f, 189f / 265f }, { 204f / 265f, 235f / 265f, 197f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Set3" colour map with 12 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Set312 {
		private final float[][] cols = { { 141f / 265f, 211f / 265f, 199f / 265f },
				{ 255f / 265f, 255f / 265f, 179f / 265f }, { 190f / 265f, 186f / 265f, 218f / 265f },
				{ 251f / 265f, 128f / 265f, 114f / 265f }, { 128f / 265f, 177f / 265f, 211f / 265f },
				{ 253f / 265f, 180f / 265f, 98f / 265f }, { 179f / 265f, 222f / 265f, 105f / 265f },
				{ 252f / 265f, 205f / 265f, 229f / 265f }, { 217f / 265f, 217f / 265f, 217f / 265f },
				{ 188f / 265f, 128f / 265f, 189f / 265f }, { 204f / 265f, 235f / 265f, 197f / 265f },
				{ 255f / 265f, 237f / 265f, 111f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.QUALITATIVE;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Spectral" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Spectral3 {
		private final float[][] cols = { { 252f / 265f, 141f / 265f, 89f / 265f },
				{ 255f / 265f, 255f / 265f, 191f / 265f }, { 153f / 265f, 213f / 265f, 148f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Spectral" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Spectral4 {
		private final float[][] cols = { { 215f / 265f, 25f / 265f, 28f / 265f },
				{ 253f / 265f, 174f / 265f, 97f / 265f }, { 171f / 265f, 221f / 265f, 164f / 265f },
				{ 43f / 265f, 131f / 265f, 186f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Spectral" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Spectral5 {
		private final float[][] cols = { { 215f / 265f, 25f / 265f, 28f / 265f },
				{ 253f / 265f, 174f / 265f, 97f / 265f }, { 255f / 265f, 255f / 265f, 191f / 265f },
				{ 171f / 265f, 221f / 265f, 164f / 265f }, { 43f / 265f, 131f / 265f, 186f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Spectral" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Spectral6 {
		private final float[][] cols = { { 213f / 265f, 62f / 265f, 79f / 265f },
				{ 252f / 265f, 141f / 265f, 89f / 265f }, { 254f / 265f, 224f / 265f, 139f / 265f },
				{ 230f / 265f, 245f / 265f, 152f / 265f }, { 153f / 265f, 213f / 265f, 148f / 265f },
				{ 50f / 265f, 136f / 265f, 189f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Spectral" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Spectral7 {
		private final float[][] cols = { { 213f / 265f, 62f / 265f, 79f / 265f },
				{ 252f / 265f, 141f / 265f, 89f / 265f }, { 254f / 265f, 224f / 265f, 139f / 265f },
				{ 255f / 265f, 255f / 265f, 191f / 265f }, { 230f / 265f, 245f / 265f, 152f / 265f },
				{ 153f / 265f, 213f / 265f, 148f / 265f }, { 50f / 265f, 136f / 265f, 189f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Spectral" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Spectral8 {
		private final float[][] cols = { { 213f / 265f, 62f / 265f, 79f / 265f },
				{ 244f / 265f, 109f / 265f, 67f / 265f }, { 253f / 265f, 174f / 265f, 97f / 265f },
				{ 254f / 265f, 224f / 265f, 139f / 265f }, { 230f / 265f, 245f / 265f, 152f / 265f },
				{ 171f / 265f, 221f / 265f, 164f / 265f }, { 102f / 265f, 194f / 265f, 165f / 265f },
				{ 50f / 265f, 136f / 265f, 189f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Spectral" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Spectral9 {
		private final float[][] cols = { { 213f / 265f, 62f / 265f, 79f / 265f },
				{ 244f / 265f, 109f / 265f, 67f / 265f }, { 253f / 265f, 174f / 265f, 97f / 265f },
				{ 254f / 265f, 224f / 265f, 139f / 265f }, { 255f / 265f, 255f / 265f, 191f / 265f },
				{ 230f / 265f, 245f / 265f, 152f / 265f }, { 171f / 265f, 221f / 265f, 164f / 265f },
				{ 102f / 265f, 194f / 265f, 165f / 265f }, { 50f / 265f, 136f / 265f, 189f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Spectral" colour map with 10 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Spectral10 {
		private final float[][] cols = { { 158f / 265f, 1f / 265f, 66f / 265f }, { 213f / 265f, 62f / 265f, 79f / 265f },
				{ 244f / 265f, 109f / 265f, 67f / 265f }, { 253f / 265f, 174f / 265f, 97f / 265f },
				{ 254f / 265f, 224f / 265f, 139f / 265f }, { 230f / 265f, 245f / 265f, 152f / 265f },
				{ 171f / 265f, 221f / 265f, 164f / 265f }, { 102f / 265f, 194f / 265f, 165f / 265f },
				{ 50f / 265f, 136f / 265f, 189f / 265f }, { 94f / 265f, 79f / 265f, 162f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "Spectral" colour map with 11 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	Spectral11 {
		private final float[][] cols = { { 158f / 265f, 1f / 265f, 66f / 265f }, { 213f / 265f, 62f / 265f, 79f / 265f },
				{ 244f / 265f, 109f / 265f, 67f / 265f }, { 253f / 265f, 174f / 265f, 97f / 265f },
				{ 254f / 265f, 224f / 265f, 139f / 265f }, { 255f / 265f, 255f / 265f, 191f / 265f },
				{ 230f / 265f, 245f / 265f, 152f / 265f }, { 171f / 265f, 221f / 265f, 164f / 265f },
				{ 102f / 265f, 194f / 265f, 165f / 265f }, { 50f / 265f, 136f / 265f, 189f / 265f },
				{ 94f / 265f, 79f / 265f, 162f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.DIVERGING;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlGn" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlGn3 {
		private final float[][] cols = { { 247f / 265f, 252f / 265f, 185f / 265f },
				{ 173f / 265f, 221f / 265f, 142f / 265f }, { 49f / 265f, 163f / 265f, 84f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlGn" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlGn4 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 204f / 265f },
				{ 194f / 265f, 230f / 265f, 153f / 265f }, { 120f / 265f, 198f / 265f, 121f / 265f },
				{ 35f / 265f, 132f / 265f, 67f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlGn" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlGn5 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 204f / 265f },
				{ 194f / 265f, 230f / 265f, 153f / 265f }, { 120f / 265f, 198f / 265f, 121f / 265f },
				{ 49f / 265f, 163f / 265f, 84f / 265f }, { 0f / 265f, 104f / 265f, 55f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlGn" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlGn6 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 204f / 265f },
				{ 217f / 265f, 240f / 265f, 163f / 265f }, { 173f / 265f, 221f / 265f, 142f / 265f },
				{ 120f / 265f, 198f / 265f, 121f / 265f }, { 49f / 265f, 163f / 265f, 84f / 265f },
				{ 0f / 265f, 104f / 265f, 55f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlGn" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlGn7 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 204f / 265f },
				{ 217f / 265f, 240f / 265f, 163f / 265f }, { 173f / 265f, 221f / 265f, 142f / 265f },
				{ 120f / 265f, 198f / 265f, 121f / 265f }, { 65f / 265f, 171f / 265f, 93f / 265f },
				{ 35f / 265f, 132f / 265f, 67f / 265f }, { 0f / 265f, 90f / 265f, 50f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlGn" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlGn8 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 229f / 265f },
				{ 247f / 265f, 252f / 265f, 185f / 265f }, { 217f / 265f, 240f / 265f, 163f / 265f },
				{ 173f / 265f, 221f / 265f, 142f / 265f }, { 120f / 265f, 198f / 265f, 121f / 265f },
				{ 65f / 265f, 171f / 265f, 93f / 265f }, { 35f / 265f, 132f / 265f, 67f / 265f },
				{ 0f / 265f, 90f / 265f, 50f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlGn" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlGn9 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 229f / 265f },
				{ 247f / 265f, 252f / 265f, 185f / 265f }, { 217f / 265f, 240f / 265f, 163f / 265f },
				{ 173f / 265f, 221f / 265f, 142f / 265f }, { 120f / 265f, 198f / 265f, 121f / 265f },
				{ 65f / 265f, 171f / 265f, 93f / 265f }, { 35f / 265f, 132f / 265f, 67f / 265f },
				{ 0f / 265f, 104f / 265f, 55f / 265f }, { 0f / 265f, 69f / 265f, 41f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlGnBu" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlGnBu3 {
		private final float[][] cols = { { 237f / 265f, 248f / 265f, 177f / 265f },
				{ 127f / 265f, 205f / 265f, 187f / 265f }, { 44f / 265f, 127f / 265f, 184f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlGnBu" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlGnBu4 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 204f / 265f },
				{ 161f / 265f, 218f / 265f, 180f / 265f }, { 65f / 265f, 182f / 265f, 196f / 265f },
				{ 34f / 265f, 94f / 265f, 168f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlGnBu" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlGnBu5 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 204f / 265f },
				{ 161f / 265f, 218f / 265f, 180f / 265f }, { 65f / 265f, 182f / 265f, 196f / 265f },
				{ 44f / 265f, 127f / 265f, 184f / 265f }, { 37f / 265f, 52f / 265f, 148f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlGnBu" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlGnBu6 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 204f / 265f },
				{ 199f / 265f, 233f / 265f, 180f / 265f }, { 127f / 265f, 205f / 265f, 187f / 265f },
				{ 65f / 265f, 182f / 265f, 196f / 265f }, { 44f / 265f, 127f / 265f, 184f / 265f },
				{ 37f / 265f, 52f / 265f, 148f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlGnBu" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlGnBu7 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 204f / 265f },
				{ 199f / 265f, 233f / 265f, 180f / 265f }, { 127f / 265f, 205f / 265f, 187f / 265f },
				{ 65f / 265f, 182f / 265f, 196f / 265f }, { 29f / 265f, 145f / 265f, 192f / 265f },
				{ 34f / 265f, 94f / 265f, 168f / 265f }, { 12f / 265f, 44f / 265f, 132f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlGnBu" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlGnBu8 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 217f / 265f },
				{ 237f / 265f, 248f / 265f, 177f / 265f }, { 199f / 265f, 233f / 265f, 180f / 265f },
				{ 127f / 265f, 205f / 265f, 187f / 265f }, { 65f / 265f, 182f / 265f, 196f / 265f },
				{ 29f / 265f, 145f / 265f, 192f / 265f }, { 34f / 265f, 94f / 265f, 168f / 265f },
				{ 12f / 265f, 44f / 265f, 132f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlGnBu" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlGnBu9 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 217f / 265f },
				{ 237f / 265f, 248f / 265f, 177f / 265f }, { 199f / 265f, 233f / 265f, 180f / 265f },
				{ 127f / 265f, 205f / 265f, 187f / 265f }, { 65f / 265f, 182f / 265f, 196f / 265f },
				{ 29f / 265f, 145f / 265f, 192f / 265f }, { 34f / 265f, 94f / 265f, 168f / 265f },
				{ 37f / 265f, 52f / 265f, 148f / 265f }, { 8f / 265f, 29f / 265f, 88f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlOrBr" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlOrBr3 {
		private final float[][] cols = { { 255f / 265f, 247f / 265f, 188f / 265f },
				{ 254f / 265f, 196f / 265f, 79f / 265f }, { 217f / 265f, 95f / 265f, 14f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlOrBr" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlOrBr4 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 212f / 265f },
				{ 254f / 265f, 217f / 265f, 142f / 265f }, { 254f / 265f, 153f / 265f, 41f / 265f },
				{ 204f / 265f, 76f / 265f, 2f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlOrBr" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlOrBr5 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 212f / 265f },
				{ 254f / 265f, 217f / 265f, 142f / 265f }, { 254f / 265f, 153f / 265f, 41f / 265f },
				{ 217f / 265f, 95f / 265f, 14f / 265f }, { 153f / 265f, 52f / 265f, 4f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlOrBr" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlOrBr6 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 212f / 265f },
				{ 254f / 265f, 227f / 265f, 145f / 265f }, { 254f / 265f, 196f / 265f, 79f / 265f },
				{ 254f / 265f, 153f / 265f, 41f / 265f }, { 217f / 265f, 95f / 265f, 14f / 265f },
				{ 153f / 265f, 52f / 265f, 4f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlOrBr" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlOrBr7 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 212f / 265f },
				{ 254f / 265f, 227f / 265f, 145f / 265f }, { 254f / 265f, 196f / 265f, 79f / 265f },
				{ 254f / 265f, 153f / 265f, 41f / 265f }, { 236f / 265f, 112f / 265f, 20f / 265f },
				{ 204f / 265f, 76f / 265f, 2f / 265f }, { 140f / 265f, 45f / 265f, 4f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlOrBr" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlOrBr8 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 229f / 265f },
				{ 255f / 265f, 247f / 265f, 188f / 265f }, { 254f / 265f, 227f / 265f, 145f / 265f },
				{ 254f / 265f, 196f / 265f, 79f / 265f }, { 254f / 265f, 153f / 265f, 41f / 265f },
				{ 236f / 265f, 112f / 265f, 20f / 265f }, { 204f / 265f, 76f / 265f, 2f / 265f },
				{ 140f / 265f, 45f / 265f, 4f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlOrBr" colour map with 9 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlOrBr9 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 229f / 265f },
				{ 255f / 265f, 247f / 265f, 188f / 265f }, { 254f / 265f, 227f / 265f, 145f / 265f },
				{ 254f / 265f, 196f / 265f, 79f / 265f }, { 254f / 265f, 153f / 265f, 41f / 265f },
				{ 236f / 265f, 112f / 265f, 20f / 265f }, { 204f / 265f, 76f / 265f, 2f / 265f },
				{ 153f / 265f, 52f / 265f, 4f / 265f }, { 102f / 265f, 37f / 265f, 6f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlOrRd" colour map with 3 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlOrRd3 {
		private final float[][] cols = { { 255f / 265f, 237f / 265f, 160f / 265f },
				{ 254f / 265f, 178f / 265f, 76f / 265f }, { 240f / 265f, 59f / 265f, 32f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlOrRd" colour map with 4 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlOrRd4 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 178f / 265f },
				{ 254f / 265f, 204f / 265f, 92f / 265f }, { 253f / 265f, 141f / 265f, 60f / 265f },
				{ 227f / 265f, 26f / 265f, 28f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlOrRd" colour map with 5 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlOrRd5 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 178f / 265f },
				{ 254f / 265f, 204f / 265f, 92f / 265f }, { 253f / 265f, 141f / 265f, 60f / 265f },
				{ 240f / 265f, 59f / 265f, 32f / 265f }, { 189f / 265f, 0f / 265f, 38f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlOrRd" colour map with 6 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlOrRd6 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 178f / 265f },
				{ 254f / 265f, 217f / 265f, 118f / 265f }, { 254f / 265f, 178f / 265f, 76f / 265f },
				{ 253f / 265f, 141f / 265f, 60f / 265f }, { 240f / 265f, 59f / 265f, 32f / 265f },
				{ 189f / 265f, 0f / 265f, 38f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlOrRd" colour map with 7 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlOrRd7 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 178f / 265f },
				{ 254f / 265f, 217f / 265f, 118f / 265f }, { 254f / 265f, 178f / 265f, 76f / 265f },
				{ 253f / 265f, 141f / 265f, 60f / 265f }, { 252f / 265f, 78f / 265f, 42f / 265f },
				{ 227f / 265f, 26f / 265f, 28f / 265f }, { 177f / 265f, 0f / 265f, 38f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	},

	/**
	 * ColorBrewer "YlOrRd" colour map with 8 colours. See <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a> for more
	 * information.
	 */
	YlOrRd8 {
		private final float[][] cols = { { 255f / 265f, 255f / 265f, 204f / 265f },
				{ 255f / 265f, 237f / 265f, 160f / 265f }, { 254f / 265f, 217f / 265f, 118f / 265f },
				{ 254f / 265f, 178f / 265f, 76f / 265f }, { 253f / 265f, 141f / 265f, 60f / 265f },
				{ 252f / 265f, 78f / 265f, 42f / 265f }, { 227f / 265f, 26f / 265f, 28f / 265f },
				{ 177f / 265f, 0f / 265f, 38f / 265f } };

		@Override
		public void apply(float x, float[] out) {
			final int i = Math.min((int) (x * cols.length), cols.length - 1);

			final float[] col = cols[i];

			out[0] = col[0];
			out[1] = col[1];
			out[2] = col[2];
		}

		@Override
		public Type type() {
			return Type.SEQUENTIAL;
		}

		@Override
		public Mode mode() {
			return Mode.DISCRETE;
		}
	};

	/**
	 * Apply a colourmap to a single pixel
	 * 
	 * @param val
	 *            the pixel value
	 * @param out
	 *            the mapped colour
	 */
	public abstract void apply(float val, float[] out);

	/**
	 * Apply a colourmap to a single pixel
	 * 
	 * @param val
	 *            the pixel value
	 * @return the mapped colour
	 */
	public Float[] apply(float val) {
		final float[] out = new float[3];
		apply(val, out);
		return new Float[] { out[0], out[1], out[2] };
	}

	/**
	 * Apply a colourmap to an image
	 * 
	 * @param img
	 *            the image to map
	 * @return the mapped image
	 */
	public MBFImage apply(FImage img) {
		final float[] pixOut = new float[3];

		final int width = img.width;
		final int height = img.height;
		final float[][] pix = img.pixels;

		final MBFImage out = new MBFImage(width, height, ColourSpace.RGB);
		final float[][] r = out.getBand(0).pixels;
		final float[][] g = out.getBand(1).pixels;
		final float[][] b = out.getBand(2).pixels;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				apply(pix[y][x], pixOut);

				r[y][x] = pixOut[0];
				g[y][x] = pixOut[1];
				b[y][x] = pixOut[2];
			}
		}

		return out;
	}

	/**
	 * @return the type of colour map
	 */
	public abstract Type type();

	/**
	 * @return the mapping mode
	 */
	public abstract Mode mode();

	/**
	 * Generate an image to visualise the color map. The image shows vertical
	 * bands with increasing values from 0 on the left to 1.0 on the right.
	 * 
	 * @param width
	 *            the width of the visualisation image in pixels
	 * @param height
	 *            the height of the visualisation image in pixels
	 * @return the visualisation of the color map
	 */
	public MBFImage visualise(int width, int height) {
		final FImage f = new FImage(width, height);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				f.pixels[y][x] = (float) x / (float) width;
			}
		}

		return this.apply(f);
	}

	/**
	 * Generate an image to visualise the color map. The image shows vertical
	 * bands with increasing values from 0 on the left to 1.0 on the right. The
	 * generated image is 256*100 pixels.
	 * 
	 * @return the visualisation of the color map
	 */
	public MBFImage visualise() {
		return this.visualise(256, 100);
	}

	/**
	 * Generate a list of colours using a colour map
	 * 
	 * @param ncolours
	 *            the number of colours to generate
	 * @return the colours
	 */
	public Float[][] generateColours(int ncolours) {
		final Float[][] cm = new Float[ncolours][];

		// if the colourmap is cyclic, we don't want the generated colours to
		// wrap
		cm[0] = this.apply(0);
		cm[ncolours - 1] = this.apply(1);

		boolean same = false;
		for (int i = 0; i < cm[0].length; i++) {
			if (Math.abs(cm[0][i] - cm[ncolours - 1][i]) < 0.00001) {
				same = true;
				break;
			}
		}

		if (same) {
			for (int i = 1; i < ncolours; i++) {
				cm[i] = this.apply(i / (float) (ncolours));
			}
		} else {
			for (int i = 1; i < ncolours - 1; i++) {
				cm[i] = this.apply(i / (float) (ncolours - 1));
			}
		}
		return cm;
	}

	/**
	 * Types of colour map.
	 * <p>
	 * The descriptions of the different types are taken from <a
	 * href="http://colorbrewer2.org">the colorbrewer website</a>
	 */
	public static enum Type {
		/**
		 * Sequential maps are suited to ordered data that progresses from low
		 * to high. The maps use light colors for low values and darker colors
		 * for higher values.
		 */
		SEQUENTIAL,
		/**
		 * Qualitative maps do not imply magnitude differences between classes.
		 * Hues are used to create the primary visual differences between
		 * classes. Qualitative schemes are best suited to representing nominal
		 * or categorical data.
		 */
		QUALITATIVE,
		/**
		 * Diverging schemes put equal emphasis on mid-range critical values and
		 * extremes at both ends of the data range. The critical class or break
		 * in the middle of the legend is emphasized with light colors and low
		 * and high extremes are emphasized with dark colors that have
		 * contrasting hues.
		 */
		DIVERGING
	}

	/**
	 * Colour map modes. Maps produce either interpolated colours or discrete
	 * colours.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static enum Mode {
		/**
		 * Interpolated colour map
		 */
		INTERPOLATED,
		/**
		 * Discrete colour map
		 */
		DISCRETE
	}

	// Generate the colorbrewer maps from a CSV file
	// public static void main(String[] args) throws IOException {
	// List<String> lines = FileUtils.readLines(new
	// File("/Users/jon/Desktop/ColorBrewer_all_schemes_RGBonly3.csv"));
	//
	// //ColorName,NumOfColors,Type,CritVal,ColorNum,ColorLetter,R,G,B,SchemeType
	//
	// String currentName = null;
	// String currentMode = null;
	// int currNum = 0;
	// List<int[]> currentColStack = null;
	//
	// for (String line : lines) {
	// String[] split = line.split(",");
	//
	// if (split[0].length() > 0) {
	// if (currentColStack != null) {
	// String fmt =
	// "	/**" + "\n" +
	// "	 * ColorBrewer \"%s\" colour map with %d colours." + "\n" +
	// "	 * See <a href=\"http://colorbrewer2.org\">the colorbrewer website</a> for more information."
	// + "\n" +
	// "	 */" + "\n" +
	// "	%s {" + "\n" +
	// "		private final float[][] cols = {%s};" + "\n" +
	// "		" + "\n" +
	// "		@Override" + "\n" +
	// "		public void apply(float x, float[] out) {" + "\n" +
	// "			int i = Math.min((int) (x * cols.length), cols.length-1);" + "\n" +
	// "			" + "\n" +
	// "			float[] col = cols[i];" + "\n" +
	// "			" + "\n" +
	// "			out[0] = col[0];" + "\n" +
	// "			out[1] = col[1];" + "\n" +
	// "			out[2] = col[2];" + "\n" +
	// "		}" + "\n" +
	// "" + "\n" +
	// "		@Override" + "\n" +
	// "		public Type type() {" + "\n" +
	// "			return Type.%s;" + "\n" +
	// "		}" + "\n" +
	// "" + "\n" +
	// "		@Override" + "\n" +
	// "		public Mode mode() {" + "\n" +
	// "			return Mode.DISCRETE;" + "\n" +
	// "		}" + "\n" +
	// "	}," + "\n";
	//
	// String name = currentName+currNum;
	// String type = currentMode.equals("qual") ? "QUALITATIVE" :
	// currentMode.equals("seq") ? "SEQUENTIAL" : "DIVERGING";
	//
	// String cols = "";
	// for (int i=0; i<currNum-1; i++) {
	// cols += "{" + currentColStack.get(i)[0] + "f/265f, " +
	// currentColStack.get(i)[1] + "f/265f, " + currentColStack.get(i)[2] +
	// "f/265f}, ";
	// }
	// cols += "{" + currentColStack.get(currNum-1)[0] + "f/265f, " +
	// currentColStack.get(currNum-1)[1] + "f/265f, " +
	// currentColStack.get(currNum-1)[2] + "f/265f}";
	//
	//
	// System.out.println(String.format(fmt, currentName, currNum, name, cols,
	// type));
	// }
	//
	// currentColStack = new ArrayList<int[]>();
	// currNum = Integer.parseInt(split[1]);
	// currentName = split[0];
	// currentMode = split[2];
	// }
	//
	// int [] col = {Integer.parseInt(split[6]), Integer.parseInt(split[7]),
	// Integer.parseInt(split[8])};
	// currentColStack.add(col);
	// }
	// }
}
