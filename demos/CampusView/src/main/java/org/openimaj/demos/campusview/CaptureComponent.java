package org.openimaj.demos.campusview;

import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;

import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class CaptureComponent extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final List<Device> devices = VideoCapture.getVideoDevices();
	
	private JComboBox comboBox;
	private JPanel panel;
	private JLabel label;
	
	private VideoDisplay<MBFImage> display;

	private int capHeight = 320;
	private int capWidth = 240;
	private double capRate = 25;

	/**
	 * Create the panel.
	 */
	public CaptureComponent() {
		this(320, 240, 25);
	}
	
	/**
	 * Create the panel.
	 */
	public CaptureComponent(int capWidth, int capHeight, double capRate) {
		this.capWidth = capWidth;
		this.capHeight = capHeight;
		this.capRate = capRate;
		
		setBackground(Color.BLACK);
		setLayout(null);
		
		comboBox = new JComboBox();
		comboBox.setBounds(6, 286, 320, 27);
		add(comboBox);
		
		panel = new JPanel();
		panel.setBounds(6, 34, 320, 240);
		add(panel);
		
		label = new JLabel("Camera #1");
		label.setForeground(Color.WHITE);
		label.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
		label.setBounds(6, 6, 124, 16);
		add(label);
		
		initSrcList();
	}

	public void setTitle(String title) {
		label.setText(title);
	}
	
	public String getTitle() {
		return label.getText();
	}
	
	private void initSrcList() {
		comboBox.addItem("None");
		
		for (Device d : devices) 
			comboBox.addItem(d);
		
		comboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setupVideo();
			}
			
		});
		
		comboBox.setSelectedItem(0);
	}

	private void setupVideo() {
		if (comboBox.getSelectedItem().equals("None")) 
			return;
		
		Device dev = (Device) comboBox.getSelectedItem();
		
		if (display != null) {
			((VideoCapture)display.getVideo()).stopCapture();
			panel.removeAll();
		}
		
		System.out.println(dev);
		
		display = VideoDisplay.createVideoDisplay(new VideoCapture(capWidth, capHeight, capRate, dev), panel);
	}
	
	/**
	 * @return the capHeight
	 */
	public int getCapHeight() {
		return capHeight;
	}

	/**
	 * @param capHeight the capHeight to set
	 */
	public void setCapHeight(int capHeight) {
		this.capHeight = capHeight;
		setupVideo();
	}

	/**
	 * @return the capWidth
	 */
	public int getCapWidth() {
		return capWidth;
	}

	/**
	 * @param capWidth the capWidth to set
	 */
	public void setCapWidth(int capWidth) {
		this.capWidth = capWidth;
		setupVideo();
	}

	/**
	 * @return the capRate
	 */
	public double getCapRate() {
		return capRate;
	}

	/**
	 * @param capRate the capRate to set
	 */
	public void setCapRate(double capRate) {
		this.capRate = capRate;
		setupVideo();
	}
	
	public MBFImage getCurrentFrame() {
		return display.getVideo().getCurrentFrame();
	}
}
