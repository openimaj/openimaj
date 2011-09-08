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
