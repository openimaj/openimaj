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
package org.openimaj.vis;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

/**
 *	A top level class for visualisations. This class handles the creation
 *	of an image (MBFImage) for drawing the visualisation into. It also makes
 *	the visualisation available as a Swing JPanel and, using {@link #showWindow(String)},
 *	allows the visualisation to be displayed in a window.
 *	<p>
 *	This class is abstract - it does not know how to paint the data on which it
 *	is typed. It is therefore necessary to subclass this class with specific
 *	implementations of visualisations for specific types.  To do this, override
 *	the {@link #update()} method in this class to draw the visualisation into
 *	the <code>visImage</code> member. The implementation of the {@link #update()}
 *	method may call {@link #repaint()} if it so wishes to update any displays
 *	of the data. This is called automatically when {@link #setData(Object)} is
 *	called to update the data object.
 *	<p>
 *	The class also allows for linking visualisations so that one visualisation
 *	can draw on top of another visualisation's output. However, the visualisations
 *	do not affect each other's visualisation images.  If you call the constructor
 *	that takes a visualisation, this visualisation will become an overlay of
 *	the given visualisation. Each time the underlying visualisation is updated,
 *	a copy of the visualisation is stored in this visualisation for drawing as
 *	a background.  This means that this visualisation can update more often than
 *	the underlying visualisation, however, it can be slow if the underlying
 *	visualisation updates often because it's making a copy of the image.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 27 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 * 	@param <T> The type of the data to be visualised
 */
public abstract class VisualisationImpl<T> extends JPanel
	implements ComponentListener, Visualisation<T>, AnimatedVisualisationListener
{
	/** */
	private static final long serialVersionUID = 1L;

	/** The visualisation image */
	protected MBFImage visImage = null;

	/** The double buffer image */
	private MBFImage dbImage = null;

	/** The data to be visualised */
	protected T data = null;

	/** Whether to allow resizing of the visualisation */
	private boolean allowResize = true;

	/** The overlay image */
	private MBFImage overlayImage = null;

	/** The list of visualisations that wish to overlay on this vis */
	private final List<VisualisationImpl<?>> overlays = new ArrayList<VisualisationImpl<?>>();

	/** Whether to clear the image before redrawing */
	protected boolean clearBeforeDraw = true;

	/** The background colour to clear the image to */
	private final Float[] backgroundColour = new Float[]{0f,0f,0f,1f};

	/** The visualisation that this vis is being overlaid on */
	private VisualisationImpl<?> overlaidOn = null;

	/** The required size of the vis */
	private Dimension requiredSize = new Dimension( 400, 400 );

	/**
	 * 	Default constructor
	 */
	protected VisualisationImpl() {}

	/**
	 * 	Create a new visualisation with the given width and height
	 *	@param width The width
	 *	@param height The height
	 */
	public VisualisationImpl( final int width, final int height )
	{
		this.visImage = new MBFImage( width, height, 4 );
		this.dbImage = this.visImage.clone();
		this.setPreferredSize( new Dimension( width, height ) );
		this.setSize( new Dimension( width, height ) );
		this.requiredSize = new Dimension( width, height );
	}

	/**
	 * 	Create a new visualisation using an existing image.
	 *	@param overlayOn The visualisation on which to overlay
	 */
	public VisualisationImpl( final VisualisationImpl<?> overlayOn )
	{
		this.overlaidOn = overlayOn;

		// Create an image the same size as the overlay vis
		final MBFImage vi = overlayOn.getVisualisationImage();
		this.visImage = new MBFImage( vi.getWidth(), vi.getHeight(), 4 );
		this.dbImage = this.visImage.clone();
		this.setPreferredSize( new Dimension( vi.getWidth(), vi.getHeight() ) );
		this.setSize( new Dimension( vi.getWidth(), vi.getHeight() ) );
		this.requiredSize = this.getSize();

		// Add this as an overlay on the other vis. This also forces
		// an update so that we get their visualisation to overlay on
		overlayOn.addOverlay( this );
		this.addComponentListener( this );
	}

	/**
	 * 	Add an overlay to this visualisation
	 *	@param v The visualisation to overlay on this visualisation
	 */
	public void addOverlay( final VisualisationImpl<?> v )
	{
		this.overlays.add( v );
		v.updateVis( this.visImage );
	}

	/**
	 * 	Remove the given overlay from this visualisation
	 *	@param v The visualisation to remove
	 */
	public void removeOverlay( final VisualisationImpl<?> v )
	{
		this.overlays.remove( v );
	}

	/**
	 * 	Called to update the visualisation. This method can expect the
	 * 	<code>visImage</code> member to be available and of the correct size.
	 * 	The method simply needs to draw the visualisation to this {@link MBFImage}.
	 * 	You should wrap any drawing code in a synchronized block, synchronized on
	 * 	the visImage - this stops the image being repainted to the screen half way
	 * 	through drawing.
	 * 	<p>
	 *  Update is called from the paint() method so should
	 * 	ideally not force a repaint() as this will call a continuous repaint
	 * 	loop.
	 */
	public abstract void update();

	/**
	 * 	Call to force and update of the visualisation
	 */
	@Override
	public void updateVis()
	{
		if( this.visImage == null )
		{
			this.visImage = new MBFImage( (int)this.requiredSize.getWidth(), (int)this.requiredSize.getHeight(), 4 );
			this.dbImage = this.visImage.clone();
		}

//		System.out.println( "Updating "+this );
		synchronized( this.visImage )
		{
			if( this.allowResize && (this.visImage.getWidth() != this.requiredSize.getWidth() ||
					 this.visImage.getHeight() != this.requiredSize.getHeight()) )
			{
				this.visImage = new MBFImage( (int)this.requiredSize.getWidth(), (int)this.requiredSize.getHeight(), 4 );
				this.dbImage = this.visImage.clone();
				System.out.println( this.requiredSize );
			}

			if( this.clearBeforeDraw )
				this.visImage.fill( this.backgroundColour );

			if( this.overlayImage != null )
				this.visImage.drawImage( this.overlayImage, 0, 0 );
		}

		this.update();

		synchronized( this.visImage ) {
			for( final VisualisationImpl<?> v : this.overlays )
				v.updateVis( this.visImage );

			final MBFImage t = this.dbImage;
			this.dbImage = this.visImage;
			this.visImage = t;
		}

		this.repaint();
	}

	/**
	 * 	Update the visualisation using the given image as the base image
	 * 	on which to overlay.
	 *	@param overlay The overlay
	 */
	public void updateVis( final MBFImage overlay )
	{
		if( overlay != null )
				this.overlayImage = overlay.clone();
		else	this.overlayImage = null;
		this.updateVis();
	}

	/**
	 * 	Set the data to be visualised.
	 *	@param data The data to be visualised
	 */
	@Override
	public void setData( final T data )
	{
		synchronized( data )
		{
			this.data = data;
		}

		this.updateVis();
	}

	/**
	 * 	Returns the image to which the bars will be drawn.
	 *	@return The image
	 */
	@Override
	public MBFImage getVisualisationImage()
	{
		// The implementation of this appears to return our double
		// buffered image. That's because at the end of the updateVis()
		// method we swap the images over, so the last drawn image is
		// only available in dbImage. (Any drawing that's occuring right
		// now will be happening on visImage, so it's ok to access this
		// here.)
		return this.dbImage.clone();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint( final Graphics g )
	{
		synchronized( this.visImage )
		{
			final BufferedImage bi = ImageUtilities.createBufferedImageForDisplay( this.dbImage );
			g.drawImage( bi, 0, 0, null );
		}
	}

	/**
	 *	{@inheritDoc}
	 * 	@see javax.swing.JComponent#update(java.awt.Graphics)
	 */
	@Override
	public void update( final Graphics g )
	{
		this.updateVis();
		this.repaint();
	}

	/**
	 *	Show a window containing this visualisation
	 * 	@param title The title of the window
	 * 	@return The window that was created
	 */
	public JFrame showWindow( final String title )
	{
		final JFrame f = new JFrame( title );
		f.getContentPane().setLayout( new GridLayout(1,1) );
		f.getContentPane().add( this );
		f.setResizable( this.allowResize );
		f.pack();
		f.setVisible( true );
		this.addComponentListener( this );
		this.requiredSize = this.getSize();
		this.updateVis();
		return f;
	}

	/**
	 * 	Whether this visualisation can be resized.
	 *	@return TRUE if this visualisation can be resized.
	 */
	public boolean isAllowResize()
    {
	    return this.allowResize;
    }

	/**
	 * 	Set whether this visualisation can be resized.
	 *	@param allowResize TRUE to allow the visualisation to be resizable
	 */
	public void setAllowResize( final boolean allowResize )
    {
	    this.allowResize = allowResize;
    }

	/**
	 * 	Set the required size of the vis
	 *	@param d The required size
	 */
	@Override
	public void setRequiredSize( final Dimension d )
	{
		this.requiredSize = d;
	}

	/**
	 * 	Get the current required size of the vis
	 *	@return The required size
	 */
	public Dimension getRequiredSize()
	{
		return this.requiredSize;
	}

	/**
	 * 	Sets whether to clear the image before drawing. Has no effect if
	 * 	fade drawing is used.
	 *	@param tf TRUE to clear the image
	 */
	public void setClearBeforeDraw( final boolean tf )
	{
		this.clearBeforeDraw = tf;
	}

	@Override
	public void componentHidden( final ComponentEvent e )
	{
	}

	@Override
	public void componentMoved( final ComponentEvent e )
	{
	}

	@Override
	public void componentShown( final ComponentEvent e )
	{
	}

	@Override
	public void componentResized( final ComponentEvent e )
	{
		if( this.getSize().width == this.visImage.getWidth() &&
			this.getSize().height == this.visImage.getHeight() )
			return;

		this.requiredSize = this.getSize();
		if( this.overlaidOn != null )
				this.overlaidOn.setSize( this.getSize() );
		else	this.updateVis();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.AnimatedVisualisationListener#newVisualisationAvailable(org.openimaj.vis.AnimatedVisualisationProvider)
	 */
	@Override
	public void newVisualisationAvailable( final AnimatedVisualisationProvider avp )
	{
//		avp.updateVis();
		this.updateVis();
	}
}
