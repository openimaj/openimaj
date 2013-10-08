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
package org.openimaj.vis.general;

import javax.media.nativewindow.util.PointImmutable;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;

/**
 *	A wrapper around a NEWT window that allows you to get
 *	a canvas on which to draw.
 *
 * 	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@created 4 Jul 2013
 * 	@version $Author$, $Revision$, $Date$
 */
public class JOGLWindow
{
	/** The width of the OpenGL viewport */
	private int width;

	/** The height of the OpenGL viewport */
	private int height;

	/** The position of the window (if windowed) */
	private PointImmutable wpos;

	/** Whether the window is undecorated (if windowed) */
	private boolean undecorated = false;

	/** Whether the 3D view should always be on top */
	private boolean alwaysOnTop = false;

	/** Whether to run full screen (rather than windowed) */
	private boolean fullscreen = false;

	/** Whether the mouse is visible in the window */
	private boolean mouseVisible = true;

	/** Whether the mouse is confined to the window */
	private boolean mouseConfined = false;

	/** The window that is created */
	private GLWindow glWindow;

	/** Which screen to create the viewport on */
	private final int screenIdx = 0;

	/**
	 * 	Create a JOGL Window with the given size and the default
	 * 	properties.
	 *
	 *	@param width The width in pixels
	 *	@param height The height in pixels
	 */
	public JOGLWindow( final int width, final int height )
	{
		this.showWindow( width, height );
	}

	/**
	 * 	Constructor that does not initialise the GL window. This allows you
	 * 	to alter the window properties prior to calling showWindow().
	 */
	public JOGLWindow()
	{
	}

	/**
	 * 	Force an initialisation of the window. Only call this if you used
	 * 	the no arguments constructor.
	 *
	 *	@param width The width
	 *	@param height The height
	 */
	public void showWindow( final int width, final int height )
	{
		this.width = width;
		this.height = height;
		this.initGL();
	}

	/**
	 * Initialise the JOGL Window
	 */
	private void initGL()
	{
		// Get the OpenGL profile and its capabilities.
		// Tries to get OpenGL 3
		final GLProfile glp = GLProfile.get( GLProfile.GL2 );
		final GLCapabilities caps = new GLCapabilities( glp );

		// Get the display
		final Display dpy = NewtFactory.createDisplay( null );

		// Get the screen on the display (defaults to the first screen)
		final Screen screen = NewtFactory.createScreen( dpy, this.screenIdx );

		// Create a window
		this.glWindow = GLWindow.create( screen, caps );

		// Set the size and position of the window
		this.glWindow.setSize( this.width, this.height );
		if( null != this.wpos ) this.glWindow.setPosition( this.wpos.getX(), this.wpos.getY() );

		// Set the properties of the window
		this.glWindow.setUndecorated( this.undecorated );
		this.glWindow.setAlwaysOnTop( this.alwaysOnTop );
		this.glWindow.setFullscreen( this.fullscreen );
		this.glWindow.setPointerVisible( this.mouseVisible );
		this.glWindow.confinePointer( this.mouseConfined );

		// Add a listener to kill the app once the window is closed
		this.glWindow.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowDestroyNotify(final WindowEvent e)
			{
				System.exit(1);
			};
		} );

		// Show the window
		this.glWindow.setVisible( true );
	}

	/**
	 * 	Closes the window and cleans up.
	 */
	public void close()
	{
		this.glWindow.destroy();
	}

	/**
	 * 	Get the drawable surface for 3D operations.
	 *	@return the drawable surface
	 */
	public GLAutoDrawable getDrawableSurface()
	{
		return this.glWindow;
	}

	/**
	 *	@return the undecorated
	 */
	public boolean isUndecorated()
	{
		return this.undecorated;
	}

	/**
	 *	@param undecorated the undecorated to set
	 */
	public void setUndecorated( final boolean undecorated )
	{
		this.undecorated = undecorated;
	}

	/**
	 *	@return the alwaysOnTop
	 */
	public boolean isAlwaysOnTop()
	{
		return this.alwaysOnTop;
	}

	/**
	 *	@param alwaysOnTop the alwaysOnTop to set
	 */
	public void setAlwaysOnTop( final boolean alwaysOnTop )
	{
		this.alwaysOnTop = alwaysOnTop;
	}

	/**
	 *	@return the fullscreen
	 */
	public boolean isFullscreen()
	{
		return this.fullscreen;
	}

	/**
	 *	@param fullscreen the fullscreen to set
	 */
	public void setFullscreen( final boolean fullscreen )
	{
		this.fullscreen = fullscreen;
	}

	/**
	 *	@return the mouseVisible
	 */
	public boolean isMouseVisible()
	{
		return this.mouseVisible;
	}

	/**
	 *	@param mouseVisible the mouseVisible to set
	 */
	public void setMouseVisible( final boolean mouseVisible )
	{
		this.mouseVisible = mouseVisible;
	}

	/**
	 *	@return the mouseConfined
	 */
	public boolean isMouseConfined()
	{
		return this.mouseConfined;
	}

	/**
	 *	@param mouseConfined the mouseConfined to set
	 */
	public void setMouseConfined( final boolean mouseConfined )
	{
		this.mouseConfined = mouseConfined;
	}

	/**
	 * Simple test that opens a window, waits 2 seconds, then closes it.
	 * @param args Command-line args (not used)
	 * @throws InterruptedException
	 */
	public static void main( final String[] args ) throws InterruptedException
	{
		// Simple test that opens a window, waits 2 seconds, then closes it.
		final JOGLWindow jw = new JOGLWindow( 400, 400 );
		Thread.sleep( 2000 );
		jw.close();
	}
}
