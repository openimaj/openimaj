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
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import org.openimaj.image.colour.ColourMap;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.vis.general.DotPlotVisualisation.ColouredDot;

/**
 *
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 11 Jul 2013
 * @version $Author$, $Revision$, $Date$
 */
public class DotPlotVisualisation3D extends XYZVisualisation3D<ColouredDot> implements ItemPlotter3D<ColouredDot>
{
	/** A colour map to use */
	private ColourMap colourMap = ColourMap.Autumn;

	/** Colour map range */
	private double colourMapMin = -1;

	/** Colour map range */
	private double colourMapMax = 1;

	/**
	 *
	 * @param width
	 * @param height
	 */
	public DotPlotVisualisation3D( final int width, final int height )
	{
		super( width, height, null );
		this.plotter = this;
	}

	/**
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @param size
	 */
	public void addPoint( final double x, final double y, final double z, final double size )
	{
		Float[] c = RGBColour.RED;
		if( this.colourMap != null )
			c = this.colourMap.apply( (float)((size-this.colourMapMin)/(this.colourMapMax-this.colourMapMin)) );
		this.data.add( new LocatedObject3D<ColouredDot>( x, y, z, new ColouredDot( size, c ) ) );
	}

	@Override
	public void renderRestarting()
	{
	}

	@Override
	public void plotObject( final GLAutoDrawable drawable,
			final org.openimaj.vis.general.XYZVisualisation3D.LocatedObject3D<ColouredDot> object,
			final AxesRenderer3D renderer )
	{
		final double[] p = renderer.calculatePosition( new double[]
				{ object.x, object.y, object.z } );

		final GL2 gl = drawable.getGL().getGL2();

		gl.glPushMatrix();

		// Translate to the position of the dot
		gl.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );
		gl.glTranslated( p[0], p[1], p[2] );

		final double[] s = renderer.scaleDimension(
			new double[] {object.object.size, object.object.size, object.object.size} );
		gl.glScaled( s[0], s[1], s[2] );

		// Create a sphere
		if( !this.isEnableLights() )
			gl.glColor3f( object.object.colour[0], object.object.colour[1], object.object.colour[2] );
		else
		{
	        final float[] rgba = { object.object.colour[0], object.object.colour[1], object.object.colour[2] };
	        gl.glMaterialfv( GL.GL_FRONT, GLLightingFunc.GL_AMBIENT, rgba, 0);
	        gl.glMaterialfv( GL.GL_FRONT, GLLightingFunc.GL_SPECULAR, rgba, 0);
	        gl.glMaterialf( GL.GL_FRONT, GLLightingFunc.GL_SHININESS, 0.05f);
		}

		final GLUquadric qobj0 = this.glu.gluNewQuadric();
		this.glu.gluQuadricDrawStyle( qobj0, GLU.GLU_FILL );
		this.glu.gluQuadricNormals( qobj0, GLU.GLU_SMOOTH );
		this.glu.gluSphere( qobj0, 1, 12, 12 );
		this.glu.gluDeleteQuadric( qobj0 );

		gl.glPopMatrix();
	}

	@Override
	public void init( final GLAutoDrawable drawable )
	{
		super.init( drawable );

		final float eyeX = 0.5f, eyeY = 1f, eyeZ = 0f;
		final float lookAtX = 0f, lookAtY = 0, lookAtZ = 0;
		final float upX = 0, upY = 1, upZ = 0;
		this.glu.gluLookAt( eyeX, eyeY, eyeZ, lookAtX, lookAtY, lookAtZ, upX, upY, upZ );

		// Instantiate the camera mover
		this.cameraPosition = new RotatingCameraProvider(
				eyeX, eyeY, eyeZ,
				lookAtX, lookAtY, lookAtZ,
				0.0004f, 0.0001f, 0.0002f, 0.75f, 0.75f, 1f );
	}

	/**
	 *	@return the colourMap
	 */
	public ColourMap getColourMap()
	{
		return this.colourMap;
	}

	/**
	 *	@param colourMap the colourMap to set
	 */
	public void setColourMap( final ColourMap colourMap )
	{
		this.colourMap = colourMap;
	}

	/**
	 *	@return the colourMapRange
	 */
	public double getColourMapMin()
	{
		return this.colourMapMin;
	}

	/**
	 *	@param colourMapRange the colourMapRange to set
	 */
	public void setColourMapMin( final double colourMapRange )
	{
		this.colourMapMin = colourMapRange;
	}

	/**
	 *	@return the colourMapMax
	 */
	public double getColourMapMax()
	{
		return this.colourMapMax;
	}

	/**
	 *	@param colourMapMax the colourMapMax to set
	 */
	public void setColourMapMax( final double colourMapMax )
	{
		this.colourMapMax = colourMapMax;
	}

	/**
	 *	@param min
	 *	@param max
	 */
	public void setColourMapRange( final double min, final double max )
	{
		this.colourMapMin = min;
		this.colourMapMax = max;
	}

	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
		final DotPlotVisualisation3D dpv = new DotPlotVisualisation3D( 1000, 600 );
		dpv.getAxesRenderer().setAxesRanges( -1, 1, -1, 1, -1, 1 );
		dpv.setColourMapRange( 0, 0.1 );
		dpv.setEnableLights( false );

		for( int i = 0; i < 100; i++ )
			dpv.addPoint( (Math.random() - 0.5) * 2, (Math.random() - 0.5) * 2,
					(Math.random() - 0.5) * 2, Math.random() / 10 );
	}
}
