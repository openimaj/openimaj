package org.openimaj.demos.campusview;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class CaptureControls extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private JTextField capHeightField;
	private JTextField capWidthField;
	private JTextField capRateTextField;
	private JTextField batchID;
	private JTextField operator;
	
	private CaptureControlsDelegate delegate;

	private JLabel metaFile;
	private JLabel imageDir;
	
	/**
	 * Create the panel.
	 */
	public CaptureControls() {
		
		setLayout( new GridBagLayout() );
		
		final JPanel batchControls = new JPanel(new GridBagLayout());
		batchControls.setBorder( BorderFactory.createTitledBorder( "Batch Controls" ) );
		final JPanel captureControls = new JPanel(new GridBagLayout());
		captureControls.setBorder( BorderFactory.createTitledBorder( "Capture Controls" ) );
		
		// setBackground( new Color(255,255,255,210) );
		
		GridBagLayout gridBagLayout = new GridBagLayout();
//		gridBagLayout.columnWidths = new int[]{123, 97, 217, 77, 0};
//		gridBagLayout.rowHeights = new int[]{16, 16, 16, 21, 1, 16, 28, 0};
//		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
//		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		setBorder( BorderFactory.createEmptyBorder( 10,10,10,10 ) );

		GridBagConstraints gbc = new GridBagConstraints();
		JLabel batchIdL = new JLabel( "Batch Identifier: ");
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		batchControls.add( batchIdL, gbc );
		
		batchID = new JTextField("1");
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.weightx = 1;
		batchControls.add( batchID, gbc );
		
		JLabel operatorLabel = new JLabel( "Operator: ");
		gbc.gridx = 0; gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy++;
		batchControls.add( operatorLabel, gbc );
		
		operator = new JTextField("Jon");
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.gridx++;
		gbc.weightx = 1;
		batchControls.add( operator, gbc );
		
		JLabel lblCaptureWidth = new JLabel("Capture Width:");
		lblCaptureWidth.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblCaptureWidth = new GridBagConstraints();
		gbc_lblCaptureWidth.anchor = GridBagConstraints.EAST;
		gbc_lblCaptureWidth.insets = new Insets(0, 0, 5, 5);
		gbc_lblCaptureWidth.gridx = 0;
		gbc_lblCaptureWidth.gridy = 10;
		batchControls.add(lblCaptureWidth, gbc_lblCaptureWidth);
		
		capWidthField = new JTextField();
		capWidthField.setColumns(10);
		GridBagConstraints gbc_capWidthField = new GridBagConstraints();
		gbc_capWidthField.fill = GridBagConstraints.HORIZONTAL;
		gbc_capWidthField.anchor = GridBagConstraints.NORTH;
		gbc_capWidthField.insets = new Insets(0, 0, 5, 5);
		gbc_capWidthField.gridx = 1;
		gbc_capWidthField.gridy = 10;
		gbc_capWidthField.weightx = 1;
		batchControls.add(capWidthField, gbc_capWidthField);
		
		JLabel lblNewLabel = new JLabel("Capture Height:");
		lblNewLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 20;
		batchControls.add(lblNewLabel, gbc_lblNewLabel);
		
		capHeightField = new JTextField();
		GridBagConstraints gbc_capHeightField = new GridBagConstraints();
		gbc_capHeightField.anchor = GridBagConstraints.NORTH;
		gbc_capHeightField.fill = GridBagConstraints.HORIZONTAL;
		gbc_capHeightField.insets = new Insets(0, 0, 5, 5);
		gbc_capHeightField.gridx = 1;
		gbc_capHeightField.gridy = 20;
		gbc_capHeightField.weightx = 1;
		batchControls.add(capHeightField, gbc_capHeightField);
		capHeightField.setColumns(10);
		
		JLabel lblCaptureRate = new JLabel("Capture Rate (secs):");
		lblCaptureRate.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblCaptureRate = new GridBagConstraints();
		gbc_lblCaptureRate.anchor = GridBagConstraints.EAST;
		gbc_lblCaptureRate.insets = new Insets(0, 0, 5, 5);
		gbc_lblCaptureRate.gridx = 0;
		gbc_lblCaptureRate.gridy = 30;
		gbc_lblCaptureRate.weightx = 1;
		batchControls.add(lblCaptureRate, gbc_lblCaptureRate);
		
		capRateTextField = new JTextField("5");
		capRateTextField.setColumns(10);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.anchor = GridBagConstraints.NORTH;
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 30;
		batchControls.add(capRateTextField, gbc_textField);
		
		JLabel lblImageDirectory = new JLabel("Image Directory:");
		lblImageDirectory.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblImageDirectory = new GridBagConstraints();
		gbc_lblImageDirectory.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblImageDirectory.insets = new Insets(0, 0, 5, 5);
		gbc_lblImageDirectory.gridx = 0;
		gbc_lblImageDirectory.gridy = 40;
		batchControls.add(lblImageDirectory, gbc_lblImageDirectory);
		
		imageDir = new JLabel( System.getProperty("user.home")+"/campusview/images" );
		GridBagConstraints gbc_imageDir = new GridBagConstraints();
		gbc_imageDir.gridwidth = 2;
		gbc_imageDir.fill = GridBagConstraints.HORIZONTAL;
		gbc_imageDir.insets = new Insets(0, 0, 5, 5);
		gbc_imageDir.gridx = 1;
		gbc_imageDir.gridy = 40;
		batchControls.add(imageDir, gbc_imageDir);
		
		JButton btnSetDir = new JButton("Set");
		GridBagConstraints gbc_btnSetDir = new GridBagConstraints();
		gbc_btnSetDir.anchor = GridBagConstraints.WEST;
		gbc_btnSetDir.insets = new Insets(0, 0, 5, 0);
		gbc_btnSetDir.gridx = 3;
		gbc_btnSetDir.gridy = 40;
		batchControls.add(btnSetDir, gbc_btnSetDir);
		btnSetDir.addActionListener( new ActionListener()
		{			
			@Override
			public void actionPerformed( ActionEvent e )
			{
			    JFileChooser chooser = new JFileChooser(); 
			    chooser.setCurrentDirectory( new File(imageDir.getText()) );
			    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    chooser.setAcceptAllFileFilterUsed( false );
			    if( chooser.showSaveDialog( CaptureControls.this ) == JFileChooser.APPROVE_OPTION )
			    	imageDir.setText( chooser.getSelectedFile().getAbsolutePath() );
			}
		} );
		
//		JLabel lblMetadataFile = new JLabel("Metadata File:");
//		lblMetadataFile.setHorizontalAlignment(SwingConstants.TRAILING);
//		GridBagConstraints gbc_lblMetadataFile = new GridBagConstraints();
//		gbc_lblMetadataFile.fill = GridBagConstraints.HORIZONTAL;
//		gbc_lblMetadataFile.insets = new Insets(0, 0, 5, 5);
//		gbc_lblMetadataFile.gridx = 0;
//		gbc_lblMetadataFile.gridy = 50;
//		batchControls.add(lblMetadataFile, gbc_lblMetadataFile);
//		
//		metaFile = new JLabel(System.getProperty("user.home")+"/campusview/metadata.csv");
//		GridBagConstraints gbc_metaFile = new GridBagConstraints();
//		gbc_metaFile.gridwidth = 2;
//		gbc_metaFile.anchor = GridBagConstraints.WEST;
//		gbc_metaFile.insets = new Insets(0, 0, 5, 5);
//		gbc_metaFile.gridx = 1;
//		gbc_metaFile.gridy = 50;
//		batchControls.add(metaFile, gbc_metaFile);
//		
//		JButton btnSetFile = new JButton("Set");
//		GridBagConstraints gbc_btnSetFile = new GridBagConstraints();
//		gbc_btnSetFile.insets = new Insets(0, 0, 5, 0);
//		gbc_btnSetFile.anchor = GridBagConstraints.WEST;
//		gbc_btnSetFile.gridx = 3;
//		gbc_btnSetFile.gridy = 50;
//		batchControls.add(btnSetFile, gbc_btnSetFile);
//		btnSetFile.addActionListener( new ActionListener()
//		{			
//			@Override
//			public void actionPerformed( ActionEvent e )
//			{
//			    JFileChooser chooser = new JFileChooser(); 
//			    chooser.setCurrentDirectory( new File(metaFile.getText()) );
//			    chooser.setAcceptAllFileFilterUsed( false );
//			    if( chooser.showSaveDialog( CaptureControls.this ) == JFileChooser.APPROVE_OPTION )
//			    	metaFile.setText( chooser.getSelectedFile().getAbsolutePath() );
//			}
//		} );
		
		JButton btnSnapshot = new JButton("Snapshot");
		GridBagConstraints gbc_btnSnapshot = new GridBagConstraints();
		gbc_btnSnapshot.anchor = GridBagConstraints.SOUTH;
		gbc_btnSnapshot.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSnapshot.insets = new Insets(0, 0, 0, 5);
		gbc_btnSnapshot.gridx = 0;
		gbc_btnSnapshot.gridy = 6;
		btnSnapshot.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) {
				delegate.snapshot();
			}
			
		});
		captureControls.add(btnSnapshot, gbc_btnSnapshot);
		btnSnapshot.setEnabled( false );
		
		final JButton btnStartRecording = new JButton("Start Recording");
		final JButton btnStopRecording = new JButton("Stop Recording");
		
		GridBagConstraints gbc_btnStartRecording = new GridBagConstraints();
		gbc_btnStartRecording.gridwidth = 1;
		gbc_btnStartRecording.anchor = GridBagConstraints.SOUTHWEST;
		gbc_btnStartRecording.insets = new Insets(0, 0, 0, 5);
		gbc_btnStartRecording.gridx = 1;
		gbc_btnStartRecording.gridy = 6;
		captureControls.add(btnStartRecording, gbc_btnStartRecording);
		btnStartRecording.setEnabled( false );
		btnStartRecording.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				delegate.startRecording( getRate() );
				btnStopRecording.setEnabled( true );
				btnStartRecording.setEnabled( false );
			}
		});

		GridBagConstraints gbc_btnStopRecording = new GridBagConstraints();
		gbc_btnStopRecording.gridwidth = 1;
		gbc_btnStopRecording.anchor = GridBagConstraints.SOUTHWEST;
		gbc_btnStopRecording.insets = new Insets(0, 0, 0, 5);
		gbc_btnStopRecording.gridx = 2;
		gbc_btnStopRecording.gridy = 6;
		captureControls.add(btnStopRecording, gbc_btnStopRecording);
		btnStopRecording.setEnabled( false );
		btnStopRecording.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				delegate.stopRecording();
				btnStartRecording.setEnabled( true );
				btnStopRecording.setEnabled( false );
			}
		});

		gbc.gridx = 0;
		gbc.gridy = 100;
		gbc.weightx = 1; gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JPanel buttonPanel = new JPanel( new GridBagLayout() );
		
		final JButton startBatch = new JButton("Start Batch");
		final JButton stopBatch = new JButton("Stop Batch");
		startBatch.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				for( int i = 0; i < captureControls.getComponentCount(); i++ )
					captureControls.getComponent( i ).setEnabled( true );
				for( int i = 0; i < batchControls.getComponentCount(); i++ )
					batchControls.getComponent( i ).setEnabled( false );
				stopBatch.setEnabled( true );
				startBatch.setEnabled( false );
				btnStopRecording.setEnabled( false );
				
				// TODO: hard coded values here
				delegate.startBatch( getImageDir(), null, getOperator(), "Private" );
			}
		});
		buttonPanel.add( startBatch, gbc );
		
		stopBatch.setEnabled( false );
		stopBatch.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				for( int i = 0; i < captureControls.getComponentCount(); i++ )
					captureControls.getComponent( i ).setEnabled( false );
				for( int i = 0; i < batchControls.getComponentCount(); i++ )
					batchControls.getComponent( i ).setEnabled( true );
				stopBatch.setEnabled( false );
				startBatch.setEnabled( true );
				batchID.setText( ""+(getBatchId()+1) );
				
				delegate.stopBatch();
			}
		});
		gbc.gridx++;
		buttonPanel.add( stopBatch, gbc );
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		batchControls.add( buttonPanel, gbc );
		
		gbc.gridx = gbc.gridy = 0;
		gbc.weightx = gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		add( batchControls, gbc );
		gbc.gridy++;
		add( captureControls, gbc );
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
	
	public int getBatchId()
	{
		return Integer.parseInt( batchID.getText() );
	}
	
	public String getOperator()
	{
		return operator.getText();
	}
	
	public int getRate()
	{
		return Integer.parseInt( this.capRateTextField.getText() );
	}
}
