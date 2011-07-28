package org.openimaj.image.typography.hershey;

import org.openimaj.image.typography.FontStyle;

public class HersheyFontStyle<T> extends FontStyle<T> {
	public final static int HORIZONTAL_CENTER = 0;
	public final static int HORIZONTAL_LEFT = 1;
	public final static int HORIZONTAL_RIGHT = 2;
	public final static int HORIZONTAL_NORMAL = 1;

	public final static int VERTICAL_TOP = 0;
	public final static int VERTICAL_HALF = 1;
	public final static int VERTICAL_CAP = 2;
	public final static int VERTICAL_BOTTOM = 3;
	public final static int VERTICAL_NORMAL = 3;

	protected float italicSlant = 0.75f;
	protected float width;
	protected float height;

	protected int horizontalAlignment = HORIZONTAL_NORMAL;
	protected int verticalAlignment = VERTICAL_NORMAL;
}
