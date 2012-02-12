package org.openimaj.content.slideshow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.RootPaneContainer;


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
public abstract class Slideshow implements KeyListener {
	private static final long serialVersionUID = 1L;

	protected RootPaneContainer container;
	
	protected List<Slide> slides;
	protected int currentSlideIndex = -1;
	protected Component currentSlideComp;

	protected int slideWidth;
	protected int slideHeight;

	protected Slide currentSlide;

	private JPanel contentPanel;
	
	/**
	 * Default constructor.
	 * @param container the root window
	 * @param slides the slides
	 * @param slideWidth the width to display the slides
	 * @param slideHeight the height to display the slides
	 * @param background a background image to display behind the slides (the slides need to be transparent!)
	 * @throws IOException if the first slide can't be loaded
	 */
	public Slideshow(RootPaneContainer container, List<Slide> slides, final int slideWidth, final int slideHeight, final BufferedImage background) throws IOException {
		this.container = container;
		
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
		
		container.getContentPane().setBackground(Color.BLACK);
		
		JScrollPane scroller = new JScrollPane(scrollContent);
		scroller.setBackground(Color.BLACK);
		scroller.setBorder(null);
		container.getContentPane().add(scroller, BorderLayout.CENTER);
		
		((Component) container).addKeyListener(this);

		this.slides = slides;
		
		displayNextSlide();
		pack();
		
		((Component) container).setVisible(true);
	}
	
	protected abstract void pack();

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
		((Component) container).repaint();
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
		setFullscreen(!isFullscreen());
	}
	
	protected abstract boolean isFullscreen();

	/**
     * Method allows changing whether this window is displayed in fullscreen or
     * windowed mode.
     * @param fullscreen true = change to fullscreen,
     *                   false = change to windowed
     */
    public abstract void setFullscreen( boolean fullscreen );

	@Override
	public void keyReleased(KeyEvent e) {
		if (currentSlide instanceof KeyListener) {
			((KeyListener)currentSlide).keyReleased(e);
		}
	}
}