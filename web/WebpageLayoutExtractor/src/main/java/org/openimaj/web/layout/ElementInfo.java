package org.openimaj.web.layout;

import org.openimaj.math.geometry.shape.Rectangle;

import com.trolltech.qt.webkit.QWebElement;

public class ElementInfo {
	QWebElement element;
	Rectangle bounds;
	boolean isContent = false;
	boolean isInsideContent = false;
	
	/**
	 * @return the element
	 */
	public QWebElement getElement() {
		return element;
	}
	
	/**
	 * @param element the element to set
	 */
	public void setElement(QWebElement element) {
		this.element = element;
	}
	
	/**
	 * @return the bounds
	 */
	public Rectangle getBounds() {
		return bounds;
	}
	
	/**
	 * @param bounds the bounds to set
	 */
	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}
	
	/**
	 * @return the isContent
	 */
	public boolean isContent() {
		return isContent;
	}
	
	/**
	 * @param isContent the isContent to set
	 */
	public void setContent(boolean isContent) {
		this.isContent = isContent;
	}
	
	/**
	 * @return the isInsideContent
	 */
	public boolean isInsideContent() {
		return isInsideContent;
	}
	
	/**
	 * @param isInsideContent the isInsideContent to set
	 */
	public void setInsideContent(boolean isInsideContent) {
		this.isInsideContent = isInsideContent;
	}
	
	public String getElementId() {
		String id = element.attribute("id");
		
		if (id == null || id.contains("__gen_id_"))
			return "";
		
		return id;
	}
	
	@Override
	public String toString() {
		return String.format("ElementInfo[%s](%d,%d,%d,%d)", element.tagName(), (int)bounds.x, (int)bounds.y, (int)bounds.width, (int)bounds.height);
	}
	
	public static String getCSVHeader() {
		return String.format("%s,%s,%s,%s,%s,%s,%s,%s", "tagName", "id", "x", "y", "width", "height","isContent","isInsideContent");
	}
	
	public String toCSVString() {
		return String.format("%s,%s,%d,%d,%d,%d,%s,%s", element.tagName(), 
				getElementId(), 
				(int)bounds.x, (int)bounds.y, (int)bounds.width, (int)bounds.height,isContent,isInsideContent);
	}
}
