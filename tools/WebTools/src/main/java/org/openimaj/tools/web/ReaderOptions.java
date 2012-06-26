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
package org.openimaj.tools.web;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

/**
 * Options for the Reader command-line program.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ReaderOptions {
	@Option(name = "--print-debug", aliases="-d", usage = "print debug messages to standard error")
	private boolean debug = false;
	
	@Option(name = "-html", usage = "get article html")
	private boolean html = false;
	
	@Option(name = "-text", usage = "get article text")
	private boolean text = false;
	
	@Option(name = "-title", usage = "get article title")
	private boolean title = false;
	
	@Option(name = "-links", usage = "get article links")
	private boolean links = false;
	
	@Option(name = "-images", usage = "get article images")
	private boolean images = false;

	@Option(name = "-subhead", usage = "get article sub-headings")
	private boolean subhead = false;
	
	@Option(name = "-date", usage = "get article date")
	private boolean date = false;
	
	@Argument(required = true, usage = "document paths or urls to process", metaVar="files_or_urls")
	private List<String> documents = new ArrayList<String>();
	
	/**
	 * @return the debug
	 */
	public boolean isDebug() {
		return debug;
	}
	
	/**
	 * @return the html
	 */
	public boolean isHtml() {
		return html;
	}

	/**
	 * @return the text
	 */
	public boolean isText() {
		return text;
	}

	/**
	 * @return the title
	 */
	public boolean isTitle() {
		return title;
	}

	/**
	 * @return the links
	 */
	public boolean isLinks() {
		return links;
	}

	/**
	 * @return the images
	 */
	public boolean isImages() {
		return images;
	}

	/**
	 * @return the subhead
	 */
	public boolean isSubhead() {
		return subhead;
	}
	
	/**
	 * @return the documents
	 */
	public List<String> getDocuments() {
		return documents;
	}

	/**
	 * @return the subhead
	 */
	public boolean isDate() {
		return date;
	}
	
	/**
	 * @return true if there are multiple docs being processed
	 */
	public boolean isMultiDocument() {
		return documents.size() > 1;
	}
	
	private int countModes() {
		int count = 0;
		
		if (html) count++;
		if (text) count++;
		if (title) count++;
		if (links) count++;
		if (images) count++;
		if (subhead) count++;
		if (date) count++;
		
		return count;
	}
	
	/**
	 * @return true if more than one mode is set.
	 */
	public boolean isMultiMode() {
		return countModes() > 1;
	}
	
	protected void validate() throws CmdLineException {
		if (countModes() == 0)
			throw new CmdLineException(null, "At least one of the [-html, -text, -title, -links or -images] options is required.");
	}
}
