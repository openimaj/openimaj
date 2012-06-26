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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
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
		return display(ImageUtilities.createBufferedImageForDisplay(image), title);
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
		return display(ImageUtilities.createBufferedImageForDisplay(image, bimg), frame);
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
		return display(ImageUtilities.createBufferedImageForDisplay(image, bimg), frame);
	}

	/**
	 * 	Class that extends {@link Component} that will paint
	 * 	into that component an image at the origin, at the original
	 * 	size of the image (no scaling).
	 *
	 *	@author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class ImageComponent extends Component {
		private static final long serialVersionUID = 1L;
		protected BufferedImage image;
		private boolean autoResize = false;
		private boolean autoPack = false;

		/**
		 * Default constructor
		 */
		public ImageComponent() {}
		
		/**
		 * Default constructor. Allows setting of the autoResize
		 * parameter which if true changes the size of the component
		 * to fit the contents.
		 * 
		 * @param autoResize automatically resize the component to the content size
		 */
		public ImageComponent(boolean autoResize) {
			this.autoResize = autoResize;
			
			if (autoResize)
				autoPack = true;
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
		public ImageComponent(boolean autoResize, boolean autoPack) {
			this.autoResize = autoResize;
			this.autoPack = autoPack;
		}
		
		/**
		 * Construct with given image
		 * @param image the image
		 */
		public ImageComponent(BufferedImage image) {
			this.image = image;
			setSize((image.getWidth()), (image.getHeight()));
			setPreferredSize(new Dimension(getWidth(), getHeight()));
		}
		
		/**
		 * Set the image to draw
		 * @param image the image
		 */
		public void setImage(BufferedImage image) {
			this.image = image;
			if(this.autoResize){
				this.setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));
				this.setSize(new Dimension(image.getWidth(),image.getHeight()));
				Component c = SwingUtilities.getRoot(this);
				
				if(c == null) return;
				
				c.validate();
				
				if(c instanceof JFrame && autoPack) {
					JFrame f = (JFrame) c;
					f.pack();
				}
			}
			
			
			this.repaint();
		}
		
		@Override
		public void paint(Graphics g) {
			Component f = SwingUtilities.getRoot(this);
			if( image != null )
				g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), f);
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
	public static JFrame display(BufferedImage image, JFrame frame) {
		if (frame == null) return makeDisplayFrame("Image: " + windowCount,image.getWidth(),image.getHeight(),image);
		
		if (frame.getContentPane().getComponentCount() > 0 && frame.getContentPane().getComponent(0) instanceof ImageComponent) {
			ImageComponent cmp = ((ImageComponent)frame.getContentPane().getComponent(0));
			if(!frame.isVisible()){
				boolean ar = cmp.autoResize;
				boolean ap = cmp.autoPack;
				cmp.autoResize = true;
				cmp.autoPack = true;
				cmp.setImage(image);
				cmp.autoResize = ar;
				cmp.autoPack = ap;
				frame.setVisible(true);
			}
			else{
				cmp.setImage(image);
			}
		} else {
			frame.getContentPane().removeAll();
			
			Component c = new ImageComponent(image);
			
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
		if (GraphicsEnvironment.isHeadless()) return null;
		
        return makeDisplayFrame( title, image.getWidth(), image.getHeight(), image );
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
		final JFrame f = makeFrame(title);
		
		ImageComponent c = new ImageComponent();
		if( img != null )
			c.setImage( img );
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
