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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A script for reading the RDF dump from DMOZ and flattening it to
 * CSV format
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class Dmoz2CSV {
	static class Topic {
		String name;
		List<String> link = new ArrayList<String>();
		List<String> link1 = new ArrayList<String>();
	}

	static class ExternalPage {
		String about;
		String title;
		String description;
	}

	static class RecordsHandler extends DefaultHandler {
		Set<Topic> topics = new HashSet<Topic>();
		Map<String, ExternalPage> resources = new HashMap<String, ExternalPage>();
		Topic currentTopic = null;
		ExternalPage currentResource = null;
		boolean isTitle = false;
		boolean isDescription = false;

		@Override
		public void startElement(String ns, String localName, String qName, Attributes atts) {
			if (qName.equals("Topic")) {
				currentTopic = new Topic();
				currentTopic.name = atts.getValue("r:id");
			} else if (qName.equals("link")) {
				currentTopic.link.add(atts.getValue("r:resource"));
			} else if (qName.equals("link1")) {
				currentTopic.link1.add(atts.getValue("r:resource"));
			} else if (qName.equals("ExternalPage")) {
				currentResource = new ExternalPage();
				currentResource.about = atts.getValue("about");
			} else if (qName.equals("d:Title")) {
				isTitle = true;
			} else if (qName.equals("d:Description")) {
				isDescription = true;
			}
		}

		@Override
		public void characters(char[] chars, int offset, int length) {
			if (isDescription)
				currentResource.description = new String(chars, offset, length);
			if (isTitle)
				currentResource.title = new String(chars, offset, length);

			isTitle = false;
			isDescription = false;
		}

		@Override
		public void endElement(String ns, String localName, String qName) {
			if (qName.equals("Topic")) {
				if (currentTopic.link.size() > 0 && currentTopic.link1.size() > 0)
					topics.add(currentTopic);
			} else if (qName.equals("ExternalPage")) {
				resources.put(currentResource.about, currentResource);
			}
		}
	}

	/**
	 * Returns a field value escaped for special characters
	 * @param input A String to be evaluated
	 * @return A properly formatted String
	 */
	static String escape(String input) {
		input = input.replaceAll("\n", " ");
		input = input.replaceAll("\r", " ");

		if (input.contains(",") || input.contains("\"") || (!input.trim().equals(input))) { 
			return '"' + input.replaceAll("\"", "\"\"") + '"';
		} else {
			return input;
		}
	}

	/**
	 * Appends a row of values to the output
	 * @param values A list of values
	 * @return this CsvBuffer instance
	 */
	static String toCSV(Object... values) {
		List<String> escapedValues = new ArrayList<String>();
		for (Object o : values) escapedValues.add(escape(o.toString()));

		StringBuilder content = new StringBuilder();
		for (int i=0; i<escapedValues.size(); i++) { 
			content.append(escapedValues.get(i));
			if (i < escapedValues.size()-1) content.append(",");
		}
		content.append("\r\n");
		return content.toString();
	}

	/**
	 * Main method. 
	 * @param args
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(String[] args) throws SAXException, ParserConfigurationException, IOException {
		RecordsHandler handler = new RecordsHandler();
		XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		reader.setContentHandler(handler);

		for (String file : args) {
			InputSource is = new InputSource(new InputStreamReader(new FileInputStream(file), "UTF-8"));

			is.setEncoding("UTF-8");
			reader.parse(is);

			for (Topic top : handler.topics) {
				for (String it : top.link1) {
					System.out.println(toCSV(top.name, "LINK1", it, handler.resources.get(it).title, handler.resources.get(it).description));
				}

				for (String it : top.link) {
					System.out.println(toCSV(top.name, "LINK", it, handler.resources.get(it).title, handler.resources.get(it).description));
				}
			}
		}
	}
}