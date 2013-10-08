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
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.gl2.GLUgl2;

import org.openimaj.image.MBFImage;
import org.openimaj.vis.general.CameraPositionProvider;
import org.openimaj.vis.general.JOGLWindow;

import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.gl2.GLUT;

/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 11 Jul 2013
 *	@version $Author$, $Revision$, $Date$
 * 	@param <D> Data type
 */
public abstract class Visualisation3D<D> implements
	GLEventListener, Visualisation<D>,
	AnimatedVisualisationProvider
{
	/** The GLU library we'll use */
	protected final GLUgl2 glu = new GLUgl2();

	/** The GLUT library we'll use */
	protected final GLUT glut = new GLUT();

	/** The JOGL Window (NEWT) */
	protected JOGLWindow window;

	/** Animation listeners */
	private final List<AnimatedVisualisationListener> listeners =
			new ArrayList<AnimatedVisualisationListener>();

	/** The animation */
	private final Animator animator;

	/** Object that provide the camera position over time */
	protected CameraPositionProvider cameraPosition;

	/** The data! */
	protected D data;

	/** Whether lighting should be enabled */
	private boolean enableLights = false;


	/**
	 * 	Render the visualisation into the drawable
	 *	@param drawable The drawable
	 */
	protected abstract void renderVis( GLAutoDrawable drawable );


	/**
	 *	@param width
	 *	@param height
	 */
	public Visualisation3D( final int width, final int height )
	{
		this.window = new JOGLWindow( width, height );

		if( this.window.getDrawableSurface() == null )
			throw new RuntimeException( "Unable to get OpenGL surface." );

		this.window.getDrawableSurface().addGLEventListener( this );

		this.animator = new Animator( this.window.getDrawableSurface() );
		this.animator.add( this.window.getDrawableSurface() );
		this.animator.start();
	}

	/**
	 * 	Closes the window and cleans up
	 *
	 *	{@inheritDoc}
	 * 	@see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable
	{
		this.animator.stop();
		this.window.close();
	};

	@Override
	public void addAnimatedVisualisationListener( final AnimatedVisualisationListener avl )
	{
		this.listeners.add( avl );
	}

	@Override
	public void removeAnimatedVisualisationListener( final AnimatedVisualisationListener avl )
	{
		this.listeners.remove( avl );
	}

	@Override
	public void init( final GLAutoDrawable drawable )
	{
		final GL2 gl = drawable.getGL().getGL2();
		gl.setSwapInterval( 1 );
		gl.glEnable( GL.GL_DEPTH_TEST );
        gl.glDepthFunc( GL.GL_LEQUAL );
        gl.glShadeModel( GLLightingFunc.GL_SMOOTH );
        gl.glHint( GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST );
//		gl.glEnable( GL.GL_BLEND );
//		gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
		gl.glEnable( GL2GL3.GL_POLYGON_SMOOTH );

		final float w = this.window.getDrawableSurface().getWidth();
		final float h = this.window.getDrawableSurface().getHeight();

		// Set the projection matrix (only done once - just here)
		gl.glMatrixMode( GLMatrixFunc.GL_PROJECTION );
		gl.glLoadIdentity();
		this.glu.gluPerspective( 50, (w / h), 0.01, 10 );

		// Set the initial model matrix
		gl.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );
		gl.glLoadIdentity();
		gl.glViewport( 0, 0, (int) w, (int) h ); /* viewport size in pixels */
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.media.opengl.GLEventListener#display(javax.media.opengl.GLAutoDrawable)
	 */
	@Override
	public void display( final GLAutoDrawable drawable )
	{
		this.updateVis();

		final GL2 gl = drawable.getGL().getGL2();
		gl.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );
		gl.glLoadIdentity();

		if( this.cameraPosition != null )
		{
			final float[] pos = this.cameraPosition.getCameraPosition();
			this.glu.gluLookAt( pos[0], pos[1], pos[2], pos[3], pos[4], pos[5], pos[6], pos[7], pos[8] );
		}

        // Prepare light parameters.
		if( this.enableLights )
		{
	        final float SHINE_ALL_DIRECTIONS = 1;
	        final float[] lightPos = {-30, 0, 0, SHINE_ALL_DIRECTIONS};
	        final float[] lightColorAmbient = {0.2f, 0.2f, 0.2f, 1f};
	        final float[] lightColorSpecular = {0.5f, 0.5f, 0.5f, 1f};

	        // Set light parameters.
	        gl.glLightfv( GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_POSITION, lightPos, 0 );
	        gl.glLightfv( GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_AMBIENT, lightColorAmbient, 0 );
	        gl.glLightfv( GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_SPECULAR, lightColorSpecular, 0 );

	        // Enable lighting in GL.
	        gl.glEnable( GLLightingFunc.GL_LIGHT1 );
	        gl.glEnable( GLLightingFunc.GL_LIGHTING );
		}

		gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );

		this.renderVis( drawable );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see javax.media.opengl.GLEventListener#dispose(javax.media.opengl.GLAutoDrawable)
	 */
	@Override
	public void dispose( final GLAutoDrawable drawable )
	{
		drawable.removeGLEventListener( this );
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.media.opengl.GLEventListener#reshape(javax.media.opengl.GLAutoDrawable,
	 *      int, int, int, int)
	 */
	@Override
	public void reshape( final GLAutoDrawable drawable, final int arg1, final int arg2, final int arg3, final int arg4 )
	{
		final GL2 gl = drawable.getGL().getGL2();
		final float w = this.window.getDrawableSurface().getWidth();
		final float h = this.window.getDrawableSurface().getHeight();
		gl.glMatrixMode( GLMatrixFunc.GL_PROJECTION );
		gl.glLoadIdentity();
		this.glu.gluPerspective( 50, (w / h), 0.01, 10 );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.VisualisationImageProvider#getVisualisationImage()
	 */
	@Override
	public MBFImage getVisualisationImage()
	{
		// TODO: Need to convert the GL buffer to a MBFImage
		return null;
	}

	@Override
	public void setRequiredSize( final Dimension d )
	{
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.Visualisation#setData(java.lang.Object)
	 */
	@Override
	public void setData( final D data )
	{
		this.data = data;
	}


	/**
	 *	@return the enableLights
	 */
	public boolean isEnableLights()
	{
		return this.enableLights;
	}


	/**
	 *	@param enableLights the enableLights to set
	 */
	public void setEnableLights( final boolean enableLights )
	{
		this.enableLights = enableLights;
	}
}
