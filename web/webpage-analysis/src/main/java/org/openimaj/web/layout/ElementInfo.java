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
package org.openimaj.web.layout;

import org.openimaj.math.geometry.shape.Rectangle;

import com.trolltech.qt.webkit.QWebElement;

/**
 * Information about a DOM element
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
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
	
	/**
	 * @return The ID of the element. Auto-generated if if doesn't hove one.
	 */
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
	
	/**
	 * @return Get the column headings for CSV output
	 */
	public static String getCSVHeader() {
		return String.format("%s,%s,%s,%s,%s,%s,%s,%s", "tagName", "id", "x", "y", "width", "height","isContent","isInsideContent");
	}
	
	/**
	 * @return Write element to CSV data
	 */
	public String toCSVString() {
		return String.format("%s,%s,%d,%d,%d,%d,%s,%s", element.tagName(), 
				getElementId(), 
				(int)bounds.x, (int)bounds.y, (int)bounds.width, (int)bounds.height,isContent,isInsideContent);
	}
}
