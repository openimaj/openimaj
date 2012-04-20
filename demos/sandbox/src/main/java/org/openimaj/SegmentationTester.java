package org.openimaj;


import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.GridBagLayout;


import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.segmentation.FelzenszwalbHuttenlocherSegmenter;
import org.openimaj.image.segmentation.SegmentationUtilities;
import org.openimaj.image.typography.hershey.HersheyFont;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class SegmentationTester extends JFrame{
	private static final long serialVersionUID = 1L;
	
	private float K = 5;
	private float SIGMA = (float) 0.5;
	private float MIN_PIXELS_FRACTION = (float) 0.01;

	//final FelzenszwalbHuttenlocherSegmenter FSegmenter = new FelzenszwalbHuttenlocherSegmenter(SIGMA, K, 1000);
	
	
	
	public SegmentationTester() {
		
		
		this.setTitle("FelzenszwalbHuttenlocherSegmenter");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		this.setLayout(gridBagLayout);
		
		JLabel kLabel = new JLabel("K value");
		JLabel thresholdLabel = new JLabel("SIGMA value");
		JLabel minSizeLabel = new JLabel("Mininum #pixels/segment");
		
		final JTextField kField = new JTextField(""+K);
		final JTextField thresholdField = new JTextField(""+SIGMA);
		final JTextField minSizeField = new JTextField(""+MIN_PIXELS_FRACTION);
		
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		getContentPane().add(kLabel, gbc_panel);
		
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 0;
		getContentPane().add(kField, gbc_panel);
		
		
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		getContentPane().add(thresholdLabel, gbc_panel);
		
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 1;
		getContentPane().add(thresholdField, gbc_panel);
		
		
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		getContentPane().add(minSizeLabel, gbc_panel);
		
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 2;
		getContentPane().add(minSizeField, gbc_panel);
		
		
		JLabel urlLabel = new JLabel("URL");
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 3;
		getContentPane().add(urlLabel, gbc_panel);
		
		final JTextField urlField = new JTextField(30);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 4;
		gbc_panel.gridwidth = 2;
		getContentPane().add(urlField, gbc_panel);
		
		JButton segment = new JButton("Segment Image");
		segment.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				
				K = Float.parseFloat(kField.getText().trim());
				SIGMA = Float.parseFloat(thresholdField.getText().trim());
				MIN_PIXELS_FRACTION = Float.parseFloat(minSizeField.getText().trim());
				
				try {
					MBFImage image = ImageUtilities.readMBF(new URL(urlField.getText().trim()));
					
					int pixelNumberInImage = image.getRows()*image.getCols();
					int mininumPixelsInSegment = Math.round(((float)pixelNumberInImage) * MIN_PIXELS_FRACTION);
					
					FelzenszwalbHuttenlocherSegmenter<MBFImage> FSegmenter = new FelzenszwalbHuttenlocherSegmenter<MBFImage>(SIGMA, K, mininumPixelsInSegment);
				    
				    List < ConnectedComponent > segments = FSegmenter.segment(image); 
				    
				    MBFImage segImage = SegmentationUtilities.renderSegments(image, segments);
				    for(int i=0; i<segments.size(); i++){
				    	segImage.drawText("Region" + i , segments.get(i).calculateCentroidPixel().x, 
				    			segments.get(i).calculateCentroidPixel().y,
				    			HersheyFont.TIMES_BOLD, 20);
				    }
				    //DisplayUtilities.display(image);
				    DisplayUtilities.display(segImage);
				    System.out.println(segments.size());
				    
//				    FSegmenter = null;
//				    segments = null;
//				    Runtime.getRuntime().gc();
				    
				    
				    
				   
					
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 5;
		gbc_panel.gridwidth = 1;
		getContentPane().add(segment, gbc_panel);
		
		this.pack();
		this.setVisible(true);
		this.setResizable(false);
		
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SegmentationTester tester = new SegmentationTester();
	}

}
