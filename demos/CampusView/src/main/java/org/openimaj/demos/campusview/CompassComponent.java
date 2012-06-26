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
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 13 Jul 2011
 */
public class CompassComponent extends JPanel
{
	/** */
    private static final long serialVersionUID = 1L;

    private CompassSerialReader compass = new CompassSerialReader( "/dev/cu.KeySerial1" );
    
    private JLabel compassLabel = null;
    
    /**
     * Default constructor
     */
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
                    
                    if( compass.getCompassData() != null )
                    	compassLabel.setText( ""+compass.getCompassData().toString() );
				}
			}
		}).start();

    }
    
    /**
     * @return the compass reader
     */
    public CompassSerialReader getCompass()
    {
    	return compass;
    }
}
