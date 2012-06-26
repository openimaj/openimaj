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
package org.openimaj.web;

import java.net.URL;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;

import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.webkit.QWebElement;
import com.trolltech.qt.webkit.QWebElementCollection;
import com.trolltech.qt.webkit.QWebFrame;
import com.trolltech.qt.webkit.QWebPage;

/**
 * An offscreen web-browser that can be accessed programmatically.
 * Allows rendering to an {@link MBFImage}, etc.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ProgrammaticBrowser {
	private static final Logger logger = Logger.getLogger(ProgrammaticBrowser.class);
	
	private QWebPage webpage;
	private QWebFrame webframe;
	private Boolean currentLoadingStatus;

	private long mainLoopSleepTime = 10; //in ms

	private static boolean qapp_init = false;
	
	/**
	 * Default constructor. Uses an {@link DefaultBrowserDelegate},
	 * so you wont see any dialogs, etc. 
	 */
	public ProgrammaticBrowser() {
		this(new DefaultBrowserDelegate());
	}
	
	/**
	 * Construct with given delegate object.
	 * Setting the delegate to null will enable default
	 * behavior - i.e. dialogs will be drawn on the screen!
	 * @param delegate 
	 */
	public ProgrammaticBrowser(final BrowserDelegate delegate) {
		synchronized (ProgrammaticBrowser.class) {
			if (!qapp_init) {
				QApplication.initialize(new String [] {});
				qapp_init = true;
			}
		}
				
        webpage = new QWebPage() {
			/* (non-Javadoc)
			 * @see com.trolltech.qt.webkit.QWebPage#javaScriptAlert(com.trolltech.qt.webkit.QWebFrame, java.lang.String)
			 */
			@Override
			protected void javaScriptAlert(QWebFrame originatingFrame, String msg) {
				if (delegate != null) 
					delegate.javaScriptAlert(originatingFrame, msg);
				else 
					super.javaScriptAlert(originatingFrame, msg);
			}

			/* (non-Javadoc)
			 * @see com.trolltech.qt.webkit.QWebPage#javaScriptConfirm(com.trolltech.qt.webkit.QWebFrame, java.lang.String)
			 */
			@Override
			protected boolean javaScriptConfirm(QWebFrame originatingFrame, String msg) {
				if (delegate != null) 
					return delegate.javaScriptConfirm(originatingFrame, msg);
				return super.javaScriptConfirm(originatingFrame, msg);
			}

			/* (non-Javadoc)
			 * @see com.trolltech.qt.webkit.QWebPage#javaScriptConsoleMessage(java.lang.String, int, java.lang.String)
			 */
			@Override
			protected void javaScriptConsoleMessage(String message, int lineNumber, String sourceID) {
				if (delegate != null)
					delegate.javaScriptConsoleMessage(message, lineNumber, sourceID);
				super.javaScriptConsoleMessage(message, lineNumber, sourceID);
			}

			/* (non-Javadoc)
			 * @see com.trolltech.qt.webkit.QWebPage#javaScriptPrompt(com.trolltech.qt.webkit.QWebFrame, java.lang.String, java.lang.String)
			 */
			@Override
			protected String javaScriptPrompt(QWebFrame originatingFrame, String msg, String defaultValue) {
				if (delegate != null)
					delegate.javaScriptPrompt(originatingFrame, msg, defaultValue);
				return super.javaScriptPrompt(originatingFrame, msg, defaultValue);
			}
        };
        webframe = webpage.mainFrame();
        
        currentLoadingStatus = null;

        webpage.loadFinished.connect(this, "loadFinished(boolean)");
        webpage.loadStarted.connect(this, "loadStarted()");
	}
	
	private void mainEventLoop() {
        QApplication.processEvents();
        
        try { Thread.sleep(mainLoopSleepTime); } catch (InterruptedException e) {}
	}
                        
	protected void loadStarted() {
    	logger.debug("Loading page " + getURL());
    }
    
	protected boolean waitForLoad() {
    	try {
			return waitForLoad(0);
		} catch (TimeoutException e) {
			//should never happen!!!
			throw new RuntimeException(e);
		}
    }
    
	protected void loadFinished(boolean successful) {
    	currentLoadingStatus = successful;
    	logger.info(String.format("Page load finished (%d bytes): %s (%s)", getHTML().length(), getURL(), successful ? "successful" : "error"));
    }
    
    private boolean waitForLoad(long timeout) throws TimeoutException {
    	mainEventLoop();
    	
    	long itime = System.currentTimeMillis();
    	
    	currentLoadingStatus = null;
        while (currentLoadingStatus == null) {
        	if (timeout != 0 && System.currentTimeMillis() - itime > timeout)
                throw new TimeoutException(String.format("Timeout reached: %d seconds", timeout));
            mainEventLoop();
        }
        mainEventLoop();
        if (currentLoadingStatus) {
            webpage.setViewportSize(webpage.mainFrame().contentsSize());
        }
        
        return currentLoadingStatus;
    }
    
    /**
     * Run the browsers main event loop for timeout milliseconds
     * @param timeout the running time
     */
    public void mainLoop(long timeout) {
    	mainEventLoop();
    	
    	long itime = System.currentTimeMillis();
    	
    	while (true) {
        	if (timeout == 0 || System.currentTimeMillis() - itime > timeout)
                break;
            mainEventLoop();
        }
        mainEventLoop();
    }
    
    /**
     * Get the HTML of the currently loaded page
     * @return the html as a string
     */
    public String getHTML() {
    	return webframe.toHtml();
    }
    
    /**
     * Get the URL for the currently loaded page
     * @return the url
     */
    public String getURL() {
        return webframe.url().toString();
    }
    
    /**
     * Load the page with the given URL
     * @param url the url to load.
     * @return true if successful; false otherwise.
     */
    public boolean load(URL url) {
    	return load(url.toString());
    }
    
    /**
     * Load the page with the given URL
     * @param url the url to load.
     * @param timeout the amount of time to wait for the page to load before failing
     * @return true if successful; false otherwise.
     * @throws TimeoutException 
     */
    public boolean load(URL url, long timeout) throws TimeoutException {
    	return load(url.toString(), timeout);
    }
    
    /**
     * Load the page with the given URL
     * @param url the url to load.
     * @param timeout the amount of time to wait for the page to load before failing
     * @return true if successful; false otherwise.
     * @throws TimeoutException 
     */
    public boolean load(String url, long timeout) throws TimeoutException {
        webframe.load(new QUrl(url));
        return waitForLoad(timeout);
    }
    
    /**
     * Load the page with the given URL
     * @param url the url to load.
     * @return true if successful; false otherwise.
     */
    public boolean load(String url) {
        webframe.load(new QUrl(url));
        return waitForLoad();
    }
    
    /**
     * Load the given html string into the browser
     * @param html the html string
     * @return true if successful; false otherwise.
     */
    public boolean loadHTML(String html) {
        webframe.setHtml(html);
        return waitForLoad();
    }

    /**
	 * Get the BODY element of the loaded page
	 * @return body element or null if it doesn't exist
	 */
	public QWebElement getBody() {
		return webframe.findFirstElement("BODY");
	}
    
	/**
	 * Get all DOM elements matching the given CSS selector
	 * @param selectorQuery the CSS selector
	 * @return collection of elements
	 */
	public QWebElementCollection findAllElements(String selectorQuery) {
		return webframe.findAllElements(selectorQuery);
	}
	
	/**
	 * Get the first DOM element corresponding to the given CSS selector
	 * @param selectorQuery the CSS selector
	 * @return the first element, or null if no matching element is found
	 */
	public QWebElement findFirstElement(String selectorQuery) {
		return webframe.findFirstElement(selectorQuery);
	}
	
	/**
	 * Get a render of the page as an image
	 * @return Rendered page image
	 */
	public MBFImage renderToImage() {
		QWebElement ele = webframe.documentElement();
		
		if (ele == null) return null;
		
		QSize size = ele.geometry().size();
		
		System.out.println(size);
		
		if (size.width() <= 0 || size.height() <= 0)
			return null;
		
		QImage image = new QImage(size, QImage.Format.Format_ARGB32_Premultiplied);
		QPainter p = new QPainter(image);
		p.setRenderHint(QPainter.RenderHint.Antialiasing, false);
		p.setRenderHint(QPainter.RenderHint.TextAntialiasing, false);
		p.setRenderHint(QPainter.RenderHint.SmoothPixmapTransform, false);
		ele.render(p);
		p.end();
		
		int width = image.width();
		int height = image.height();
		
		MBFImage mbfimage = new MBFImage(width, height, ColourSpace.RGB);
		FImage rf = mbfimage.bands.get(0);
		FImage gf = mbfimage.bands.get(1);
		FImage bf = mbfimage.bands.get(2);
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {				
				int rgb = image.pixel(x, y);
				int r = ((rgb >> 16) & 0xff);
				int g = ((rgb >> 8) & 0xff);
				int b = ((rgb) & 0xff);
				
				rf.pixels[y][x] = r / 255f;
				gf.pixels[y][x] = g / 255f;
				bf.pixels[y][x] = b / 255f;
			}
		}
		return mbfimage;
	}
	
	/**
	 * Get a render of the page as an image
	 * @param width 
	 * @param height 
	 * @return Rendered page image
	 */
	public MBFImage renderToImage(int width, int height) {
		QWebElement ele = webframe.documentElement();
		
		if (ele == null) return null;
		
		QSize size = ele.geometry().size();
		
		if (size.width() < width) width = size.width();
		if (size.height() < height) height = size.height();
		
		if (width <= 0 || height <= 0)
			return null;
		
		QImage image = new QImage(new QSize(width, height), QImage.Format.Format_ARGB32_Premultiplied);
		QPainter p = new QPainter(image);
		p.setRenderHint(QPainter.RenderHint.Antialiasing, false);
		p.setRenderHint(QPainter.RenderHint.TextAntialiasing, false);
		p.setRenderHint(QPainter.RenderHint.SmoothPixmapTransform, false);
		ele.render(p);
		p.end();
		
		MBFImage mbfimage = new MBFImage(width, height, ColourSpace.RGB);
		FImage rf = mbfimage.bands.get(0);
		FImage gf = mbfimage.bands.get(1);
		FImage bf = mbfimage.bands.get(2);
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {				
				int rgb = image.pixel(x, y);
				int r = ((rgb >> 16) & 0xff);
				int g = ((rgb >> 8) & 0xff);
				int b = ((rgb) & 0xff);
				
				rf.pixels[y][x] = r / 255f;
				gf.pixels[y][x] = g / 255f;
				bf.pixels[y][x] = b / 255f;
			}
		}
		return mbfimage;
	}
	
	/**
	 * Get the width of the browser. The width is automatically adjusted to
	 * fit the content.
	 * @return the width in pixels
	 */
	public int getWidth() {
		return webframe.contentsSize().width();
	}
	
	/**
	 * Get the height of the browser. The height is automatically adjusted to
	 * fit the content.
	 * @return the height in pixels
	 */
	public int getHeight() {
		return webframe.contentsSize().height();
	}
}
