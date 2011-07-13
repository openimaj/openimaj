/**
 * 
 */
package org.openimaj.demos.campusview;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openimaj.hardware.compass.CompassSerialReader;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 13 Jul 2011
 */
public class CompassComponent extends JPanel
{
	/** */
    private static final long serialVersionUID = 1L;

    private CompassSerialReader compass = new CompassSerialReader( "/dev/ttyUSB1" );
    
    private JLabel compassLabel = null;
    
    public CompassComponent()
    {
    	new Thread( compass ).start();
    	setLayout( new GridBagLayout() );
    	
    	compassLabel = new JLabel( "No Compass Data" );
    	compassLabel.setFont( new Font( "Courier", Font.BOLD, 16 ) );
    	compassLabel.setForeground( Color.white );
    	compassLabel.setBackground( Color.black );
    	compassLabel.setOpaque( true );
    	compassLabel.setBorder( BorderFactory.createEmptyBorder( 4,4,4,4 ) );
    	
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.gridx = gbc.gridy = 0;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weightx = gbc.weighty = 1;
    	add( compassLabel, gbc );
    	
    	new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				while( true )
				{
					try
                    {
	                    Thread.sleep( 1000 );
                    }
                    catch( InterruptedException e )
                    {
	                    e.printStackTrace();
                    }
                    
                    compassLabel.setText( ""+compass.getCompassData().toString() );
				}
			}
		}).start();

    }
    
    public CompassSerialReader getCompass()
    {
    	return compass;
    }
}
