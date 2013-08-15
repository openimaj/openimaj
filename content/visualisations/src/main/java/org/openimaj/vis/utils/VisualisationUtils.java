package org.openimaj.vis.utils;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openimaj.util.function.Operation;

/**
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class VisualisationUtils {
	/**
	 * @param title 
	 * @param min
	 * @param max
	 * @param change
	 * @return the frame containing the slider
	 */
	public static JFrame displaySlider(String title, int min, int max, final Operation<JSlider> change){
		JFrame sliderFrame = new JFrame();
		sliderFrame.setTitle(title);
		sliderFrame.setSize(200,100);
		final JSlider slider = new JSlider(min, max);
		slider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				change.perform(slider);
			}
		});
		sliderFrame.add(slider);
		sliderFrame.setVisible(true);
		return sliderFrame;
		
	}
}
