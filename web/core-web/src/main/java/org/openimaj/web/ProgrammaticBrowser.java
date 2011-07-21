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

public class ProgrammaticBrowser {
	private static final Logger logger = Logger.getLogger(ProgrammaticBrowser.class);
	
	private QWebPage webpage;
	private QWebFrame webframe;
	private Boolean currentLoadingStatus;

	private long mainLoopSleepTime = 10; //in ms
	
	public ProgrammaticBrowser() {
		QApplication.initialize(new String [] {});
        webpage = new QWebPage();
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
    
    public String getHTML() {
        return webframe.toHtml();
    }
    
    public String getURL() {
        return webframe.url().toString();
    }
    
    public boolean load(URL url) {
    	return load(url.toString());
    }
    
    public boolean load(String url) {
        webframe.load(new QUrl(url));
        return waitForLoad();
    }

    /**
	 * Get the BODY element of the loaded page
	 * @return body element or null if it doesn't exist
	 */
	public QWebElement getBody() {
		return webframe.findFirstElement("BODY");
	}
    
	public QWebElementCollection findAllElements(String selectorQuery) {
		return webframe.findAllElements(selectorQuery);
	}
	
	public QWebElement findFirstElement(String selectorQuery) {
		return webframe.findFirstElement(selectorQuery);
	}
	
	/**
	 * Get a render of the page as an image
	 * @return Rendered page image
	 */
	public MBFImage renderToImage() {
		QSize size = webframe.contentsSize();
		QImage image = new QImage(size, QImage.Format.Format_RGB888);
		QPainter p = new QPainter(image);
		p.setRenderHint(QPainter.RenderHint.Antialiasing, false);
		p.setRenderHint(QPainter.RenderHint.TextAntialiasing, false);
		p.setRenderHint(QPainter.RenderHint.SmoothPixmapTransform, false);
		webframe.render(p);
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
	
	public int getWidth() {
		return webframe.contentsSize().width();
	}
	
	public int getHeight() {
		return webframe.contentsSize().height();
	}
}
