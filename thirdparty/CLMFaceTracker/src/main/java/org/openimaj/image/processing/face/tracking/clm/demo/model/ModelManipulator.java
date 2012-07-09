/**
 * 
 */
package org.openimaj.image.processing.face.tracking.clm.demo.model;

import java.awt.BorderLayout;

import javax.swing.JFrame;

/**
 *	
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 9 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class ModelManipulator
{

	/**
	 *	@param args
	 */
	public static void main( String[] args )
	{
		JFrame f = new JFrame();
		f.getContentPane().add( new ModelManipulatorGUI(), BorderLayout.CENTER );
		f.setSize( 800,600 );
		f.setVisible( true );
	}
}
