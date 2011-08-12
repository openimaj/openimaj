package org.openimaj.web;

import org.apache.log4j.Logger;

import com.trolltech.qt.webkit.QWebFrame;

/**
 * A {@link BrowserDelegate} that does nothing other
 * than log any javascript calls, etc. 
 * 
 * Confirm will always return false. Prompt will
 * always return the default value. No dialogs will
 * actually be shown.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class DefaultBrowserDelegate implements BrowserDelegate {
	private static final Logger logger = Logger.getLogger(DefaultBrowserDelegate.class);
	
	@Override
	public void javaScriptAlert(QWebFrame originatingFrame, String msg) {
		logger.info("javaScriptAlert(" + msg + ")");
	}

	@Override
	public boolean javaScriptConfirm(QWebFrame originatingFrame, String msg) {
		logger.info("javaScriptConfirm(" + msg + ")");
		return false;
	}

	@Override
	public void javaScriptConsoleMessage(String message, int lineNumber, String sourceID) {
		logger.info("javaScriptConsoleMessage(" + sourceID + ":" + lineNumber+ ": " + message + ")");
	}

	@Override
	public String javaScriptPrompt(QWebFrame originatingFrame, String msg, String defaultValue) {
		logger.info("javaScriptPrompt(" + msg + ")");
		return defaultValue;
	}
}
