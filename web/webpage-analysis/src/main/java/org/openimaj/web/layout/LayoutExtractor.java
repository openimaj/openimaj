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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.web.ProgrammaticBrowser;
import org.openimaj.web.readability.Readability;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.trolltech.qt.webkit.QWebElement;
import com.trolltech.qt.webkit.QWebElementCollection;

/**
 * Class for extracting information on the layout of DOM elements in
 * a web page.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class LayoutExtractor {
	private static final String GEN_ID = "__openimaj_gen_id_";

	private static final Logger logger = Logger.getLogger(LayoutExtractor.class);
	
	private ProgrammaticBrowser browser;
	
	private long timeout = 0;

	/**
	 * Default constructor
	 */
	public LayoutExtractor() {
		browser = new ProgrammaticBrowser();
	}
	
	/**
	 * Default constructor
	 * @param timeout 
	 */
	public LayoutExtractor(long timeout) {
		this();
		this.timeout = timeout;
	}

	/**
	 * Load a web page from a URL
	 * @param url the url
	 * @return true if successful; false otherwise
	 */
	public boolean load(String url) {
		boolean ret;
		try {
			ret = browser.load(url, timeout);
		} catch (TimeoutException e) {
			return false;
		}
				
		if (ret) augmentDOM();
		
		return ret;
	}
	
	/**
	 * Load a web page from a URL
	 * @param url the url
	 * @return true if successful; false otherwise
	 */
	public boolean load(URL url) {
		boolean ret;
		try {
			ret = browser.load(url, timeout);
		} catch (TimeoutException e) {
			return false;
		}
				
		if (ret) augmentDOM();
		
		return ret;
	}
	
	/**
	 * Load a web page from an HTML string
	 * @param html the HTML string
	 * @return true if successful; false otherwise
	 */
	public boolean loadHTML(String html) {
		boolean ret = browser.loadHTML(html);
				
		if (ret) augmentDOM();
		
		return ret;
	}
	
	private void augmentDOM() {
		QWebElement body = getBody();
		
		if (body == null) {
			logger.warn("body not found");
			return;
		}

		QWebElementCollection nl = body.findAll("*");
		for (int i=0; i<nl.count(); i++) {
			QWebElement ei = nl.at(i);
			
			if (ei.attribute("id") == null || ei.attribute("id").equals("")) {
				ei.setAttribute("id", GEN_ID+i);
			}
		}
	}

	/**
	 * Get the layout info of the page
	 * 
	 * @return information about the page layout
	 */
	public List<ElementInfo> getLayoutInfo() {
		List<ElementInfo> info = new ArrayList<ElementInfo>();
		
		Set<String> contentIds = getContentIds();
		
		QWebElementCollection elements = browser.findAllElements("*");
		
		for (int i=0; i<elements.count(); i++) {
			ElementInfo ei = new ElementInfo();
			
			ei.element = elements.at(i);
			
			ei.bounds = new Rectangle(
					ei.element.geometry().left(),
					ei.element.geometry().top(),
					ei.element.geometry().width(),
					ei.element.geometry().height()
					);
			
			if (contentIds.contains(ei.element.attribute("id"))) {
				ei.isContent = true;
			}
			
			QWebElement parent = ei.element;
			while (!(parent = parent.parent()).isNull()) {
				String id = parent.attribute("id");
				
				if (contentIds.contains(id)) {
					ei.isInsideContent = true;
					break;
				}
			}
			
			info.add(ei);
		}

		return info;
	}

	/**
	 * Render ALL the content boxes to a new image in the given color
	 * @param color Color 
	 * @return new image illustrating ALL content boxes
	 */
	public MBFImage renderLayoutInfo(Float[] color) {
		int w = browser.getWidth();
		int h = browser.getHeight();
		
		//Pixel p = LayoutUtils.renderSize(page.mainFrame());
		MBFImage image = new MBFImage(w, h, ColourSpace.RGB);
		return renderLayoutInfo(image, color);
	}

	/**
	 * Render ALL the content boxes to the given image in the given color
	 * @param image Image to draw on top of
	 * @param colour Color
	 * @return the rendered image
	 */
	public MBFImage renderLayoutInfo(MBFImage image, Float[] colour) {
		MBFImageRenderer renderer = image.createRenderer();
		
		for (ElementInfo e : getLayoutInfo()) {
			Rectangle r = e.getBounds();
			renderer.drawShape(r, colour);
		}
		
		return image;
	}

	/**
	 * Get the BODY element of the loaded page
	 * @return body element or null if it doesn't exist
	 */
	public QWebElement getBody() {
		return browser.getBody();
	}
	
	protected String nodeToString(QWebElement n, boolean pretty) {
		return n.toOuterXml();
	}

	private Set<String> getContentIds() {
		Set<String> ids = new HashSet<String>();
		try {
			String html = browser.getHTML();
			
			Readability r = Readability.getReadability(html);

			Element d = (Element) r.getArticleHTML_DOM();
			if (d==null) return ids;
			NodeList nl = d.getElementsByTagName("*");

			for (int i=0; i<nl.getLength(); i++) {
				Node idnode = nl.item(i).getAttributes().getNamedItem("id");
				if (idnode != null) {
					ids.add(idnode.getNodeValue());
				}
			}
		} catch (Exception e) {
			logger.error("Error finding content ids: " + e);
		}

		return ids;
	}

	/**
	 * Render the layout of the content.
	 * @param contentColour Colour for content
	 * @param nonContent Colour for non-content
	 * @param nonContentInside Colour for non-content inside content
	 * @return rendered image with boxes
	 */
	public MBFImage renderContentLayout(Float[] contentColour, Float [] nonContent, Float [] nonContentInside) {
		int w = browser.getWidth();
		int h = browser.getHeight();
		
		MBFImage image = new MBFImage(w, h, ColourSpace.RGB);
		return renderContentLayout(image, contentColour, nonContent, nonContentInside);
	}
	
	/**
	 * Render the layout of the content.
	 * @param image image to draw into
	 * @param contentColour Colour for content
	 * @param nonContent Colour for non-content
	 * @param nonContentInside Colour for non-content inside content
	 * @return rendered image with boxes
	 */
	public MBFImage renderContentLayout(MBFImage image, Float[] contentColour, Float [] nonContent, Float [] nonContentInside) {
		List<Rectangle> content_areas = new ArrayList<Rectangle>();
		List<Rectangle> non_content_areas = new ArrayList<Rectangle>();
		List<Rectangle> non_content_areas_inside = new ArrayList<Rectangle>();
		
		for (ElementInfo ei : getLayoutInfo()) {
			if (ei.isContent) {
				content_areas.add(ei.bounds);
			} else if (ei.isInsideContent) {
				non_content_areas_inside.add(ei.bounds);
			} else {
				non_content_areas.add(ei.bounds);
			}
		}

		MBFImageRenderer renderer = image.createRenderer();
		for (Rectangle r : content_areas) {
			renderer.drawShape(r, contentColour);
		}

		for (Rectangle r : non_content_areas_inside) {
			renderer.drawShape(r, nonContentInside);
		}
		
		for (Rectangle r : non_content_areas) {
			renderer.drawShape(r, nonContent);
		}

		return image;
	}
	
	/**
	 * Render the current page to an image
	 * @return an image of the current page, or null if there is no content
	 */
	public MBFImage render() {
		return browser.renderToImage();
	}
	
	/**
	 * Render the current page to an image of the given size or smaller
	 * @param maxwidth 
	 * @param maxheight 
	 * @return an image of the current page, or null if there is no content
	 */
	public MBFImage render(int maxwidth, int maxheight) {
		return browser.renderToImage(maxwidth, maxheight);
	}
	
	/**
	 * Run the browser for ms milliseconds. This
	 * allows it to update its content, etc.
	 * @param ms time to wait
	 */
	public void waitForBrowser(long ms) {
		browser.mainLoop(ms);
	}
}
