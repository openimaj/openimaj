package org.openimaj.demos.campusview;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

public class CampusView implements CaptureControlsDelegate {

	private JFrame frame;
	
	private List<CaptureComponent> captureComponents = new ArrayList<CaptureComponent>();
	
	private int captureCount = 0;

	private CaptureControls captureControls;
	
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
	private void initialize() throws IOException {
		frame = new JFrame();
		frame.getContentPane().setBackground(Color.BLACK);
		frame.setUndecorated(true);
		frame.setResizable(false);
		frame.setBounds(0, 0, 1680, 1050);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		CaptureComponent captureComponent1 = new CaptureComponent();
		captureComponent1.setTitle("Camera #1");
		captureComponent1.setBounds(6, 6, 332, 322);
		frame.getContentPane().add(captureComponent1);
		captureComponents.add(captureComponent1);
		
		CaptureComponent captureComponent2 = new CaptureComponent();
		captureComponent2.setTitle("Camera #2");
		captureComponent2.setBounds(350, 6, 332, 322);
		frame.getContentPane().add(captureComponent2);
		captureComponents.add(captureComponent2);
		
		CaptureComponent captureComponent3 = new CaptureComponent();
		captureComponent3.setTitle("Camera #3");
		captureComponent3.setBounds(694, 6, 332, 322);
		frame.getContentPane().add(captureComponent3);
		captureComponents.add(captureComponent3);
		
		CaptureComponent captureComponent6 = new CaptureComponent();
		captureComponent6.setTitle("Camera #6");
		captureComponent6.setBounds(694, 340, 332, 322);
		frame.getContentPane().add(captureComponent6);
		captureComponents.add(captureComponent6);
		
		CaptureComponent captureComponent4 = new CaptureComponent();
		captureComponent4.setTitle("Camera #4");
		captureComponent4.setBounds(6, 340, 332, 322);
		frame.getContentPane().add(captureComponent4);
		captureComponents.add(captureComponent4);
		
		CaptureComponent captureComponent5 = new CaptureComponent();
		captureComponent5.setTitle("Camera #5");
		captureComponent5.setBounds(350, 340, 332, 322);
		frame.getContentPane().add(captureComponent5);
		captureComponents.add(captureComponent5);
		
		captureControls = new CaptureControls();
		captureControls.setBounds(1038, 40, 563, 235);
		captureControls.setDelegate(this);
		frame.getContentPane().add(captureControls);
		
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        gd.setFullScreenWindow(frame);
        frame.setVisible(true);        
	}
	
	@Override
	public void snapshot(File dir, File md) {
		MBFImage[] images = new MBFImage[captureComponents.size()];
		
		for (CaptureComponent cc : captureComponents) {
			int id = Integer.parseInt(cc.getName().substring(cc.getName().indexOf("#"))) - 1;
			images[id] = cc.getCurrentFrame().clone();
		}
		
		for (int i=0; i<images.length; i++) {
			try {
				ImageUtilities.write(images[i], new File(dir, "im"+captureCount+"-"+i+".png"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
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
