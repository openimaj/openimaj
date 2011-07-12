package org.openimaj.demos.campusview;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class CaptureControls extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private JTextField capHeightField;
	private JTextField capWidthField;
	private JTextField textField;

	private CaptureControlsDelegate delegate;

	private JLabel metaFile;

	private JLabel imageDir;
	
	/**
	 * Create the panel.
	 */
	public CaptureControls() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{123, 97, 217, 77, 0};
		gridBagLayout.rowHeights = new int[]{16, 16, 16, 21, 1, 16, 28, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblCaptureWidth = new JLabel("Capture Width:");
		lblCaptureWidth.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblCaptureWidth = new GridBagConstraints();
		gbc_lblCaptureWidth.anchor = GridBagConstraints.EAST;
		gbc_lblCaptureWidth.insets = new Insets(0, 0, 5, 5);
		gbc_lblCaptureWidth.gridx = 0;
		gbc_lblCaptureWidth.gridy = 0;
		add(lblCaptureWidth, gbc_lblCaptureWidth);
		
		capWidthField = new JTextField();
		capWidthField.setColumns(10);
		GridBagConstraints gbc_capWidthField = new GridBagConstraints();
		gbc_capWidthField.fill = GridBagConstraints.HORIZONTAL;
		gbc_capWidthField.anchor = GridBagConstraints.NORTH;
		gbc_capWidthField.insets = new Insets(0, 0, 5, 5);
		gbc_capWidthField.gridx = 1;
		gbc_capWidthField.gridy = 0;
		add(capWidthField, gbc_capWidthField);
		
		JLabel lblNewLabel = new JLabel("Capture Height:");
		lblNewLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		add(lblNewLabel, gbc_lblNewLabel);
		
		capHeightField = new JTextField();
		GridBagConstraints gbc_capHeightField = new GridBagConstraints();
		gbc_capHeightField.anchor = GridBagConstraints.NORTH;
		gbc_capHeightField.fill = GridBagConstraints.HORIZONTAL;
		gbc_capHeightField.insets = new Insets(0, 0, 5, 5);
		gbc_capHeightField.gridx = 1;
		gbc_capHeightField.gridy = 1;
		add(capHeightField, gbc_capHeightField);
		capHeightField.setColumns(10);
		
		JLabel lblCaptureRate = new JLabel("Capture Rate:");
		lblCaptureRate.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblCaptureRate = new GridBagConstraints();
		gbc_lblCaptureRate.anchor = GridBagConstraints.EAST;
		gbc_lblCaptureRate.insets = new Insets(0, 0, 5, 5);
		gbc_lblCaptureRate.gridx = 0;
		gbc_lblCaptureRate.gridy = 2;
		add(lblCaptureRate, gbc_lblCaptureRate);
		
		textField = new JTextField();
		textField.setColumns(10);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.anchor = GridBagConstraints.NORTH;
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 2;
		add(textField, gbc_textField);
		
		JLabel lblImageDirectory = new JLabel("Image Directory:");
		lblImageDirectory.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblImageDirectory = new GridBagConstraints();
		gbc_lblImageDirectory.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblImageDirectory.insets = new Insets(0, 0, 5, 5);
		gbc_lblImageDirectory.gridx = 0;
		gbc_lblImageDirectory.gridy = 3;
		add(lblImageDirectory, gbc_lblImageDirectory);
		
		imageDir = new JLabel("/foo/bar/images");
		GridBagConstraints gbc_imageDir = new GridBagConstraints();
		gbc_imageDir.gridwidth = 2;
		gbc_imageDir.fill = GridBagConstraints.HORIZONTAL;
		gbc_imageDir.insets = new Insets(0, 0, 5, 5);
		gbc_imageDir.gridx = 1;
		gbc_imageDir.gridy = 3;
		add(imageDir, gbc_imageDir);
		
		JButton btnSetDir = new JButton("Set");
		GridBagConstraints gbc_btnSetDir = new GridBagConstraints();
		gbc_btnSetDir.anchor = GridBagConstraints.WEST;
		gbc_btnSetDir.insets = new Insets(0, 0, 5, 0);
		gbc_btnSetDir.gridx = 3;
		gbc_btnSetDir.gridy = 3;
		add(btnSetDir, gbc_btnSetDir);
		
		JLabel lblMetadataFile = new JLabel("Metadata File:");
		lblMetadataFile.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblMetadataFile = new GridBagConstraints();
		gbc_lblMetadataFile.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblMetadataFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblMetadataFile.gridx = 0;
		gbc_lblMetadataFile.gridy = 4;
		add(lblMetadataFile, gbc_lblMetadataFile);
		
		metaFile = new JLabel("/foo/bar/metadata.csv");
		GridBagConstraints gbc_metaFile = new GridBagConstraints();
		gbc_metaFile.gridwidth = 2;
		gbc_metaFile.anchor = GridBagConstraints.WEST;
		gbc_metaFile.insets = new Insets(0, 0, 5, 5);
		gbc_metaFile.gridx = 1;
		gbc_metaFile.gridy = 4;
		add(metaFile, gbc_metaFile);
		
		JButton btnSetFile = new JButton("Set");
		GridBagConstraints gbc_btnSetFile = new GridBagConstraints();
		gbc_btnSetFile.insets = new Insets(0, 0, 5, 0);
		gbc_btnSetFile.anchor = GridBagConstraints.WEST;
		gbc_btnSetFile.gridx = 3;
		gbc_btnSetFile.gridy = 4;
		add(btnSetFile, gbc_btnSetFile);
		
		JButton btnSnapshot = new JButton("Snapshot");
		GridBagConstraints gbc_btnSnapshot = new GridBagConstraints();
		gbc_btnSnapshot.anchor = GridBagConstraints.SOUTH;
		gbc_btnSnapshot.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSnapshot.insets = new Insets(0, 0, 0, 5);
		gbc_btnSnapshot.gridx = 0;
		gbc_btnSnapshot.gridy = 6;
		btnSnapshot.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				delegate.snapshot(getImageDir(), getMetadataFile());
			}
			
		});
		add(btnSnapshot, gbc_btnSnapshot);
		
		JButton btnStartRecording = new JButton("Start Recording");
		GridBagConstraints gbc_btnStartRecording = new GridBagConstraints();
		gbc_btnStartRecording.gridwidth = 2;
		gbc_btnStartRecording.anchor = GridBagConstraints.SOUTHWEST;
		gbc_btnStartRecording.insets = new Insets(0, 0, 0, 5);
		gbc_btnStartRecording.gridx = 1;
		gbc_btnStartRecording.gridy = 6;
		add(btnStartRecording, gbc_btnStartRecording);
	}

	protected File getImageDir() {
		return new File(imageDir.getText());
	}

	protected File getMetadataFile() {
		return new File(metaFile.getText());
	}

	public void setDelegate(CaptureControlsDelegate delegate) {
		this.delegate = delegate;
	}
}
