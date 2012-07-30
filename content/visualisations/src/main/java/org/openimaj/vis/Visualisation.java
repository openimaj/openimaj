/**
 * 
 */
package org.openimaj.vis;

import java.awt.Dimension;
import java.awt.Graphics;

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
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 27 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 * 	@param <T> The type of the data to be visualised 
 */
public abstract class Visualisation<T> extends JPanel
{
	/** */
	private static final long serialVersionUID = 1L;

	/** The visualisation image */
	protected MBFImage visImage = null;
	
	/** The data to be visualised */
	protected T data = null;
	
	protected Visualisation() {};
	
	/**
	 * 	Create a new visualisation with the given width and height
	 *	@param width The width
	 *	@param height The height
	 */
	public Visualisation( int width, int height )
	{
		this.visImage = new MBFImage( width, height, 4 );
		this.setPreferredSize( new Dimension( width, height ) );
		this.setSize( new Dimension( width, height ) );
	}

	/**
	 * 	Create a new visualisation using an existing image.
	 *	@param imageToDrawTo The image to use to draw to
	 */
	public Visualisation( MBFImage imageToDrawTo )
	{
		this.visImage = imageToDrawTo;
		this.setPreferredSize( new Dimension( 
				imageToDrawTo.getWidth(), imageToDrawTo.getHeight() ) );
		this.setSize( new Dimension( 
				imageToDrawTo.getWidth(), imageToDrawTo.getHeight() ) );
	}
	
	/**
	 * 	Called to update the visualisation with the data stored in the
	 * 	data variable.
	 */
	public abstract void update();
	
	/**
	 * 	Set the data to be visualised.
	 *	@param data The data to be visualised
	 */
	public void setData( T data )
	{
		this.data = data;
		this.update();
		repaint();
	}
	
	/**
	 * 	Returns the image to which the bars will be drawn.
	 *	@return The image
	 */
	public MBFImage getVisualisationImage()
	{
		return this.visImage;
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint( Graphics g )
	{
		g.drawImage( ImageUtilities.createBufferedImageForDisplay( visImage ), 
				0, 0, null );
	}
	
	/**
	 *	Show a window containing this visualisation 
	 * 	@param title The title of the window 
	 */
	public void showWindow( String title )
	{
		JFrame f = new JFrame( title );
		f.getContentPane().add( this );
		f.pack();
		f.setVisible( true );
	}
}
