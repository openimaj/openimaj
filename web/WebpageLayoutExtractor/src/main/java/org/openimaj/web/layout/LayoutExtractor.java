package org.openimaj.web.layout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.web.ProgrammaticBrowser;
import org.openimaj.web.readability.Readability;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.trolltech.qt.QSignalEmitter;
import com.trolltech.qt.webkit.QWebElement;
import com.trolltech.qt.webkit.QWebElementCollection;

public class LayoutExtractor extends QSignalEmitter {	
	private ProgrammaticBrowser browser;

	private LayoutExtractor() {
		browser = new ProgrammaticBrowser();
	}

	public boolean loadPage(String url) {
		boolean ret = browser.load(url);
		
		if (ret) augmentDOM();
		
		return ret;
	}
	
	private void augmentDOM() {
		QWebElement body = getBody();
		
		if (body == null) {
			System.err.println("body not found");
			return;
		}

		QWebElementCollection nl = body.findAll("*");
		for (int i=0; i<nl.count(); i++) {
			QWebElement ei = nl.at(i);
			
			if (ei.attribute("id") == null || ei.attribute("id").equals("")) {
				ei.setAttribute("id", "__gen_id_"+i);
			}
		}
	}
	
//	/**
//	 * Construct an HTMLLayoutExtractor from a string of html
//	 * @param html String of html
//	 * @return new HTMLLayoutExtractor
//	 */
//	public static HTMLLayoutExtractor loadPageHTML(String html) {
//		HTMLLayoutExtractor extr = new HTMLLayoutExtractor();
//		MozillaAutomation.blockingLoadHTML(extr.moz, html, "http://foo.bar");
//		return extr;
//	}

	

	/**
	 * Get the layout info of the page
	 * 
	 * @return information about the page layout
	 */
	public List<ElementInfo> getLayoutInfo() {
		List<ElementInfo> info = new ArrayList<ElementInfo>();
		
		Set<String> contentIds = getContentIds();
		
		QWebElementCollection elements = browser.findAllElements("*");
		System.err.println("element count: " + elements.count());
//		LayoutUtils lu = new LayoutUtils(moz);

		for (int i=0; i<elements.count(); i++) {
			ElementInfo ei = new ElementInfo();
			
			ei.element = elements.at(i);
			
//			ei.bounds = lu.getElementBox(ei.element);
			ei.bounds = new Rectangle(
					ei.element.geometry().left(),
					ei.element.geometry().top(),
					ei.element.geometry().width(),
					ei.element.geometry().height()
					);
			
			System.out.println(ei.element.attribute("id"));
			
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
		for (ElementInfo e : getLayoutInfo()) {
			Rectangle r = e.getBounds();
			image.drawShape(r, colour);
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
			System.err.println("Error finding content ids");
			e.printStackTrace();
		}

		return ids;
	}

	public MBFImage renderContentLayout() 
	{
		//Pixel p = LayoutUtils.renderSize(page.mainFrame());
		int w = browser.getWidth();
		int h = browser.getHeight();
		
		MBFImage image = new MBFImage(w, h, ColourSpace.RGB);
		return renderContentLayout(image, RGBColour.WHITE);
	}
	
	public MBFImage renderContentLayout(MBFImage image, Float[] c) {
		//Color c2 = new Color(c.getRed(), c.getBlue(), c.getGreen(), 10);

		List<Rectangle> content_areas = new ArrayList<Rectangle>();
		List<Rectangle> non_content_areas_inside = new ArrayList<Rectangle>();
		
		for (ElementInfo ei : getLayoutInfo()) {
			if (ei.isContent) {
				content_areas.add(ei.bounds);
			} else if (ei.isInsideContent) {
				non_content_areas_inside.add(ei.bounds);
			}
		}

		for (Rectangle r : content_areas) {
//			g.setColor(c2);
//			g.fillRect(r.x, r.y, r.width, r.height);
//			g.setColor(c);
//			g.drawRect(r.x, r.y, r.width, r.height);
			image.drawShape(r, c);
		}

		for (Rectangle r : non_content_areas_inside) {
//			g.setColor(Color.GREEN);
//			g.drawRect(r.x, r.y, r.width, r.height);
			image.drawShape(r, RGBColour.GREEN);
		}

		return image;
	}

	public void extract() throws IOException {
		List<ElementInfo> info = getLayoutInfo();
		System.out.println(ElementInfo.getCSVHeader());
		for (ElementInfo ei : info) {
			System.out.println(ei.toCSVString());
		}

		System.out.println("Extracting images");
		MBFImage im = browser.renderToImage();
		ImageUtilities.write(im, "png", new File("out.png"));
		ImageUtilities.write(renderLayoutInfo(im, RGBColour.RED), "png", new File("layout.png"));

		System.out.println("Extracting content image");
		im = browser.renderToImage();
		ImageUtilities.write(renderContentLayout(im, RGBColour.RED), "png", new File("contentlayout.png"));

		System.out.println("done");
	}
	
	public static void main( String[] args ) throws IOException {
		LayoutExtractor extractor = new LayoutExtractor();
		extractor.loadPage("http://www.bbc.co.uk/news/uk-england-14214375");
		extractor.extract();
	}
}
