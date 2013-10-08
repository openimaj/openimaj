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

import java.awt.Font;
import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;

import org.openimaj.image.colour.RGBColour;
import org.openimaj.vis.DataUnitsTransformer;

import com.jogamp.opengl.util.awt.TextRenderer;


/**
 *	A class for drawing rulers in a 3D world.
 *
 *	// TODO : This could be made more efficient using DisplayLists.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 8 Jul 2013
 */
public class AxisRenderer3D
	extends AxisRenderer<float[]>
	implements DataUnitsTransformer<float[], Double, Double>
{
	/** The OpenGL surface we're rendering to */
	private GLAutoDrawable glad;

	/** The length of the axis */
	private final double axisLength = 1;

	private int gridDirection = 1;

	private final TextRenderer textRenderer;

	/**
	 *	Default constructor sets some default axis values
	 */
	public AxisRenderer3D()
	{
		Float[] c = RGBColour.WHITE;
		this.config.getRenderingConfig().setColour( new float[]{ c[0], c[1], c[2] } );
		c = RGBColour.GRAY;
		this.config.getRenderingConfig().setMajorTickColour( new float[]{ c[0], c[1], c[2] } );
		this.config.getRenderingConfig().setMajorGridColour( new float[]{ c[0], c[1], c[2] } );
		this.config.getRenderingConfig().setMinorTickColour( new float[]{ c[0], c[1], c[2] } );
		c = new Float[] {0.3f,0.3f,0.3f};
		this.config.getRenderingConfig().setMinorGridColour( new float[]{ c[0], c[1], c[2] } );
		this.config.getRenderingConfig().setMajorTickLength( 0.04 );
		this.config.getRenderingConfig().setMinorTickLength( 0.01 );
		this.config.getRenderingConfig().setMajorTickSpacing( 1 );
		this.config.getRenderingConfig().setMinorTickSpacing( 0.5 );

		this.config.getRenderingConfig().setThickness( 3 );
		this.config.getRenderingConfig().setMajorTickThickness( 1 );
		this.config.getRenderingConfig().setMinorTickThickness( 0.5 );

		this.textRenderer = new TextRenderer( new Font( "SansSerif", Font.BOLD, 36 ) );
	}

	@Override
	public void drawAxis( final AxisConfig<float[]> config )
	{
		final GL2 gl = this.glad.getGL().getGL2();

		gl.glPushMatrix();

		this.orient( gl );

		final float zero = 0.001f;
		gl.glBegin( GL.GL_LINE_STRIP );
		{
			gl.glLineWidth( (float)config.getRenderingConfig().getThickness() );
			gl.glColor3f( config.getRenderingConfig().getColour()[0],
					config.getRenderingConfig().getColour()[1],
					config.getRenderingConfig().getColour()[2] );

			final float n1 = this.calculatePosition( config.getMinValue() ).floatValue();
			final float n2 = this.calculatePosition( config.getMaxValue() ).floatValue();

			// We draw in the x axis, so the orientation has to be set appropriately
			gl.glVertex3f( n1, zero, zero );
			gl.glVertex3f( n2, zero, zero );
		}
		gl.glEnd();

		gl.glPopMatrix();
	}

	@Override
	public void drawAxisLabel( final AxisConfig<float[]> config )
	{
		final GL2 gl = this.glad.getGL().getGL2();

		gl.glPushMatrix();
//		this.orient( gl );

		final double[] o = this.config.getRenderingConfig().getNameOrientation();
		if( o != null )
		{
			for( int i = 0; i < o.length; i += 4 )
				gl.glRotated( o[i], o[i+1],	o[i+2], o[i+3] );
		}

		this.textRenderer.begin3DRendering();
		{
			gl.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );

			// TODO: Text colour and size
			this.textRenderer.setColor( 1.0f, 0.2f, 0.2f, 0.8f );
			final float scale = 0.003f;

			final Rectangle2D nameBounds = this.textRenderer.getBounds( config.getName() );
			this.textRenderer.draw3D( config.getName(), (float)(1-nameBounds.getWidth()*scale),
					(float)(-nameBounds.getHeight()*scale)
					*config.getRenderingConfig().getNameDirection(), 0.1f, scale );
		}
		this.textRenderer.end3DRendering();
		gl.glPopMatrix();
	}

	@Override
	public void drawMajorTick( final double location, final AxisConfig<float[]> config )
	{
		final GL2 gl = this.glad.getGL().getGL2();

		gl.glPushMatrix();

		this.orient( gl );

		gl.glLineWidth( (float)config.getRenderingConfig().getMajorTickThickness() );
		gl.glColor3f( config.getRenderingConfig().getMajorTickColour()[0],
				config.getRenderingConfig().getMajorTickColour()[1],
				config.getRenderingConfig().getMajorTickColour()[2] );

		final float l = (float)config.getRenderingConfig().getMajorTickLength();
		final float l2 = -l;

		final float ll = this.calculatePosition( location ).floatValue();

		final float zero = 0.001f;
		gl.glBegin( GL.GL_LINE_STRIP );
		{
			// We draw in the x axis, so the orientation has to be set appropriately
			gl.glVertex3f( ll, l, zero );
			gl.glVertex3f( ll, l2, zero );
		}
		gl.glEnd();

		gl.glBegin( GL.GL_LINE_STRIP );
		{
			// We draw in the x axis, so the orientation has to be set appropriately
			gl.glVertex3f( ll, zero, l );
			gl.glVertex3f( ll, zero, l2 );
		}
		gl.glEnd();

		gl.glPopMatrix();
	}

	@Override
	public void drawMajorTickGridline( final double location, final AxisConfig<float[]> config )
	{
		final GL2 gl = this.glad.getGL().getGL2();

		gl.glPushMatrix();

		this.orient( gl );

		gl.glLineWidth( (float)config.getRenderingConfig().getMajorGridThickness() );
		gl.glColor3f( config.getRenderingConfig().getMajorGridColour()[0],
				config.getRenderingConfig().getMajorGridColour()[1],
				config.getRenderingConfig().getMajorGridColour()[2] );

		final float ll = this.calculatePosition( location ).floatValue();

		final float zero = 0.001f;
		gl.glBegin( GL.GL_LINE_STRIP );
		{
			// We draw in the x axis, so the orientation has to be set appropriately
			gl.glVertex3f( ll, zero, zero );
			gl.glVertex3f( ll, 1, zero );
		}
		gl.glEnd();

		gl.glBegin( GL.GL_LINE_STRIP );
		{
			// We draw in the x axis, so the orientation has to be set appropriately
			gl.glVertex3f( ll, zero, zero );
			gl.glVertex3f( ll, zero, 1*this.gridDirection );
		}
		gl.glEnd();

		gl.glPopMatrix();
	}

	@Override
	public void drawMinorTick( final double location, final AxisConfig<float[]> config )
	{
		final GL2 gl = this.glad.getGL().getGL2();

		gl.glPushMatrix();

		this.orient( gl );

		final float zero = 0.001f;
		gl.glBegin( GL.GL_LINE_STRIP );
		{
//			gl.glEnable( GL2.GL_LINE_STIPPLE );
//			gl.glLineStipple( 2, (short) 0x00FF );
			gl.glLineWidth( (float)config.getRenderingConfig().getMinorTickThickness() );
			gl.glColor3f( config.getRenderingConfig().getMinorTickColour()[0],
					config.getRenderingConfig().getMinorTickColour()[1],
					config.getRenderingConfig().getMinorTickColour()[2] );

			final float l = (float)config.getRenderingConfig().getMinorTickLength();
			final float l2 = -l;

			final float ll = this.calculatePosition( location ).floatValue();

			// We draw in the x axis, so the orientation has to be set appropriately
			gl.glVertex3f( ll, l, zero );
			gl.glVertex3f( ll, l2, zero );
		}
		gl.glEnd();

		gl.glPopMatrix();
	}

	@Override
	public void drawMinorTickGridline( final double location, final AxisConfig<float[]> config )
	{
		final GL2 gl = this.glad.getGL().getGL2();

		gl.glPushMatrix();

		this.orient( gl );

		gl.glLineWidth( (float)config.getRenderingConfig().getMinorGridThickness() );
		gl.glColor3f( config.getRenderingConfig().getMinorGridColour()[0],
				config.getRenderingConfig().getMinorGridColour()[1],
				config.getRenderingConfig().getMinorGridColour()[2] );
        final float[] rgba = { config.getRenderingConfig().getMinorGridColour()[0],
				config.getRenderingConfig().getMinorGridColour()[1],
				config.getRenderingConfig().getMinorGridColour()[2] };
        gl.glMaterialfv( GL.GL_FRONT, GLLightingFunc.GL_AMBIENT, rgba, 0);
        gl.glMaterialfv( GL.GL_FRONT, GLLightingFunc.GL_SPECULAR, rgba, 0);
        gl.glMaterialf( GL.GL_FRONT, GLLightingFunc.GL_SHININESS, 0f);

		final float ll = this.calculatePosition( location ).floatValue();

		final float zero = 0.001f;
		gl.glBegin( GL.GL_LINE_STRIP );
		{
			// We draw in the x axis, so the orientation has to be set appropriately
			gl.glVertex3f( ll, zero, zero );
			gl.glVertex3f( ll, 1, zero );
		}
		gl.glEnd();

		gl.glBegin( GL.GL_LINE_STRIP );
		{
			// We draw in the x axis, so the orientation has to be set appropriately
			gl.glVertex3f( ll, zero, zero );
			gl.glVertex3f( ll, zero, 1*this.gridDirection );
		}
		gl.glEnd();

		gl.glPopMatrix();
	}

	private void orient( final GL2 gl )
	{
		if( this.config.getOrientation()[0] != 0 )
		{
			gl.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );
			final double[] o = this.config.getOrientation();
			for( int i = 0; i < o.length; i+=4 )
				gl.glRotated( o[i], o[i+1], o[i+2], o[i+3] );
		}
	}

	/**
	 *	@param glad
	 */
	public void setGLAD( final GLAutoDrawable glad )
	{
		this.glad = glad;
	}

	@Override
	public Double calculatePosition( final Double units )
	{
		return this.axisLength/(this.config.getMaxValue()-this.config.getMinValue()) * units;
	}

	@Override
	public Double calculateUnitsAt( final Double position )
	{
		return position / (this.axisLength/(this.config.getMaxValue()-this.config.getMinValue()));
	}

	/**
	 *	@param i
	 */
	public void setGridDirection( final int i )
	{
		this.gridDirection  = i;
	}

	@Override
	public Double scaleDimension( final Double dimension )
	{
		return dimension * (this.axisLength/(this.config.getMaxValue()-this.config.getMinValue()));
	}
}
