package org.openimaj.text.nlp.sentiment.model.wordlist;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.io.IOUtils;
import org.openimaj.text.nlp.sentiment.model.wordlist.util.TFF;
import org.openimaj.text.nlp.sentiment.model.wordlist.util.TFF.Clue;
import org.openimaj.text.nlp.sentiment.type.DiscreteCountSentiment;

/**
 * An implementation of the weight
 * 
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
@Reference(
	author = { "Janyce Wiebe","Theresa Wilson","Claire Cardie" }, 
	title = "Annotating expressions of opinions and emotions in language. ", 
	type = ReferenceType.Article, 
	year = "2005"
)
public class SentiWordNetModel extends WordListSentimentModel<DiscreteCountSentiment,SentiWordNetModel>{
	private static final String DEFAULT_MODEL = "/org/openimaj/text/sentiment/mpqa/subjclueslen1polar.tff";
	private TFF model;
	
	/**
	 * Construct the sentiment model using the default word clue TFF
	 * @throws IOException
	 */
	public SentiWordNetModel() throws IOException {
		this.model = IOUtils.read(SentiWordNetModel.class.getResourceAsStream(DEFAULT_MODEL), TFF.class);
	}
	
	/**
	 * Construct the sentiment model using the provided tff file
	 * @param f 
	 * @throws IOException
	 */
	public SentiWordNetModel(File f) throws IOException {
		this.model = IOUtils.read(f, TFF.class);
	}
	
	/**
	 * @param mpqa used by clone
	 */
	public SentiWordNetModel(SentiWordNetModel mpqa) {
		this.model = mpqa.model.clone();
	}

	@Override
	public SentiWordNetModel clone() {
		return new SentiWordNetModel(this);
	}

	@Override
	public DiscreteCountSentiment predict(List<String> data) {
		int total = data.size();
		DiscreteCountSentiment sentiment = new DiscreteCountSentiment(total);
		for (String word : data) {
			List<Clue> clueList = this.model.entriesMap.get(word);
			if(clueList  == null) continue; // This isn't a sentiment bearing word
			// this is hidiously simplistic!
			for (Clue clue : clueList) {
				sentiment.incrementClue(clue, 1);
			}
		}
		return sentiment;
	}

}
