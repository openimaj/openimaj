package org.openimaj.demos.sandbox.tldcpp.tracker;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.openimaj.demos.sandbox.tldcpp.videotld.TLDMain;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;

public class RectangleSelectionListener implements MouseListener, MouseMotionListener {

	private TLDMain tldMain;
	private boolean rectangleSelectMode;
	private Point2dImpl start;
	private Point2dImpl end;
	private Rectangle currentRect;

	public RectangleSelectionListener(TLDMain tldMain) {
		this.tldMain = tldMain;
		this.rectangleSelectMode = false;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		mousePressed(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(rectangleSelectMode){
			this.start = new Point2dImpl(e.getX(),e.getY());
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(rectangleSelectMode){
			this.end = new Point2dImpl(e.getX(),e.getY());
			try {
				tldMain.selectObject(getRect(start,end));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			this.rectangleSelectMode = false;
			this.currentRect = null;
		}
	}

	private Rectangle getRect(Point2dImpl start, Point2dImpl end) {
		float x = Math.min(start.x, end.x);
		float y = Math.min(start.y, end.y);
		float width = Math.abs(start.x - end.x);
		float height = Math.abs(start.y - end.y);
		return new Rectangle(x,y,width,height);
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	public void selectBoundingBox() {
		this.currentRect = new Rectangle(0,0,1,1);
		this.rectangleSelectMode = true;
	}
	
	public Rectangle currentRect(){
		if(!this.rectangleSelectMode) return null;
		return this.currentRect;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if(this.rectangleSelectMode){
			end = new Point2dImpl(e.getX(),e.getY());
			this.currentRect = getRect(start,end);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public boolean drawRect(MBFImage frame) {
		if(this.rectangleSelectMode){
			frame.drawShape(this.currentRect, RGBColour.RED);
			return true;
		}
		else{
			return false;
		}
	}

}
