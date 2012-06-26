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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.cyberneko.html.parsers.DOMParser;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.openimaj.web.readability.Anchor;
import org.openimaj.web.readability.Readability;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Command-line driver for the readability4j engine.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class Reader {
	/**
	 * Main method.
	 * @param args
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static void main(String [] args) throws MalformedURLException, IOException, SAXException {
		ReaderOptions options = new ReaderOptions();
		CmdLineParser parser = new CmdLineParser(options);
		
		PrintStream out = new PrintStream(System.out, true, "UTF-8");
		
	    try {
		    parser.parseArgument(args);
		    options.validate();
		} catch(CmdLineException e) {
		    System.err.println(e.getMessage());
		    System.err.println("Usage: java -jar Readability4J.jar [options...] files_or_urls");
		    parser.printUsage(System.err);
		    return;
		}
		
		for (String document : options.getDocuments()) {
			InputSource is = null;
			
			if (document.contains("://")) {
				is = new InputSource(new URL(document).openStream());
			} else {
				is = new InputSource(new FileInputStream(new File(document)));
			}
			
			DOMParser domparser = new DOMParser();
			domparser.parse(is);
			
			Readability r = new Readability(domparser.getDocument(), options.isDebug());
			
			if (options.isMultiDocument()) {
				//print document location if parsing multiple
				out.println("*** Document: " + document + " ***");
			}
			
			if (options.isTitle()) {
				if (options.isMultiMode()) 
					out.println("* TITLE *");
				out.println(r.getArticleTitle());
			}
			
			if (options.isSubhead()) {
				if (options.isMultiMode()) 
					out.println("* SUB-HEADINGS *");
				
				for (String heading : r.getArticleSubheadings()) {
					out.println(heading);
				}
			}

			if (options.isDate()) {
				if (options.isMultiMode()) 
					out.println("* DATE *");

				out.println(r.getArticleDate());
			}
			
			if (options.isHtml()) {
				if (options.isMultiMode()) 
					out.println("* HTML *");
				out.println(r.getArticleHTML());
			}
			
			if (options.isText()) {
				if (options.isMultiMode()) 
					out.println("* TEXT *");
				out.println(r.getArticleText());
			}
			
			if (options.isLinks()) {
				if (options.isMultiMode()) 
					out.println("* LINKS *");
				
				for (Anchor a : r.getArticleLinks()) {
					out.println(a.getHref() + "\t" + a.getText());
				}
			}
			
			if (options.isImages()) {
				if (options.isMultiMode()) 
					out.println("* IMAGES *");
				
				for (String img : r.getArticleImages()) {
					out.println(img);
				}
			}
		}
	}
}
