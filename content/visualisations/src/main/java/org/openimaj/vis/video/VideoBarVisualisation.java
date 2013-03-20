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
/**
 *
 */
package org.openimaj.vis.video;

import java.awt.Dimension;
import java.awt.Graphics;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.time.Timecode;
import org.openimaj.video.Video;
import org.openimaj.video.timecode.FrameNumberVideoTimecode;
import org.openimaj.video.timecode.HrsMinSecFrameTimecode;
import org.openimaj.vis.timeline.Timeline.TimelineMarker;
import org.openimaj.vis.timeline.Timeline.TimelineMarkerType;
import org.openimaj.vis.timeline.TimelineObject;

/**
 * Displays a block, or bar, which represents the data. The block will be scaled
 * to fit the JPanel in which its drawn. The block will contain a visImage of
 * the data content. The visImage of the content is determined by one of the
 * subclasses of this class.
 * <p>
 * This class will process the data in a separate thread. Obviously, it's not
 * sensible to call this class with a "live" data stream, such as from a
 * VideoCapture object.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 3 Jul 2012
 * @version $Author$, $Revision$, $Date$
 */
public abstract class VideoBarVisualisation extends TimelineObject<Video<MBFImage>> {
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * A marker for marking data frames within the data bar
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 6 Jul 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	public class VideoTimelineMarker extends TimelineMarker {
		/** The frame number in the data */
		public int frameNumber = 0;
	}

	/**
	 * Process a particular frame of the data. The frame and timecode of the
	 * frame are provided.
	 *
	 * @param frame
	 *            The frame to process
	 * @param t
	 *            The timecode.
	 */
	public abstract void processFrame(MBFImage frame, Timecode t);

	/**
	 * Forces a redraw of the specific visImage onto the bar canvas. The
	 * method should completely redraw the visualisation into the given
	 * MBFImage, but it should not clear the visualisation first. It can
	 * be assumed that the image will have been cleared already.
	 *
	 * @param vis
	 *            The visImage to update.
	 */
	@Override
	public abstract void updateVis(MBFImage vis);

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.vis.Visualisation#update()
	 */
	@Override
	public void update() {
		this.updateVis(this.visImage);
	}

	/** The background colour of the bar */
	private Float[] barColour = new Float[] { 0.3f, 0.5f, 0.7f };

	/** Whether to also show the audio waveform. */
	private final boolean showAudio = false;

	/** The height to plot the audio */
	private final int audioHeight = 50;

	/** Number of frames in the data in total */
	private long nFrames = -1;

	/** The start position of the data (as a timeline object) */
	private long start = 0;

	/** The marker that's used for processing progress */
	private VideoTimelineMarker processingMarker = new VideoTimelineMarker();

	/** The frame being processed */
	private int nFrame = 0;

	/**
	 *
	 * @param data
	 */
	protected VideoBarVisualisation(final Video<MBFImage> video) {
		this.data = video;

		this.nFrames = this.data.countFrames();
		this.setPreferredSize(new Dimension(1, 120 + (this.showAudio ? this.audioHeight : 0)));
	}

	/**
	 * Begin processing the data in a separate thread. The data will be reset
	 * after processing is complete.
	 */
	public void processVideo() {
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				VideoBarVisualisation.this.processVideoThread();
				VideoBarVisualisation.this.data.reset();
			}
		}).start();
	}

	/**
	 * The processing method used in the processing thread.
	 */
	private void processVideoThread() {
		this.processingMarker = new VideoTimelineMarker();
		this.processingMarker.type = TimelineMarkerType.LABEL;

		// Iterate through the data to get each frame.
		this.nFrame = 0;
		for (final MBFImage frame : this.data) {
			this.processingMarker.frameNumber = this.nFrame;

			// Process the frame
			this.processFrame(frame, new FrameNumberVideoTimecode(
					this.nFrame, this.data.getFPS()));
			this.nFrame++;
			System.out.println( this.nFrame );

			this.repaint();
		}

		this.processingMarker = null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(final Graphics g) {
		// Resize the vis image if necessary
		final int w = Math.min(this.getWidth(), this.getViewSize().width);
		final int h = Math.min(this.getHeight(), this.getViewSize().height);

		// Create a new vis image if the current image is the wrong size,
		// or the image is not yet instantiated.
		if (this.visImage == null ||
				(w > 0 && h > 0 && this.visImage.getWidth() != w &&
				this.visImage.getHeight() != h))
			this.visImage = new MBFImage(w, h, 3);

		// Wipe out the vis.
		this.visImage.fill(this.barColour);

		// Draw the vis specifics
		this.updateVis(this.visImage);

		// Copy the vis to the Swing UI
		g.drawImage(ImageUtilities.createBufferedImage(this.visImage),
				0, 0, null);

		// Draw the processing marker
		if (this.processingMarker != null) {
			final double d = this.getTimePosition(this.processingMarker.frameNumber);
			final HrsMinSecFrameTimecode tc = new HrsMinSecFrameTimecode(
					this.processingMarker.frameNumber, this.data.getFPS());
			this.processingMarker.label = String.format("%.2f%% %s",
					this.processingMarker.frameNumber / (float) this.nFrames * 100f, tc.toString());
			this.processingMarker.type.drawMarker(this.processingMarker, g, (int) d, h);
		}
	}

	/**
	 * @return the barColour
	 */
	public Float[] getBarColour() {
		return this.barColour;
	}

	/**
	 * @param barColour
	 *            the barColour to set
	 */
	public void setBarColour(final Float[] barColour) {
		this.barColour = barColour;
	}

	/**
	 * Return the data being shown by this bar.
	 *
	 * @return The data.
	 */
	public Video<MBFImage> getVideo() {
		return this.data;
	}

	/**
	 * Returns the position of the given timecode at the scale of the current
	 * display. The position is given in pixels from the start of the bar.
	 *
	 * @param t
	 *            the timecode for which to give the position.
	 * @return The position in pixels of the timecode.
	 */
	protected double getTimePosition(final Timecode t) {
		final long n = (this.nFrames <0? this.nFrame : this.nFrames);
		final double msLength = n / this.data.getFPS() * 1000;
		return t.getTimecodeInMilliseconds() / msLength * this.getWidth();
	}

	/**
	 * Returns the position of the given frame at the scale of the current
	 * display. The position is given in pixel from the start of the bar.
	 *
	 * @param nFrame
	 *            The frame index
	 * @return The position in pixels of the frame.
	 */
	protected double getTimePosition(final int nFrame) {
		return nFrame / (double) this.nFrames * this.getWidth();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.vis.timeline.TimelineObject#getStartTimeMilliseconds()
	 */
	@Override
	public long getStartTimeMilliseconds() {
		return this.start;
	}

	/**
	 * Set the start time of this data object.
	 *
	 * @param t
	 *            The start time.
	 */
	public void setStartTimeMilliseconds(final long t) {
		this.start = t;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.vis.timeline.TimelineObject#getEndTimeMilliseconds()
	 */
	@Override
	public long getEndTimeMilliseconds() {
		return this.start + (long) (this.nFrames / this.data.getFPS() * 1000);
	}
}
