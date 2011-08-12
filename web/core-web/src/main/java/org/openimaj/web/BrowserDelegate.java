package org.openimaj.web;

import com.trolltech.qt.webkit.QWebFrame;

/**
 * Delegate for {@link ProgrammaticBrowser} to hand javascript
 * messages to.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public interface BrowserDelegate {
	/**
	 * @see com.trolltech.qt.webkit.QWebPage#javaScriptAlert(com.trolltech.qt.webkit.QWebFrame, java.lang.String)
	 */
	public void javaScriptAlert(QWebFrame originatingFrame, String msg);

	/**
	 * @see com.trolltech.qt.webkit.QWebPage#javaScriptConfirm(com.trolltech.qt.webkit.QWebFrame, java.lang.String)
	 */
	public boolean javaScriptConfirm(QWebFrame originatingFrame, String msg);

	/**
	 * @see com.trolltech.qt.webkit.QWebPage#javaScriptConsoleMessage(java.lang.String, int, java.lang.String)
	 */
	public void javaScriptConsoleMessage(String message, int lineNumber, String sourceID);

	/**
	 * @see com.trolltech.qt.webkit.QWebPage#javaScriptPrompt(com.trolltech.qt.webkit.QWebFrame, java.lang.String, java.lang.String)
	 */
	public String javaScriptPrompt(QWebFrame originatingFrame, String msg, String defaultValue);
}
