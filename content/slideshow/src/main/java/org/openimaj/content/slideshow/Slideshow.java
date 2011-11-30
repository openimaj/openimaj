package org.openimaj.content.slideshow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


/**
 * Implementation of a slideshow made up of {@link Slide}s. 
 * Binds the left and right arrow keys to forward/backward, 'q' to quit 
 * and 'f' to toggle fullscreen mode. If the current slide being
 * displayed is also a {@link KeyListener} then keypresses
 * other than these will be passed to the slide.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class Slideshow extends JFrame implements KeyListener {
	private static final long serialVersionUID = 1L;
	
	private List<Slide> slides;
	private int currentSlideIndex = -1;
	private DisplayMode dispModeOld;
	private boolean fullscreen = false;
	private Component currentSlideComp;

	private int slideWidth;
	private int slideHeight;

	private Slide currentSlide;

	private JPanel contentPanel;
	
	/**
	 * Default constructor.
	 * @param slides the slides
	 * @param slideWidth the width to display the slides
	 * @param slideHeight the height to display the slides
	 * @param background a background image to display behind the slides (the slides need to be transparent!)
	 * @throws IOException if the first slide can't be loaded
	 */
	public Slideshow(List<Slide> slides, final int slideWidth, final int slideHeight, final BufferedImage background) throws IOException {
		this.slideWidth = slideWidth;
		this.slideHeight = slideHeight;
		
		contentPanel = new JPanel() {
            private static final long serialVersionUID = 1L;
            
			@Override
			public void paintComponent( Graphics g ) 
			{
				setOpaque( false );
				g.drawImage( background, 0, 0, slideWidth, slideHeight, null );
				super.paintComponent( g );
			};
		};
		contentPanel.setSize(slideWidth, slideHeight);
		contentPanel.setPreferredSize(new Dimension(slideWidth, slideHeight));
		contentPanel.setLayout( new GridBagLayout() );
		
		JPanel scrollContent = new JPanel();
		scrollContent.setLayout( new GridBagLayout() );
		scrollContent.setSize(contentPanel.getSize());
		scrollContent.setPreferredSize(contentPanel.getSize());
		scrollContent.add(contentPanel);
		scrollContent.setBackground(Color.BLACK);
		
		getContentPane().setBackground(Color.BLACK);
		
		JScrollPane scroller = new JScrollPane(scrollContent);
		scroller.setBackground(Color.BLACK);
		scroller.setBorder(null);
		getContentPane().add(scroller, BorderLayout.CENTER);
		
		addKeyListener(this);

		this.slides = slides;
		
		displayNextSlide();
		pack();
		
		setVisible(true);
	}

	/**
	 * Display the next slide
	 * 
	 * @throws IOException
	 */
	public void displayNextSlide() throws IOException {
		if (currentSlideIndex < slides.size() - 1) {
			currentSlideIndex++;
			displaySlide(slides.get(currentSlideIndex));
		}
	}

	/**
	 * Display the previous slide
	 * @throws IOException
	 */
	public void displayPrevSlide() throws IOException {
		if (currentSlideIndex > 0) {
			currentSlideIndex--;
			displaySlide(slides.get(currentSlideIndex));
		}
	}

	protected void displaySlide(Slide slide) throws IOException {
		if (currentSlideComp != null) {
			contentPanel.remove(currentSlideComp);
			currentSlide.close();			
		}
		
		currentSlide = slide;
		currentSlideComp = currentSlide.getComponent(slideWidth, slideHeight);
		currentSlideComp.setPreferredSize(new Dimension(slideWidth, slideHeight));
		currentSlideComp.setMaximumSize(new Dimension(slideWidth, slideHeight));
		
		contentPanel.add(currentSlideComp, new GridBagConstraints());
		
		contentPanel.validate();
		repaint();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (currentSlide instanceof KeyListener) {
			((KeyListener)currentSlide).keyTyped(e);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		try {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				displayPrevSlide();
				break;
			case KeyEvent.VK_RIGHT:
				displayNextSlide();
				break;
			case KeyEvent.VK_F:
				toggleFullscreen();
				break;
			case KeyEvent.VK_ESCAPE:
				setFullscreen(false);
				break;
			case KeyEvent.VK_Q:
				System.exit(0);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		if (currentSlide instanceof KeyListener) {
			((KeyListener)currentSlide).keyPressed(e);
		}
	}

	private void toggleFullscreen() {
		setFullscreen(!fullscreen);
	}
	
	/**
     * Method allows changing whether this window is displayed in fullscreen or
     * windowed mode.
     * @param fullscreen true = change to fullscreen,
     *                   false = change to windowed
     */
    public void setFullscreen( boolean fullscreen )
    {
        //get a reference to the device.
        GraphicsDevice device  = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode dispMode = device.getDisplayMode();
        //save the old display mode before changing it.
        dispModeOld = device.getDisplayMode();

        if( this.fullscreen != fullscreen )
        { //are we actually changing modes.
            //change modes.
            this.fullscreen = fullscreen;
            // toggle fullscreen mode
            if( !fullscreen )
            {
                //change to windowed mode.
                //set the display mode back to the what it was when
                //the program was launched.
                device.setDisplayMode(dispModeOld);
                //hide the frame so we can change it.
                setVisible(false);
                //remove the frame from being displayable.
                dispose();
                //put the borders back on the frame.
                setUndecorated(false);
                //needed to unset this window as the fullscreen window.
                device.setFullScreenWindow(null);
                //recenter window
                setLocationRelativeTo(null);
                setResizable(true);

                //reset the display mode to what it was before
                //we changed it.
                setVisible(true);

            }
            else
            { //change to fullscreen.
                //hide everything
                setVisible(false);
                //remove the frame from being displayable.
                dispose();
                //remove borders around the frame
                setUndecorated(true);
                //make the window fullscreen.
                device.setFullScreenWindow(this);
                //attempt to change the screen resolution.
                device.setDisplayMode(dispMode);
                setResizable(false);
                setAlwaysOnTop(false);
                //show the frame
                setVisible(true);
            }
            //make sure that the screen is refreshed.
            repaint();
        }
    }

	@Override
	public void keyReleased(KeyEvent e) {
		if (currentSlide instanceof KeyListener) {
			((KeyListener)currentSlide).keyReleased(e);
		}
	}
}