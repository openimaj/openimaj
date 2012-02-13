package org.openimaj.content.slideshow;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

/**
 * Utility class for dealing with fullscreen Swing applications.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class FullscreenUtility {
	protected JFrame window;
	protected DisplayMode dispModeOld;
	protected boolean fullscreen = false;
	
	/**
	 * Construct with the given JFrame. The utility will
	 * allow the frame to be toggled between windowed and
	 * fullscreen mode.
	 * @param frame The frame.
	 */
	public FullscreenUtility(JFrame frame) {
		this.window = frame;
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
                window.setVisible(false);
                //remove the frame from being displayable.
                window.dispose();
                //put the borders back on the frame.
                window.setUndecorated(false);
                //needed to unset this window as the fullscreen window.
                device.setFullScreenWindow(null);
                //recenter window
                window.setLocationRelativeTo(null);
                window.setResizable(true);

                //reset the display mode to what it was before
                //we changed it.
                window.setVisible(true);
            }
            else
            { //change to fullscreen.
                //hide everything
            	window.setVisible(false);
                //remove the frame from being displayable.
            	window.dispose();
                //remove borders around the frame
            	window.setUndecorated(true);
                //make the window fullscreen.
                device.setFullScreenWindow(window);
                //attempt to change the screen resolution.
                device.setDisplayMode(dispMode);
                window.setResizable(false);
                window.setAlwaysOnTop(false);
                //show the frame
                window.setVisible(true);
            }
            //make sure that the screen is refreshed.
            window.repaint();
        }
    }
}
