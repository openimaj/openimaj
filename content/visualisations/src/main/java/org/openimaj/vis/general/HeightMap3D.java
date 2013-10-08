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

import java.io.IOException;
import java.net.MalformedURLException;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;

import org.openimaj.image.FImage;
import org.openimaj.image.colour.ColourMap;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.vis.Visualisation3D;

/**
 *	Visualisation that draws a height map in 3d.
 *
 * 	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@created 11 Jul 2013
 * 	@version $Author$, $Revision$, $Date$
 */
public class HeightMap3D extends Visualisation3D<double[][]>
{
	/**
	 *	Rendering style for the height map.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 12 Jul 2013
	 */
	public enum HeightMapType
	{
		/** Draw height map as points */
		POINTS,

		/** Draw height map as lines */
		LINE,

		/** Draw height map as wireframe */
		WIRE,

		/** Draw height map as a solid */
		SOLID,

		/** Draw solid and wire frame */
		SOLID_AND_WIRE,

		/** Draw height map as a textured solid */
		TEXTURED
	}

	/** The way to draw the height map grid */
	private HeightMapType renderType = HeightMapType.SOLID_AND_WIRE;

	/** The colour map to apply to the height field */
	private ColourMap colourMap = ColourMap.Autumn;

	/** The colour of the solid if no colour map specified, or the colour of the wireframe */
	private float[] colour = new float[]{ 0f, 0f, 0f };

	/** The maximum expected data value */
	private double max = 1;

	/** The minimum expected data value */
	private double min = 0;

	/** Whether to auto scale so that the data fits in a cube (1 in the y axis) */
	private boolean autoScale = false;

	/** The length of the patch to render */
	private double length = 1;

	/** The width of the patch to render */
	private double width = 1;

	/** Whether the camera needs to be reset on the next iteration */
	private boolean resetCamera = true;

	/**
	 * @param width
	 * @param height
	 */
	public HeightMap3D( final int width, final int height )
	{
		super( width, height );
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.vis.VisualisationImageProvider#updateVis()
	 */
	@Override
	public void updateVis()
	{
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.vis.Visualisation3D#renderVis(javax.media.opengl.GLAutoDrawable)
	 */
	@Override
	protected void renderVis( final GLAutoDrawable drawable )
	{
		if( this.resetCamera )
			this.setupCamera();

		final GL2 gl = drawable.getGL().getGL2();

		gl.glPushMatrix();
		gl.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );

		// Determine whether we're creating lines or quads
		switch( this.renderType )
		{
			case POINTS:
				gl.glBegin( GL.GL_POINTS );
				break;
			case LINE:
				gl.glBegin( GL.GL_LINES );
				break;
			case SOLID_AND_WIRE:
				gl.glEnable( GL.GL_POLYGON_OFFSET_FILL );
				gl.glPolygonOffset( 1, 1 );
			case SOLID:
			case TEXTURED:
				gl.glBegin( GL2.GL_QUADS );
			    gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL );
			    break;
			case WIRE:
				break;
			default:
				break;
		}

		this.renderHeightMap( gl );

		gl.glDisable( GL.GL_POLYGON_OFFSET_FILL );
		if( this.renderType == HeightMapType.SOLID_AND_WIRE )
		{
			final ColourMap cm = this.colourMap;
			this.colourMap = null;
			this.colour = new float[] { 0f,0f,0f };

			gl.glEnd();
			gl.glBegin( GL.GL_LINE_STRIP );
			this.renderType = HeightMapType.WIRE;

			gl.glDisable( GLLightingFunc.GL_LIGHTING );
		    gl.glDisable( GLLightingFunc.GL_LIGHT0 );

			this.renderHeightMap( gl );

			this.renderType = HeightMapType.SOLID_AND_WIRE;
			this.colourMap = cm;
		}

		gl.glEnd();

		// Reset stuff
		gl.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
//	    gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL );

		gl.glPopMatrix();
	}

	/**
	 * @see "http://azerdark.wordpress.com/2010/01/09/landscape-terrain-using-jogl/"
	 * @param gl
	 * @param pHeightMap
	 */
	private void renderHeightMap( final GL2 gl )
	{
		if( this.data == null || this.data.length < 2 ) return;

		// Determine the size of the grid from the first data element.
		final int N = this.data.length;
		final int M = this.data[0].length;

		final double stepSizeX = this.length/N;
		final double stepSizeY = -this.width/M;

		for( int X = 0; X < N-1; X += 1 )
		{
			if( this.renderType == HeightMapType.WIRE )
				gl.glBegin( GL.GL_LINE_STRIP );

			for( int Y = 0; Y < M-1; Y += 1 )
			{
				// ----------------------------------------------
				this.createVertex( gl, N, M, stepSizeX, stepSizeY, X, Y );
				this.createVertex( gl, N, M, stepSizeX, stepSizeY, X, Y+1 );
				this.createVertex( gl, N, M, stepSizeX, stepSizeY, X+1, Y+1 );
				this.createVertex( gl, N, M, stepSizeX, stepSizeY, X+1, Y );
				// ----------------------------------------------

				if( this.renderType == HeightMapType.WIRE )
					this.createVertex( gl, N, M, stepSizeX, stepSizeY, X, Y );
			}

			if( this.renderType == HeightMapType.WIRE )
				gl.glEnd();
		}
	}

	/**
	 * 	Creates a single colour mapped vertex.
	 *
	 *	@param gl The GL context
	 *	@param N The number of points in the X direction
	 *	@param M The number of points in the Z direction
	 *	@param stepSizeX The size of each quad in the patch
	 *	@param stepSizeY The size of each quad in the patch
	 *	@param X The index along the X direction
	 *	@param Y The index along the Z direction (Y in the data)
	 */
	private void createVertex( final GL2 gl, final int N, final int M,
			final double stepSizeX, final double stepSizeY, final int X, final int Y )
	{
		final double x = X * stepSizeX;
		final double z = Y * stepSizeY;
		double y = this.data[Y][X];

		if( this.autoScale )
			y *= 1d/(this.max-this.min);

		if( this.renderType == HeightMapType.TEXTURED )
			gl.glTexCoord2f( (float)(x / N), (float)(z / M) );
		else
		{
			final float[] c = this.colour;
			if( this.colourMap != null )
				this.colourMap.apply( (float)(this.data[Y][X] * 1d/(this.max-this.min)), c );

			gl.glColor3f( c[0], c[1], c[2] );
		}
		gl.glVertex3d( x, y, z );
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.media.opengl.GLEventListener#init(javax.media.opengl.GLAutoDrawable)
	 */
	@Override
	public void init( final GLAutoDrawable drawable )
	{
		super.init( drawable );
		this.setupCamera();
	}

	/**
	 * 	Set the camera to be pointing to the middle of the height map
	 */
	private void setupCamera()
	{
		System.out.println( "Resetting camera: "+this.length+", "+this.width );

		// Set the initial look at
		final float eyeX = 0.5f, eyeY = 1f, eyeZ = 0f;
		final float lookAtX = (float)this.length/2, lookAtY = 0f, lookAtZ = (float)-this.width/2;
		final float upX = 0, upY = 1, upZ = 0;
		this.glu.gluLookAt( eyeX, eyeY, eyeZ, lookAtX, lookAtY, lookAtZ, upX, upY, upZ );

		// Instantiate the camera mover
		this.cameraPosition = new RotatingCameraProvider(
				eyeX, eyeY, eyeZ,
				lookAtX, lookAtY, lookAtZ,
				0.0004f, 0.0001f, 0.0002f, 0.75f, 0.75f, 1f );

		this.resetCamera = false;
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
	 * 	Set the maximum value expected in this heightmap.
	 *	@param max The maximum value
	 */
	public void setMaxValue( final double max )
	{
		this.max = max;
	}

	/**
	 * 	Set the minimum value expected in this heightmap;
	 *	@param min The minimum value
	 */
	public void setMinValue( final double min )
	{
		this.min = min;
	}

	/**
	 * 	Set whether to fit the maximum possible value into the height map cube.
	 *	@param autoScale TRUE to autoscale.
	 */
	public void setAutoScale( final boolean autoScale )
	{
		this.autoScale = autoScale;
	}

	/**
	 * 	Set the length of the rendered map
	 *	@param l The length
	 */
	public void setLength( final double l )
	{
		this.length = l;
		this.resetCamera = true;
	}

	/**
	 * 	Set the width of the rendered map
	 *	@param w the width
	 */
	public void setWidth( final double w )
	{
		this.width = w;
		this.resetCamera = true;
	}

	/**
	 * 	Set the way the height map is rendered.
	 *	@param type The type
	 */
	public void setHeightMapType( final HeightMapType type )
	{
		this.renderType = type;
	}

	/**
	 * 	Create a height map from an FImage.
	 *	@param img The image
	 *	@return The height map
	 */
	public static HeightMap3D createFromFImage( final FImage img )
	{
		final HeightMap3D hm = new HeightMap3D( img.getWidth(), img.getHeight() );
		hm.setMaxValue( 10 );
		hm.setMinValue( 0 );
		hm.setData( ArrayUtils.convertToDouble( img.pixels ) );
		return hm;
	}

	/**
	 * 	Example of the height map. Switches between showing a 2D Gaussian and a 2D sinc
	 * 	function.
	 *
	 *	@param args
	 * 	@throws IOException
	 * 	@throws MalformedURLException
	 * 	@throws InterruptedException
	 */
	public static void main( final String[] args ) throws MalformedURLException,
		IOException, InterruptedException
	{
//		HeightMap3D.createFromFImage( ImageUtilities.readF(
//				new URL("http://www.alvaromartin.net/images/surfaceclipmaps/heightmap.jpg") ) );

		final HeightMap3D hm = new HeightMap3D( 1000, 800 );
		hm.setEnableLights( false );
		hm.setMaxValue( 1 );
		hm.setMinValue( -0.2 );

		final double[][] sinc = HeightMap3D.getSinc( 60, 60, 120 );
		final double[][] gauss = HeightMap3D.getGaussian( 60, 60 );

		boolean showingSinc = true;
		hm.setData( sinc );

		while( true )
		{
			showingSinc = !showingSinc;

			Thread.sleep( 2000 );

			if( showingSinc )
					hm.setData( sinc );
			else	hm.setData( gauss );
		}
	}

	/**
	 * 	Helper function that's used to draw a 2d Gaussian function
	 *	@param N The number of patches in X
	 *	@param M The number of patches in Z
	 *	@return The Gaussian data
	 */
	private static double[][] getGaussian( final int N, final int M )
	{
		final double[][] g = new double[M][N];

		final double xo = N/2d;
		final double yo = M/2d;
		final double sx = N/12d;
		final double sy = M/12d;
		final double A = 0.7;

		for( int y = 0; y < M; y++ )
		{
			for( int x = 0; x < N; x++ )
			{
				g[y][x] = A * Math.exp( - (Math.pow(x-xo,2) / Math.pow(2*sx,2)
						+ Math.pow(y-yo,2) / Math.pow(2*sy,2) ));
			}
		}

		return g;
	}

	/**
	 * 	Helper function that's used to draw a 2d sinc function
	 *	@param N The number of patches in X
	 *	@param M The number of patches in Z
	 *	@param scalar The scalar for the sinc function
	 *	@return The sinc data
	 */
	private static double[][] getSinc( final int N, final int M, final double scalar )
	{
		final double[][] s = new double[M][N];

		final double scalex = N/scalar;
		final double scaley = M/scalar;

		for( int y = 0; y < M; y++ )
		{
			for( int x = 0; x < N; x++ )
			{
				double xx = x - N/2d;
				double yy = y - M/2d;
				xx *= scalex;
				yy *= scaley;

				double vx = Math.sin(xx)/xx;
				if( xx == 0 ) vx = 1;
				double vy = Math.sin(yy)/yy;
				if( yy == 0 ) vy = 1;

				s[y][x] = vx * vy;
			}
		}

		return s;
	}
}
