package org.openimaj.demos.sandbox;

import java.applet.Applet;
import java.awt.Color;
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
    		this.setSize(650, 500);
    		this.setBackground(Color.BLACK);
    		
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
				public void run() {
                    try {
                    	Driver vs = new Driver(CLMDemoApplet.this);
                		CLMDemoApplet.this.addKeyListener(vs);
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
