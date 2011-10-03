/**
 * 
 */
package org.openimaj.demos.video.processing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 28 Sep 2011
 */
public class SourcePanel extends JPanel
{
	/** */
    private static final long serialVersionUID = 1L;
	private JRadioButton webcamButton;
	private JRadioButton fileButton;
	private VideoProcessingDemo vpd;

    public SourcePanel( VideoProcessingDemo vpd )
    {
    	this.vpd = vpd;
    	init();
    }
    
    private void init()
    {
    	this.setLayout( new GridBagLayout() );
    	this.setBorder( BorderFactory.createTitledBorder( "Source" ) );
    	
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.gridx = gbc.gridy = 0;
    	gbc.weightx = 1; gbc.weighty = 0;
    	
    	webcamButton = new JRadioButton("Webcam",true);
    	fileButton   = new JRadioButton("File");
    	
    	ButtonGroup bg = new ButtonGroup();
    	bg.add( webcamButton );
    	bg.add( fileButton );
    	
    	webcamButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				try
                {
	                vpd.useWebcam();
                }
                catch( IOException e1 )
                {
	                e1.printStackTrace();
                }
			}
		});

    	fileButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				JFileChooser jfc = new JFileChooser();
	
			    int returnVal = jfc.showOpenDialog( vpd );		    
			    if( returnVal == JFileChooser.APPROVE_OPTION ) 
			    {			    	
					vpd.useFile( jfc.getSelectedFile() );
			    }
			}
		});
    	
    	this.add( webcamButton, gbc );
    	gbc.gridy++;
    	this.add( fileButton, gbc );
    }
}
