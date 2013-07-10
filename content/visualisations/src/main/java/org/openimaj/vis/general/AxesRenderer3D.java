/**
 *
 */
package org.openimaj.vis.general;

import javax.media.opengl.GLAutoDrawable;

import org.openimaj.vis.DataUnitsTransformer;

/**
 *	A class for generating X, Y and Z axes in a 3D visualisation and providing
 *	data unit transformations.
 *
 *	// TODO: Need to add getters/setters
 *	// TODO: Need to implement data transformation
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 10 Jul 2013
 */
public class AxesRenderer3D implements DataUnitsTransformer<float[], double[], double[]>
{
	/** The x axis renderer */
	private final AxisRenderer3D xAxisRenderer = new AxisRenderer3D();

	/** The y axis renderer */
	private final AxisRenderer3D yAxisRenderer = new AxisRenderer3D();

	/** The z axis renderer */
	private final AxisRenderer3D zAxisRenderer = new AxisRenderer3D();

	/**
	 *
	 */
	public AxesRenderer3D()
	{
		this.xAxisRenderer.setGridDirection( -1 );
		this.yAxisRenderer.getConfig().setOrientation( new double[] { 90, 0, 0, 1, 180, 1, 0, 0 } );
		this.zAxisRenderer.getConfig().setOrientation( new double[] { 90, 0, 1, 0 } );
		this.yAxisRenderer.getConfig().getRenderingConfig().setNameOrientation(
				new double[] { 90, 0, 0, 1 } );
		this.yAxisRenderer.getConfig().getRenderingConfig().setNameDirection( -1 );
		this.zAxisRenderer.getConfig().getRenderingConfig().setNameOrientation(
				new double[] { 90, 0, 1, 0, -90, 1, 0, 0 } );
		this.zAxisRenderer.getConfig().getRenderingConfig().setNameDirection( -1 );
		this.xAxisRenderer.getConfig().setName( "x-axis" );
		this.yAxisRenderer.getConfig().setName( "y-axis" );
		this.zAxisRenderer.getConfig().setName( "z-axis" );

		this.setAxisThickness( 4 );
		this.setDrawMajorGrid( false );
		this.setDrawMinorGrid( false );
	}

	/**
	 *	@param b
	 */
	public void setDrawMajorGrid( final boolean b )
	{
		this.xAxisRenderer.getConfig().getRenderingConfig().setDrawMajorGrid( b );
		this.yAxisRenderer.getConfig().getRenderingConfig().setDrawMajorGrid( b );
		this.zAxisRenderer.getConfig().getRenderingConfig().setDrawMajorGrid( b );
	}

	/**
	 *	@param b
	 */
	public void setDrawMinorGrid( final boolean b )
	{
		this.xAxisRenderer.getConfig().getRenderingConfig().setDrawMinorGrid( b );
		this.yAxisRenderer.getConfig().getRenderingConfig().setDrawMinorGrid( b );
		this.zAxisRenderer.getConfig().getRenderingConfig().setDrawMinorGrid( b );
	}

	/**
	 *	@param glad
	 */
	public void renderAxis( final GLAutoDrawable glad )
	{
		this.xAxisRenderer.setGLAD( glad );
		this.yAxisRenderer.setGLAD( glad );
		this.zAxisRenderer.setGLAD( glad );

		this.xAxisRenderer.renderAxis();
		this.yAxisRenderer.renderAxis();
		this.zAxisRenderer.renderAxis();
	}

	/**
	 *	@param d
	 */
	public void setAxisThickness( final double d )
	{
		this.xAxisRenderer.getConfig().getRenderingConfig().setThickness( d );
	}

	/**
	 *	@param glad
	 */
	public void setGLAutoDrawable( final GLAutoDrawable glad )
	{

	}

	@Override
	public void precalc()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public double[] calculatePosition( final double[] units )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] calculateUnitsAt( final double[] position )
	{
		// TODO Auto-generated method stub
		return null;
	}
}
