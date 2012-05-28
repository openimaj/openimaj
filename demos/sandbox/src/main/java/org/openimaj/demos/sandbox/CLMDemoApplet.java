package org.openimaj.demos.sandbox;

import java.applet.Applet;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.openimaj.image.processing.face.tracking.clm.demo.Driver;

public class CLMDemoApplet extends Applet {
	private static final long serialVersionUID = 1L;

    @Override
	public void init() {
    	try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
				public void run() {
                    try {
                    	JFrame window = new JFrame();
                		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                		
                		window.setLayout(new GridBagLayout());
                		JPanel c = new JPanel();
                		c.setLayout(new GridBagLayout());
                		window.getContentPane().add(c);
                		
                		Driver vs = new Driver(c);
                		SwingUtilities.getRoot(window).addKeyListener(vs);
                		
                		window.pack();
                		window.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
