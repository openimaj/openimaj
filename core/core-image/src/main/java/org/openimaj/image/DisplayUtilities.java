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
package org.openimaj.image;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.openimaj.image.DisplayUtilities.ImageComponent.ImageComponentListener;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processor.connectedcomponent.render.BlobRenderer;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Static methods for displaying images using Swing.
 *
 * In addition to normal windows, the class also supports "named windows" which
 * can be referred to by name.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class DisplayUtilities
{
	private static int windowCount = 0;

	private static int windowOpenCount = 0;

	private static Map<String, JFrame> namedWindows = new HashMap<String, JFrame>();

	/**
	 * Get the number of open windows
	 *
	 * @return number of open windows
	 */
	public static int openWindowCount()
	{
		return DisplayUtilities.windowOpenCount;
	}

	/**
	 * Display an image with the default name
	 *
	 * @param image
	 *            the image
	 * @return frame containing the image
	 */
	public static JFrame display(final Image<?, ?> image)
	{
		return DisplayUtilities.display(image, "Image: "
				+ DisplayUtilities.windowCount);
	}

	/**
	 * Display an image with the default name
	 *
	 * @param image
	 *            the image
	 * @return frame containing the image
	 */
	public static JFrame display(final BufferedImage image)
	{
		return DisplayUtilities.display(image, "Image: "
				+ DisplayUtilities.windowCount);
	}

	/**
	 * Display an image with the given title
	 *
	 * @param image
	 *            the image
	 * @param title
	 *            the title
	 * @return frame containing the image
	 */
	public static JFrame display(final Image<?, ?> image, final String title)
	{
		return DisplayUtilities.display(
				ImageUtilities.createBufferedImageForDisplay(image), title,
				image);
	}

	/**
	 * Display an image with the default name No additional functionality, such
	 * as zooming, is enabled.
	 *
	 * @param image
	 *            the image
	 * @return frame containing the image
	 */
	public static JFrame displaySimple(final Image<?, ?> image)
	{
		return DisplayUtilities.displaySimple(image, "Image: "
				+ DisplayUtilities.windowCount);
	}

	/**
	 * Display an image with the default name. No additional functionality, such
	 * as zooming, is enabled.
	 *
	 * @param image
	 *            the image
	 * @return frame containing the image
	 */
	public static JFrame displaySimple(final BufferedImage image)
	{
		return DisplayUtilities.displaySimple(image, "Image: "
				+ DisplayUtilities.windowCount);
	}

	/**
	 * Display an image with the given title. No additional functionality, such
	 * as zooming, is enabled.
	 *
	 * @param image
	 *            the image
	 * @param title
	 *            the title
	 * @return frame containing the image
	 */
	public static JFrame displaySimple(final Image<?, ?> image,
			final String title)
	{
		return DisplayUtilities.displaySimple(
				ImageUtilities.createBufferedImageForDisplay(image), title,
				image);
	}

	private static BufferedImage getImage(final JFrame frame)
	{
		if (frame == null)
			return null;

		if (frame.getContentPane().getComponentCount() > 0
				&& frame.getContentPane().getComponent(0) instanceof ImageComponent)
		{
			return ((ImageComponent) frame.getContentPane().getComponent(0)).image;
		}

		return null;
	}

	/**
	 * Display an image in the given frame
	 *
	 * @param image
	 *            the image
	 * @param frame
	 *            the frame
	 * @return the frame
	 */
	public static JFrame display(final Image<?, ?> image, final JFrame frame)
	{
		final BufferedImage bimg = DisplayUtilities.getImage(frame);
		return DisplayUtilities.display(
				ImageUtilities.createBufferedImageForDisplay(image, bimg),
				frame);
	}

	/**
	 * Set the position of a named window.
	 *
	 * @param name
	 *            The window name
	 * @param x
	 *            the x position
	 * @param y
	 *            the y position
	 */
	public static void positionNamed(final String name, final int x,
			final int y)
	{
		final JFrame w = DisplayUtilities.createNamedWindow(name);
		w.setBounds(x, y, w.getWidth(), w.getHeight());
	}

	/**
	 * Update the image that is being displayed in the given named window.
	 *
	 * @param name
	 *            The named window
	 * @param newImage
	 *            The new image to display
	 * @param title
	 *            The window title
	 */
	public static void updateNamed(final String name,
			final Image<?, ?> newImage, final String title)
	{
		final JFrame w = DisplayUtilities.createNamedWindow(name, title, true);
		final BufferedImage bimg = DisplayUtilities.getImage(w);

		((ImageComponent) w.getContentPane().getComponent(0))
				.setImage(ImageUtilities.createBufferedImageForDisplay(
						newImage, bimg));
	}

	/**
	 * Create a named window with a title that is also the name
	 *
	 * @param name
	 * @return the window
	 */
	public static JFrame createNamedWindow(final String name)
	{
		return DisplayUtilities.createNamedWindow(name, name, false);
	}

	/**
	 * Create a named window with a title
	 *
	 * @param name
	 * @param title
	 * @return the window
	 */
	public static JFrame createNamedWindow(final String name,
			final String title)
	{
		return DisplayUtilities.createNamedWindow(name, title, false);
	}

	/**
	 * Create a named window that auto resizes
	 *
	 * @param name
	 * @param title
	 * @param autoResize
	 * @return the window
	 */
	public static JFrame createNamedWindow(final String name,
			final String title, final boolean autoResize)
	{
		if (DisplayUtilities.namedWindows.containsKey(name))
			return DisplayUtilities.namedWindows.get(name);
		final JFrame frame = DisplayUtilities.makeDisplayFrame(title, 0, 0,
				null);
		((ImageComponent) frame.getContentPane().getComponent(0)).autoResize = autoResize;
		((ImageComponent) frame.getContentPane().getComponent(0)).autoPack = autoResize;
		DisplayUtilities.namedWindows.put(name, frame);
		return frame;
	}

	/**
	 * Display an image in the given frame by name (will be created if not
	 * already done so using {@link #createNamedWindow(String)}
	 *
	 * @param image
	 *            the image
	 * @param name
	 *            the name of the frame
	 * @return the frame
	 */
	public static JFrame displayName(final Image<?, ?> image, final String name)
	{
		final JFrame frame = DisplayUtilities.createNamedWindow(name);
		final BufferedImage bimg = DisplayUtilities.getImage(frame);
		return DisplayUtilities.display(
				ImageUtilities.createBufferedImageForDisplay(image, bimg),
				frame, image);
	}

	/**
	 * Display an image in the given frame by name (will be created if not
	 * already done so using {@link #createNamedWindow(String)}
	 *
	 * @param image
	 *            the image
	 * @param name
	 *            the name of the frame
	 * @param autoResize
	 *            should the frame resize to fit its contents
	 * @return the frame
	 */
	public static JFrame displayName(final Image<?, ?> image,
			final String name, final boolean autoResize)
	{
		final JFrame frame = DisplayUtilities.createNamedWindow(name, name,
				autoResize);
		final BufferedImage bimg = DisplayUtilities.getImage(frame);
		return DisplayUtilities.display(
				ImageUtilities.createBufferedImageForDisplay(image, bimg),
				frame, image);
	}

	/**
	 * An image viewer that displays and image and allows zooming and panning of
	 * images.
	 * <p>
	 * When allowZooming is TRUE, clicking in the image will zoom in. CTRL-click
	 * in the image to zoom out.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 */
	public static class ImageComponent extends JComponent implements
			MouseListener, MouseMotionListener
	{
		/**
		 * Listener for zoom and pan events
		 *
		 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
		 * @created 25 Jul 2012
		 * @version $Author$, $Revision$, $Date$
		 */
		public static interface ImageComponentListener
		{
			/**
			 * Called when the image has been zoomed to the new zoom factor.
			 *
			 * @param newScaleFactor
			 *            The new zoom factor
			 */
			public void imageZoomed(double newScaleFactor);

			/**
			 * Called when the image has been panned to a new position.
			 *
			 * @param newX
			 *            The new X position
			 * @param newY
			 *            The new Y position
			 */
			public void imagePanned(double newX, double newY);
		}

		/** */
		private static final long serialVersionUID = 1L;

		/** The image being displayed */
		protected BufferedImage image;

		/** The original image being displayed. Used for pixel interrogation */
		protected Image<?, ?> originalImage;

		/** Whether to auto resize the component to the content size */
		private boolean autoResize = false;

		/** Whether to pack the component on resize */
		private boolean autoPack = false;

		/** Whether to size the image to fit within the component's given size */
		private boolean autoFit = false;

		/** When using autoFit, whether to keep the aspect ratio constant */
		private boolean keepAspect = true;

		/** Draw a grid where there is no image */
		private boolean drawTransparencyGrid = false;

		/** Whether to draw the mouse over pixel colour on the next paint */
		private boolean drawPixelColour = false;

		/** Whether to show pixel colours on mouse over */
		private boolean showPixelColours = true;

		/** Whether to show the XY coordinate of the mouse */
		private boolean showXY = true;

		/** Whether to allow zooming */
		private boolean allowZooming = true;

		/** Whether to allow dragging */
		private boolean allowDragging = true;

		/** Gives the image-coord point in the centre of the image */
		private double drawX = 0;

		/** Gives the image-coord point in the centre of the image */
		private double drawY = 0;

		/** Gives the image scale */
		private double scaleFactorX = 1;

		/** Gives the image scale */
		private double scaleFactorY = 1;

		/** The last location of the drag - x-coordinate */
		private int dragStartX = 0;

		/** The last location of the drag - y-coordinate */
		private int dragStartY = 0;

		/** The x-coordinate of the pixel being displayed */
		private int pixelX = 0;

		/** The y-coordinate of the pixel being displayed */
		private int pixelY = 0;

		/** The current mouse coordinate */
		private int mouseX = 0;

		/** The current mouse coordinate */
		private int mouseY = 0;

		/** The current pixel colour */
		private Float[] currentPixelColour = null;

		/** List of listeners */
		private final ArrayList<ImageComponentListener> listeners =
				new ArrayList<ImageComponentListener>();

		/** The last displayed image */
		private BufferedImage displayedImage = null;

		/**
		 * Default constructor
		 */
		public ImageComponent()
		{
			this(false, false);
		}

		/**
		 * Default constructor. Allows setting of the autoResize parameter which
		 * if true changes the size of the component to fit the contents.
		 *
		 * @param autoResize
		 *            automatically resize the component to the content size
		 */
		public ImageComponent(final boolean autoResize)
		{
			this(autoResize, true);
		}

		/**
		 * Construct with given image
		 *
		 * @param image
		 *            the image
		 */
		public ImageComponent(final BufferedImage image)
		{
			this(true, true);
			this.setImage(image);
		}

		/**
		 * Default constructor. Allows setting of the autoResize parameter which
		 * if true changes the size of the component to fit the contents, and
		 * the autoPack parameter which automatically packs the containers root
		 * (if its a JFrame) whenever it is resized.
		 *
		 * @param autoResize
		 *            automatically resize the component to the content size
		 * @param autoPack
		 *            automatically pack the root component on resize
		 */
		public ImageComponent(final boolean autoResize, final boolean autoPack)
		{
			this(1f, autoResize, autoPack);
		}

		/**
		 * Default constructor. Allows setting of the autoResize parameter which
		 * if true changes the size of the component to fit the contents, and
		 * the autoPack parameter which automatically packs the containers root
		 * (if its a JFrame) whenever it is resized.
		 *
		 * @param initialScale
		 *            initial scale of the image
		 * @param autoResize
		 *            automatically resize the component to the content size
		 * @param autoPack
		 *            automatically pack the root component on resize
		 */
		public ImageComponent(final float initialScale,
				final boolean autoResize, final boolean autoPack)
		{
			this.autoPack = autoPack;
			this.autoResize = autoResize;
			this.scaleFactorX = initialScale;
			this.scaleFactorY = initialScale;

			this.addMouseListener(this);
			this.addMouseMotionListener(this);

			// Add a component listener so that we can detect when the
			// component has been resized so that we can update
			this.addComponentListener(new ComponentAdapter()
			{
				@Override
				public void componentResized(final ComponentEvent e)
				{
					ImageComponent.this.calculateScaleFactorsToFit(
							ImageComponent.this.image, ImageComponent.this.getBounds());
				};
			});
		}

		/**
		 * Add the given listener to this image component.
		 *
		 * @param l
		 *            The listener to add
		 */
		public void addImageComponentListener(final ImageComponentListener l)
		{
			this.listeners.add(l);
		}

		/**
		 * Remove the given listener from this image component.
		 *
		 * @param l
		 *            The listener to remove.
		 */
		public void removeImageComponentListener(final ImageComponentListener l)
		{
			this.listeners.remove(l);
		}

		/**
		 * Set whether to allow zooming.
		 *
		 * @param allowZoom
		 *            TRUE to allow zooming
		 */
		public void setAllowZoom(final boolean allowZoom)
		{
			this.allowZooming = allowZoom;
			if (allowZoom)
				this.autoFit = false;
		}

		/**
		 * Set whether to allow panning.
		 *
		 * @param allowPan
		 *            TRUE to allow panning
		 */
		public void setAllowPanning(final boolean allowPan)
		{
			this.allowDragging = allowPan;
			if (allowPan)
				this.autoFit = false;
		}

		/**
		 * Set whether to allow drawing of the transparency grid.
		 *
		 * @param drawGrid
		 *            TRUE draws the grid
		 */
		public void setTransparencyGrid(final boolean drawGrid)
		{
			this.drawTransparencyGrid = drawGrid;
			this.repaint();
		}

		/**
		 * Set whether to show pixel colours or not.
		 *
		 * @param showPixelColours
		 *            TRUE to show pixel colours
		 */
		public void setShowPixelColours(final boolean showPixelColours)
		{
			this.showPixelColours = showPixelColours;
			this.repaint();
		}

		/**
		 * Set whether to show the XY position of the mouse curson or not
		 *
		 * @param showXYPosition
		 *            TRUE to show XY position
		 */
		public void setShowXYPosition(final boolean showXYPosition)
		{
			this.showXY = showXYPosition;
			this.repaint();
		}

		/**
		 * Set the image to draw
		 *
		 * @param image
		 *            the image
		 */
		public void setImage(final BufferedImage image)
		{
			this.image = image;

			if (this.autoFit)
			{
				this.calculateScaleFactorsToFit(image, this.getBounds());
			}
			else if (this.autoResize)
			{
				// If the component isn't the right shape, we'll resize the
				// component.
				if (image.getWidth() != this.getWidth() ||
						image.getHeight() != this.getHeight())
				{
					this.setPreferredSize(new Dimension(
							(int) (image.getWidth() * this.scaleFactorX),
							(int) (image.getHeight() * this.scaleFactorY)));
					this.setSize(new Dimension(
							(int) (image.getWidth() * this.scaleFactorX),
							(int) (image.getHeight() * this.scaleFactorY)));
				}

				final Component c = SwingUtilities.getRoot(this);
				if (c == null)
					return;
				c.validate();

				if (c instanceof JFrame && this.autoPack)
				{
					final JFrame f = (JFrame) c;
					f.pack();
				}
			}

			if (this.showPixelColours)
				// This forces a repaint if showPixelColours is true
				this.updatePixelColours();
			else
				this.repaint();
		}

		/**
		 * Given an image, will calculate two scale factors for the X and Y
		 * dimensions of the image, such that the image will fit within the
		 * bounds.
		 *
		 * @param image
		 *            The image to fit
		 * @param bounds
		 *            The bounds to fit within
		 */
		private void calculateScaleFactorsToFit(final BufferedImage image,
				final java.awt.Rectangle bounds)
		{
			if (image == null || bounds == null)
				return;

			if (this.autoFit)
			{
				// If we can stretch the image it's pretty simple.
				if (!this.keepAspect)
				{
					this.scaleFactorX = bounds.width / (double) image.getWidth();
					this.scaleFactorY = bounds.height / (double) image.getHeight();
				}
				// Otherwise we need to find the ratios to fit while keeping
				// aspect
				else
				{
					this.scaleFactorX = this.scaleFactorY = Math.min(
							bounds.width / (double) image.getWidth(),
							bounds.height / (double) image.getHeight());
				}
			}
		}

		/**
		 * Move the image to the given position (image coordinates)
		 *
		 * @param x
		 *            The x image coordinate
		 * @param y
		 *            The y image coordinate
		 */
		public void moveTo(final double x, final double y)
		{
			if (this.drawX != x || this.drawY != y)
			{
				this.drawX = x;
				this.drawY = y;
				this.repaint();

				for (final ImageComponentListener l : this.listeners)
					l.imagePanned(x, y);
			}
		}

		/**
		 * Set the scale factor to zoom to
		 *
		 * @param sf
		 *            The scale factor
		 */
		public void zoom(final double sf)
		{
			this.scaleFactorX = this.scaleFactorY = sf;
			this.repaint();

			for (final ImageComponentListener l : this.listeners)
				l.imageZoomed(sf);
		}

		/**
		 * Set the scale factor to draw the image in the x-direction. Allows the
		 * image to be stretched or shrunk horizontally.
		 *
		 * @param sf
		 *            The new scale factor
		 */
		public void setScaleFactorX(final double sf)
		{
			this.scaleFactorX = sf;
		}

		/**
		 * Set the scale factor to draw the image in the y-direction. Allows the
		 * image to be stretched or shrunk vertically.
		 *
		 * @param sf
		 *            The new scale factor
		 */
		public void setScaleFactorY(final double sf)
		{
			this.scaleFactorY = sf;
		}

		/**
		 * Set the scale factor to draw the image. Allows the image to be
		 * stretched or shrunk both horizontall or vertically.
		 *
		 * @param sfx
		 *            The new x scale factor
		 * @param sfy
		 *            The new y scale factor
		 */
		public void setScaleFactor(final double sfx, final double sfy)
		{
			this.setScaleFactorX(sfx);
			this.setScaleFactorY(sfy);
		}

		/**
		 * If you want to be able to inspect the original image's pixel values
		 * (rather than the generated BufferedImage) set the original image
		 * here. Use null to enforce showing the BufferedImage pixel values.
		 * This does not set the BufferedImage that is being used for the
		 * display.
		 *
		 * @param image
		 *            The original image.
		 */
		public void setOriginalImage(final Image<?, ?> image)
		{
			this.originalImage = image;
		}

		/**
		 * Make sure the x and y position we're drawing the image in is not
		 * going mad.
		 */
		private void sanitiseVars()
		{
			// Make sure we're not going out of the space
			// this.moveTo(
			// Math.max(
			// this.image.getWidth() / this.scaleFactorX / 2,
			// Math.min(
			// this.drawX,
			// this.image.getWidth()
			// - (this.getWidth() / 2 / this.scaleFactorX) ) ),
			// Math.max( this.image.getHeight() / this.scaleFactorY / 2,
			// Math.min(
			// this.drawY,
			// this.image.getHeight()
			// - (this.getHeight() / 2 / this.scaleFactorY) ) ) );
		}

		/**
		 * {@inheritDoc}
		 *
		 * @see javax.swing.JComponent#paint(java.awt.Graphics)
		 */
		@Override
		public void paint(final Graphics gfx)
		{
			// Create a double buffer into which we'll draw first.
			final BufferedImage img = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
			final Graphics2D g = (Graphics2D) img.getGraphics();

			if (this.drawTransparencyGrid)
			{
				final BufferedImage transparencyGrid = new BufferedImage(
						this.getWidth(), this.getHeight(),
						BufferedImage.TYPE_3BYTE_BGR);
				final Graphics tg = transparencyGrid.getGraphics();

				final int gridSizeX = (int) (20 * this.scaleFactorX);
				final int gridSizeY = (int) (20 * this.scaleFactorY);
				for (int y = 0; y < this.getHeight(); y += gridSizeY)
				{
					for (int x = 0; x < this.getWidth(); x += gridSizeX)
					{
						final int c = (x / gridSizeX + y / gridSizeY) % 2;
						if (c == 0)
							tg.setColor(new Color(220, 220, 220));
						else
							tg.setColor(Color.white);

						tg.fillRect(x, y, gridSizeX, gridSizeY);
					}
				}

				g.drawImage(transparencyGrid, 0, 0, null);
			}

			// Draw the image
			if (this.image != null)
			{
				// Scale and translate to the image drawing coordinates
				g.scale(this.scaleFactorX, this.scaleFactorY);
				g.translate(-this.drawX, -this.drawY);

				// Blat the image to the screen
				g.drawImage(this.image, 0, 0, this.image.getWidth(),
						this.image.getHeight(), null);

				// Reset the graphics back to the original pixel-based coords
				g.translate(this.drawX, this.drawY);
				g.scale(1 / this.scaleFactorX, 1 / this.scaleFactorY);

				// If we're to show pixel colours and we're supposed to do it
				// on this time around...
				if ((this.showPixelColours || this.showXY)
						&& this.drawPixelColour)
				{
					final StringBuffer pixelColourStrB = new StringBuffer();

					if (this.showXY)
						pixelColourStrB.append("[" + this.pixelX + ","
								+ this.pixelY + "] ");

					if (this.showPixelColours)
						pixelColourStrB.append(Arrays
								.toString(this.currentPixelColour));

					// Calculate the size to draw
					final FontMetrics fm = g.getFontMetrics();
					final int fw = fm.stringWidth(pixelColourStrB.toString());
					final int fh = fm.getHeight() + fm.getDescent();
					final int p = 4; // padding
					final int dx = 0;
					int dy = this.getHeight() - (fh + p);

					// If the mouse is over where we want to put the box,
					// we'll move the box to another corner
					if (this.mouseX <= dx + fw + p && this.mouseX >= dx &&
							this.mouseY >= dy && this.mouseY <= dy + fh + p)
						dy = 0;

					// Draw a box
					g.setColor(new Color(0, 0, 0, 0.5f));
					g.fillRect(dx, dy, fw + p, fh + p);

					// Draw the text
					g.setColor(Color.white);
					g.drawString(pixelColourStrB.toString(), dx + p / 2, dy
							+ fm.getHeight() + p / 2);
				}
			}

			// Blat our offscreen image to the screen
			gfx.drawImage(img, 0, 0, null);

			// Store this displayed image
			this.displayedImage = img;
		}

		/**
		 * {@inheritDoc}
		 *
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(final MouseEvent e)
		{
			if (e.getButton() == MouseEvent.BUTTON1 && this.allowZooming)
			{
				if (e.isControlDown())
				{
					// Scale the scalars down
					this.scaleFactorX /= 2;
					this.scaleFactorY /= 2;

					final double moveX = this.drawX - e.getX() / this.scaleFactorX / 2;
					final double moveY = this.drawY - e.getY() / this.scaleFactorY / 2;
					if (this.allowDragging)
						this.moveTo(moveX, moveY);
					else
						this.moveTo(0, 0);
				}
				else
				{
					// Scale the scalars up
					this.scaleFactorX *= 2;
					this.scaleFactorY *= 2;

					// Make sure we zoom in on the bit the user clicked on
					if (this.allowDragging)
						this.moveTo(
								this.drawX + e.getX() / this.scaleFactorX,
								this.drawY + e.getY() / this.scaleFactorY);
					else
						this.moveTo(0, 0);
				}

				// Make sure we're not going to draw out of bounds.
				this.sanitiseVars();

				this.repaint();
			}
		}

		@Override
		public void mousePressed(final MouseEvent e)
		{
			if (this.allowDragging)
			{
				this.dragStartX = e.getX();
				this.dragStartY = e.getY();
			}
		}

		@Override
		public void mouseReleased(final MouseEvent e)
		{
		}

		@Override
		public void mouseEntered(final MouseEvent e)
		{
		}

		@Override
		public void mouseExited(final MouseEvent e)
		{
			this.drawPixelColour = false;
			this.repaint();
		}

		@Override
		public void mouseDragged(final MouseEvent e)
		{
			if (!this.allowDragging)
				return;

			final int diffx = e.getX() - this.dragStartX;
			final int diffy = e.getY() - this.dragStartY;

			if (diffx == 0 && diffy == 0)
				return;

			// Update the draw position
			this.moveTo(this.drawX - diffx / this.scaleFactorX,
					this.drawY - diffy / this.scaleFactorY);

			// Reset the draggers
			this.dragStartX = e.getX();
			this.dragStartY = e.getY();

			// Make sure the drag stays within the bounds
			this.sanitiseVars();

			// Redraw the component
			this.repaint();
		}

		@Override
		public void mouseMoved(final MouseEvent e)
		{
			if (this.image == null)
				return;

			// Convert the screen coords into image coords
			final double x = e.getX() / this.scaleFactorX + this.drawX;
			final double y = e.getY() / this.scaleFactorY + this.drawY;

			// If we're outside the image we don't print anything
			if (x >= this.image.getWidth() || y >= this.image.getHeight() ||
					x < 0 || y < 0)
			{
				this.drawPixelColour = false;
				this.repaint();
				return;
			}

			// Pixel coordinates in the image
			this.pixelX = (int) x;
			this.pixelY = (int) y;

			this.mouseX = e.getX();
			this.mouseY = e.getY();

			this.updatePixelColours();
		}

		/**
		 * Update the display of pixel colours
		 */
		protected void updatePixelColours()
		{
			if (this.showPixelColours && this.image != null)
			{
				// If we don't have the original image, we'll just use the
				// colours from the BufferedImage
				if (this.originalImage == null)
				{
					final int colour = this.image.getRGB(this.pixelX, this.pixelY);
					this.currentPixelColour = new Float[3];
					this.currentPixelColour[0] = (float) ((colour & 0x00ff0000) >> 16);
					this.currentPixelColour[1] = (float) ((colour & 0x0000ff00) >> 8);
					this.currentPixelColour[2] = (float) ((colour & 0x000000ff));
				}
				else
				{
					// If we're outside of the original image's coordinates,
					// we don't need to do anything else..
					if (this.pixelX >= this.originalImage.getWidth() || this.pixelX < 0 ||
							this.pixelY >= this.originalImage.getHeight() || this.pixelY < 0)
						return;

					// If we have the original image we get each of the bands
					// from it and update the current pixel colour member
					if (this.originalImage instanceof FImage)
					{
						final Object o = this.originalImage.getPixel(this.pixelX, this.pixelY);
						this.currentPixelColour = new Float[1];
						this.currentPixelColour[0] = (Float) o;
					}
					else if (this.originalImage instanceof MBFImage)
					{
						final MBFImage i = (MBFImage) this.originalImage;
						this.currentPixelColour = new Float[i.numBands()];
						for (int b = 0; b < i.numBands(); b++)
							this.currentPixelColour[b] = i.getBand(b)
									.getPixel(this.pixelX, this.pixelY);
					}
				}

				this.drawPixelColour = true;
				this.repaint();
			}

			if (this.showXY)
			{
				this.drawPixelColour = true;
				this.repaint();
			}
		}

		/**
		 * Sets whether to automatically size the image to fit within the bounds
		 * of the image component which is being sized externally. This
		 * shouldn't be used in combination with autoResize. When this method is
		 * called with TRUE, zooming and dragging are disabled.
		 *
		 * @param tf
		 *            TRUE to auto fit the image.
		 */
		public void setAutoFit(final boolean tf)
		{
			this.autoFit = tf;
			if (this.autoFit)
			{
				this.allowZooming = false;
				this.allowDragging = false;
			}
		}

		/**
		 * Sets whether to keep the aspect ratio of the image constant when the
		 * image is being autoFit into the component.
		 *
		 * @param tf
		 *            TRUE to keep the aspect ratio constant
		 */
		public void setKeepAspect(final boolean tf)
		{
			this.keepAspect = tf;
		}

		/**
		 * Sets whether to automatically resize the component to fit image (at
		 * it's given scale factor) within it. Note that in certain
		 * circumstances, where the image component is being sized by external
		 * forces (such as a layout manager), setting this to true can cause
		 * weird results where the image is pulled out and in constantly. This
		 * shouldn't be used in combination with autoFit.
		 *
		 * @param tf
		 *            TRUE to resize the component.
		 */
		public void setAutoResize(final boolean tf)
		{
			this.autoResize = tf;
		}

		/**
		 * Sets whether the component is to attempt to pack a frame into which
		 * it is added. If it is not in a frame this will have no effect. This
		 * allows the frame to resize with the component.
		 *
		 * @param tf
		 *            TRUE to auto pack the parent frame.
		 */
		public void setAutoPack(final boolean tf)
		{
			this.autoPack = tf;
		}

		/**
		 * Returns the current mouse position in pixels within the viewport.
		 * Will return the last known position if the mouse is no longer within
		 * the viewport.
		 *
		 * @return The position in pixels
		 */
		public Point2d getCurrentMousePosition()
		{
			return new Point2dImpl(this.mouseX, this.mouseY);
		}

		/**
		 * Returns the current mouse position in the coordinates of the image
		 * and is determined by the scaling factors and the position of the
		 * image within the viewport. If the mouse is no longer in the viewport,
		 * the last known mouse position will be returned.
		 *
		 * @return The position in image coordinates.
		 */
		public Point2d getCurrentMouseImagePosition()
		{
			return new Point2dImpl(this.pixelX, this.pixelY);
		}

		/**
		 * Returns the current pixel colour at the point of the mouse. The
		 * number of elements in the array will equal be 3, if no original has
		 * been supplied to the image component. The values will be between 0
		 * and 255 and ordered red, green and blue. If the original has been
		 * supplied, then the number of elements will be equal to the number of
		 * bands in the original image and the values will be the original pixel
		 * values in the original image.
		 *
		 * @return The current pixel colour.
		 */
		public Float[] getCurrentPixelColour()
		{
			return this.currentPixelColour;
		}

		/**
		 * Returns the current displayed pixel colour (as an RGB encoded int)
		 * from the currently displayed image.
		 *
		 * @return The current displayed pixel colour.
		 */
		public int getCurrentDisplayedPixelColour()
		{
			return this.displayedImage.getRGB(this.mouseX, this.mouseY);
		}

		/**
		 * Returns the currently displaying image.
		 *
		 * @return The displayed image.
		 */
		public BufferedImage getDisplayedImage()
		{
			return this.displayedImage;
		}
	}

	/**
	 * An extension of {@link ImageComponent} that scales the displayed image.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public static class ScalingImageComponent extends ImageComponent
	{
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private boolean hq = false;

		/**
		 * Construct the ScalingImageComponent with fast scaling enabled
		 */
		public ScalingImageComponent() {
		}

		/**
		 * Construct the ScalingImageComponent, choosing between fast scaling or
		 * high quality scaling
		 *
		 * @param hq
		 *            true if high quality scaling is required.
		 */
		public ScalingImageComponent(boolean hq) {
			this.hq = hq;
		}

		@Override
		public void paint(final Graphics g)
		{
			if (hq)
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING,
						RenderingHints.VALUE_RENDER_QUALITY);

			final Component f = SwingUtilities.getRoot(this);
			if (this.image != null)
				g.drawImage(this.image, 0, 0, this.getWidth(),
						this.getHeight(), f);
		}
	}

	/**
	 * Display an image in the given frame
	 *
	 * @param image
	 *            the image
	 * @param frame
	 *            the frame
	 * @return the frame
	 */
	public static JFrame display(final BufferedImage image, final JFrame frame)
	{
		return DisplayUtilities.display(image, frame, null);
	}

	/**
	 * Displays an image in the given named window
	 *
	 * @param image
	 *            The image
	 * @param name
	 *            The name of the window
	 * @return The frame that was created.
	 */
	public static JFrame displayName(final BufferedImage image, final String name)
	{
		final JFrame f = DisplayUtilities.createNamedWindow(name);
		return DisplayUtilities.display(image, f);

	}

	/**
	 * Display an image in the given frame
	 *
	 * @param image
	 *            the image
	 * @param frame
	 *            the frame
	 * @param originalImage
	 *            the original image
	 * @return the frame
	 */
	public static JFrame display(final BufferedImage image,
			final JFrame frame, final Image<?, ?> originalImage)
	{
		if (frame == null)
			return DisplayUtilities.makeDisplayFrame("Image: "
					+ DisplayUtilities.windowCount, image.getWidth(),
					image.getHeight(), image);

		if (frame.getContentPane().getComponentCount() > 0
				&& frame.getContentPane().getComponent(0) instanceof ImageComponent)
		{
			final ImageComponent cmp = ((ImageComponent) frame.getContentPane()
					.getComponent(0));
			if (!frame.isVisible())
			{
				final boolean ar = cmp.autoResize;
				final boolean ap = cmp.autoPack;
				cmp.autoResize = true;
				cmp.autoPack = true;
				cmp.setImage(image);
				cmp.setOriginalImage(originalImage);
				cmp.autoResize = ar;
				cmp.autoPack = ap;
				frame.setVisible(true);
			}
			else
			{
				cmp.setImage(image);
				cmp.setOriginalImage(originalImage);
			}
		}
		else
		{
			frame.getContentPane().removeAll();

			final ImageComponent c = new ImageComponent(image);
			c.setOriginalImage(originalImage);

			frame.add(c);
			frame.pack();
			frame.setVisible(true);
		}
		return frame;
	}

	/**
	 * Make a frame with the given title.
	 *
	 * @param title
	 *            the title
	 * @return the frame
	 */
	public static JFrame makeFrame(final String title)
	{
		final JFrame f = new JFrame(title);
		f.setResizable(false);
		f.setUndecorated(false);

		f.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(final WindowEvent evt)
			{
				DisplayUtilities.windowOpenCount = DisplayUtilities.windowCount - 1;
				f.dispose();
			}
		});
		return f;
	}

	/**
	 * Display an image with the given title. No additional functionality, such
	 * as zooming, is enabled.
	 *
	 * @param image
	 *            the image
	 * @param title
	 *            the title
	 * @return frame containing the image
	 */
	public static JFrame displaySimple(final BufferedImage image,
			final String title)
	{
		return DisplayUtilities.displaySimple(image, title, null);
	}

	/**
	 * Display an image with the given title
	 *
	 * @param image
	 *            the image
	 * @param title
	 *            the title
	 * @return frame containing the image
	 */
	public static JFrame display(final BufferedImage image, final String title)
	{
		return DisplayUtilities.display(image, title, null);
	}

	/**
	 * Display an image with the given title. No additional functionality, such
	 * as zooming, is enabled.
	 *
	 * @param image
	 *            the image
	 * @param title
	 *            the title
	 * @param originalImage
	 *            original image
	 * @return frame containing the image
	 */
	public static JFrame displaySimple(final BufferedImage image,
			final String title, final Image<?, ?> originalImage)
	{
		if (GraphicsEnvironment.isHeadless())
			return null;

		return DisplayUtilities.makeDisplayFrameSimple(title,
				image.getWidth(), image.getHeight(), image, originalImage);
	}

	/**
	 * Get a frame that will display an image. No additional functionality, such
	 * as zooming, is enabled.
	 *
	 * @param title
	 *            the frame title
	 * @param width
	 *            the frame width
	 * @param height
	 *            the frame height
	 * @param img
	 *            the image to display
	 * @param originalImage
	 *            the original image
	 * @return A {@link JFrame} that allows images to be displayed.
	 */
	public static JFrame makeDisplayFrameSimple(final String title,
			final int width, final int height, final BufferedImage img,
			final Image<?, ?> originalImage)
	{
		final JFrame f = DisplayUtilities.makeFrame(title);

		final ImageComponent c = new ImageComponent();
		if (img != null)
			c.setImage(img);
		c.setOriginalImage(originalImage);
		c.setSize(width, height);
		c.setPreferredSize(new Dimension(c.getWidth(), c.getHeight()));

		c.removeMouseListener(c);
		c.removeMouseMotionListener(c);
		c.setShowPixelColours(false);
		c.setShowXYPosition(false);
		c.setAllowZoom(false);
		c.setAutoscrolls(false);
		c.setAllowPanning(false);

		f.add(c);
		f.pack();
		f.setVisible(img != null);

		DisplayUtilities.windowCount++;

		return f;
	}

	/**
	 * Display an image with the given title
	 *
	 * @param image
	 *            the image
	 * @param title
	 *            the title
	 * @param originalImage
	 *            original image
	 * @return frame containing the image
	 */
	public static JFrame display(final BufferedImage image,
			final String title, final Image<?, ?> originalImage)
	{
		if (GraphicsEnvironment.isHeadless())
			return null;

		return DisplayUtilities.makeDisplayFrame(title, image.getWidth(),
				image.getHeight(), image, originalImage);
	}

	/**
	 * Get a frame that will display an image.
	 *
	 * @param title
	 *            the frame title
	 * @param width
	 *            the frame width
	 * @param height
	 *            the frame height
	 * @return A {@link JFrame} that allows images to be displayed.
	 */
	public static JFrame makeDisplayFrame(final String title, final int width,
			final int height)
	{
		return DisplayUtilities.makeDisplayFrame(title, width, height, null);
	}

	/**
	 * Get a frame that will display an image.
	 *
	 * @param title
	 *            the frame title
	 * @param width
	 *            the frame width
	 * @param height
	 *            the frame height
	 * @param img
	 *            the image to display
	 * @return A {@link JFrame} that allows images to be displayed.
	 */
	public static JFrame makeDisplayFrame(final String title, final int width,
			final int height, final BufferedImage img)
	{
		return DisplayUtilities.makeDisplayFrame(title, width, height, img,
				null);
	}

	/**
	 * Get a frame that will display an image.
	 *
	 * @param title
	 *            the frame title
	 * @param width
	 *            the frame width
	 * @param height
	 *            the frame height
	 * @param img
	 *            the image to display
	 * @param originalImage
	 *            the original image
	 * @return A {@link JFrame} that allows images to be displayed.
	 */
	public static JFrame makeDisplayFrame(final String title, final int width,
			final int height, final BufferedImage img,
			final Image<?, ?> originalImage)
	{
		final JFrame f = DisplayUtilities.makeFrame(title);

		final ImageComponent c = new ImageComponent();
		if (img != null)
			c.setImage(img);
		c.setOriginalImage(originalImage);
		c.setSize(width, height);
		c.setPreferredSize(new Dimension(c.getWidth(), c.getHeight()));

		f.add(c);
		f.pack();
		f.setVisible(img != null);

		DisplayUtilities.windowCount++;

		return f;
	}

	/**
	 * Render a connected component and display it
	 *
	 * @param input
	 *            the connected component
	 * @return frame containing the rendered image
	 */
	public static JFrame display(final ConnectedComponent input)
	{
		return DisplayUtilities.display(input, 1.0f);
	}

	/**
	 * Render a connected component with a given grey level and display it
	 *
	 * @param input
	 *            the connected component
	 * @param col
	 *            the grey level
	 * @return frame containing the rendered image
	 */
	public static JFrame display(final ConnectedComponent input,
			final float col)
	{
		final ConnectedComponent cc = input.clone();

		final Rectangle bb = cc.calculateRegularBoundingBox();

		// Render the mask, leaving a 10 px border
		cc.translate(10 - (int) bb.x, 10 - (int) bb.y);
		final FImage mask = new FImage((int) Math.max(bb.width + 20, 100),
				(int) Math.max(bb.height + 20, 100));
		final BlobRenderer<Float> br = new BlobRenderer<Float>(mask, 1.0F);
		cc.process(br);

		return DisplayUtilities.display(mask);
	}

	/**
	 * Render a polygon to an image and display it.
	 *
	 * @param input
	 *            the polygon
	 * @return the frame
	 */
	public static JFrame display(final Polygon input)
	{
		return DisplayUtilities.display(input, 1.0f);
	}

	/**
	 * Render a polygon with a given grey level and display it
	 *
	 * @param input
	 *            the polygon
	 * @param col
	 *            the grey level
	 * @return frame containing the rendered image
	 */
	public static JFrame display(final Polygon input, final float col)
	{
		final Polygon p = input.clone();

		final Rectangle bb = p.calculateRegularBoundingBox();

		// Render the mask, leaving a 1 px border
		p.translate(10 - bb.x, 10 - bb.y);
		final FImage mask = new FImage((int) (bb.width + 20),
				(int) (bb.height + 20));
		mask.createRenderer().drawPolygon(p, col);

		return DisplayUtilities.display(mask);
	}

	/**
	 * Display multiple images in an array
	 *
	 * @param title
	 *            the frame title
	 * @param images
	 *            the images
	 * @return the frame
	 */
	public static JFrame display(final String title,
			final Image<?, ?>... images)
	{
		final BufferedImage[] bimages = new BufferedImage[images.length];

		for (int i = 0; i < images.length; i++)
			bimages[i] = ImageUtilities
					.createBufferedImageForDisplay(images[i]);

		return DisplayUtilities.display(title, bimages);
	}

	/**
	 * Display multiple images in a collection
	 *
	 * @param title
	 *            the frame title
	 * @param images
	 *            the images
	 * @return the frame
	 */
	public static JFrame display(final String title,
			final Collection<? extends Image<?, ?>> images)
	{
		final BufferedImage[] bimages = new BufferedImage[images.size()];

		int i = 0;
		for (final Image<?, ?> img : images)
			bimages[i++] = ImageUtilities
					.createBufferedImageForDisplay(img);

		return DisplayUtilities.display(title, bimages);
	}

	/**
	 * Display multiple images in an array
	 *
	 * @param title
	 *            the frame title
	 * @param cols
	 *            number of columns
	 * @param images
	 *            the images
	 * @return the frame
	 */
	public static JFrame display(final String title, final int cols,
			final Image<?, ?>... images)
	{
		final JFrame f = new JFrame(title);

		f.getContentPane().setLayout(new GridLayout(0, cols));

		for (final Image<?, ?> image : images)
		{
			if (image != null)
			{
				final ImageComponent ic = new ImageComponent(
						ImageUtilities.createBufferedImageForDisplay(image));
				ic.setOriginalImage(image);
				f.getContentPane().add(ic);
			}
		}

		f.pack();
		f.setVisible(true);

		return f;
	}

	/**
	 * Display multiple images in an array
	 *
	 * @param title
	 *            the frame title
	 * @param cols
	 *            number of columns
	 * @param images
	 *            the images
	 * @return the frame
	 */
	public static JFrame displayLinked(final String title, final int cols,
			final Image<?, ?>... images)
	{
		final JFrame f = new JFrame(title);

		f.getContentPane().setLayout(new GridLayout(0, cols));

		ImageComponent ic = null;
		for (final Image<?, ?> image : images)
		{
			if (image != null)
			{
				final ImageComponent ic2 = new ImageComponent(
						ImageUtilities.createBufferedImageForDisplay(image));

				if (ic != null)
				{
					ic.addImageComponentListener(new ImageComponentListener()
					{
						@Override
						public void imageZoomed(final double newScaleFactor)
						{
							ic2.zoom(newScaleFactor);
						}

						@Override
						public void imagePanned(final double newX,
								final double newY)
						{
							ic2.moveTo(newX, newY);
						}
					});
				}

				ic2.setOriginalImage(image);
				f.getContentPane().add(ic2);

				ic = ic2;
			}
		}

		f.pack();
		f.setVisible(true);

		return f;
	}

	/**
	 * Display multiple images in an array of frames
	 *
	 * @param title
	 *            the frame title
	 * @param images
	 *            the images
	 * @return the frame
	 */
	public static JFrame display(final String title,
			final BufferedImage... images)
	{
		if (GraphicsEnvironment.isHeadless())
			return null;

		final JFrame f = new JFrame(title);

		final int box_size = 200;
		final int n_images = images.length;
		final int n_boxes_x = 4;
		final int width = n_boxes_x * box_size;
		final int height = box_size * n_images / n_boxes_x;

		f.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(final WindowEvent evt)
			{
				DisplayUtilities.windowOpenCount = DisplayUtilities.windowCount - 1;
				f.dispose();
			}
		});

		final Container scrollContainer = new Container();
		scrollContainer.setLayout(new FlowLayout());

		final Container container = new Container();
		container.setSize(new Dimension(width, height));
		container.setPreferredSize(new Dimension(width, height));
		container.setLayout(new GridLayout(0, n_boxes_x));
		scrollContainer.add(container);

		for (final BufferedImage img : images)
		{
			final JComponent c = new JComponent()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void paint(final Graphics g)
				{
					final int cw = this.getWidth();
					final int ch = this.getHeight();
					if (img.getWidth() < cw && img.getHeight() < ch)
					{
						final int x = (cw - img.getWidth()) / 2;
						final int y = (ch - img.getHeight()) / 2;
						g.drawImage(img, x, y, img.getWidth(),
								img.getHeight(), f);
					}
					else if (img.getWidth() > img.getHeight())
					{
						final float sf = (float) cw / (float) img.getWidth();
						final int h = Math.round(sf * img.getHeight());
						g.drawImage(img, 0, (ch - h) / 2, cw, h, f);
					}
					else
					{
						final float sf = (float) ch / (float) img.getHeight();
						final int w = Math.round(sf * img.getWidth());
						g.drawImage(img, (cw - w) / 2, 0, w, ch, f);
					}
					// TODO: scale image proportionally and draw centered

				}
			};
			c.setSize(200, 200);
			c.setPreferredSize(new Dimension(c.getWidth(), c.getHeight()));
			container.add(c);
		}
		f.setSize(new Dimension(840, 600));
		f.setPreferredSize(new Dimension(840, 600));

		f.getContentPane().add(new JScrollPane(scrollContainer));

		f.pack();
		f.setVisible(true);

		DisplayUtilities.windowCount++;

		return f;
	}

}
