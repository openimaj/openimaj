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

/**
 * Panel for displaying a video as part of a larger slide
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class TutorialPanel extends JPanel implements VideoDisplayListener<MBFImage>{
	private static final long serialVersionUID = 2105054613577879944L;
	
	private MBFImage toDraw;
	private BufferedImage bimg;
	private ScalingImageComponent comp;
	
	/**
	 * Default constructor
	 * 
	 * @param name 
	 * @param capture
	 * @param width
	 * @param height
	 */
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

	/**
	 * Update the frame
	 * @param toDraw
	 */
	public abstract void doTutorial(MBFImage toDraw);
}
