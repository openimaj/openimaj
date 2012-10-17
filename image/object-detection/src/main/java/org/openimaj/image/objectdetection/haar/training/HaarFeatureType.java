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
package org.openimaj.image.objectdetection.haar.training;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.openimaj.image.objectdetection.haar.HaarFeature;

/**
 * Definitions of standard haar-like features.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public enum HaarFeatureType {
	/**
	 * Two component feature:
	 * 
	 * <pre>
	 * 0 X
	 * </pre>
	 */
	X2 {
		@Override
		public HaarFeature create(int x, int y, int dx, int dy, int winWidth, int winHeight) {
			// haar_x2
			if ((x + dx * 2 <= winWidth) && (y + dy <= winHeight)) {
				return HaarFeature.create(false,
						x, y, dx * 2, dy, -1,
						x + dx, y, dx, dy, +2);
			}
			return null;
		}
	},
	/**
	 * Two component feature:
	 * 
	 * <pre>
	 * 0
	 * X
	 * </pre>
	 */
	Y2 {
		@Override
		public HaarFeature create(int x, int y, int dx, int dy, int winWidth, int winHeight) {
			// haar_y2
			if ((x + dx <= winWidth) && (y + dy * 2 <= winHeight)) {
				return HaarFeature.create(false,
						x, y, dx, dy * 2, -1,
						x, y + dy, dx, dy, +2);
			}
			return null;
		}
	},
	/**
	 * Three component feature:
	 * 
	 * <pre>
	 * 0 X 0
	 * </pre>
	 */
	X3 {
		@Override
		public HaarFeature create(int x, int y, int dx, int dy, int winWidth, int winHeight) {
			// haar_x3
			if ((x + dx * 3 <= winWidth) && (y + dy <= winHeight)) {
				return HaarFeature.create(false,
						x, y, dx * 3, dy, -1,
						x + dx, y, dx, dy, +3);
			}
			return null;
		}
	},
	/**
	 * Three component feature:
	 * 
	 * <pre>
	 * 0
	 * X
	 * 0
	 * </pre>
	 */
	Y3 {
		@Override
		public HaarFeature create(int x, int y, int dx, int dy, int winWidth, int winHeight) {
			// haar_y3
			if ((x + dx <= winWidth) && (y + dy * 3 <= winHeight)) {
				return HaarFeature.create(false,
						x, y, dx, dy * 3, -1,
						x, y + dy, dx, dy, +3);
			}
			return null;
		}
	},
	/**
	 * Three component feature:
	 * 
	 * <pre>
	 * 0 X X 0
	 * </pre>
	 */
	X4 {
		@Override
		public HaarFeature create(int x, int y, int dx, int dy, int winWidth, int winHeight) {
			// haar_x4
			if ((x + dx * 4 <= winWidth) && (y + dy <= winHeight)) {
				return HaarFeature.create(false,
						x, y, dx * 4, dy, -1,
						x + dx, y, dx * 2, dy, +2);
			}
			return null;
		}
	},
	/**
	 * Three component feature:
	 * 
	 * <pre>
	 * 0
	 * X
	 * X
	 * 0
	 * </pre>
	 */
	Y4 {
		@Override
		public HaarFeature create(int x, int y, int dx, int dy, int winWidth, int winHeight) {
			// haar_y4
			if ((x + dx <= winWidth) && (y + dy * 4 <= winHeight)) {
				return HaarFeature.create(false,
						x, y, dx, dy * 4, -1,
						x, y + dy, dx, dy * 2, +2);
			}
			return null;
		}
	},
	/**
	 * Four component feature:
	 * 
	 * <pre>
	 * X 0
	 * 0 X
	 * </pre>
	 */
	X2Y2 {
		@Override
		public HaarFeature create(int x, int y, int dx, int dy, int winWidth, int winHeight) {
			// x2_y2
			if ((x + dx * 2 <= winWidth) && (y + dy * 2 <= winHeight)) {
				return HaarFeature.create(false,
						x, y, dx * 2, dy * 2, -1,
						x, y, dx, dy, +2,
						x + dx, y + dy, dx, dy, +2);
			}
			return null;
		}
	},
	/**
	 * Centre-surround feature:
	 * 
	 * <pre>
	 * 0 0 0
	 * 0 X 0
	 * 0 0 0
	 * </pre>
	 */
	CS {
		@Override
		public HaarFeature create(int x, int y, int dx, int dy, int winWidth, int winHeight) {
			if ((x + dx * 3 <= winWidth) && (y + dy * 3 <= winHeight)) {
				return HaarFeature.create(false,
						x, y, dx * 3, dy * 3, -1,
						x + dx, y + dy, dx, dy, +9);
			}
			return null;
		}
	},
	/**
	 * Tilted two component feature:
	 * 
	 * <pre>
	 * 0 - -X
	 * </pre>
	 */
	TX2 {
		@Override
		public HaarFeature create(int x, int y, int dx, int dy, int winWidth, int winHeight) {
			// tilted haar_x2
			if ((x + 2 * dx <= winWidth) && (y + 2 * dx + dy <= winHeight) && (x - dy
					>= 0))
			{
				return HaarFeature.create(true,
						x, y, dx * 2, dy, -1,
						x, y, dx, dy, +2);
			}
			return null;
		}
	},
	/**
	 * Tilted two component feature:
	 * 
	 * <pre>
	 * - 0
	 * X -
	 * </pre>
	 */
	TY2 {
		@Override
		public HaarFeature create(int x, int y, int dx, int dy, int winWidth, int winHeight) {
			// tilted haar_y2
			if ((x + dx <= winWidth) && (y + dx + 2 * dy <= winHeight) && (x - 2 * dy
					>= 0))
			{
				return HaarFeature.create(true,
						x, y, dx, 2 * dy, -1,
						x, y, dx, dy, +2);
			}
			return null;
		}
	},
	/**
	 * Tilted three component feature:
	 * 
	 * <pre>
	 * 0 - -
	 * 		-X -
	 * 		- -0
	 * </pre>
	 */
	TX3 {
		@Override
		public HaarFeature create(int x, int y, int dx, int dy, int winWidth, int winHeight) {
			// tilted haar_x3
			if ((x + 3 * dx <= winWidth) && (y + 3 * dx + dy <= winHeight) && (x - dy
					>= 0))
			{
				return HaarFeature.create(true,
						x, y, dx * 3, dy, -1,
						x + dx, y + dx, dx, dy, +3);
			}
			return null;
		}
	},
	/**
	 * Tilted three component feature:
	 * 
	 * <pre>
	 * - - 0
	 * - X -
	 * 0 - -
	 * </pre>
	 */
	TY3 {
		@Override
		public HaarFeature create(int x, int y, int dx, int dy, int winWidth, int winHeight) {
			// tilted haar_y3
			if ((x + dx <= winWidth) && (y + dx + 3 * dy <= winHeight) && (x - 3 * dy
					>= 0))
			{
				return HaarFeature.create(true,
						x, y, dx, 3 * dy, -1,
						x - dy, y + dy, dx, dy, +3);
			}
			return null;
		}
	},
	/**
	 * Tilted three component feature:
	 * 
	 * <pre>
	 * 0 - - -
	 * 		-X - -
	 * 		- -X -
	 * 		- - -0
	 * </pre>
	 */
	TX4 {
		@Override
		public HaarFeature create(int x, int y, int dx, int dy, int winWidth, int winHeight) {
			// tilted haar_x4
			if ((x + 4 * dx <= winWidth) && (y + 4 * dx + dy <= winHeight) && (x - dy
					>= 0))
			{
				return HaarFeature.create(true,
						x, y, dx * 4, dy, -1,
						x + dx, y + dx, dx * 2, dy, +2);
			}
			return null;
		}
	},
	/**
	 * Tilted three component feature:
	 * 
	 * <pre>
	 * - - - 0
	 * - - X -
	 * - X - - 
	 * 0 - - -
	 * </pre>
	 */
	TY4 {
		@Override
		public HaarFeature create(int x, int y, int dx, int dy, int winWidth, int winHeight) {
			// tilted haar_y4
			if ((x + dx <= winWidth) && (y + dx + 4 * dy <= winHeight) && (x - 4 * dy
					>= 0))
			{
				return HaarFeature.create(true,
						x, y, dx, 4 * dy, -1,
						x - dy, y + dy, dx, 2 * dy, +2);
			}
			return null;
		}
	};

	/**
	 * Set of all the features
	 */
	public static EnumSet<HaarFeatureType> ALL = EnumSet.allOf(HaarFeatureType.class);

	/**
	 * Set of the basic features (non tilted edges & lines + {@link #X2Y2})
	 */
	public static EnumSet<HaarFeatureType> BASIC = EnumSet.of(X2, Y2, X3, Y3, X2Y2);

	/**
	 * Set of the core features (all but tilted features)
	 */
	public static EnumSet<HaarFeatureType> CORE = EnumSet.of(X2, Y2, X3, Y3, X2Y2, X4, Y4, CS);

	/**
	 * Create a feature.
	 * 
	 * @param x
	 *            x-location
	 * @param y
	 *            y-location
	 * @param dx
	 *            x-delta
	 * @param dy
	 *            y-delta
	 * @param winWidth
	 *            window width
	 * @param winHeight
	 *            window height
	 * @return the new feature, or null if the parameters are out of range.
	 */
	public abstract HaarFeature create(int x, int y, int dx, int dy, int winWidth, int winHeight);

	/**
	 * Generate features of the given types for all possible locations and sizes
	 * in the given window bounds.
	 * 
	 * @param winWidth
	 *            window width
	 * @param winHeight
	 *            window height
	 * @param types
	 *            types of feature to generate
	 * @return the generated features
	 */
	public static List<HaarFeature> generateFeatures(int winWidth, int winHeight, HaarFeatureType... types) {
		return generateFeatures(winWidth, winHeight, Arrays.asList(types));
	}

	/**
	 * Generate features of the given types for all possible locations and sizes
	 * in the given window bounds.
	 * 
	 * @param winWidth
	 *            window width
	 * @param winHeight
	 *            window height
	 * @param types
	 *            types of feature to generate
	 * @return the generated features
	 */
	public static List<HaarFeature> generateFeatures(int winWidth, int winHeight, Collection<HaarFeatureType> types) {
		final List<HaarFeature> features = new ArrayList<HaarFeature>();

		for (int x = 0; x < winWidth; x++) {
			for (int y = 0; y < winHeight; y++) {
				for (int dx = 1; dx <= winWidth; dx++) {
					for (int dy = 1; dy <= winHeight; dy++) {
						for (final HaarFeatureType type : types) {
							final HaarFeature f = type.create(x, y, dx, dy, winWidth, winHeight);

							if (f != null) {
								features.add(f);
							}
						}
					}
				}
			}
		}

		return features;
	}
}
