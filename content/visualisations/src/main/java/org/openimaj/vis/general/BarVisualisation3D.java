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

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.fixedfunc.GLMatrixFunc;

import org.openimaj.image.colour.ColourMap;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.vis.Visualisation3D;

/**
 * Plots oneOverDataLength bars in oneOverDataLength 3-dimensional space, which means there are 2 dimensions for
 * representing the coordinate of oneOverDataLength bar.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 4 Jul 2013
 * @version $Author$, $Revision$, $Date$
 */
public class BarVisualisation3D extends Visualisation3D<double[][]>
{
	private AxesRenderer3D axesRenderer;

	/** The colour map for the bars */
	private ColourMap colourMap = ColourMap.Autumn;

	/** The name of the x axis */
	private String xAxisName = "X-Axis";

	/** The name of the y axis */
	private String yAxisName = "Y-Axis";

	/** The name of the z axis */
	private String zAxisName = "Z-Axis";

	/** The colour of the x axis */
	private Float[] xAxisColour = RGBColour.WHITE;

	/** The colour of the y axis */
	private Float[] yAxisColour = RGBColour.GREEN;

	/** The colour of the z axis */
	private Float[] zAxisColour = RGBColour.BLUE;

	/** The maximum value of the data (for auto scaling) */
	private double max = 1;

	/** Whether to automatically calculate the maximum value and scale */
	private boolean autoScale = true;

	/** Precalculated 1/data.length */
	private double oneOverDataLength;

	/** Precalculated 1/max */
	private double oneOverMax;

	/**
	 *	@param width
	 *	@param height
	 */
	public BarVisualisation3D( final int width, final int height )
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
	 * Renders the visualisation
	 */
	@Override
	protected void renderVis( final GLAutoDrawable drawable )
	{
		if( drawable == null || this.axesRenderer == null ) return;

		final GL2 gl = drawable.getGL().getGL2();
		gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );

		this.axesRenderer.renderAxis( drawable );

		// Create the boxes
		if( this.data != null )
		{
//				synchronized( this.data )
			{
				for( int z = 0; z < this.data.length; z++ )
				{
					final double b = 1d / this.data[z].length;
					for( int x = 0; x < this.data[z].length; x++ )
					{
						final double v = this.oneOverMax * this.data[z][x];
						gl.glPushMatrix();
						{
							final float[] colour = new float[3];
							this.colourMap.apply( (float) (this.data[z][x] / this.max), colour );
							gl.glColor3f( colour[0], colour[1], colour[2] );
							gl.glTranslatef(
									(float) (b * x + b / 2d),
									(float) (v / 2d),
									(float)(this.oneOverDataLength * z + this.oneOverDataLength / 2d)-1f );
							gl.glScalef( (float) b, (float)Math.abs(v), (float) this.oneOverDataLength );
							gl.glEnable( GL.GL_POLYGON_OFFSET_FILL );
							gl.glPolygonOffset( 1, 1 );
							this.glut.glutSolidCube( 1f );
							gl.glDisable( GL.GL_POLYGON_OFFSET_FILL );
							gl.glColor3f( 0, 0, 0 );
							this.glut.glutWireCube( 1f );
						}
						gl.glPopMatrix();
					}
				}
			}
		}
	}

	protected DoubleBuffer get2dPoint( final GL2 gl, final double x, final double y, final double z )
	{
		final DoubleBuffer model = DoubleBuffer.allocate(16);
		gl.glGetDoublev( GLMatrixFunc.GL_MODELVIEW_MATRIX, model );

		final DoubleBuffer proj = DoubleBuffer.allocate(16);
		gl.glGetDoublev( GLMatrixFunc.GL_PROJECTION_MATRIX, proj );

		final IntBuffer view = IntBuffer.allocate(4);
		gl.glGetIntegerv( GL.GL_VIEWPORT, view );

		final DoubleBuffer winPos = DoubleBuffer.allocate(3);
		final boolean b = this.glu.gluProject( x, y, z, model, proj, view, winPos );

		if( !b ) System.out.println( "FAIL ");
		return winPos;
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

		this.axesRenderer = new AxesRenderer3D();

		// Set the initial look at
		final float eyeX = 0.5f, eyeY = 1f, eyeZ = 2f;
		final float lookAtX = 0.5f, lookAtY = 0, lookAtZ = -1f;
		final float upX = 0, upY = 1, upZ = 0;
		this.glu.gluLookAt( eyeX, eyeY, eyeZ, lookAtX, lookAtY, lookAtZ, upX, upY, upZ );

		// Instantiate the camera mover
		this.cameraPosition = new RotatingCameraProvider(
				eyeX, eyeY, eyeZ,
				lookAtX, lookAtY, lookAtZ,
				0.0004f, 0.0004f, 0f, 0.75f );
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.vis.Visualisation#setData(java.lang.Object)
	 */
	@Override
	public void setData( final double[][] data )
	{

		super.setData( data );

		if( this.autoScale )
		{
			this.max = 0;
			for( final double[] d : this.data )
				this.max = Math.max( this.max,
						Math.max( Math.abs( ArrayUtils.maxValue( d ) ),
								Math.abs( ArrayUtils.minValue(d) ) ) );
		}

		this.oneOverDataLength = 1d / this.data.length;
		this.oneOverMax = 1d / this.max;
	}

	/**
	 * 	Set the maximum data value
	 *	@param max The maximum
	 */
	public void setMaximum( final double max )
	{
		this.max = max;
		this.oneOverMax = 1d / this.max;
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
	 *	@return the xAxisName
	 */
	public String getxAxisName()
	{
		return this.xAxisName;
	}

	/**
	 *	@param xAxisName the xAxisName to set
	 */
	public void setxAxisName( final String xAxisName )
	{
		this.xAxisName = xAxisName;
	}

	/**
	 *	@return the yAxisName
	 */
	public String getyAxisName()
	{
		return this.yAxisName;
	}

	/**
	 *	@param yAxisName the yAxisName to set
	 */
	public void setyAxisName( final String yAxisName )
	{
		this.yAxisName = yAxisName;
	}

	/**
	 *	@return the zAxisName
	 */
	public String getzAxisName()
	{
		return this.zAxisName;
	}

	/**
	 *	@param zAxisName the zAxisName to set
	 */
	public void setzAxisName( final String zAxisName )
	{
		this.zAxisName = zAxisName;
	}

	/**
	 *	@return the xAxisColour
	 */
	public Float[] getxAxisColour()
	{
		return this.xAxisColour;
	}

	/**
	 *	@param xAxisColour the xAxisColour to set
	 */
	public void setxAxisColour( final Float[] xAxisColour )
	{
		this.xAxisColour = xAxisColour;
	}

	/**
	 *	@return the yAxisColour
	 */
	public Float[] getyAxisColour()
	{
		return this.yAxisColour;
	}

	/**
	 *	@param yAxisColour the yAxisColour to set
	 */
	public void setyAxisColour( final Float[] yAxisColour )
	{
		this.yAxisColour = yAxisColour;
	}

	/**
	 *	@return the zAxisColour
	 */
	public Float[] getzAxisColour()
	{
		return this.zAxisColour;
	}

	/**
	 *	@param zAxisColour the zAxisColour to set
	 */
	public void setzAxisColour( final Float[] zAxisColour )
	{
		this.zAxisColour = zAxisColour;
	}

	/**
	 *	@return the cameraPosition
	 */
	public CameraPositionProvider getCameraPosition()
	{
		return this.cameraPosition;
	}

	/**
	 *	@param cameraPosition the cameraPosition to set
	 */
	public void setCameraPosition( final CameraPositionProvider cameraPosition )
	{
		this.cameraPosition = cameraPosition;
	}

	/**
	 *	@return the autoScale
	 */
	public boolean isAutoScale()
	{
		return this.autoScale;
	}

	/**
	 *	@param autoScale the autoScale to set
	 */
	public void setAutoScale( final boolean autoScale )
	{
		this.autoScale = autoScale;
	}

	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
		final BarVisualisation3D bv = new BarVisualisation3D( 1000, 1000 );
		bv.setData( new double[][]
		{
			{ 6, 7, 8, 9, 10 },
			{ 5, 6, 7, 8, 9 },
			{ 4, 5, 6, 7, 8 },
			{ 3, 4, 5, 6, 7 },
			{ 2, 3, 4, 5, 6 },
			{ 1, 2, 3, 4, 5 },
			{ 0, 1, 2, 3, 4 },
			{-1, 0, 1, 2, 3 },
			{-2, -1, 0, 1, 2},
			{-3, -2, -1, 0, 1},
			{-4, -3, -2, -1, 0},
			{-5, -4, -3, -2, -1}
		});
	}
}
