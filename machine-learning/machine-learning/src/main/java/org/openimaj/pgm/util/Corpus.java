package org.openimaj.pgm.util;

import java.util.List;

/**
 * A corpus holds a list of documents
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class Corpus {
	private List<Document> documents;
	private int vocabularySize;
	public int getVocabularySize() {
		return vocabularySize;
	}
	public List<Document> getDocuments() {
		return documents;
	}
}
