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

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.fixedfunc.GLMatrixFunc;

import org.openimaj.math.geometry.shape.Rectangle3D;

/**
 *	Plots rectangles into a 3D space.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 1 Aug 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class Rectangle3DPlotter extends XYZVisualisation3D<Rectangle3D> implements ItemPlotter3D<Rectangle3D>
{
	/**
	 * 	Where the actual position of the rectangle is relative to the geometry.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 1 Aug 2013
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static enum RectanglePlotPosition
	{
		/** Centre the rectangle on the data point */
		CENTRAL,

		/** Put the top left of the rectangle on the data point */
		TOP_LEFT,

		/** Put the bottom left of the rectangle on the data point */
		BOTTOM_LEFT,

		/** Put the bottom right of the rectangle on the data point */
		BOTTOM_RIGHT,

		/** Put the top right of the rectangle on the data point */
		TOP_RIGHT
	}

	/** The offset for the rectangle position */
	private final RectanglePlotPosition pos = RectanglePlotPosition.CENTRAL;

	/** Whether to draw a dot at the point (only if centrally drawn) */
	private final boolean drawDotAtPoint = true;

	/**
	 *	Create a visualisation with the given size
	 *	@param width Width in pixels of the vis
	 *	@param height Height in pixels of the vis
	 */
	public Rectangle3DPlotter( final int width, final int height )
	{
		super( width, height );
		super.setPlotter( this );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.general.ItemPlotter3D#renderRestarting()
	 */
	@Override
	public void renderRestarting()
	{
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.general.ItemPlotter3D#plotObject(javax.media.opengl.GLAutoDrawable, org.openimaj.vis.general.XYZVisualisation3D.LocatedObject3D, org.openimaj.vis.general.AxesRenderer3D)
	 */
	@Override
	public void plotObject( final GLAutoDrawable drawable, final LocatedObject3D<Rectangle3D> object,
			final AxesRenderer3D renderer )
	{
		// object.object.x,y,z is where we plot the rectangle.
		// object.x,y,z is the data point position.
		final double[] p = renderer.calculatePosition( new double[]
				{ object.object.x, object.object.y, object.object.z } );
		final double[] p2 = renderer.calculatePosition( new double[]
				{ object.x, object.y, object.z } );

		final GL2 gl = drawable.getGL().getGL2();

		gl.glPushMatrix();

		// Translate to the position of the rectangle
		gl.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );
		gl.glTranslated( p2[0], p2[1], p2[2] );
		gl.glRotated( object.object.xRotation, 1, 0, 0 );
		gl.glRotated( object.object.yRotation, 0, 1, 0 );
		gl.glRotated( object.object.zRotation, 0, 0, 1 );

		final double[] dims = renderer.scaleDimension( new double[]{object.object.width,object.object.height,0} );
		final double w = dims[0];
		final double h = dims[1];

		gl.glBegin( GL.GL_LINE_LOOP );
		gl.glVertex3d( p2[0]-p[0], p2[1]-p[1], 0 );
		gl.glVertex3d( p2[0]-p[0]-w, p2[1]-p[1], 0 );
		gl.glVertex3d( p2[0]-p[0]-w, p2[1]-p[1]-h, 0 );
		gl.glVertex3d( p2[0]-p[0], p2[1]-p[1]-h, 0 );
		gl.glEnd();

		gl.glPopMatrix();

		if( this.pos == RectanglePlotPosition.CENTRAL && this.drawDotAtPoint )
		{
			gl.glPushMatrix();
			gl.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );
			gl.glBegin( GL.GL_POINTS );
			gl.glVertex3d( p2[0], p2[1], p2[2] );
			gl.glEnd();
			gl.glPopMatrix();
		}
	}

	@Override
	public void init( final GLAutoDrawable drawable )
	{
		super.init( drawable );

		final float eyeX = 0f, eyeY = 0.1f, eyeZ = 1f;
		final float lookAtX = 0f, lookAtY = 0f, lookAtZ = 0f;
		final float upX = 0, upY = 1, upZ = 0;
		this.glu.gluLookAt( eyeX, eyeY, eyeZ, lookAtX, lookAtY, lookAtZ, upX, upY, upZ );

		// Instantiate the camera mover
		this.cameraPosition = new RotatingCameraProvider(
				eyeX, eyeY, eyeZ,
				lookAtX, lookAtY, lookAtZ,
				0.0004f, 0.0001f, 0.00001f, 1f, 1f, 1f );
	}

	/**
	 *	Note that your rectangle may be altered at this point. Pass in a clone
	 *	if you don't want it to get changed.
	 *
	 *	@param rect The rectangle.
	 */
	public void addRectangle( final Rectangle3D rect )
	{
		final double x = rect.x, y = rect.y, z = rect.z;
		switch( this.pos )
		{
			case CENTRAL:
				rect.x -= rect.width/2;
				rect.y -= rect.height/2;
				break;
			case TOP_LEFT:
				break;
			case TOP_RIGHT:
				rect.x -= rect.width;
				break;
			case BOTTOM_LEFT:
				rect.y -= rect.height;
				break;
			case BOTTOM_RIGHT:
				rect.x -= rect.width;
				rect.y -= rect.height;
				break;
		}

		super.data.add( new LocatedObject3D<Rectangle3D>( x, y, z, rect ) );
	}

	/**
	 *
	 *	@param args
	 */
	public static void main( final String[] args )
	{
		final Rectangle3DPlotter rp = new Rectangle3DPlotter( 1000, 800 );
		rp.getAxesRenderer().setAxesRanges( -1, 1, -1, 1, -1, 1 );

		final int n = 40;
		for( int i = 0; i < n ; i++ )
		{
			final Rectangle3D r = new Rectangle3D();
			r.x = (float)(Math.random()*2-1);
			r.y = (float)(Math.random()*2-1);
			r.z = (float)(Math.random()*2-1);
			r.width = (float)(Math.random()/2);
			r.height = r.width;
			r.xRotation = Math.random() * 90;
			r.yRotation = Math.random() * 90;
			r.zRotation = Math.random() * 90;

			System.out.println( "Rect "+i+" = "+r );

			rp.addRectangle( r );
		}
	}
}
