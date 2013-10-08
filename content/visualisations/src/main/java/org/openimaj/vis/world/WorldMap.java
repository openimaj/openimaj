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
package org.openimaj.vis.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.content.animation.animator.ValueAnimator;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.image.renderer.RenderHints;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.vis.AnimatedVisualisationListener;
import org.openimaj.vis.AnimatedVisualisationProvider;
import org.openimaj.vis.general.AxesRenderer2D;
import org.openimaj.vis.general.ItemPlotter;
import org.openimaj.vis.general.LabelledPointVisualisation;
import org.openimaj.vis.general.LabelledPointVisualisation.LabelledDot;
import org.openimaj.vis.general.XYPlotVisualisation;

import Jama.Matrix;

/**
 * Draws a world map visualisation as an XY plot. The XY coordinates are scaled
 * to be equal to longitude and latitude.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @param <T>
 *            The type of data to plot on the image
 * @created 11 Jun 2013
 */
public class WorldMap<T> extends XYPlotVisualisation<T>
		implements AnimatedVisualisationProvider
{
	/** */
	private static final long serialVersionUID = 1L;

	/** The colour to use for the sea */
	private Float[] seaColour = new Float[] { 0.4f, 0.5f, 1f, 1f };

	/** The colour to outline the countries with */
	private Float[] defaultCountryOutlineColour = new Float[] { 0f, 0f, 0f };

	/** The colour to fill the land with */
	private Float[] defaultCountryLandColour = new Float[] { 0f, 1f, 0f };

	/** The colour to fill the land when highlighted */
	private Float[] highlightCountryLandColour = new Float[] { 1f, 0f, 0f };

	/** The polygons that represent the countries in the world */
	private WorldPolygons worldPolys;

	/** List of countries which should be highlighted */
	private final Set<String> activeCountries = new HashSet<String>();

	/** These are overrides for the default country highglight colour */
	private final HashMap<String, Float[]> countryHighlightColours = new HashMap<String, Float[]>();

	/** Listeners for animations */
	private final List<AnimatedVisualisationListener> listeners = new ArrayList<AnimatedVisualisationListener>();

	/** The cached world image */
	private MBFImage cachedWorldImage = null;

	/** List of colour animations in progress */
	private final Map<String, ValueAnimator<Float[]>> colourAnimators = new HashMap<String, ValueAnimator<Float[]>>();

	/** Thread for animating colours */
	private Thread animationThread = null;

	int xmin = -180;
	int xmax = 180;
	int ymin = -90;
	int ymax = 90;

	/**
	 * @param width
	 *            Width of the visualisation
	 * @param height
	 *            Height of the visualisation
	 * @param plotter
	 *            The plotter to plot data with
	 */
	public WorldMap(final int width, final int height,
			final ItemPlotter<T, Float[], MBFImage> plotter)
	{
		super(width, height, plotter);
		this.init();
	}

	/**
	 * @param width
	 *            Width of the visualisation
	 * @param height
	 *            Height of the visualisation
	 * @param plotter
	 *            The plotter to plot data with
	 * @param xmin
	 *            min x value to be plotted
	 * @param xmax
	 *            max x value to be plotted
	 * @param ymin
	 *            min y value to be plotted
	 * @param ymax
	 *            max y value to be plotted
	 */
	public WorldMap(final int width, final int height, final ItemPlotter<T, Float[], MBFImage> plotter,
			int xmin, int xmax, int ymin, int ymax)
	{
		super(width, height, plotter);
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		this.init();
	}

	/**
	 * Initialise
	 */
	private void init()
	{
		super.setAutoScaleAxes(false);
		super.setAutoPositionXAxis(true);
		super.axesRenderer2D.setAutoScaleAxes(false);

		super.axesRenderer2D.setMinXValue(xmin);
		super.axesRenderer2D.setMaxXValue(xmax);
		super.axesRenderer2D.setMinYValue(ymin);
		super.axesRenderer2D.setMaxYValue(ymax);
		super.axesRenderer2D.setAxisPaddingLeft(50);
		super.axesRenderer2D.setAxisPaddingBottom(50);
		super.axesRenderer2D.setAxisPaddingRight(50);
		super.axesRenderer2D.setAxisPaddingTop(50);
		super.axesRenderer2D.setxMajorTickSpacing(10);
		super.axesRenderer2D.setyMajorTickSpacing(10);
		super.axesRenderer2D.setxMinorTickSpacing(5);
		super.axesRenderer2D.setyMinorTickSpacing(5);
		super.axesRenderer2D.setxLabelSpacing(90);
		super.axesRenderer2D.setyLabelSpacing(45);
		super.axesRenderer2D.setxAxisColour(RGBColour.WHITE);
		super.axesRenderer2D.setyAxisColour(RGBColour.WHITE);
		super.axesRenderer2D.setyTickLabelColour(RGBColour.WHITE);
		super.axesRenderer2D.setxTickLabelColour(RGBColour.WHITE);
		super.axesRenderer2D.setDrawXAxisName(false);
		super.axesRenderer2D.setDrawYAxisName(false);
		super.axesRenderer2D.setDrawMajorTickGrid(false);
		super.axesRenderer2D.setDrawMinorTickGrid(false);
		this.worldPolys = new WorldPolygons();
		this.addAnimatedVisualisationListener(this);
	}

	/**
	 * Add a country to highlight
	 * 
	 * @param countryCode
	 *            The country code to highlight
	 */
	public void addHighlightCountry(final String countryCode)
	{
		this.activeCountries.add(countryCode);
	}

	/**
	 * Add a country to highlight
	 * 
	 * @param countryCode
	 *            The country code to highlight
	 * @param colour
	 *            The colour to highlight the country
	 */
	public void addHighlightCountry(final String countryCode, final Float[] colour)
	{
		this.activeCountries.add(countryCode);
		this.countryHighlightColours.put(countryCode, colour);
	}

	/**
	 * Remove a highlighted country
	 * 
	 * @param countryCode
	 *            The country code to remove
	 */
	public void removeHighlightCountry(final String countryCode)
	{
		this.activeCountries.remove(countryCode);
		this.countryHighlightColours.remove(countryCode);
	}

	/**
	 * Fill the image with the sea's colour. Uses the member seaColour to
	 * determine this. If you want another sea texture, override this method.
	 */
	protected void drawSea(final MBFImage img)
	{
		img.fill(this.seaColour);
	}

	private void drawCachedImage(final MBFImage img,
			final AxesRenderer2D<Float[], MBFImage> axesRenderer)
	{
		synchronized (axesRenderer)
		{
			System.out.println("Drawing cached world image " + img.getWidth() + "x" + img.getHeight());
			this.cachedWorldImage = new MBFImage(img.getWidth(), img.getHeight(), 4);

			// Fill the image with the sea colour.
			// We'll draw the countries on top of this.
			this.drawSea(this.cachedWorldImage);

			// Make the image fit into the axes centred around 0,0 long/lat
			final Point2d mid = axesRenderer.calculatePosition(0, 0);
			final Point2d dateLine0 = axesRenderer.calculatePosition(180, 0);
			final Point2d northPole = axesRenderer.calculatePosition(0, -90);

			System.out.println("0,0 @ " + mid);
			System.out.println("dateLine: " + dateLine0);
			System.out.println("northpole: " + northPole);

			final double scaleX = (dateLine0.getX() - mid.getX()) / 180d;
			final double scaleY = (northPole.getY() - mid.getY()) / 90d;
			Matrix trans = Matrix.identity(3, 3);
			trans = trans.times(
					TransformUtilities.scaleMatrixAboutPoint(
							scaleX, -scaleY, mid
							)
					);

			// Translate to 0,0
			trans = trans.times(
					TransformUtilities.translateMatrix(mid.getX(), mid.getY())
					);

			// Now draw the countries onto the sea. We transform each of the
			// shapes
			// by the above transform matrix prior to plotting them to the
			// image.
			for (final WorldPlace wp : this.worldPolys.getShapes())
			{
				// Each place may have more than one polygon.
				final List<Shape> shapes = wp.getShapes();

				final MBFImageRenderer ir = this.cachedWorldImage.createRenderer(RenderHints.ANTI_ALIASED);

				// For each of the polygons... draw them to the image.
				for (Shape s : shapes)
				{
					s = s.transform(trans);

					// Fill the country with the land colour
					ir.drawShapeFilled(s, this.defaultCountryLandColour);

					// Draw the outline shape of the country
					ir.drawShape(s, 1, this.defaultCountryOutlineColour);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.vis.general.XYPlotVisualisation#beforeAxesRender(org.openimaj.image.MBFImage,
	 *      org.openimaj.vis.general.AxesRenderer2D)
	 */
	@Override
	synchronized public void beforeAxesRender(final MBFImage visImage,
			final AxesRenderer2D<Float[], MBFImage> axesRenderer)
	{
		synchronized (axesRenderer)
		{
			// Redraw the world if the image dimensions aren't the same.
			if (this.cachedWorldImage == null ||
					visImage.getWidth() != this.cachedWorldImage.getWidth() ||
					visImage.getHeight() != this.cachedWorldImage.getHeight())
				this.drawCachedImage(visImage, axesRenderer);

			// Blat the cached image
			System.out.println("Blitting cached world image");
			visImage.drawImage(this.cachedWorldImage, 0, 0);

			// Make the image fit into the axes centred around 0,0 long/lat
			final Point2d mid = axesRenderer.calculatePosition(0, 0);
			final Point2d dateLine0 = axesRenderer.calculatePosition(180, 0);
			final Point2d northPole = axesRenderer.calculatePosition(0, -90);
			final double scaleX = (dateLine0.getX() - mid.getX()) / 180d;
			final double scaleY = (northPole.getY() - mid.getY()) / 90d;
			Matrix trans = Matrix.identity(3, 3);
			trans = trans.times(
					TransformUtilities.scaleMatrixAboutPoint(
							scaleX, -scaleY, mid
							)
					);

			// Translate to 0,0
			trans = trans.times(
					TransformUtilities.translateMatrix(mid.getX(), mid.getY())
					);

			// Now draw the countries onto the sea. We transform each of the
			// shapes
			// by the above transform matrix prior to plotting them to the
			// image.
			final HashSet<String> k = new HashSet<String>(this.activeCountries);
			for (final String countryCode : k)
			{
				final WorldPlace wp = this.worldPolys.byCountryCode(countryCode);

				// Each place may have more than one polygon.
				final List<Shape> shapes = wp.getShapes();

				final MBFImageRenderer ir = visImage.createRenderer(RenderHints.ANTI_ALIASED);

				// For each of the polygons... draw them to the image.
				for (Shape s : shapes)
				{
					s = s.transform(trans);

					// Draw the country in the highlight colour
					final Float[] col = this.countryHighlightColours.get(wp.getISOA2());
					ir.drawShapeFilled(s, col == null ? this.highlightCountryLandColour : col);

					// Draw the outline shape of the country
					ir.drawShape(s, 1, this.defaultCountryOutlineColour);
				}
			}
		}
	}

	/**
	 * @return the seaColour
	 */
	public Float[] getSeaColour()
	{
		return this.seaColour;
	}

	/**
	 * @param seaColour
	 *            the seaColour to set
	 */
	public void setSeaColour(final Float[] seaColour)
	{
		this.seaColour = seaColour;
	}

	/**
	 * @return the defaultCountryOutlineColour
	 */
	public Float[] getDefaultCountryOutlineColour()
	{
		return this.defaultCountryOutlineColour;
	}

	/**
	 * @param defaultCountryOutlineColour
	 *            the defaultCountryOutlineColour to set
	 */
	public void setDefaultCountryOutlineColour(final Float[] defaultCountryOutlineColour)
	{
		this.defaultCountryOutlineColour = defaultCountryOutlineColour;
	}

	/**
	 * @return the defaultCountryLandColour
	 */
	public Float[] getDefaultCountryLandColour()
	{
		return this.defaultCountryLandColour;
	}

	/**
	 * @param defaultCountryLandColour
	 *            the defaultCountryLandColour to set
	 */
	public void setDefaultCountryLandColour(final Float[] defaultCountryLandColour)
	{
		this.defaultCountryLandColour = defaultCountryLandColour;
	}

	/**
	 * @return the highlightCountryLandColour
	 */
	public Float[] getHighlightCountryLandColour()
	{
		return this.highlightCountryLandColour;
	}

	/**
	 * @param highlightCountryLandColour
	 *            the highlightCountryLandColour to set
	 */
	public void setHighlightCountryLandColour(final Float[] highlightCountryLandColour)
	{
		this.highlightCountryLandColour = highlightCountryLandColour;
	}

	/**
	 * Returns a country code for a given country name.
	 * 
	 * @param countryName
	 *            The country name
	 * @return the country code
	 */
	public String getCountryCodeByName(final String countryName)
	{
		final WorldPlace p = this.worldPolys.byCountry(countryName);
		if (p == null)
			return null;
		return p.getISOA2();
	}

	/**
	 * Returns the lat/long of a country given its country code
	 * 
	 * @param countryCode
	 *            The country code
	 * @return The lat long as a point2d
	 */
	public Point2d getCountryLocation(final String countryCode)
	{
		final WorldPlace wp = this.worldPolys.byCountryCode(countryCode);
		if (wp == null)
			return null;
		return new Point2dImpl(wp.getLongitude(), wp.getLatitude());
	}

	@Override
	public void clearData()
	{
		this.activeCountries.clear();
		this.countryHighlightColours.clear();
		super.clearData();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.vis.AnimatedVisualisationProvider#addAnimatedVisualisationListener(org.openimaj.vis.AnimatedVisualisationListener)
	 */
	@Override
	public void addAnimatedVisualisationListener(final AnimatedVisualisationListener avl)
	{
		this.listeners.add(avl);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.vis.AnimatedVisualisationProvider#removeAnimatedVisualisationListener(org.openimaj.vis.AnimatedVisualisationListener)
	 */
	@Override
	public void removeAnimatedVisualisationListener(final AnimatedVisualisationListener avl)
	{
		this.listeners.remove(avl);
	}

	/**
	 * Fire the animation event
	 */
	protected void fireAnimationEvent()
	{
		for (final AnimatedVisualisationListener l : this.listeners)
			l.newVisualisationAvailable(this);
	}

	/**
	 * Animate the colour of a country
	 * 
	 * @param countryCode
	 *            The country to animate
	 * @param colourAnimator
	 *            The colour animator
	 */
	public void animateCountryColour(final String countryCode, final ValueAnimator<Float[]> colourAnimator)
	{
		this.colourAnimators.put(countryCode, colourAnimator);
		this.activeCountries.add(countryCode);
		if (this.animationThread == null)
		{
			this.animationThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					while (!WorldMap.this.colourAnimators.isEmpty())
					{
						// Update the colour for each of the countries in our
						// animators
						final Iterator<String> caIt = WorldMap.this.colourAnimators.keySet().iterator();
						while (caIt.hasNext())
						{
							final String cc = caIt.next();

							// Get the next colour for the country and update
							// its highlight colour
							final Float[] colour = WorldMap.this.colourAnimators.get(cc).nextValue();
							WorldMap.this.countryHighlightColours.put(cc, colour);

							// If the animator's finished. we'll remove it
							if (WorldMap.this.colourAnimators.get(cc).hasFinished())
							{
								caIt.remove();

								if (Arrays.equals(colour, WorldMap.this.getDefaultCountryLandColour()))
									WorldMap.this.activeCountries.remove(cc);
							}
						}

						// Fire the animation event
						WorldMap.this.fireAnimationEvent();
					}

					WorldMap.this.animationThread = null;
				}
			});
			this.animationThread.start();
		}
	}

	/**
	 * Demonstration method.
	 * 
	 * @param args
	 *            command-line args (unused)
	 */
	public static void main(final String[] args)
	{
		final WorldMap<LabelledDot> wp = new WorldMap<LabelledDot>(
				1200, 800, new LabelledPointVisualisation());

		// Show and highlight some stuff.
		wp.addPoint(-67.271667, -55.979722, new LabelledDot("Cape Horn", 1d, RGBColour.WHITE));
		wp.addPoint(-0.1275, 51.507222, new LabelledDot("London", 1d, RGBColour.WHITE));
		wp.addPoint(139.6917, 35.689506, new LabelledDot("Tokyo", 1d, RGBColour.WHITE));
		wp.addPoint(37.616667, 55.75, new LabelledDot("Moscow", 1d, RGBColour.WHITE));
		wp.addHighlightCountry("cn");
		wp.addHighlightCountry("us", new Float[] { 0f, 0.2f, 1f, 1f });
		wp.getAxesRenderer().setDrawMajorTickGrid(true);
		wp.showWindow("World");

		/*
		 * // Wait 3 seconds ... try { Thread.sleep( 3000 ); } catch( final
		 * InterruptedException e ) {}
		 * 
		 * // ... and flash Russia wp.animateCountryColour( "ru", new
		 * ColourSpaceAnimator( new Float[]{0.8f,1f,0.8f},
		 * wp.getDefaultCountryLandColour(), 5000 ) );
		 * 
		 * // Wait another 2 seconds... try { Thread.sleep( 2000 ); } catch(
		 * final InterruptedException e ) {}
		 * 
		 * // ... and flash Australia wp.animateCountryColour( "au", new
		 * ColourSpaceAnimator( new Float[]{1f,0.8f,0.8f},
		 * wp.getHighlightCountryLandColour(), 5000 ) );
		 */}
}
