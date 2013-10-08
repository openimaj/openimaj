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

import org.openimaj.image.typography.Font;

/**
 *	Configuration for an axis.
 *
 * 	@param <Q> The colour representation of an axis.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 8 Jul 2013
 */
public class AxisConfig<Q>
{
	/** n dimensional position */
	private double[] location = new double[] {0,0};

	/** Whether to auto scale to fit the view. If so, the location may be null */
	private boolean autoScale = true;

	/** n dimensional orientation */
	private double[] orientation = new double[] {0};

	/** Name of the axis */
	private String name = "Axis";

	/** Maximum value */
	private double maxValue = 10;

	/** Minimum value */
	private double minValue = 0;

	/** The rendering configuration */
	private AxisRenderingConfig<Q> renderingConfig = new AxisRenderingConfig<Q>();

	/**
	 *
	 *	@param <Q>
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 8 Jul 2013
	 */
	public static class AxisRenderingConfig<Q>
	{
		/** Whether to render this axis at all */
		private boolean renderAxis = true;

		/** Colour to draw the main axis */
		private Q colour;

		/** The thickness of the axis line */
		private double thickness = 3;

		/** Whether to draw the minor ticks */
		private boolean drawMinorTicks = true;

		/** Whether to draw the major ticks */
		private boolean drawMajorTicks = true;

		/** How often to draw the minor ticks */
		private double minorTickSpacing = 1;

		/** How often to drawn the major ticks */
		private double majorTickSpacing = 1;

		/** Length of the minor ticks */
		private double minorTickLength = 7;

		/** Length of the major ticks */
		private double majorTickLength = 11;

		/** The thickness of the major ticks */
		private double majorTickThickness = 3;

		/** The thickness of the minor ticks */
		private double minorTickThickness = 1;

		/** Colour of the minor ticks */
		private Q minorTickColour;

		/** Colour of the major ticks */
		private Q majorTickColour;

		/** Whether to draw the minor tick labels */
		private boolean drawMinorTickLabels = false;

		/** How often to draw the ticks (should be integer multiple of tick spacing */
		private double minorTickLabelSpacing = 1;

		/** Whether to draw the major tick labels */
		private boolean drawMajorTickLabels = false;

		/** How often to draw the ticks (should be integer multiple of tick spacing */
		private double majorTickLabelSpacing = 1;

		/** The font to use for major ticks */
		@SuppressWarnings( "rawtypes" )
		private Font majorTickLabelFont;

		/** The font to use for minor ticks */
		@SuppressWarnings( "rawtypes" )
		private Font minorTickLabelFont;

		/** Whether to fit labels in the spaces */
		private boolean autoSpaceLabels = true;

		/** Whether to remove ticks if they get too close together */
		private boolean autoSpaceTicks = true;

		/** Size of the axis name label */
		private double nameSize = 10;

		/** Colour of the axis name label */
		private Q nameColour;

		/** The label transformer to convert a unit to a label */
		private LabelTransformer labelTransformer;

		/** Whether to draw the minor grid */
		private boolean drawMinorGrid = false;

		/** Whether to draw the major grid */
		private boolean drawMajorGrid = false;

		/** The colour of the minor grid */
		private Q minorGridColour;

		/** The colour of the major grid */
		private Q majorGridColour;

		/** The thickness of the lines of the major grid */
		private double majorGridThickness = 1;

		/** The thickness of the lines on the minor grid */
		private double minorGridThickness = 1;

		/** Set if the axis name should be oriented */
		private double[] nameOrientation = null;

		/** The direction of the name */
		private int nameDirection = 1;

		/**
		 *	@return the colour
		 */
		public Q getColour()
		{
			return this.colour;
		}

		/**
		 *	@param colour the colour to set
		 */
		public void setColour( final Q colour )
		{
			this.colour = colour;
		}

		/**
		 *	@return the thickness
		 */
		public double getThickness()
		{
			return this.thickness;
		}

		/**
		 *	@param thickness the thickness to set
		 */
		public void setThickness( final double thickness )
		{
			this.thickness = thickness;
		}

		/**
		 *	@return the drawMinorTicks
		 */
		public boolean isDrawMinorTicks()
		{
			return this.drawMinorTicks;
		}

		/**
		 *	@param drawMinorTicks the drawMinorTicks to set
		 */
		public void setDrawMinorTicks( final boolean drawMinorTicks )
		{
			this.drawMinorTicks = drawMinorTicks;
		}

		/**
		 *	@return the drawMajorTicks
		 */
		public boolean isDrawMajorTicks()
		{
			return this.drawMajorTicks;
		}

		/**
		 *	@param drawMajorTicks the drawMajorTicks to set
		 */
		public void setDrawMajorTicks( final boolean drawMajorTicks )
		{
			this.drawMajorTicks = drawMajorTicks;
		}

		/**
		 *	@return the minorTickSpacing
		 */
		public double getMinorTickSpacing()
		{
			return this.minorTickSpacing;
		}

		/**
		 *	@param minorTickSpacing the minorTickSpacing to set
		 */
		public void setMinorTickSpacing( final double minorTickSpacing )
		{
			this.minorTickSpacing = minorTickSpacing;
		}

		/**
		 *	@return the majorTickSpacing
		 */
		public double getMajorTickSpacing()
		{
			return this.majorTickSpacing;
		}

		/**
		 *	@param majorTickSpacing the majorTickSpacing to set
		 */
		public void setMajorTickSpacing( final double majorTickSpacing )
		{
			this.majorTickSpacing = majorTickSpacing;
		}

		/**
		 *	@return the minorTickLength
		 */
		public double getMinorTickLength()
		{
			return this.minorTickLength;
		}

		/**
		 *	@param minorTickLength the minorTickLength to set
		 */
		public void setMinorTickLength( final double minorTickLength )
		{
			this.minorTickLength = minorTickLength;
		}

		/**
		 *	@return the majorTickLength
		 */
		public double getMajorTickLength()
		{
			return this.majorTickLength;
		}

		/**
		 *	@param majorTickLength the majorTickLength to set
		 */
		public void setMajorTickLength( final double majorTickLength )
		{
			this.majorTickLength = majorTickLength;
		}

		/**
		 *	@return the minorTickColour
		 */
		public Q getMinorTickColour()
		{
			return this.minorTickColour;
		}

		/**
		 *	@param minorTickColour the minorTickColour to set
		 */
		public void setMinorTickColour( final Q minorTickColour )
		{
			this.minorTickColour = minorTickColour;
		}

		/**
		 *	@return the majorTickColour
		 */
		public Q getMajorTickColour()
		{
			return this.majorTickColour;
		}

		/**
		 *	@param majorTickColour the majorTickColour to set
		 */
		public void setMajorTickColour( final Q majorTickColour )
		{
			this.majorTickColour = majorTickColour;
		}

		/**
		 *	@return the drawMinorTickLabels
		 */
		public boolean isDrawMinorTickLabels()
		{
			return this.drawMinorTickLabels;
		}

		/**
		 *	@param drawMinorTickLabels the drawMinorTickLabels to set
		 */
		public void setDrawMinorTickLabels( final boolean drawMinorTickLabels )
		{
			this.drawMinorTickLabels = drawMinorTickLabels;
		}

		/**
		 *	@return the minorTickLabelSpacing
		 */
		public double getMinorTickLabelSpacing()
		{
			return this.minorTickLabelSpacing;
		}

		/**
		 *	@param minorTickLabelSpacing the minorTickLabelSpacing to set
		 */
		public void setMinorTickLabelSpacing( final double minorTickLabelSpacing )
		{
			this.minorTickLabelSpacing = minorTickLabelSpacing;
		}

		/**
		 *	@return the drawMajorTickLabels
		 */
		public boolean isDrawMajorTickLabels()
		{
			return this.drawMajorTickLabels;
		}

		/**
		 *	@param drawMajorTickLabels the drawMajorTickLabels to set
		 */
		public void setDrawMajorTickLabels( final boolean drawMajorTickLabels )
		{
			this.drawMajorTickLabels = drawMajorTickLabels;
		}

		/**
		 *	@return the majorTickLabelSpacing
		 */
		public double getMajorTickLabelSpacing()
		{
			return this.majorTickLabelSpacing;
		}

		/**
		 *	@param majorTickLabelSpacing the majorTickLabelSpacing to set
		 */
		public void setMajorTickLabelSpacing( final double majorTickLabelSpacing )
		{
			this.majorTickLabelSpacing = majorTickLabelSpacing;
		}

		/**
		 *	@return the majorTickLabelFont
		 */
		@SuppressWarnings( "rawtypes" )
		public Font getMajorTickLabelFont()
		{
			return this.majorTickLabelFont;
		}

		/**
		 *	@param majorTickLabelFont the majorTickLabelFont to set
		 */
		@SuppressWarnings( "rawtypes" )
		public void setMajorTickLabelFont( final Font majorTickLabelFont )
		{
			this.majorTickLabelFont = majorTickLabelFont;
		}

		/**
		 *	@return the minorTickLabelFont
		 */
		@SuppressWarnings( "rawtypes" )
		public Font getMinorTickLabelFont()
		{
			return this.minorTickLabelFont;
		}

		/**
		 *	@param minorTickLabelFont the minorTickLabelFont to set
		 */
		@SuppressWarnings( "rawtypes" )
		public void setMinorTickLabelFont( final Font minorTickLabelFont )
		{
			this.minorTickLabelFont = minorTickLabelFont;
		}

		/**
		 *	@return the autoSpaceLabels
		 */
		public boolean isAutoSpaceLabels()
		{
			return this.autoSpaceLabels;
		}

		/**
		 *	@param autoSpaceLabels the autoSpaceLabels to set
		 */
		public void setAutoSpaceLabels( final boolean autoSpaceLabels )
		{
			this.autoSpaceLabels = autoSpaceLabels;
		}

		/**
		 *	@return the autoSpaceTicks
		 */
		public boolean isAutoSpaceTicks()
		{
			return this.autoSpaceTicks;
		}

		/**
		 *	@param autoSpaceTicks the autoSpaceTicks to set
		 */
		public void setAutoSpaceTicks( final boolean autoSpaceTicks )
		{
			this.autoSpaceTicks = autoSpaceTicks;
		}

		/**
		 *	@return the nameSize
		 */
		public double getNameSize()
		{
			return this.nameSize;
		}

		/**
		 *	@param nameSize the nameSize to set
		 */
		public void setNameSize( final double nameSize )
		{
			this.nameSize = nameSize;
		}

		/**
		 *	@return the nameColour
		 */
		public Q getNameColour()
		{
			return this.nameColour;
		}

		/**
		 *	@param nameColour the nameColour to set
		 */
		public void setNameColour( final Q nameColour )
		{
			this.nameColour = nameColour;
		}

		/**
		 *	@return the labelTransformer
		 */
		public LabelTransformer getLabelTransformer()
		{
			return this.labelTransformer;
		}

		/**
		 *	@param labelTransformer the labelTransformer to set
		 */
		public void setLabelTransformer( final LabelTransformer labelTransformer )
		{
			this.labelTransformer = labelTransformer;
		}

		/**
		 *	@return the drawMinorGrid
		 */
		public boolean isDrawMinorGrid()
		{
			return this.drawMinorGrid;
		}

		/**
		 *	@param drawMinorGrid the drawMinorGrid to set
		 */
		public void setDrawMinorGrid( final boolean drawMinorGrid )
		{
			this.drawMinorGrid = drawMinorGrid;
		}

		/**
		 *	@return the drawMajorGrid
		 */
		public boolean isDrawMajorGrid()
		{
			return this.drawMajorGrid;
		}

		/**
		 *	@param drawMajorGrid the drawMajorGrid to set
		 */
		public void setDrawMajorGrid( final boolean drawMajorGrid )
		{
			this.drawMajorGrid = drawMajorGrid;
		}

		/**
		 *	@return the minorGridColour
		 */
		public Q getMinorGridColour()
		{
			return this.minorGridColour;
		}

		/**
		 *	@param minorGridColour the minorGridColour to set
		 */
		public void setMinorGridColour( final Q minorGridColour )
		{
			this.minorGridColour = minorGridColour;
		}

		/**
		 *	@return the majorGridColour
		 */
		public Q getMajorGridColour()
		{
			return this.majorGridColour;
		}

		/**
		 *	@param majorGridColour the majorGridColour to set
		 */
		public void setMajorGridColour( final Q majorGridColour )
		{
			this.majorGridColour = majorGridColour;
		}

		/**
		 *	@return the majorGridThickness
		 */
		public double getMajorGridThickness()
		{
			return this.majorGridThickness;
		}

		/**
		 *	@param majorGridThickness the majorGridThickness to set
		 */
		public void setMajorGridThickness( final double majorGridThickness )
		{
			this.majorGridThickness = majorGridThickness;
		}

		/**
		 *	@return the minorGridThickness
		 */
		public double getMinorGridThickness()
		{
			return this.minorGridThickness;
		}

		/**
		 *	@param minorGridThickness the minorGridThickness to set
		 */
		public void setMinorGridThickness( final double minorGridThickness )
		{
			this.minorGridThickness = minorGridThickness;
		}

		/**
		 *	Returns whether an axis should be rendered
		 *	@return Whether the axis should be rendered
		 */
		public boolean isRenderAxis()
		{
			return this.renderAxis;
		}

		/**
		 *
		 *	@param renderAxis
		 */
		public void setRenderAxis( final boolean renderAxis )
		{
			this.renderAxis = renderAxis;
		}

		/**
		 *	@return the majorTickThickness
		 */
		public double getMajorTickThickness()
		{
			return this.majorTickThickness;
		}

		/**
		 *	@param majorTickThickness the majorTickThickness to set
		 */
		public void setMajorTickThickness( final double majorTickThickness )
		{
			this.majorTickThickness = majorTickThickness;
		}

		/**
		 *	@return the minorTickThickness
		 */
		public double getMinorTickThickness()
		{
			return this.minorTickThickness;
		}

		/**
		 *	@param minorTickThickness the minorTickThickness to set
		 */
		public void setMinorTickThickness( final double minorTickThickness )
		{
			this.minorTickThickness = minorTickThickness;
		}

		/**
		 *	@return the nameOrientation
		 */
		public double[] getNameOrientation()
		{
			return this.nameOrientation;
		}

		/**
		 *	@param nameOrientation the nameOrientation to set
		 */
		public void setNameOrientation( final double[] nameOrientation )
		{
			this.nameOrientation = nameOrientation;
		}

		/**
		 *
		 *	@param i
		 */
		public void setNameDirection( final int i )
		{
			this.nameDirection = i;
		}

		/**
		 *	@return the nameDirection
		 */
		public int getNameDirection()
		{
			return this.nameDirection;
		}
	}


	/**
	 *	@return the location
	 */
	public double[] getLocation()
	{
		return this.location;
	}


	/**
	 *	@param location the location to set
	 */
	public void setLocation( final double[] location )
	{
		this.location = location;
	}


	/**
	 *	@return the orientation
	 */
	public double[] getOrientation()
	{
		return this.orientation;
	}


	/**
	 *	@param orientation the orientation to set
	 */
	public void setOrientation( final double[] orientation )
	{
		this.orientation = orientation;
	}


	/**
	 *	@return the name
	 */
	public String getName()
	{
		return this.name;
	}


	/**
	 *	@param name the name to set
	 */
	public void setName( final String name )
	{
		this.name = name;
	}


	/**
	 *	@return the maxValue
	 */
	public double getMaxValue()
	{
		return this.maxValue;
	}


	/**
	 *	@param maxValue the maxValue to set
	 */
	public void setMaxValue( final double maxValue )
	{
		this.maxValue = maxValue;
	}


	/**
	 *	@return the minValue
	 */
	public double getMinValue()
	{
		return this.minValue;
	}


	/**
	 *	@param minValue the minValue to set
	 */
	public void setMinValue( final double minValue )
	{
		this.minValue = minValue;
	}


	/**
	 *	@return the renderingConfig
	 */
	public AxisRenderingConfig<Q> getRenderingConfig()
	{
		return this.renderingConfig;
	}


	/**
	 *
	 *	@param config The rendering config
	 */
	public void setRenderingConfig( final AxisRenderingConfig<Q> config )
	{
		this.renderingConfig = config;
	}


	/**
	 * 	Whether the axis should attempt to auto scale to fit the view
	 *	@return TRUE to auto scale
	 */
	public boolean isAutoScale()
	{
		return this.autoScale;
	}

	/**
	 * 	Set whether the axis should attempt to auto scale to fit the view
	 *	@param autoScale Whether to auto scale
	 */
	public void setAutoScale( final boolean autoScale )
	{
		this.autoScale = autoScale;
	}
}
