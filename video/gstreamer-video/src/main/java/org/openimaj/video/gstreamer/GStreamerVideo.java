package org.openimaj.video.gstreamer;

import org.bridj.Pointer;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;

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
	private double fps;

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

		if (gstreamer.getBands() == 3) {
			final byte[] d = data.getBytes(width * height * 3);
			final float[][] r = frame.bands.get(0).pixels;
			final float[][] g = frame.bands.get(1).pixels;
			final float[][] b = frame.bands.get(2).pixels;

			for (int i = 0, y = 0; y < height; y++) {
				for (int x = 0; x < width; x++, i += 3) {
					final int blue = d[i + 0] & 0xFF;
					final int green = d[i + 1] & 0xFF;
					final int red = d[i + 2] & 0xFF;
					r[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[red];
					g[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[green];
					b[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[blue];
				}
			}
		} else {
			final byte[] d = data.getBytes(width * height * 3);
			final float[][] g = frame.bands.get(0).pixels;

			for (int i = 0, y = 0; y < height; y++) {
				for (int x = 0; x < width; x++, i++) {
					g[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[d[i] & 0xFF];
				}
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
		return fps;
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

		// final Pointer<Byte> cpipeline = Pointer.pointerToCString(pipeline +
		// "\0");
		final byte[] b = pipeline.getBytes();
		final byte[] b1 = new byte[b.length + 1];
		System.arraycopy(b, 0, b1, 0, b.length);
		b1[b.length] = 0;
		final Pointer<Byte> cpipeline = Pointer.pointerToBytes(b1);
		if (!gstreamer.open(cpipeline)) {
			// cpipeline.release();
			throw new RuntimeException(new GStreamerException("Error"));
		}
		// cpipeline.release();

		isStopped = false;

		gstreamer.nextFrame();
		gstreamer.getImage();

		width = gstreamer.getWidth();
		height = gstreamer.getHeight();
		final int bands = gstreamer.getBands();
		this.fps = gstreamer.getProperty(OpenIMAJCapGStreamer.PROP_FPS);
		frame = new MBFImage(width, height, bands == 3 ? ColourSpace.RGB : ColourSpace.LUMINANCE_AVG);
		System.out.println("here11");
	}

	public static void main(String[] args) {
		final GStreamerVideo gsv = new GStreamerVideo(
				"videotestsrc ! video/x-raw,format=GRAY8 ! videoconvert ! appsink");
		System.out.println("here");

		VideoDisplay.createVideoDisplay(gsv);
	}
}
