/**
 * 
 */
package org.openimaj.demos.video.processing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.keypoints.FacialKeypoint;
import org.openimaj.image.processing.face.keypoints.KEDetectedFace;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 28 Sep 2011
 */
public class ProcessingPanel extends JPanel 
	implements VideoDisplayListener<MBFImage>
{
	/** */
    private static final long serialVersionUID = 1L;
    
    private boolean faceDetect = true;
    private boolean faceKPDetect = false;

    /**
     * 
     */
    public ProcessingPanel()
    {
    	init();
    }

    /**
     * 
     */
    private void init()
    {
    	this.setLayout( new GridBagLayout() );
    	this.setBorder( BorderFactory.createTitledBorder( "Processing" ) );
    	
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.gridx = gbc.gridy = 0;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.weightx = 1; gbc.weighty = 0;
    	gbc.gridwidth = 1;
    	
    	// -----------------------------------------------------
    	final JCheckBox faceDetectorButton = new JCheckBox( "Face Detection", faceDetect );
    	faceDetectorButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				faceDetect = faceDetectorButton.isSelected();
			}
		});
    	this.add( faceDetectorButton, gbc );

    	// -----------------------------------------------------
    	final JCheckBox faceKPDetectorButton = new JCheckBox( "Facial Keypoint Detection", faceKPDetect );
    	faceKPDetectorButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				faceKPDetect = faceKPDetectorButton.isSelected();
			}
		});
    	gbc.gridy++;
    	this.add( faceKPDetectorButton, gbc );

    }

    /**
     *  @inheritDoc
     *  @see org.openimaj.video.VideoDisplayListener#afterUpdate(org.openimaj.video.VideoDisplay)
     */
	@Override
    public void afterUpdate( VideoDisplay<MBFImage> display )
    {
    }

	/**
	 *  @inheritDoc
	 *  @see org.openimaj.video.VideoDisplayListener#beforeUpdate(org.openimaj.image.Image)
	 */
	@Override
    public void beforeUpdate( MBFImage frame )
    {
		if( faceDetect )
		{
			HaarCascadeDetector d = new HaarCascadeDetector( 40 );
			List<DetectedFace> faces = d.detectFaces( Transforms.calculateIntensityNTSC( frame ) );

			for( DetectedFace face : faces )
			{
				Shape transBounds = face.getBounds();				
				MBFImageRenderer renderer = frame.createRenderer();
				renderer.drawPolygon(transBounds.asPolygon(), RGBColour.RED);
			}
		}
		
		if( faceKPDetect )
		{
			System.out.println( "HERE" );
			FKEFaceDetector fkp = new FKEFaceDetector(
					HaarCascadeDetector.BuiltInCascade.frontalface_alt.load());
			List<KEDetectedFace> faces = fkp.detectFaces( 
					Transforms.calculateIntensityNTSC( frame ) );
			
			for(KEDetectedFace face : faces)
			{
				Shape transBounds = face.getBounds();
				MBFImageRenderer renderer = frame.createRenderer();
				renderer.drawPolygon(transBounds.asPolygon(), RGBColour.RED);
				
				for(FacialKeypoint kp: face.getKeypoints())
				{
					Point2d pt = kp.position.clone();
					pt.translate((float)transBounds.minX(), (float)transBounds.minY());	
					renderer.drawPoint(pt, RGBColour.GREEN, 3);
				}
			}
		}
    }
}
