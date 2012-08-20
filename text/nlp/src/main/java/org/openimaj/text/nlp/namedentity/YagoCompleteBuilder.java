package org.openimaj.text.nlp.namedentity;

import org.openimaj.text.nlp.namedentity.YagoEntityCompleteExtractorFactory.YagoEntityCompleteExtractor;

/**
 * Tool for building the Alias text file and Lucene Index required by
 * {@link YagoEntityCompleteExtractorFactory} to build
 * {@link YagoEntityCompleteExtractor}
 */
public class YagoCompleteBuilder {

	/**
	 * Name of default root directory to be created in the home directory.
	 */
	public static String ROOT_DIRECTORY = ".YagoEntity";

}
