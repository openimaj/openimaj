package org.openimaj.demos.sandbox;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

import org.openimaj.image.processing.face.tracking.clm.demo.Puppeteer;

public class CLMDemoApplet extends JApplet {
	private static final long serialVersionUID = 1L;

    @Override
	public void init() {
    	try {
    		this.setSize(640, 480);
    		
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
				public void run() {
                    try {
                    	//Driver vs = new Driver(CLMDemoApplet.this);
                    	Puppeteer vs = new Puppeteer(CLMDemoApplet.this);
                		CLMDemoApplet.this.addKeyListener(vs);
                		CLMDemoApplet.this.doLayout();
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
