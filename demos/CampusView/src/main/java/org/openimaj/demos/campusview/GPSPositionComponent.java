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

import org.openimaj.hardware.gps.GPSSerialReader;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 13 Jul 2011
 */
public class GPSPositionComponent extends JPanel
{
	/** */
    private static final long serialVersionUID = 1L;
    
    /** The GPS device */
    private GPSSerialReader gps = new GPSSerialReader( "/dev/ttyUSB0" );
    
    private JLabel latLabel = null;
    private JLabel longLabel = null;

    public GPSPositionComponent()
    {
    	new Thread( gps ).start();
    	setLayout( new GridBagLayout() );
    	// setBorder( BorderFactory.createBevelBorder(BevelBorder.LOWERED) );
    	
    	latLabel = new JLabel( "No GPS" );
    	latLabel.setFont( new Font( "Courier", Font.BOLD, 16 ) );
    	latLabel.setForeground( Color.white );
    	latLabel.setOpaque( true );
    	latLabel.setBackground( Color.black );
    	latLabel.setBorder( BorderFactory.createEmptyBorder( 4,4,4,4 ) );
    	
    	longLabel = new JLabel( "No GPS" );
    	longLabel.setFont( new Font( "Courier", Font.BOLD, 16 ) );
    	longLabel.setForeground( Color.white );
    	longLabel.setBackground( Color.black );
    	longLabel.setOpaque( true );
    	longLabel.setBorder( BorderFactory.createEmptyBorder( 4,4,4,4 ) );
    	
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.gridx = gbc.gridy = 0;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weightx = gbc.weighty = 1;

    	add( latLabel, gbc );
    	gbc.gridy++;
    	add( longLabel, gbc );
    	
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
                    
                    latLabel.setText(  "Latitude : "+gps.getLatitude() );
                    longLabel.setText( "Longitude: "+gps.getLongitude() );
				}
			}
		}).start();
    }
    
    public GPSSerialReader getGPS()
    {
    	return gps;
    }
}
