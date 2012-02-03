package org.openimaj.demos.touchtable;

import org.openimaj.io.ReadWriteableASCII;

public interface CameraConfig extends ReadWriteableASCII{
	public Touch transformTouch(Touch point);
}
