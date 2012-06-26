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
package org.openimaj.demos.campusview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
//import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.joda.time.DateTime;
import org.openimaj.hardware.compass.CompassData;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

/**
 * The CampusView capture application
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *
 */
public class CampusView implements CaptureControlsDelegate {

	private JFrame frame;
	
	private List<CaptureComponent> captureComponents = new ArrayList<CaptureComponent>();
	
	private int captureCount = 0;

	private CaptureControls captureControls;
	
	private GPSPositionComponent gpsComp;
	
	private CompassComponent compassComp;
	
	private JPanel contentPanel;
	
	private double captureRate = 5;
	private boolean runCapture = false;

	private File imageDir;

	private File imageMetadata;

	private File batchMetadata;
	
	/**
	 * Launch the application.
	 * @param args 
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					CampusView window = new CampusView();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @throws IOException 
	 */
	public CampusView() throws IOException {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws IOException 
	 */
	private void initialize() throws IOException 
	{
		contentPanel = new JPanel()
		{
//			private BufferedImage img = ImageIO.read( CampusView.class.getResource( "/sea.jpg" ) );
            private static final long serialVersionUID = 1L;
			@Override
			public void paintComponent( Graphics g ) 
			{
//				setOpaque( false );
//				g.drawImage( img, 0, 0, getWidth(), getHeight(), null );
//				super.paintComponent( g );
			};
		};
		contentPanel.setLayout( null );
		
		frame = new JFrame();
		frame.getContentPane().setBackground( Color.black );
//		frame.setUndecorated(true);
//		frame.setResizable(false);
		frame.setBounds(0, 0, 1680, 1050);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout( new BorderLayout() );
		frame.getContentPane().add( contentPanel );
		
		int numCameras = 6;
		int rowSize = 3;
		int x = 0; int y = 50;
		int maxX = 0;
		for( int i = 0; i < numCameras; i++ )
		{
			CaptureComponent captureComponent = new CaptureComponent();
			captureComponent.setTitle("Camera #"+(i+1));
			captureComponent.setBounds( x, y, captureComponent.getWidth(), 
					captureComponent.getHeight());
			contentPanel.add(captureComponent);
			captureComponents.add(captureComponent);
			
			if( i % rowSize == rowSize-1 )
			{
				y += captureComponent.getHeight()+40;
				x = 0;
			}
			else	x += captureComponent.getWidth()+30;
			
			maxX = Math.max( x+captureComponent.getWidth()+30, maxX );
		}
				
		captureControls = new CaptureControls();
		captureControls.setBounds(maxX+50, 50, 563, 350);
		captureControls.setDelegate(this);
		contentPanel.add(captureControls);
		
		gpsComp = new GPSPositionComponent();
    	gpsComp.setBackground( new Color(255,255,255,210) );
		gpsComp.setBounds( maxX+50,
				captureControls.getBounds().y+captureControls.getBounds().height+10,
				563,50 );
		contentPanel.add( gpsComp );
		
		compassComp = new CompassComponent();
    	compassComp.setBackground( new Color(255,255,255,210) );
		compassComp.setBounds( maxX+50,				
				gpsComp.getBounds().y+gpsComp.getBounds().height+10,
				563,50 );
		contentPanel.add( compassComp );
		
//		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
//        gd.setFullScreenWindow(frame);
//        frame.setVisible(true);        
	}
	
	@Override
	public void snapshot() {
		MBFImage[] images = new MBFImage[captureComponents.size()];
		
		for (CaptureComponent cc : captureComponents) {
			int id = Integer.parseInt(cc.getTitle().substring(cc.getTitle().indexOf("#")+1)) - 1;
			MBFImage f = cc.getCurrentFrame();
			if( f != null )
				images[id] = f.clone();
		}
		
		// Write all the images
		for (int i=0; i<images.length; i++) {
			try {
				if( images[i] != null )
				{
					System.out.println( "Writing image "+captureCount+"-"+i );
					
					File captureDir = new File(this.imageDir, ""+captureCount);
					captureDir.mkdirs();
					
					ImageUtilities.write(images[i], new File(captureDir, i+".png"));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		// Get some data together
		ArrayList<String> dataList = new ArrayList<String>();
		dataList.add( ""+captureCount );
		dataList.add( ""+gpsComp.getGPS().getLatitude() );
		dataList.add( ""+gpsComp.getGPS().getLongitude() );
		dataList.add( ""+new DateTime() );
		
		CompassData c = compassComp.getCompass().getCompassData();
		if( c != null )
		{
				dataList.add( ""+c.compass );
				dataList.add( ""+c.pitch );
				dataList.add( ""+c.roll );
				dataList.add( ""+c.ax );
				dataList.add( ""+c.ay );
				dataList.add( ""+c.az );
		}
		else	
		{
			dataList.add( "" );
			dataList.add( "" );
			dataList.add( "" );
			dataList.add( "" );
			dataList.add( "" );
			dataList.add( "" );
		}
		
		dataList.add( "Notes" );
		
		// Write the data
		System.out.println( "Writing CSV File: "+this.imageMetadata );
		CSVWriter.writeLine( this.imageMetadata, dataList.toArray( new String[0] ) );
		
		captureCount++;
	}

	@Override
	public void startRecording( int rateSeconds ) 
	{
		this.runCapture = true;
		new Thread( new Runnable()
		{
			@Override
			synchronized public void run()
			{
				while( runCapture )
				try
                {
	                snapshot();
	                wait( (int)captureRate*1000 );
                }
                catch( InterruptedException e )
                {
	                e.printStackTrace();
                }
			}
		}).start();
	}

	@Override
	public void stopRecording() 
	{
		this.runCapture = false;
	}

	@Override
	public void updateCaptureSettings(int capWidth, int capHeight, double capRate) 
	{
		this.captureRate = capRate;
	}

	@Override
    public void startBatch( File dir, File md, String capturer, String type )
    {
		dir.mkdirs();
		
		this.imageDir = new File(dir+File.separator+captureControls.getBatchId());
		this.imageDir.mkdirs();

		this.imageMetadata = new File(this.imageDir+File.separator+"metadata.csv");
		
		this.batchMetadata = new File( dir+File.separator+"batchMetadata.csv" );
		
		ArrayList<String> batchData = new ArrayList<String>();
		batchData.add( ""+captureControls.getBatchId() );
		batchData.add( capturer );
		batchData.add( type );
		
		CSVWriter.writeLine( this.batchMetadata, batchData.toArray(new String[0]) );
    }

	@Override
    public void stopBatch()
    {
		captureCount = 0;
    }
}
