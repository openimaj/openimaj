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
package org.openimaj.text.nlp.namedentity;

import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.store.SimpleFSDirectory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class has various methods that can be used to build the resources
 * required by {@link YagoEntityCandidateFinder},
 * {@link YagoEntityContextScorer} and {@link YagoEntityExactMatcher}. These
 * resources are a text File of entity aliases, and a lucene index of contextual
 * data.
 * 
 * The directory of the stripped down Yago tsv files is required. This directory
 * can be built with {@link SeedBuilder}.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class EntityExtractionResourceBuilder {

	/**
	 * Default file name for the alias text file.
	 */
	public static String DEFAULT_ALIAS_NAME = "AliasMapFile.txt";
	/**
	 * Default directory name for the lucene index.
	 */
	public static String DEFAULT_CONTEXT_NAME = "YagoLucene";
	private static String DEFAULT_ROOT_NAME = ".YagoEntityExtraction";
	private static String wikiApiPrefix = "http://en.wikipedia.org/w/api.php?format=xml&action=query&titles=";
	private static String wikiApiSuffix = "&prop=revisions&rvprop=content";
	private boolean verbose = true;
	// This will build for location entities. There are too many for memory.
	// Leave false.
	private boolean locations = false;
	private static BufferedWriter logOut;

	/**
	 * Builds the alias text file in the default location.
	 * 
	 * @param seedDirectoryPath
	 *            = path location of the stripped down Yago .tsv files.
	 */
	public void buildCandidateAliasFile(String seedDirectoryPath) {
		buildCandidateAliasFile(seedDirectoryPath, getDefaultRootPath()
				+ File.separator + DEFAULT_ALIAS_NAME);
	}

	/**
	 * Builds the alias text file in the specified location.
	 * 
	 * @param seedDirectoryPath
	 *            = path location of the stripped down Yago .tsv files.
	 * @param destinationPath
	 *            = path to build the alias text file.
	 */
	public void buildCandidateAliasFile(String seedDirectoryPath,
			String destinationPath)
	{
		writeAliasFile(getEntities(seedDirectoryPath), destinationPath,
				seedDirectoryPath);
	}

	/**
	 * Builds the lucene index in the default path.
	 * 
	 * @param seedDirectoryPath
	 *            = path location of the stripped down Yago .tsv files.
	 */
	public void buildContextLuceneIndex(String seedDirectoryPath) {
		buildContextLuceneIndex(seedDirectoryPath, getDefaultRootPath()
				+ File.separator + DEFAULT_CONTEXT_NAME);
	}

	/**
	 * Builds the lucene index at the specified path.
	 * 
	 * @param seedDirectoryPath
	 * @param destinationPath
	 */
	public void buildContextLuceneIndex(String seedDirectoryPath,
			String destinationPath)
	{
		try {
			buildIndex(getEntities(seedDirectoryPath), destinationPath,
					seedDirectoryPath);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Builds the alias text file and the lucene index in the default root
	 * directory.
	 * 
	 * @param seedDirectoryPath
	 */
	public void buildAll(String seedDirectoryPath) {
		validateFileStructure();
		createLogging(getDefaultRootPath() + File.separator + "log.txt");
		buildAll(seedDirectoryPath, getDefaultRootPath());
		try {
			logOut.flush();
			logOut.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Builds the alias text file and the lucene index in the specified root
	 * directory.
	 * 
	 * @param seedDirectoryPath
	 * @param destinationPath
	 */
	public void buildAll(String seedDirectoryPath, String destinationPath) {
		// Get the entities as people and organisations
		print("Building All...");
		final HashMap<String, YagoNamedEntity> entities = getEntities(seedDirectoryPath);
		writeAliasFile(entities, destinationPath + File.separator
				+ DEFAULT_ALIAS_NAME, seedDirectoryPath);
		try {
			buildIndex(entities, destinationPath + File.separator
					+ DEFAULT_CONTEXT_NAME, seedDirectoryPath);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		print("Done");
	}

	/**
	 * @return default root directory path for all YagoEntity resources.
	 */
	public static String getDefaultRootPath() {
		return System.getProperty("user.home") + File.separator
				+ DEFAULT_ROOT_NAME;
	}

	/**
	 * @return default alias text file path.
	 */
	public static String getDefaultAliasFilePath() {
		return getDefaultRootPath() + File.separator + DEFAULT_ALIAS_NAME;
	}

	/**
	 * @return defualt lucene directory path.
	 */
	public static String getDefaultIndexDirectoryPath() {
		return getDefaultRootPath() + File.separator + DEFAULT_CONTEXT_NAME;
	}

	public static String getAliasFrom(String rootName) {
		String result;
		String noGeo = null;
		if (rootName.startsWith("geoent_")) {
			noGeo = rootName.substring(rootName.indexOf('_') + 1,
					rootName.lastIndexOf('_'));
		} else
			noGeo = rootName;
		final String spaces = noGeo.replaceAll("_", " ");
		String noParen;
		if (spaces.contains("("))
			noParen = spaces.substring(0, spaces.indexOf("("));
		else
			noParen = spaces;
		String dropComma;
		if (noParen.contains(","))
			dropComma = noParen.substring(0, spaces.indexOf(","));
		else
			dropComma = noParen;
		result = dropComma;
		return result;
	}

	private void validateFileStructure() {
		final File rootDir = new File(getDefaultRootPath());
		if (!rootDir.isDirectory()) {
			rootDir.mkdir();
		}
		final File indexDir = new File(getDefaultRootPath() + File.separator
				+ DEFAULT_CONTEXT_NAME);
		if (!indexDir.isDirectory()) {
			indexDir.mkdir();
		} else {
			for (final File f : indexDir.listFiles())
				f.delete();
		}
	}

	private static void createLogging(String logFilePath) {
		final File f = new File(logFilePath);
		if (!f.isFile()) {
			try {
				f.createNewFile();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		} else {
		}
		FileWriter fstream = null;
		try {
			fstream = new FileWriter(logFilePath);
			logOut = new BufferedWriter(fstream);
			logOut.write("");
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void buildIndex(HashMap<String, YagoNamedEntity> entities,
			String destinationPath, String seedDirectoryPath)
			throws IOException
	{
		print("Building Index...");
		setEntityContextValues(entities, seedDirectoryPath);
		print("Initializing Lucene objects...");

		// initialize lucene objects
		final String[] names = { "uri", "context", "type" };
		FieldType[] types;
		final FieldType ti = new FieldType();
		ti.setIndexed(true);
		ti.setTokenized(true);
		ti.setStored(true);
		final FieldType n = new FieldType();
		n.setStored(true);
		n.setIndexed(true);
		types = new FieldType[3];
		types[0] = n;
		types[1] = ti;
		types[2] = n;
		final File f = new File(destinationPath);
		final QuickIndexer qi = new QuickIndexer(new SimpleFSDirectory(f));

		// Initialize wiki objects
		final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = null;
		Document doc;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			e.printStackTrace();
		}
		doc = null;
		final WikiModel wikiModel = new WikiModel(
				"http://www.mywiki.com/wiki/${image}",
				"http://www.mywiki.com/wiki/${title}");
		int count = 0;
		print("Building Lucene Index...");
		for (final YagoNamedEntity entity : entities.values()) {
			count++;
			if (count % 5000 == 0)
				print("Processed " + count);
			// if wikiURL, add wiki to context
			if (entity.wikiURL != null) {
				final String title = entity.wikiURL.substring(entity.wikiURL
						.lastIndexOf("/") + 1);
				try {
					doc = docBuilder.parse(wikiApiPrefix + title
							+ wikiApiSuffix);
				} catch (final SAXException e) {
					e.printStackTrace();
				} catch (final IOException e) {
					e.printStackTrace();
				}
				doc.getDocumentElement().normalize();
				final NodeList revisions = doc.getElementsByTagName("rev");
				if (revisions.getLength() > 0) {
					final String markup = revisions.item(0).getTextContent();

					// convert markup dump to plaintext.
					final String plainStr = wikiModel.render(
							new PlainTextConverter(), markup);
					// add it to the context.
					entity.addContext(plainStr);
				}
			}
			final String[] values = { entity.rootName, entity.getContext(),
					entity.type.toString() };
			qi.addDocumentFromFields(names, values, types);
		}
		qi.finalise();
	}

	private void setEntityContextValues(
			final HashMap<String, YagoNamedEntity> entities,
			String seedDirectoryPath)
	{
		print("Setting Context Values...");
		BufferedReader in = null;
		// Created
		try {
			in = openFileAsReadStream(seedDirectoryPath + File.separator
					+ "created_stripped.tsv");
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		StreamLooper sl = new StreamLooper(in) {
			@Override
			protected void doWork(String s) {
				final String[] values = s.split("\\s+");
				final String rootName = values[1];
				final String context = convertResource(values[2]);
				if (entities.keySet().contains(rootName)) {
					entities.get(rootName).addContext(context);
				}
			}
		};
		sl.loop();

		// wikiAnchorText
		try {
			in = openFileAsReadStream(seedDirectoryPath + File.separator
					+ "hasWikipediaAnchorText_stripped.tsv");
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		sl = new StreamLooper(in) {
			@Override
			protected void doWork(String s) {
				final String[] values = s.split("\\s+");
				final String rootName = values[1];
				final String context = convertLiteral(values[2]);
				if (entities.keySet().contains(rootName)) {
					entities.get(rootName).addContext(context);
				}
			}
		};
		sl.loop();

		// wikiUrl

		try {
			in = openFileAsReadStream(seedDirectoryPath + File.separator
					+ "hasWikipediaUrl_stripped.tsv");
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		sl = new StreamLooper(in) {
			@Override
			protected void doWork(String s) {
				final String[] values = s.split("\\s+");
				final String rootName = values[1];
				if (entities.keySet().contains(rootName)) {
					entities.get(rootName).wikiURL = values[2].replaceAll("\"",
							"");
				}
			}
		};
		sl.loop();
		// validate
		print("Validating Context...");
		int noContext = 0;
		for (final YagoNamedEntity ne : entities.values()) {
			for (final String alias : ne.aliasList) {
				ne.addContext(alias);
			}
			if ((ne.getContext() == null || ne.getContext().equals(""))
					&& ne.wikiURL == null)
			{
				noContext++;
			}
		}
		print("No Context: " + noContext);
	}

	private void setEntityAliasValues(
			final HashMap<String, YagoNamedEntity> entities,
			String seedDirectoryPath)
	{
		print("Setting Alias Values...");
		// Populate 'isCalled'
		BufferedReader in = null;
		try {
			in = openFileAsReadStream(seedDirectoryPath + File.separator
					+ "isCalled_stripped.tsv");
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		StreamLooper sl = new StreamLooper(in) {
			@Override
			protected void doWork(String s) {
				final String[] values = s.split("\\s+");
				final String rootName = values[1];
				final String alias = convertLiteral(values[2]);
				if (entities.keySet().contains(rootName)) {
					entities.get(rootName).addAlias(alias);
				}
			}
		};
		sl.loop();

		// populate 'means'

		try {
			in = openFileAsReadStream(seedDirectoryPath + File.separator
					+ "means_stripped.tsv");
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		sl = new StreamLooper(in) {
			@Override
			protected void doWork(String s) {
				final String[] values = s.split("\\s+");
				final String rootName = values[2];
				final String alias = convertLiteral(values[1]);
				// System.out.println(alias);
				if (entities.keySet().contains(rootName)) {
					entities.get(rootName).addAlias(alias);
				}
			}
		};
		sl.loop();
		print("Validating Aliases...");
		for (final YagoNamedEntity ne : entities.values()) {
			final String alias = getAliasFrom(ne.rootName);
			ne.addAlias(alias);
		}
	}

	private void writeAliasFile(HashMap<String, YagoNamedEntity> entities,
			String destinationPath, String seedDirectoryPath)
	{
		setEntityAliasValues(entities, seedDirectoryPath);

		BufferedWriter w;
		try {
			w = openFileAsWriteStream(destinationPath);
			w.write("");
			for (final YagoNamedEntity ne : entities.values()) {
				if (ne.aliasList.size() > 0) {
					w.append("+" + ne.rootName + "\n");
					for (final String alias : ne.aliasList) {
						w.append("." + alias + "\n");
					}
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private HashMap<String, YagoNamedEntity> getEntities(
			String seedDirectoryPath)
	{
		print("Getting Entities...");
		final HashMap<String, YagoNamedEntity> result = new HashMap<String, YagoNamedEntity>();
		BufferedReader in = null;
		try {
			in = openFileAsReadStream(seedDirectoryPath + File.separator
					+ "wordnet_person_100007846.txt");
		} catch (final FileNotFoundException e2) {
			e2.printStackTrace();
		}
		// get People
		StreamLooper sl = new StreamLooper(in) {
			@Override
			protected void doWork(String s) {
				final String[] values = s.split("\\s+");
				final String rootName = convertLiteral(values[1]);
				if (!rootName.startsWith("Category:")) {
					final YagoNamedEntity ne = new YagoNamedEntity(rootName,
							NamedEntity.Type.Person);
					result.put(rootName, ne);
				}
			}
		};
		sl.loop();

		// get Organisations
		try {
			in = openFileAsReadStream(seedDirectoryPath + File.separator
					+ "wordnet_organization_108008335.txt");
		} catch (final FileNotFoundException e1) {
			e1.printStackTrace();
		}
		sl = new StreamLooper(in) {
			@Override
			protected void doWork(String s) {
				final String[] values = s.split("\\s+");
				final String rootName = convertLiteral(values[1]);
				if (!(rootName.startsWith("Category:") || rootName
						.startsWith("geoent_")))
				{
					final YagoNamedEntity ne = new YagoNamedEntity(rootName,
							NamedEntity.Type.Organisation);
					result.put(rootName, ne);
				}
			}
		};
		sl.loop();

		if (locations) {
			// get Locations
			try {
				in = openFileAsReadStream(seedDirectoryPath + File.separator
						+ "wordnet_location_100027167.txt");
			} catch (final FileNotFoundException e1) {
				e1.printStackTrace();
			}
			sl = new StreamLooper(in) {
				@Override
				protected void doWork(String s) {
					final String[] values = s.split("\\s+");
					final String rootName = convertLiteral(values[1]);
					if (!rootName.startsWith("Category:")) {
						final YagoNamedEntity ne = new YagoNamedEntity(rootName,
								NamedEntity.Type.Location);
						result.put(rootName, ne);
					}
				}
			};
			sl.loop();
		}
		print("Total Entities: " + result.size());
		return result;
	}

	public static BufferedReader openFileAsReadStream(String path)
			throws FileNotFoundException
	{
		FileReader fr = null;
		fr = new FileReader(path);
		final BufferedReader br = new BufferedReader(fr);
		return br;
	}

	public static BufferedWriter openFileAsWriteStream(String path)
			throws IOException
	{
		FileWriter fw = null;
		fw = new FileWriter(path);
		final BufferedWriter bw = new BufferedWriter(fw);
		return bw;
	}

	private static String convertLiteral(String literal) {
		final String escaped = StringEscapeUtils.unescapeJava(literal);
		String first = null;
		if (escaped.startsWith("\""))
			first = escaped.substring(1);
		else
			first = escaped;
		if (first.endsWith("\""))
			return first.substring(0, first.length() - 1);
		else
			return first;
	}

	private static String convertResource(String literal) {
		final String escaped = StringEscapeUtils.unescapeJava(literal);
		return escaped.replaceAll("_", " ");
	}

	private void print(String message) {
		if (verbose)
			System.out.println(message);
		if (logOut != null) {
			log(message);
		}
	}

	private void log(String message) {
		try {
			logOut.append(message + "\n");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Defualt main.
	 * 
	 * @param args
	 *            = path to the seed directory.
	 */
	public static void main(String[] args) {
		new EntityExtractionResourceBuilder().buildCandidateAliasFile(args[0]);
	}

	/**
	 * Helper class to iterate through the lines of a Reader to do a bit of work
	 * on each.
	 * 
	 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
	 * 
	 */
	public static abstract class StreamLooper {
		BufferedReader reader;

		public StreamLooper(BufferedReader reader) {
			this.reader = reader;
		}

		/**
		 * Iterates through each line to do the work.
		 */
		public void loop() {
			String s = null;
			try {
				while ((s = reader.readLine()) != null) {
					doWork(s);
				}
				reader.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Do what you want to each line here.
		 * 
		 * @param s
		 */
		protected abstract void doWork(String s);
	}

}
