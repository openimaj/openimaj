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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *	Some tests of the Bar visualisation to ensure the axes are positioned correctly.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 20 May 2013
 */
public class BarVisualisationBasicTest
{
	/** The visualisation to test */
	private BarVisualisationBasic bv;

	/**
	 * 	Create some data less than 0, so the axis is above.
	 *	@return data
	 */
	private double[] dataLessThan0()
	{
		return new double[]{ -8, -7, -5, -4 };
	}

	/**
	 * 	Create some data greater than 0, so the axis is below
	 *	@return
	 */
	private double[] dataGreaterThan0()
	{
		return new double[]{ 8, 7, 5, 4 };
	}

	/**
	 * 	Create some data, uncentered around 0, so the axis is
	 * 	visible but not centred.
	 *	@return data
	 */
	private double[] dataDistributedAround0()
	{
		return new double[] { -8, -4, 8, 16 };
	}

	/**
	 * 	Show the vis. Remove the annotation to avoid this
	 * @throws InterruptedException
	 */
//	@After
	public void showVis() throws InterruptedException
	{
		this.bv.showWindow( "Test" );
		Thread.sleep( 5000 );
	}

	/**
	 * 	Create the bar vis
	 */
	@Before
	public void setUp()
	{
		this.bv = new BarVisualisationBasic( 800, 400 );
	}

	/**
	 * 	Test data below zero with a fixed position 0 axis
	 */
	@Test
	public void testFixedBelowZero()
	{
		this.bv.setAxisLocation( 200 );
		this.bv.setData( this.dataLessThan0() );

		Assert.assertEquals( true, this.bv.isFixAxis() );
		Assert.assertEquals( 16, this.bv.getAxisRangeY(), 0 );
		Assert.assertEquals( 25, this.bv.getYscale(), 0 );
		Assert.assertEquals( 200, this.bv.getAxisLocation(), 0 );
	}

	/**
	 * 	Test data below zero with a floating axis
	 */
	@Test
	public void testBelowZero()
	{
		this.bv.setFixAxis( false );
		this.bv.setAxisAlwaysVisible( false );
		this.bv.setData( this.dataLessThan0() );

		Assert.assertEquals( false, this.bv.isFixAxis() );
		Assert.assertEquals( false, this.bv.isAxisAlwaysVisible() );
		Assert.assertEquals( 4, this.bv.getAxisRangeY(), 0 );
		Assert.assertEquals( 100, this.bv.getYscale(), 0 );
		Assert.assertEquals( -400, this.bv.getAxisLocation(), 0 );
	}

	/**
	 * 	Test data below zero with a floating axis that's always visible
	 */
	@Test
	public void testBelowZeroAlwaysVis()
	{
		this.bv.setFixAxis( false );
		this.bv.setAxisAlwaysVisible( true );
		this.bv.setData( this.dataLessThan0() );

		Assert.assertEquals( false, this.bv.isFixAxis() );
		Assert.assertEquals( true, this.bv.isAxisAlwaysVisible() );
		Assert.assertEquals( 8, this.bv.getAxisRangeY(), 0 );
		Assert.assertEquals( 50, this.bv.getYscale(), 0 );
		Assert.assertEquals( 0, this.bv.getAxisLocation(), 0 );
	}

	/**
	 * 	Test data above zero with a fixed position zero axis
	 */
	@Test
	public void testFixedAboveZero()
	{
		this.bv.setAxisLocation( 200 );
		this.bv.setData( this.dataGreaterThan0() );

		Assert.assertEquals( true, this.bv.isFixAxis() );
		Assert.assertEquals( 16, this.bv.getAxisRangeY(), 0 );
		Assert.assertEquals( 25, this.bv.getYscale(), 0 );
		Assert.assertEquals( 200, this.bv.getAxisLocation(), 0 );
	}

	/**
	 * 	Test data above zero with a floating axis
	 */
	@Test
	public void testAboveZero()
	{
		this.bv.setFixAxis( false );
		this.bv.setAxisAlwaysVisible( false );
		this.bv.setData( this.dataGreaterThan0() );

		Assert.assertEquals( false, this.bv.isFixAxis() );
		Assert.assertEquals( false, this.bv.isAxisAlwaysVisible() );
		Assert.assertEquals( 4, this.bv.getAxisRangeY(), 0 );
		Assert.assertEquals( 100, this.bv.getYscale(), 0 );
		Assert.assertEquals( 800, this.bv.getAxisLocation(), 0 );
	}

	/**
	 * 	Test data above zero with a floating axis that's always visible
	 */
	@Test
	public void testAboveZeroAlwaysVis()
	{
		this.bv.setFixAxis( false );
		this.bv.setAxisAlwaysVisible( true );
		this.bv.setData( this.dataGreaterThan0() );

		Assert.assertEquals( false, this.bv.isFixAxis() );
		Assert.assertEquals( true, this.bv.isAxisAlwaysVisible() );
		Assert.assertEquals( 8, this.bv.getAxisRangeY(), 0 );
		Assert.assertEquals( 50, this.bv.getYscale(), 0 );
		Assert.assertEquals( 400, this.bv.getAxisLocation(), 0 );
	}

	/**
	 * 	Test distributed data with a fixed axis
	 */
	@Test
	public void testFixedDistributed()
	{
		this.bv.setAxisLocation( 200 );
		this.bv.setData( this.dataDistributedAround0() );

		Assert.assertEquals( true, this.bv.isFixAxis() );
		Assert.assertEquals( 32, this.bv.getAxisRangeY(), 0 );
		Assert.assertEquals( 12.5, this.bv.getYscale(), 0 );
		Assert.assertEquals( 200, this.bv.getAxisLocation(), 0 );
	}

	/**
	 * 	Test distributed data with a floating axis
	 */
	@Test
	public void testDistributed()
	{
		this.bv.setFixAxis( false );
		this.bv.setAxisAlwaysVisible( false );
		this.bv.setData( this.dataDistributedAround0() );

		Assert.assertEquals( false, this.bv.isFixAxis() );
		Assert.assertEquals( false, this.bv.isAxisAlwaysVisible() );
		Assert.assertEquals( 24, this.bv.getAxisRangeY(), 0 );
		Assert.assertEquals( 16.666, this.bv.getYscale(), 1 );
		Assert.assertEquals( 266.66, this.bv.getAxisLocation(), 1 );
	}

	/**
	 * 	Test distributed data with a floating axis that's always visible.
	 * 	Should be the same as the above test.
	 */
	@Test
	public void testDistributedAlwaysVis()
	{
		this.bv.setFixAxis( false );
		this.bv.setAxisAlwaysVisible( true );
		this.bv.setData( this.dataDistributedAround0() );

		Assert.assertEquals( false, this.bv.isFixAxis() );
		Assert.assertEquals( true, this.bv.isAxisAlwaysVisible() );
		Assert.assertEquals( 24, this.bv.getAxisRangeY(), 0 );
		Assert.assertEquals( 16.666, this.bv.getYscale(), 1 );
		Assert.assertEquals( 266.66, this.bv.getAxisLocation(), 1 );
	}
}