package org.openimaj.image.objectdetection.haar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public enum HaarFeatureType {
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

	public static EnumSet<HaarFeatureType> ALL = EnumSet.allOf(HaarFeatureType.class);
	public static EnumSet<HaarFeatureType> BASIC = EnumSet.of(X2, Y2, X3, Y3, X2Y2);
	public static EnumSet<HaarFeatureType> CORE = EnumSet.of(X2, Y2, X3, Y3, X2Y2, X4, Y4, CS);

	public abstract HaarFeature create(int x, int y, int dx, int dy, int winWidth, int winHeight);

	public static List<HaarFeature> generateFeatures(int winWidth, int winHeight, HaarFeatureType... types) {
		return generateFeatures(winWidth, winHeight, Arrays.asList(types));
	}

	/**
	 * @param winWidth
	 * @param winHeight
	 * @param types
	 * @return
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
