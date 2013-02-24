package org.openimaj.pgm.util;

import gnu.trove.impl.hash.TIntHash;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.io.FileUtils;

/**
 * A corpus from a document whose lines are documents and whose words are seperated by a space
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SimpleCorpusReader implements CorpusReader{

	private InputStream is;

	/**
	 * @param resourceAsStream
	 */
	public SimpleCorpusReader(InputStream resourceAsStream) {
		is = resourceAsStream;
	}

	@Override
	public Corpus readCorpus() throws IOException {
		String[] lines = FileUtils.readlines(is);
		Map<String,Integer> vocabulary = new HashMap<String,Integer>();
		List<TIntIntHashMap> docs = new ArrayList<TIntIntHashMap>();
		for (String docLine : lines) {
			String[] words = docLine.split(" ");
			TIntIntHashMap d = new TIntIntHashMap();
			for (String word : words) {
				Integer value = 0;
				if((value = vocabulary.get(word)) == null){
					vocabulary.put(word, value = vocabulary.size());
				}
				d.adjustValue(value, 1);
			}
			docs.add(d);
		}
		final Corpus c = new Corpus(vocabulary.size());
		for (TIntIntHashMap doc : docs) {
			final Document d = new Document(c);
			doc.forEachEntry(new TIntIntProcedure() {
				@Override
				public boolean execute(int word, int count) {
					d.values.set(word, count);
					return true;
				}
			});
			c.addDocument(d);
		}
		
		return c;
	}

}
