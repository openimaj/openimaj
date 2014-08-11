package org.openimaj.video.gstreamer;

import org.bridj.Pointer;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.video.Video;

public class GStreamerVideo extends Video<MBFImage> {
	private OpenIMAJCapGStreamer gstreamer;
	private MBFImage frame;
	private int width;
	private int height;
	private boolean isStopped = true;

	/** The timestamp at which the capture (session) was started */
	private long captureStartedTimestamp = 0;

	/** The timestamp of the current image */
	private long currentTimestamp = 0;
	private String pipeline;

	public GStreamerVideo(String pipeline) {
		this.pipeline = pipeline;

		gstreamer = new OpenIMAJCapGStreamer();

		reset();
	}

	@Override
	public synchronized MBFImage getNextFrame() {
		if (isStopped)
			return frame;

		final boolean err = gstreamer.nextFrame();
		if (!err)
			throw new RuntimeException(new GStreamerException("Error getting next frame"));

		final Pointer<Byte> data = gstreamer.getImage();
		if (data == null) {
			return frame;
		}

		final byte[] d = data.getBytes(width * height * 3);
		final float[][] r = frame.bands.get(0).pixels;
		final float[][] g = frame.bands.get(1).pixels;
		final float[][] b = frame.bands.get(2).pixels;

		for (int i = 0, y = 0; y < height; y++) {
			for (int x = 0; x < width; x++, i += 3) {
				final int red = d[i + 0] & 0xFF;
				final int green = d[i + 1] & 0xFF;
				final int blue = d[i + 2] & 0xFF;
				r[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[red];
				g[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[green];
				b[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[blue];
			}
		}

		super.currentFrame++;

		if (captureStartedTimestamp == 0)
			captureStartedTimestamp = System.currentTimeMillis();
		currentTimestamp = System.currentTimeMillis() - captureStartedTimestamp;

		return frame;
	}

	@Override
	public MBFImage getCurrentFrame() {
		return frame;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public long getTimeStamp() {
		return currentTimestamp;
	}

	@Override
	public double getFPS() {
		return gstreamer.getProperty(OpenIMAJCapGStreamer.PROP_FPS);
	}

	@Override
	public boolean hasNextFrame() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long countFrames() {
		return -1;
	}

	@Override
	public void reset() {
		gstreamer.close();

		if (!gstreamer.open(Pointer.pointerToCString(pipeline))) {
			throw new RuntimeException(new GStreamerException("Error"));
		}

		isStopped = false;
		width = gstreamer.getWidth();
		height = gstreamer.getHeight();
		frame = new MBFImage(width, height, ColourSpace.RGB);
	}
}
