package org.openimaj.demos.campusview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.openimaj.hardware.compass.CompassData;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

public class CampusView implements CaptureControlsDelegate {

	private JFrame frame;
	
	private List<CaptureComponent> captureComponents = new ArrayList<CaptureComponent>();
	
	private int captureCount = 0;

	private CaptureControls captureControls;
	
	private GPSPositionComponent gpsComp;
	
	private CompassComponent compassComp;
	
	private JPanel contentPanel;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
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
			private BufferedImage img = ImageIO.read( CampusView.class.getResource( "/sea.jpg" ) );
            private static final long serialVersionUID = 1L;
			public void paintComponent( Graphics g ) 
			{
				setOpaque( false );
				g.drawImage( img, 0, 0, getWidth(), getHeight(), null );
				super.paintComponent( g );
			};
		};
		contentPanel.setLayout( null );
		
		frame = new JFrame();
		frame.getContentPane().setBackground( Color.black );
		frame.setUndecorated(true);
		frame.setResizable(false);
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
		captureControls.setBounds(maxX+50, 50, 563, 235);
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
		
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        gd.setFullScreenWindow(frame);
        frame.setVisible(true);        
	}
	
	@Override
	public void snapshot(File dir, File md) {
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
					ImageUtilities.write(images[i], new File(dir, "im"+captureCount+"-"+i+".png"));
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
		CompassData c = compassComp.getCompass().getCompassData();
		if( c != null )
				dataList.add( ""+c.compass );
		else	dataList.add( "" );
		
		// Write the data
		CSVWriter.writeLine( captureControls.getMetadataFile(), 
				dataList.toArray( new String[0] ) );
		
		captureCount++;
	}

	@Override
	public void startRecording(File dir, File md) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopRecording() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateCaptureSettings(int capWidth, int capHeight, double capRate) {
		// TODO Auto-generated method stub
		
	}
}
