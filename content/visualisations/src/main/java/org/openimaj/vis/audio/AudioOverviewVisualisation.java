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
package org.openimaj.vis.audio;

import gnu.trove.list.array.TFloatArrayList;

import java.awt.Dimension;
import java.util.ArrayList;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.AudioProcessor;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.vis.DataUnitsTransformer;
import org.openimaj.vis.VisualisationImpl;
import org.openimaj.vis.timeline.TimelineObject;
import org.openimaj.vis.timeline.TimelineObjectAdapter;

/**
 * Utilises an audio processor to plot the audio waveform to an image. This
 * class is both a {@link VisualisationImpl} and a {@link TimelineObject}. This
 * means that it can be used to plot a complete visualisation of the overview of
 * the data or it can be used to plot temporal parts of the data into the
 * visualisation window.
 * <p>
 * An internal class (AudioOverviewGenerator) can be used to generate overviews
 * if necessary.
 * <p>
 * This class also extends {@link TimelineObjectAdapter} which allows an audio
 * waveform to be put upon a timeline.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *
 * @created 9 Jun 2011
 */
public class AudioOverviewVisualisation extends VisualisationImpl<AudioStream>
		implements TimelineObject
{
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * Generates an audio overview. This is a lower-resolution version of the
	 * audio waveform. It takes the maximum value from a set of values and
	 * stores this as the overview. By default the processor takes the maximum
	 * value from every 5000 samples. The method
	 * {@link #getAudioOverview(int, int)} allows resampling of that overview.
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 21 Jul 2011
	 *
	 */
	public class AudioOverviewGenerator extends AudioProcessor
	{
		/** Number of bins in the overview */
		private int nSamplesPerBin = -1;

		/** The maximum in the current bin for each channel */
		private float[] channelMax = null;

		/** The number of samples so far in the current bin being processed */
		private int nSamplesInBin = 0;

		/** The overview data */
		private TFloatArrayList[] audioOverview = null;

		/** The number of channels in the audio data */
		private int nChannels = 0;

		/** The audio format of the samples we're processing */
		private AudioFormat af = null;

		/**
		 * Constructor
		 *
		 * @param nSamplesPerBin
		 *            The number of samples per bin
		 * @param nChannels
		 *            The number of channels
		 */
		public AudioOverviewGenerator(final int nSamplesPerBin, final int nChannels)
		{
			this.nSamplesPerBin = nSamplesPerBin;
			this.nChannels = nChannels;
			this.audioOverview = new TFloatArrayList[nChannels];
			this.channelMax = new float[nChannels];

			for (int i = 0; i < nChannels; i++)
				this.audioOverview[i] = new TFloatArrayList();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
		 */
		@Override
		public SampleChunk process(final SampleChunk samples)
		{
			// Store the format of the data
			if (this.af == null)
				this.af = samples.getFormat();

			// Get the sample data
			final SampleBuffer b = samples.getSampleBuffer();

			// The number of samples (per channel) in this sample chunk
			final int nSamples = b.size() / this.af.getNumChannels();

			// Keep a running total of how many samples we've processed
			AudioOverviewVisualisation.this.numberOfProcessedSamples += nSamples;

			for (int x = 0; x < nSamples; x++)
			{
				for (int c = 0; c < this.nChannels; c++)
				{
					// Store the maximum for the current bin
					this.channelMax[c] = Math.max(this.channelMax[c],
							b.get(x * this.nChannels + c));
				}

				// If we're still within the bin
				if (this.nSamplesInBin < this.nSamplesPerBin)
					this.nSamplesInBin++;
				else
				{
					// We've overflowed the bin
					for (int c = 0; c < this.nChannels; c++)
					{
						// Store the current bin
						this.audioOverview[c].add(this.channelMax[c]);
						this.channelMax[c] = Integer.MIN_VALUE;
					}

					// Reset for the next bin
					this.nSamplesInBin = 0;
				}
			}

			return samples;
		}

		/**
		 * @return Get the overview data.
		 */
		public TFloatArrayList[] getAudioOverview()
		{
			return this.audioOverview;
		}

		/**
		 * Refactors the overview to given another overview. If the number of
		 * bins specified an overview that's finer than the actual overview the
		 * original overview is returned. The output of this function will then
		 * only return an array list of nBins or less.
		 *
		 * @param channel
		 *            The channel to get
		 * @param nBins
		 *            The number of bins in the overview
		 * @return A refactors overview
		 */
		public TFloatArrayList getAudioOverview(final int channel, final int nBins)
		{
			if (nBins >= this.audioOverview[channel].size())
				return this.audioOverview[channel];

			final TFloatArrayList ii = new TFloatArrayList();
			final double scalar = (double) this.audioOverview[channel].size() / (double) nBins;
			for (int xx = 0; xx < nBins; xx++)
			{
				final int startBin = (int) (xx * scalar);
				final int endBin = (int) ((xx + 1) * scalar);
				float m = Integer.MIN_VALUE;
				for (int yy = startBin; yy < endBin; yy++)
					m = Math.max(m, this.audioOverview[channel].get(yy));
				ii.add(m);
			}
			return ii;
		}

		/**
		 * Returns a polygon representing the channel overview.
		 *
		 * @param channel
		 *            The channel to get the polygon for
		 * @param mirror
		 *            whether to mirror the polygon
		 * @param width
		 *            The width of the overview to return
		 * @return A polygon
		 */
		public Polygon getChannelPolygon(final int channel, final boolean mirror, final int width)
		{
			final TFloatArrayList overview = this.getAudioOverview(channel, width);
			final int len = overview.size();
			final double scalar = width / (double) len;

			final ArrayList<Point2d> l = new ArrayList<Point2d>();
			for (int x = 0; x < len; x++)
				l.add(new Point2dImpl((float) (x * scalar), overview.get(x)));

			if (mirror)
			{
				for (int x = 1; x <= len; x++)
					l.add(new Point2dImpl((float) ((len - x) * scalar),
							-overview.get(len - x)));
			}

			// Store how long the given overview is in milliseconds
			AudioOverviewVisualisation.this.millisecondsInView = (long) (AudioOverviewVisualisation.this.numberOfProcessedSamples /
					this.af.getSampleRateKHz());

			return new Polygon(l);
		}
	}

	/**
	 * The calculation of how many milliseconds are in the last generated view
	 * at the resampled overview.
	 */
	public long millisecondsInView = 0;

	/** The number of samples that were originally read in from the data */
	public long numberOfProcessedSamples = 0;

	/** The start time in milliseconds */
	private long start = 0;

	/** The length of the audio data */
	private long length = 1000;

	/** The overview generator */
	private AudioOverviewGenerator aap = null;

	/** Number of samples per pixel */
	private int nSamplesPerPixel = 500;

	/** Whether the generation is complete */
	private boolean generationComplete = false;

	/**
	 * Default constructor
	 * 
	 * @param as
	 *            The audio data to plot
	 */
	public AudioOverviewVisualisation(final AudioStream as)
	{
		this.data = as;
		this.length = this.data.getLength();

		// How many pixels we'll overview per pixel
		this.nSamplesPerPixel = 500;
		// TODO: This is currently fixed-size but should be based on audio
		// length

		// Generate the audio overview
		this.aap = new AudioOverviewGenerator(
				this.nSamplesPerPixel, this.data.getFormat().getNumChannels());

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					synchronized (AudioOverviewVisualisation.this.aap)
					{
						AudioOverviewVisualisation.this.aap.process(AudioOverviewVisualisation.this.data);
						AudioOverviewVisualisation.this.generationComplete = true;
						AudioOverviewVisualisation.this.aap.notifyAll();
					}
				}
				catch (final Exception e)
				{
					e.printStackTrace();
					AudioOverviewVisualisation.this.aap = null;
				}
			}
		}).start();

		this.setPreferredSize(new Dimension(-1, 100));
	}

	/**
	 * Generates a waveform image that fits within the given width and height
	 * and drawn in the given colour. Note that the generated image is RGBA so
	 * that the colours need to be 4 dimensions and may stipulate transparency.
	 *
	 * @param a
	 *            The audio to draw
	 * @param w
	 *            The width of the image to return
	 * @param h
	 *            The height of the image to return
	 * @param backgroundColour
	 *            The background colour to draw on the image
	 * @param colour
	 *            The colour in which to draw the audio waveform.
	 * @return The input image.
	 */
	public static MBFImage getAudioWaveformImage(final AudioStream a,
			final int w, final int h, final Float[] backgroundColour,
			final Float[] colour)
	{
		return new AudioOverviewVisualisation(a).plotAudioWaveformImage(
				w, h, backgroundColour, colour);
	}

	/**
	 * Generates a waveform image that fits within the given width and height
	 * and drawn in the given colour. Note that the generated image is RGBA so
	 * that the colours need to be 4 dimensions and may stipulate transparency.
	 * <p>
	 * If you require information about the plot afterwards you can check the
	 * fields that are stored within this instance.
	 *
	 * @param w
	 *            The width of the image to return
	 * @param h
	 *            The height of the image to return
	 * @param backgroundColour
	 *            The background colour to draw on the image
	 * @param colour
	 *            The colour in which to draw the audio waveform.
	 * @return The input image.
	 */
	public MBFImage plotAudioWaveformImage(
			final int w, final int h, final Float[] backgroundColour,
			final Float[] colour)
	{
		// Check if the overview's been generated, if not return empty image
		if (this.aap == null)
		{
			this.visImage.drawText("Processing...", 20, 20, HersheyFont.TIMES_BOLD, 12, RGBColour.WHITE);
			return this.visImage;
		}

		// If the generation isn't complete (and aap is not null) it means
		// we're processing the overview. Wait until it's finished.
		while (!this.generationComplete)
		{
			synchronized (this.aap)
			{
				try
				{
					this.aap.wait();
				} catch (final InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}

		// Work out how high each channel will be
		final double channelSize = h / (double) this.data.getFormat().getNumChannels();

		// This is the scalar from audio amplitude to pixels
		final double ampScalar = channelSize / Integer.MAX_VALUE;

		// Create the image we're going to draw on to - RGBA
		// final MBFImage m = new MBFImage( w, h, 4 );
		final MBFImageRenderer renderer = this.visImage.createRenderer();
		this.visImage.fill(backgroundColour);

		try
		{
			// Draw the polygon onto the image
			final float ww = 1;
			for (int i = 0; i < this.data.getFormat().getNumChannels(); i++)
			{
				final Polygon p = this.aap.getChannelPolygon(i, true, w);
				p.scaleXY(ww, (float) -ampScalar / 2f);
				p.translate(0f, (float) (-p.minY() + channelSize * i));
				renderer.drawPolygonFilled(p, colour);
			}
		} catch (final Exception e)
		{
			System.err.println("WARNING: Could not process audio " +
					"to generate the audio overview.");
			e.printStackTrace();
		}

		return this.visImage;
	}

	/**
	 * Returns the length of the audio data in milliseconds. Only returns the
	 * correct value after processing. Until then, it will return 1 second.
	 *
	 * @return Length of the audio data.
	 */
	public long getLength()
	{
		return this.length;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.vis.timeline.TimelineObjectAdapter#getStartTimeMilliseconds()
	 */
	@Override
	public long getStartTimeMilliseconds()
	{
		return this.start;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.vis.timeline.TimelineObjectAdapter#getEndTimeMilliseconds()
	 */
	@Override
	public long getEndTimeMilliseconds()
	{
		return this.start + this.getLength();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.vis.VisualisationImpl#update()
	 */
	@Override
	public void update()
	{
		if (this.visImage == null)
			this.plotAudioWaveformImage(
					this.visImage.getWidth(), this.visImage.getHeight(),
					new Float[] { 1f, 1f, 0f, 1f }, new Float[] { 0f, 0f, 0f, 1f });
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.vis.timeline.TimelineObject#setStartTimeMilliseconds(long)
	 */
	@Override
	public void setStartTimeMilliseconds(final long l)
	{
		this.start = l;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.vis.timeline.TimelineObject#setDataPixelTransformer(org.openimaj.vis.DataUnitsTransformer)
	 */
	@Override
	public void setDataPixelTransformer(final DataUnitsTransformer<Float[], double[], int[]> dpt)
	{
	}
}
