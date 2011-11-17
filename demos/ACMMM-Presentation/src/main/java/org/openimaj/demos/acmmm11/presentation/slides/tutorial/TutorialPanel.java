package org.openimaj.demos.acmmm11.presentation.slides.tutorial;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.openimaj.image.DisplayUtilities.ScalingImageComponent;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

public abstract class TutorialPanel extends JPanel implements VideoDisplayListener<MBFImage>{
	private static final long serialVersionUID = 2105054613577879944L;
	
	private MBFImage toDraw;
	private BufferedImage bimg;
	private ScalingImageComponent comp;
	
	public TutorialPanel(String name, Video<MBFImage> capture, int width, int height) {
		this.setOpaque( false );
		
		this.setBorder( BorderFactory.createTitledBorder( name ) );
		
		this.setLayout(new GridBagLayout());
		
		this.comp = new ScalingImageComponent();
		this.add(comp);
		
		toDraw = new MBFImage(width,height,3);
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		if (this.comp.getWidth() <= 10) {
			Insets insets = this.getInsets();
			int width = this.getWidth() - insets.left - insets.right;
			int height = (int) (((float)width / (float)frame.getWidth())*frame.getHeight());
						
			this.comp.setSize(width, height);
			this.comp.setPreferredSize(new Dimension(width,height));
			this.validate();
		}
		
		toDraw.internalCopy(frame);
		doTutorial(toDraw);
		this.comp.setImage(bimg = ImageUtilities.createBufferedImageForDisplay( toDraw, bimg ));
	}

	public abstract void doTutorial(MBFImage toDraw);
}
