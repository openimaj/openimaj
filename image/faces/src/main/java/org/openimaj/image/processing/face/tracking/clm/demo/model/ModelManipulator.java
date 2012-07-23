/**
 * 
 */
package org.openimaj.image.processing.face.tracking.clm.demo.model;

import javax.swing.JFrame;

/**
 *	Class to start up the model manipulator in a window.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 9 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class ModelManipulator
{
	/**
	 * 	Main
	 *	@param args Command-line arguments
	 */
	public static void main( String[] args )
	{
		JFrame f = new JFrame( "CLM Model Manipulator" );
		f.getContentPane().add( new ModelManipulatorGUI() );
		f.setSize( 1200,600 );
		f.setVisible( true );
	}
}
