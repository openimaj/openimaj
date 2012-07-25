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
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processor.connectedcomponent.render.BlobRenderer;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;


/**
 * Static methods for displaying images using Swing.
 * 
 * In addition to normal windows, the class also supports
 * "named windows" which can be referred to by name.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class DisplayUtilities {
	private static int windowCount = 0;
	private static int windowOpenCount = 0;
	private static Map<String,JFrame> namedWindows = new HashMap<String,JFrame>();
	
	/**
	 * Get the number of open windows
	 * @return number of open windows
	 */
	public static int openWindowCount() {
		return windowOpenCount;
	}

	/**
	 * Display an image with the default name
	 * @param image the image
	 * @return frame containing the image
	 */
	public static JFrame display(Image<?,?> image) {
		return display(image, "Image: " + windowCount);
	}
	
	/**
	 * Display an image with the default name
	 * @param image the image
	 * @return frame containing the image
	 */
	public static JFrame display(BufferedImage image) {
		return display(image, "Image: " + windowCount);
	}
	
	/**
	 * Display an image with the given title
	 * @param image the image
	 * @param title the title
	 * @return frame containing the image
	 */
	public static JFrame display(Image<?,?> image, String title) {
		return display(ImageUtilities.createBufferedImageForDisplay(image), title, image );
	}
	
	private static BufferedImage getImage(JFrame frame) {
		if (frame == null) return null;
		
		if (frame.getContentPane().getComponentCount() > 0 && frame.getContentPane().getComponent(0) instanceof ImageComponent) {
			return ((ImageComponent)frame.getContentPane().getComponent(0)).image;
		}
		
		return null;
	}
	
	/**
	 * Display an image in the given frame
	 * @param image the image
	 * @param frame the frame
	 * @return the frame
	 */
	public static JFrame display(Image<?,?> image, JFrame frame) {
		BufferedImage bimg = getImage(frame);
		return display(ImageUtilities.createBufferedImageForDisplay(image, bimg), frame);
	}
	
	/**
	 * Set the position of a named window.
	 * @param name The window name
	 * @param x the x position
	 * @param y the y position
	 */
	public static void positionNamed(String name, int x, int y) {
		JFrame w = createNamedWindow(name);
		w.setBounds(x, y, w.getWidth(), w.getHeight());
	}
	
	/**
	 * 	Update the image that is being displayed in the given named window. 
	 *  @param name The named window
	 *  @param newImage The new image to display
	 *  @param title The window title 
	 */
	public static void updateNamed( String name, Image<?,?> newImage, String title )
	{
		JFrame w = createNamedWindow(name,title,true);
		BufferedImage bimg = getImage(w);
		
		((ImageComponent)w.getContentPane().getComponent(0)).setImage( 
			ImageUtilities.createBufferedImageForDisplay( newImage, bimg ) );
	}
	
	/**
	 * Create a named window with a title that is also the name
	 * @param name
	 * @return the window
	 */
	public static JFrame createNamedWindow(String name){
		return createNamedWindow(name,name,false);
	}
	/**
	 * Create a named window with a title
	 * @param name
	 * @param title
	 * @return the window
	 */
	public static JFrame createNamedWindow(String name, String title){
		return createNamedWindow(name,title,false);
	}
	/**
	 * Create a named window that auto resizes
	 * @param name
	 * @param title
	 * @param autoResize
	 * @return the window
	 */
	public static JFrame createNamedWindow(String name, String title, boolean autoResize) {
		if(namedWindows.containsKey(name)) return namedWindows.get(name);
		JFrame frame = DisplayUtilities.makeDisplayFrame(title, 0, 0, null);
		((ImageComponent)frame.getContentPane().getComponent(0)).autoResize = autoResize;
		((ImageComponent)frame.getContentPane().getComponent(0)).autoPack = autoResize;
		namedWindows.put(name,frame);
		return frame;
	}
	
	/**
	 * Display an image in the given frame by name (will be created if not already done so using {@link #createNamedWindow(String)}
	 * @param image the image
	 * @param name the name of the frame
	 * @return the frame
	 */
	public static JFrame displayName(Image<?,?> image, String name) {
		JFrame frame = createNamedWindow(name);
		BufferedImage bimg = getImage(frame);
		return display(ImageUtilities.createBufferedImageForDisplay(image, bimg), frame, image);
	}

	/**
	 * Display an image in the given frame by name (will be created if not already done so using {@link #createNamedWindow(String)}
	 * @param image the image
	 * @param name the name of the frame
	 * @param autoResize should the frame resize to fit its contents
	 * @return the frame
	 */
	public static JFrame displayName(Image<?,?> image,String name, boolean autoResize) {
		JFrame frame = createNamedWindow(name, name, autoResize);
		BufferedImage bimg = getImage(frame);
		return display(ImageUtilities.createBufferedImageForDisplay(image, bimg), frame, image);
	}

	/**
	 * 	An image viewer that displays and image and allows zooming and
	 * 	panning of images.
	 *
	 *	@author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 */
	public static class ImageComponent extends JComponent
		implements MouseListener, MouseMotionListener
	{
		/** */
		private static final long serialVersionUID = 1L;
		
		/** The image being displayed */
		protected BufferedImage image;
		
		/** The original image being displayed. Used for pixel interrogation */
		protected Image<?,?> originalImage;
		
		/** Whether to auto resize the component to the content size */
		private boolean autoResize = false;
		
		/** Whether to pack the component on resize */
		private boolean autoPack = false;
		
		/** Draw a grid where there is no image */
		private boolean drawTransparencyGrid = true;
		
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
		private double scaleFactor = 1;
		
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

		/**
		 * Default constructor
		 */
		public ImageComponent() 
		{
			this( false, false );
		}
		
		/**
		 * Default constructor. Allows setting of the autoResize
		 * parameter which if true changes the size of the component
		 * to fit the contents.
		 * 
		 * @param autoResize automatically resize the component to the content size
		 */
		public ImageComponent( boolean autoResize ) 
		{
			this( autoResize, true );
		}
		
		/**
		 * 	Construct with given image
		 * 	@param image the image
		 */
		public ImageComponent( BufferedImage image ) 
		{
			this( true, true );
			this.setImage( image );
		}
		
		/**
		 * Default constructor. Allows setting of the autoResize
		 * parameter which if true changes the size of the component
		 * to fit the contents, and the autoPack parameter which
		 * automatically packs the containers root (if its a JFrame)
		 * whenever it is resized.
		 * 
		 * @param autoResize automatically resize the component to the content size
		 * @param autoPack automatically pack the root component on resize
		 */
		public ImageComponent( boolean autoResize, boolean autoPack ) 
		{
			this( 1f, autoResize, autoPack );
		}
		
		/**
		 * Default constructor. Allows setting of the autoResize
		 * parameter which if true changes the size of the component
		 * to fit the contents, and the autoPack parameter which
		 * automatically packs the containers root (if its a JFrame)
		 * whenever it is resized.
		 * 
		 * @param initialScale initial scale of the image 
		 * @param autoResize automatically resize the component to the content size
		 * @param autoPack automatically pack the root component on resize
		 */
		public ImageComponent( float initialScale, boolean autoResize, boolean autoPack )
		{
			this.autoPack = autoPack;
			this.autoResize = autoResize;
			this.scaleFactor = initialScale;
			
			this.addMouseListener( this );
			this.addMouseMotionListener( this );			
			
			new Exception().printStackTrace();
		}
		
		/**
		 * Set the image to draw
		 * @param image the image
		 */
		public void setImage( BufferedImage image ) 
		{
			this.image = image;
			if( this.autoResize )
			{
				this.setPreferredSize( new Dimension(
					(int)(image.getWidth()*scaleFactor),
					(int)(image.getHeight()*scaleFactor)) );
				this.setSize( new Dimension(
					(int)(image.getWidth()*scaleFactor),
					(int)(image.getHeight()*scaleFactor) ) );
				Component c = SwingUtilities.getRoot(this);
				
				if(c == null) return;
				
				c.validate();
				
				if( c instanceof JFrame && autoPack ) 
				{
					JFrame f = (JFrame) c;
					f.pack();
				}
			}
			
			this.drawX = image.getWidth() / 2;
			this.drawY = image.getHeight() / 2;
			
			this.repaint();
		}
		
		/**
		 * 	If you want to be able to inspect the original image's
		 * 	pixel values (rather than the generated BufferedImage) set
		 * 	the original image here. Use null to enforce showing the
		 * 	BufferedImage pixel values.
		 * 
		 *	@param image The original image.
		 */
		public void setOriginalImage( Image<?,?> image )
		{
			this.originalImage = image;
		}
		
		/**
		 * 	Make sure the x and y position we're drawing the image in
		 * 	is not going mad.
		 */
		private void sanitiseVars()
		{
			// Make sure we're not going out of the space
			this.drawX = Math.max( image.getWidth()/scaleFactor/2, Math.min( this.drawX, 
					image.getWidth() - (getWidth()/2/scaleFactor) ) );
			this.drawY = Math.max( image.getHeight()/scaleFactor/2, Math.min( this.drawY, 
					image.getHeight() - (getHeight()/2/scaleFactor) ) );			
		}
		
		/**
		 *	{@inheritDoc}
		 * 	@see javax.swing.JComponent#paint(java.awt.Graphics)
		 */
		@Override
		public void paint(Graphics g) 
		{
			// Draw the image
			if( image != null )
			{
				Component root = SwingUtilities.getRoot(this);
				
				if( drawTransparencyGrid )
				{
					BufferedImage transparencyGrid = new BufferedImage( 
						getWidth(), getHeight(), BufferedImage.TYPE_3BYTE_BGR );
					Graphics tg = transparencyGrid.getGraphics();
					
					int gridSize = (int)(20 * scaleFactor);
					for( int y = 0; y < getHeight(); y += gridSize )
					{
						for( int x = 0; x < getWidth(); x += gridSize )
						{
							int c = (x/gridSize+y/gridSize)%2;
							if( c == 0 )
									tg.setColor( new Color(220,220,220) );
							else	tg.setColor( Color.white );
								
							tg.fillRect( x, y, gridSize, gridSize );
						}
					}
					
					g.drawImage( transparencyGrid, 0, 0, root );
				}
				
				// Calculate the bounding box in image coordinates
				int x = Math.max( 0, (int)(drawX - getWidth()/scaleFactor/2d) );
				int y = Math.max( 0, (int)(drawY - getHeight()/scaleFactor/2d) );
				int w = Math.min( getWidth(), (int)(getWidth()/scaleFactor) );
				int h = Math.min( getHeight(), (int)(getHeight()/scaleFactor) );
				int drawWidth = getWidth();
				int drawHeight = getHeight();
				
				// Create the image to draw
				java.awt.Image img = image;
				if( scaleFactor > 1 )
				{
					// Get the subimage of the image to draw zoomed in
					img = image.getSubimage( x, y, w, h );
				}
				else
				{
					if( scaleFactor < 1 )
					{
						// We're zooming out, so get a scaled instance and
						// fix the drawing width and height
						img = image.getScaledInstance( (int)(getWidth()*scaleFactor), 
								(int)(getHeight()*scaleFactor), java.awt.Image.SCALE_FAST );
						drawWidth = img.getWidth( null );
						drawHeight = img.getHeight( null );
					}
				}
				
				// Blat the image to the screen
				g.drawImage( img, 0, 0, drawWidth, drawHeight, root );
				
				// If we're to show pixel colours and we're supposed to do it
				// on this time around...
				if( (showPixelColours || showXY) && drawPixelColour )
				{
					StringBuffer pixelColourStrB =	new StringBuffer();
					
					if( showXY )
						pixelColourStrB.append( "["+pixelX+","+pixelY+"] " );
					
					if( showPixelColours )
						pixelColourStrB.append( Arrays.toString( currentPixelColour ) );
					
					// Calculate the size to draw
					FontMetrics fm = g.getFontMetrics();
					int fw = fm.stringWidth( pixelColourStrB.toString() );
					int fh = fm.getHeight() + fm.getDescent();
					int p = 4;	// padding
					int dx = 0;
					int dy = getHeight() - (fh+p);
					
					// If the mouse is over where we want to put the box,
					// we'll move the box to another corner
					if( mouseX <= dx+fw+p && mouseX >= dx && 
						mouseY >= dy && mouseY <= dy+fh+p )
						dy = 0;
					
					// Draw a box
					g.setColor( new Color(0,0,0,0.5f) );
					g.fillRect( dx, dy, fw + p, fh + p );
					
					// Draw the text
					g.setColor( Color.white );
					g.drawString( pixelColourStrB.toString(), dx + p/2, 
							dy + fm.getHeight() + p/2 );
				}
			}
		}

		/**
		 *	{@inheritDoc}
		 * 	@see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
        public void mouseClicked( MouseEvent e )
        {
			if( e.getButton() == MouseEvent.BUTTON1 && allowZooming )
			{
				if( e.isControlDown() )
						scaleFactor /= 2;
				else	scaleFactor *= 2;

				this.drawX = e.getX() / scaleFactor + this.drawX - getWidth()/2/scaleFactor;
				this.drawY = e.getY() / scaleFactor + this.drawY - getHeight()/2/scaleFactor;

				// Make sure we're not going to draw out of bounds.
				sanitiseVars();
				
				repaint();
			}
        }

		@Override
        public void mousePressed( MouseEvent e ) 
		{
			if( allowDragging )
			{
				this.dragStartX = e.getX();
				this.dragStartY = e.getY();
			}
		}

		@Override
        public void mouseReleased( MouseEvent e ) {}

		@Override
        public void mouseEntered( MouseEvent e ) {} 

		@Override
        public void mouseExited( MouseEvent e ) 
		{
			drawPixelColour = false;
			repaint();
		}

		@Override
        public void mouseDragged( MouseEvent e )
        {
			if( !allowDragging )
				return;
			
			int diffx = e.getX() - this.dragStartX;
			int diffy = e.getY() - this.dragStartY;
			
			if( diffx == 0 && diffy == 0 )
				return;
			
			// Update the draw position
			this.drawX -= diffx/scaleFactor;
			this.drawY -= diffy/scaleFactor;

			// Reset the draggers
			this.dragStartX = e.getX();
			this.dragStartY = e.getY();
			
			// Make sure the drag stays within the bounds
			sanitiseVars();
			
			// Redraw the component
			repaint();
        }

		@Override
        public void mouseMoved( MouseEvent e )
        {
			if( showPixelColours )
			{
				// This is the top-left of the image in image coordinates
				int ix = Math.max( 0, (int)(drawX - getWidth()/scaleFactor/2d) );
				int iy = Math.max( 0, (int)(drawY - getHeight()/scaleFactor/2d) );
				int w = Math.min( getWidth(), (int)(getWidth()/scaleFactor) );
				int h = Math.min( getHeight(), (int)(getHeight()/scaleFactor) );
				
				int x = (int)(ix + (w* e.getX()/getWidth()));
				int y = (int)(iy + (h* e.getY()/getHeight()));
				
//				System.out.println( x+","+y );
				
				if( x > image.getWidth() || y > image.getHeight() )
				{
					drawPixelColour = false;
					repaint();
					return;
				}
				
				pixelX = x;
				pixelY = y;

				System.out.println( "Original image: "+originalImage );
				
				// If we don't have the original image, we'll just use the
				// colours from the BufferedImage
				if( originalImage == null )
				{
					int colour = image.getRGB( x, y );
					currentPixelColour = new Float[3];
					currentPixelColour[0] = (float)((colour & 0x00ff0000) >> 16);
					currentPixelColour[1] = (float)((colour & 0x0000ff00) >> 8);
					currentPixelColour[2] = (float)((colour & 0x000000ff));
				}
				else
				{
					if( originalImage instanceof FImage )
					{
						Object o = originalImage.getPixel( x, y );
						currentPixelColour = new Float[1];
						currentPixelColour[0] = (Float)o;
					}
					else
					if( originalImage instanceof MBFImage )
					{
						MBFImage i = (MBFImage)originalImage;
						currentPixelColour = new Float[ i.numBands() ];
						for( int b = 0; b < i.numBands(); b++ )
							currentPixelColour[b] = (Float)i.getBand(b).getPixel( x, y );
					}
				}
				
				drawPixelColour = true;
				repaint();
			}
			
			mouseX = e.getX();
			mouseY = e.getY();
        }		
	}
	
	/**
	 * An extension of {@link ImageComponent} that scales the displayed image.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public static class ScalingImageComponent extends ImageComponent {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void paint(Graphics g) {
			Component f = SwingUtilities.getRoot(this);
			if( image != null )
				g.drawImage(image, 0, 0, getWidth(), getHeight(), f);
		}
	}

	/**
	 * Display an image in the given frame
	 * @param image the image
	 * @param frame the frame
	 * @return the frame
	 */
	public static JFrame display(BufferedImage image, JFrame frame) 
	{
		return display( image, frame, null );
	}
	
	/**
	 * Display an image in the given frame
	 * @param image the image
	 * @param frame the frame
	 * @param originalImage the original image
	 * @return the frame
	 */
	public static JFrame display(BufferedImage image, JFrame frame, Image<?,?> originalImage ) {
		if (frame == null) return makeDisplayFrame("Image: " + windowCount,image.getWidth(),image.getHeight(),image);
		
		if (frame.getContentPane().getComponentCount() > 0 && frame.getContentPane().getComponent(0) instanceof ImageComponent) {
			ImageComponent cmp = ((ImageComponent)frame.getContentPane().getComponent(0));
			if(!frame.isVisible()){
				boolean ar = cmp.autoResize;
				boolean ap = cmp.autoPack;
				cmp.autoResize = true;
				cmp.autoPack = true;
				cmp.setImage(image);
				cmp.setOriginalImage( originalImage );
				cmp.autoResize = ar;
				cmp.autoPack = ap;
				frame.setVisible(true);
			}
			else{
				cmp.setImage(image);
				cmp.setOriginalImage( originalImage );
			}
		} else {
			frame.getContentPane().removeAll();
			
			ImageComponent c = new ImageComponent(image);
			c.setOriginalImage( originalImage );
			
			frame.add(c);
			frame.pack();
			frame.setVisible(true);
		}
		return frame;
	}
	
	/**
	 * Make a frame with the given title.
	 * @param title the title
	 * @return the frame
	 */
	public static JFrame makeFrame(String title) {
		final JFrame f = new JFrame(title);
		f.setResizable(false);
		f.setUndecorated(false);
		
		f.addWindowListener ( new WindowAdapter () {
			@Override
			public void windowClosing ( WindowEvent evt )
			{
				windowOpenCount = windowCount - 1;
				f.dispose();
			}
		});
		return f;
	}
	
	/**
	 * Display an image with the given title
	 * @param image the image
	 * @param title the title
	 * @return frame containing the image
	 */
	public static JFrame display(BufferedImage image, String title) {
		return display( image, title, null );
	}
	
	/**
	 * Display an image with the given title
	 * @param image the image
	 * @param title the title
	 * @param originalImage original image
	 * @return frame containing the image
	 */
	public static JFrame display(BufferedImage image, String title, Image<?,?> originalImage ) {
		if (GraphicsEnvironment.isHeadless()) return null;
		
        return makeDisplayFrame( title, image.getWidth(), image.getHeight(), image, originalImage );
	}
	
	/**
	 * Get a frame that will display an image.
	 * @param title the frame title 
	 * @param width the frame width
	 * @param height the frame height
	 * @return A {@link JFrame} that allows images to be displayed.
	 */
	public static JFrame makeDisplayFrame( String title, int width, int height)
	{
		return makeDisplayFrame( title, width, height, null );
	}

	/**
	 * Get a frame that will display an image.
	 * @param title the frame title 
	 * @param width the frame width
	 * @param height the frame height 
	 * @param img the image to display 
	 * @return A {@link JFrame} that allows images to be displayed.
	 */
	public static JFrame makeDisplayFrame( String title, int width, int height, BufferedImage img  )
	{
		return makeDisplayFrame( title, width, height, img, null );
	}
	
	/**
	 * Get a frame that will display an image.
	 * @param title the frame title 
	 * @param width the frame width
	 * @param height the frame height 
	 * @param img the image to display 
	 * @param originalImage the original image
	 * @return A {@link JFrame} that allows images to be displayed.
	 */
	public static JFrame makeDisplayFrame( String title, int width, int height, 
			BufferedImage img, Image<?,?> originalImage  )
	{
		final JFrame f = makeFrame(title);
		
		ImageComponent c = new ImageComponent();
		if( img != null )
			c.setImage( img );
		c.setOriginalImage( originalImage );
		c.setSize( width, height );
		c.setPreferredSize( new Dimension(c.getWidth(), c.getHeight()) );
		
		f.add(c);
		f.pack();
		f.setVisible(img!=null);
        
        windowCount++;
        
        return f;
	}
	
	/**
	 * Render a connected component and display it
	 * @param input the connected component
	 * @return frame containing the rendered image
	 */
	public static JFrame display(ConnectedComponent input) {
		return display(input, 1.0f);
	}
	
	/**
	 * Render a connected component with a given grey level and display it
	 * @param input the connected component
	 * @param col the grey level
	 * @return frame containing the rendered image
	 */
	public static JFrame display(ConnectedComponent input, float col) {
		ConnectedComponent cc = input.clone();
		
		Rectangle bb = cc.calculateRegularBoundingBox();
		
		//Render the mask, leaving a 10 px border
		cc.translate(10 - (int)bb.x, 10 - (int)bb.y);
		FImage mask = new FImage((int)Math.max(bb.width + 20, 100), (int)Math.max(bb.height + 20, 100));
		BlobRenderer<Float> br = new BlobRenderer<Float>(mask, 1.0F);
		cc.process(br);
		
		return display(mask);
	}
	
	/**
	 * Render a polygon to an image and display it.
	 * @param input the polygon
	 * @return the frame
	 */
	public static JFrame display(Polygon input) {
		return display(input, 1.0f);
	}
	
	/**
	 * Render a polygon with a given grey level and display it
	 * @param input the polygon
	 * @param col the grey level
	 * @return frame containing the rendered image
	 */
	public static JFrame display(Polygon input, float col) {
		Polygon p = input.clone();
		
		Rectangle bb = p.calculateRegularBoundingBox();
		
		//Render the mask, leaving a 1 px border
		p.translate(10 - bb.x, 10 - bb.y);
		FImage mask = new FImage((int)(bb.width + 20), (int)(bb.height + 20));
		mask.createRenderer().drawPolygon(p, col);
	
		return display(mask);
	}
	
	/**
	 * Display multiple images in an array
	 * @param title the frame title
	 * @param images the images
	 * @return the frame
	 */
	public static JFrame display(String title, final Image<?,?>... images) {
		BufferedImage[] bimages = new BufferedImage[images.length];
		
		for (int i=0; i<images.length; i++)
			bimages[i] = ImageUtilities.createBufferedImageForDisplay(images[i]);
		
		return display(title, bimages);
	}
	
	/**
	 * Display multiple images in an array
	 * @param title the frame title
	 * @param images the images
	 * @return the frame
	 */
	public static JFrame display(String title, final BufferedImage... images) {
		if (GraphicsEnvironment.isHeadless()) return null;
		
		final JFrame f = new JFrame(title);
		
		final int box_size = 200;
		int n_images = images.length;
		int n_boxes_x = 4;
		int width = n_boxes_x * box_size;
		int height = box_size * n_images / n_boxes_x;
		
		f.addWindowListener ( new WindowAdapter () {
			@Override
			public void windowClosing ( WindowEvent evt )
			{
				windowOpenCount = windowCount - 1;
				f.dispose();
			}
		});
		
		Container scrollContainer = new Container();
		scrollContainer.setLayout(new FlowLayout());
		
		Container container = new Container();
		container.setSize(new Dimension(width, height));
		container.setPreferredSize(new Dimension(width, height));
		container.setLayout(new GridLayout(0, n_boxes_x));
		scrollContainer.add(container);
		
		for (final BufferedImage img : images) {
			JComponent c = new JComponent() {
				private static final long serialVersionUID = 1L;
				
				@Override
				public void paint(Graphics g) {
					int cw = this.getWidth();
					int ch = this.getHeight();
					if (img.getWidth() < cw && img.getHeight() < ch) {
						int x = (cw - img.getWidth()) / 2;
						int y = (ch - img.getHeight()) / 2;
						g.drawImage(img, x, y, img.getWidth(), img.getHeight(), f);
					} else if (img.getWidth() > img.getHeight()) {
						float sf = (float)cw / (float)img.getWidth();
						int h = Math.round(sf*img.getHeight());
						g.drawImage(img, 0, (ch-h) / 2, cw, h, f);						
					} else {
						float sf = (float)ch / (float)img.getHeight();
						int w = Math.round(sf*img.getWidth());
						g.drawImage(img, (cw-w) / 2, 0, w, ch, f);
					}
					//TODO: scale image proportionally and draw centered
					
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
        
        windowCount++;
        
        return f;
	}
	
}
