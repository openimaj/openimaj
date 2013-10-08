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

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import org.openimaj.image.colour.RGBColour;
import org.openimaj.vis.Visualisation3D;
import org.openimaj.vis.general.XYPlotVisualisation.LocatedObject;
import org.openimaj.vis.general.XYZVisualisation3D.LocatedObject3D;

/**
 *	3 dimensional plot of objects. This class provides the axes and the rendering context
 *	in which items may be plotted. Provide an {@link ItemPlotter3D} for plotting the items
 *	in the visualisation.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 11 Jul 2013
 *	@version $Author$, $Revision$, $Date$
 * 	@param <D> Plottable item
 */
public class XYZVisualisation3D<D> extends Visualisation3D<List<LocatedObject3D<D>>>
{
	/**
	 *	An extension of a {@link LocatedObject} into 3D.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 11 Jul 2013
	 *	@version $Author$, $Revision$, $Date$
	 * 	@param <D> The type of the object
	 */
	public static class LocatedObject3D<D> extends LocatedObject<D>
	{
		/** The Z position */
		public double z;

		/**
		 * 	Constructor
		 *	@param x x-coordinate
		 *	@param y y-coordinate
		 *	@param z z-coordinate
		 *	@param object The object to plot
		 */
		public LocatedObject3D( final double x, final double y, final double z, final D object )
		{
			super( x, y, object );
			this.z = z;
		}
	}

	private AxesRenderer3D axesRenderer;

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

	/** Whether to automatically calculate the maximum value and scale */
	private boolean autoScale = true;

	/** The plotter for the items in the display */
	protected ItemPlotter3D<D> plotter = null;

	/**
	 * 	Constructor that takes the item plotter
	 * 	@param width
	 * 	@param height
	 *	@param plotter
	 */
	public XYZVisualisation3D( final int width, final int height, final ItemPlotter3D<D> plotter )
	{
		super( width, height );
		this.plotter = plotter;
		this.init();
	}

	/**
	 * 	Constructor that takes the item plotter
	 * 	@param width
	 * 	@param height
	 */
	public XYZVisualisation3D( final int width, final int height )
	{
		super( width, height );
		this.init();
	}

	/**
	 *	Initialisation routine
	 */
	private void init()
	{
		this.data = new ArrayList<LocatedObject3D<D>>();
		this.axesRenderer = new AxesRenderer3D();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.VisualisationImageProvider#updateVis()
	 */
	@Override
	public void updateVis()
	{
	}

	/**
	 * 	Returns the object rendering the axes
	 *	@return The axes renderer
	 */
	public AxesRenderer3D getAxesRenderer()
	{
		return this.axesRenderer;
	}

	@Override
	protected void renderVis( final GLAutoDrawable drawable )
	{
		if( drawable == null || this.axesRenderer == null ) return;

		final GL2 gl = drawable.getGL().getGL2();

		this.axesRenderer.renderAxis( drawable );

		final List<LocatedObject3D<D>> x = new ArrayList<XYZVisualisation3D.LocatedObject3D<D>>();
		x.addAll( this.data );
		for( final LocatedObject3D<D> d : x )
		{
			gl.glPushMatrix();
			this.plotter.plotObject( drawable, d, this.axesRenderer );
			gl.glPopMatrix();
		}
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
	 *	@return the plotter
	 */
	public ItemPlotter3D<D> getPlotter()
	{
		return this.plotter;
	}

	/**
	 *	@param plotter the plotter to set
	 */
	public void setPlotter( final ItemPlotter3D<D> plotter )
	{
		this.plotter = plotter;
	}
}
