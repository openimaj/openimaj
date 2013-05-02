package org.openimaj.demos.sandbox.image.puzzles;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

import org.openimaj.demos.video.utils.PolygonDrawingListener;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;

public class ImageSelectionFrame implements KeyListener{

	private boolean polygonComplete = false;
	private PolygonDrawingListener draw;
	private MBFImage img;
	private JFrame frame;

	public ImageSelectionFrame(MBFImage img) {
		frame = DisplayUtilities.displaySimple(img);
		this.img = img.clone();
		draw = new PolygonDrawingListener();
		frame.getContentPane().addMouseListener(draw);
		frame.addKeyListener(this);
	}

	public void waitForPolygonSelection() throws InterruptedException {
		while(!polygonComplete){
			MBFImage toDraw = img.clone();
			this.draw.drawPoints(toDraw);
			DisplayUtilities.display(toDraw, frame);
			Thread.sleep(10);
		}
		frame.dispose();
	}

	public SelectedImage getSelectedImage() {
		return new SelectedImage(img,draw.getPolygon());
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			this.polygonComplete  = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}