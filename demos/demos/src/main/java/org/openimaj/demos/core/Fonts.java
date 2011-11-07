/**
 * 
 */
package org.openimaj.demos.core;

import java.awt.Font;

import org.openimaj.demos.Demo;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.typography.general.GeneralFont;

/**
 * 	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@created 5th November 2011
 */
@Demo(
	author = "David Dupplaw", 
	description = "Demonstrates some the OpenIMAJ typography", 
	keywords = { "fonts" }, 
	title = "Fonts"
)
public class Fonts 
{
	public Fonts() 
	{
		MBFImage img = new MBFImage( 800, 600, 3 );
		img.drawText( "OpenIMAJ!", 20, 100, 
			new GeneralFont("Arial", Font.PLAIN, 120), 100, RGBColour.WHITE );
		
		DisplayUtilities.display( img );
	}
	
	public static void main(String[] args) 
	{
		new Fonts();
	}
}
