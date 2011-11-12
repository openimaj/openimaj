package org.openimaj.demos.sandbox.slideshow;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;


public class Slideshow extends JFrame implements KeyListener {
	private static final long serialVersionUID = 1L;
	
	private List<Slide> slides;
	private int currentSlideIndex = -1;
	private DisplayMode dispModeOld;
	private boolean fullscreen = false;
	private Component currentSlideComp;

	private int slideWidth = 1024;
	private int slideHeight = 768;

	private Slide currentSlide;
	
	public Slideshow(List<Slide> slides) throws MalformedURLException, IOException {
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.CENTER;
		layout.setConstraints(this, gbc);
		
		getContentPane().setLayout( layout );
		
		setVisible(true);
		addKeyListener(this);

		this.slides = slides;
		
		displayNextSlide();
		pack();
	}

	public void displayNextSlide() throws IOException {
		if (currentSlideIndex < slides.size() - 1) {
			currentSlideIndex++;
			displaySlide(slides.get(currentSlideIndex));
		}
	}

	public void displayPrevSlide() throws IOException {
		if (currentSlideIndex > 0) {
			currentSlideIndex--;
			displaySlide(slides.get(currentSlideIndex));
		}
	}

	protected void displaySlide(Slide slide) throws IOException {
		if (currentSlideComp != null) {
			getContentPane().remove(currentSlideComp);
			currentSlide.close();			
		}
		
		currentSlide = slide;
		currentSlideComp = currentSlide.getComponent(slideWidth, slideHeight);
		currentSlideComp.setPreferredSize(new Dimension(slideWidth, slideHeight));
		currentSlideComp.setMaximumSize(new Dimension(slideWidth, slideHeight));
		
		getContentPane().add(currentSlideComp, new GridBagConstraints());
		
		pack();
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

	public static void main(String[] args) throws MalformedURLException, IOException {
		List<Slide> slides = new ArrayList<Slide>();
		slides.add(new PictureSlide(new URL("file:///Users/jon/Pictures/Pictures/2008_02_04/IMG_1921.JPG")));
		slides.add(new PictureSlide(new URL("file:///Users/jon/Pictures/Pictures/2008_02_07/IMG_2048.JPG")));
		slides.add(new SIFTTrackerSlide());
		
		new Slideshow(slides);
	}
}