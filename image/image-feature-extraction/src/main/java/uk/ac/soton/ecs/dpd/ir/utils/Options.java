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

package uk.ac.soton.ecs.dpd.ir.utils;

import java.util.HashMap;

/**
 * @author David Dupplaw <dpd@ecs.soton.ac.uk>
 */
/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class Options extends HashMap
{
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public Options()
	{
	}

	/**
	 * @param name
	 */
	public void addOption( String name )
	{
		addOption( name, null );
	}

	/**
	 * @param name
	 * @param value
	 */
	public void addOption( String name, Object value )
	{
		put( name, value );
	}

	/**
	 * @param o
	 */
	public void addOptions( Options o )
	{
		for( int i = 0; i < o.getNumberOfOptions(); i++ )
		{
			String name = o.getOptionNameAt( i );
			addOption( name, o );
		}
	}

	/**
	 * @param name
	 * @param value
	 */
	public void updateOption( String name, String value )
	{
		//System.out.println("Updating "+name+" to "+value );
		if( get(name) == null )
			return;
		if( get(name) instanceof Options )
		{
			Options o = (Options)get(name);
			o.updateOption( name, value );
		}
		else
		{
			if( get(name).getClass() != value.getClass() )
				coerseData( name, value );
			else	put( name, value );
		}
	}

	private void coerseData( String name, String value )
	{
//		System.out.println("  * Coersing to "+(get(name).getClass()) );
		if( get(name) instanceof Integer )
			put( name, new Integer(Integer.parseInt(value)) );
		else
		if( get(name) instanceof Double )
			put( name, new Double(Double.parseDouble(value)) );
		else	put( name, value );	// WARNING
	}

	/**
	 * @param name
	 * @return option value
	 */
	public Object getOption( String name )
	{
		if( get(name) instanceof Options )
			return ((Options)get(name)).getOption(name);

		return get(name);
	}

	/**
	 * @param name
	 * @return int value
	 */
	public int getIntOption( String name )
	{
		try
		{
			return ((Integer)getOption(name)).intValue();
		}
		catch( Exception e )
		{
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * @param name
	 * @return double value
	 */
	public double getDoubleOption( String name )
	{
		try
		{
			return ((Double)getOption(name)).doubleValue();
		}
		catch( Exception e )
		{
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * @return number of options
	 */
	public int getNumberOfOptions()
	{
		return keySet().size();
	}

	/**
	 * @param i
	 * @return option name
	 */
	public String getOptionNameAt( int i )
	{
		return (String)(keySet().toArray()[i]);
	}

	/**
	 * @param i
	 * @return option value
	 */
	public Object getOptionValueAt( int i )
	{
		return getOption( getOptionNameAt(i) );
	}
}
