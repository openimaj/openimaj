/**
 *
 */
package org.openimaj.vis.general;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.gl2.GLUgl2;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourMap;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.vis.AnimatedVisualisationListener;
import org.openimaj.vis.AnimatedVisualisationProvider;
import org.openimaj.vis.Visualisation;

import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

/**
 * Plots oneOverDataLength bars in oneOverDataLength 3-dimensional space, which means there are 2 dimensions for
 * representing the coordinate of oneOverDataLength bar.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 4 Jul 2013
 * @version $Author$, $Revision$, $Date$
 */
public class BarVisualisation3D implements Visualisation<double[][]>, GLEventListener,
	AnimatedVisualisationProvider
{
	/** The GLU library we'll use */
	private final GLUgl2 glu = new GLUgl2();

	/** The GLUT library we'll use */
	private final GLUT glut = new GLUT();

	/** The JOGLWindow in which we're drawing */
	private final JOGLWindow window;

	/** The data */
	private double[][] data;

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

	/** Object that provide the camera position over time */
	private CameraPositionProvider cameraPosition;

	/** Text renderer for the axes labels */
	private TextRenderer textRenderer;

	/** The maximum value of the data (for auto scaling) */
	private double max = 1;

	/** Whether to automatically calculate the maximum value and scale */
	private boolean autoScale = true;

	/** Precalculated 1/data.length */
	private double oneOverDataLength;

	/** Precalculated 1/max */
	private double oneOverMax;

	/** Animation listeners */
	private final List<AnimatedVisualisationListener> listeners =
			new ArrayList<AnimatedVisualisationListener>();

	/**
	 *	@param width
	 *	@param height
	 */
	public BarVisualisation3D( final int width, final int height )
	{
		this.window = new JOGLWindow( width, height );
		this.window.getDrawableSurface().addGLEventListener( this );

		final Animator animator = new Animator( this.window.getDrawableSurface() );
//		final FPSAnimator animator = new FPSAnimator( this.window.getDrawableSurface(), 25 );
		animator.add( this.window.getDrawableSurface() );
		animator.start();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.vis.VisualisationImageProvider#updateVis()
	 */
	@Override
	public void updateVis()
	{
		// final float[] pos = this.cameraPosition.getCameraPosition();
		// this.glu.gluLookAt( pos[0], pos[1], pos[2], pos[3], pos[4], pos[5],
		// pos[6], pos[7], pos[8] );
	}

	/**
	 * Renders the visualisation
	 */
	private void renderVis( final GLAutoDrawable drawable )
	{
		final GL2 gl = drawable.getGL().getGL2();
		gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );

		gl.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );
		gl.glLoadIdentity();

		final float[] pos = this.cameraPosition.getCameraPosition();
		this.glu.gluLookAt( pos[0], pos[1], pos[2], pos[3], pos[4], pos[5], pos[6], pos[7], pos[8] );

		gl.glPushMatrix();
		{
			gl.glLineWidth( 2 );
			gl.glColor3f( 1, 1, 1 );

			// Draw the X, Y axes
			final float zero = -0.001f;
			gl.glBegin( GL.GL_LINE_STRIP );
			{
				gl.glColor3f( this.xAxisColour[0], this.xAxisColour[1], this.xAxisColour[2] );
				gl.glVertex3f( 1, zero, zero );
				gl.glVertex3f( zero, zero, zero ); // x-axis
				gl.glColor3f( this.yAxisColour[0], this.yAxisColour[1], this.yAxisColour[2] );
				gl.glVertex3f( zero, 1, zero ); // y-axis
			}
			gl.glEnd();

			gl.glBegin( GL.GL_LINES );
			{
				gl.glColor3f( this.xAxisColour[0], this.xAxisColour[1], this.xAxisColour[2] );
				gl.glVertex3f( zero, zero, zero );
				gl.glColor3f( this.zAxisColour[0], this.zAxisColour[1], this.zAxisColour[2] );
				gl.glVertex3f( zero, zero, -1 ); // z-axis
			}
			gl.glEnd();

			gl.glPushMatrix();
			this.textRenderer.begin3DRendering();
			{
				gl.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );

				this.textRenderer.setColor( 1.0f, 0.2f, 0.2f, 0.8f );
				final float scale = 0.003f;

				final Rectangle2D xNameBounds = this.textRenderer.getBounds( this.xAxisName );
				this.textRenderer.draw3D( this.xAxisName, (float)(1-xNameBounds.getWidth()*scale),
						(float)(-xNameBounds.getHeight()*scale), 0.1f, scale );

				final Rectangle2D yNameBounds = this.textRenderer.getBounds( this.yAxisName );
				this.textRenderer.draw3D( this.yAxisName, (float)(-yNameBounds.getWidth()*scale),
						1f, 0.01f, scale );

				final Rectangle2D zNameBounds = this.textRenderer.getBounds( this.zAxisName );
				this.textRenderer.draw3D( this.zAxisName, (float)(-zNameBounds.getWidth()*scale),
						0f, -1.1f, scale );
			}
			this.textRenderer.end3DRendering();
			gl.glPopMatrix();

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
								this.glut.glutSolidCube( 1f );
								gl.glColor3f( 0, 0, 0 );
								this.glut.glutWireCube( 1f );
							}
							gl.glPopMatrix();
						}
					}
				}
			}
		}
		gl.glPopMatrix();
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
	 * @see org.openimaj.vis.VisualisationImageProvider#getVisualisationImage()
	 */
	@Override
	public MBFImage getVisualisationImage()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.vis.VisualisationImageProvider#setRequiredSize(java.awt.Dimension)
	 */
	@Override
	public void setRequiredSize( final Dimension d )
	{
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
		this.renderVis( drawable );
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.media.opengl.GLEventListener#dispose(javax.media.opengl.GLAutoDrawable)
	 */
	@Override
	public void dispose( final GLAutoDrawable arg0 )
	{
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.media.opengl.GLEventListener#init(javax.media.opengl.GLAutoDrawable)
	 */
	@Override
	public void init( final GLAutoDrawable drawable )
	{
		this.textRenderer = new TextRenderer( new Font( "SansSerif", Font.BOLD, 36 ) );

		final GL2 gl = drawable.getGL().getGL2();
		gl.setSwapInterval( 1 );
		gl.glEnable( GL.GL_DEPTH_TEST );
		// gl.glEnable( GLLightingFunc.GL_LIGHTING );
		// gl.glEnable( GLLightingFunc.GL_LIGHT0 );
		// gl.glEnable( GLLightingFunc.GL_COLOR_MATERIAL );
//		gl.glEnable( GL.GL_BLEND );
//		gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
//		gl.glEnable( GL2GL3.GL_POLYGON_SMOOTH );

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
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.vis.Visualisation#setData(java.lang.Object)
	 */
	@Override
	public void setData( final double[][] data )
	{
		if( data == null ) return;

		if( this.data == null )
			this.data = data;
		else
		{
//			synchronized( data )
			{
				this.data = data;
			}
		}

//		synchronized( data )
		{
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
