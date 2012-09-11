package org.openimaj.text.nlp.sentiment.lexicon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openimaj.text.nlp.io.FileLooper;

/**
 * An implementation of Hatzivassiloglou and McKeown's approach to a
 * semisupervised method of building a bipolar sentiment lexicon.
 * 
 * @author laurence
 * 
 */
public class HMLexiconBuilder {

	Set<String> positiveLexicon;
	Set<String> negativeLexicon;
	LinkedList<String> posQ;
	LinkedList<String> negQ;
	List<File> corpus;

	public HMLexiconBuilder(List<String> posBootStrap, List<String> negBootStrap) {
		this.positiveLexicon = new HashSet<String>();
		this.negativeLexicon = new HashSet<String>();
		this.posQ = new LinkedList<String>();
		this.negQ = new LinkedList<String>();
		for (String s : posBootStrap) {
			addToLexicon(positiveLexicon, posQ, s);
		}
		for (String s : negBootStrap) {
			addToLexicon(negativeLexicon, negQ, s);
		}
	}

	private void addToLexicon(Set<String> compSet, LinkedList<String> q,
			String token) {
		if (compSet.add(token))
			q.add(token);
	}

	public void buildFromCorpus(List<File> corpus) {
		this.corpus = corpus;
		process();
	}

	private void process() {
		while (!posQ.isEmpty())
			pollTheQ(positiveLexicon, posQ);
		while (!negQ.isEmpty())
			pollTheQ(negativeLexicon, negQ);
	}

	private void pollTheQ(Set<String> lexicon, LinkedList<String> q) {
		String pos = q.poll();
		for (File f : corpus) {

		}
	}

	// Converts the contents of a file into a CharSequence
	// suitable for use by the regex package.
	private CharSequence fromFile(String filename) throws IOException {
		FileInputStream fis = new FileInputStream(filename);
		FileChannel fc = fis.getChannel();

		// Create a read-only CharBuffer on the file
		ByteBuffer bbuf = fc.map(FileChannel.MapMode.READ_ONLY, 0,
				(int) fc.size());
		CharBuffer cbuf = Charset.forName("8859_1").newDecoder().decode(bbuf);
		return cbuf;
	}

}
