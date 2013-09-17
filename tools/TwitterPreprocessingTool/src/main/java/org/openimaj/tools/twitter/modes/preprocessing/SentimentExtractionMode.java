/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openimaj.tools.twitter.modes.preprocessing;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.text.nlp.sentiment.BillMPQASentiment;
import org.openimaj.text.nlp.sentiment.SentimentExtractor;
import org.openimaj.text.nlp.sentiment.model.wordlist.MPQAToken;
import org.openimaj.text.nlp.sentiment.model.wordlist.MPQATokenList;
import org.openimaj.text.nlp.sentiment.type.BipolarSentiment;
import org.openimaj.twitter.USMFStatus;
import org.tartarus.snowball.ext.EnglishStemmer;

/**
 *
 * @author bill
 */
public class SentimentExtractionMode extends TwitterPreprocessingMode<Map<String, Object>> {

    private TwitterPreprocessingMode<Map<String, List<String>>> tokMode;
	private SentimentExtractor mpqaTokenList;

    public SentimentExtractionMode() throws IOException {
        try {
            tokMode = new TokeniseMode();
            this.mpqaTokenList = new BillMPQASentiment();
        }
        catch (Exception e) {
            throw new IOException("Couldn't create required language detector and tokeniser", e);
        }
    }
    @Override
    //public Map<String, Object> process(USMFStatus twitterStatus) {
    //    throw new UnsupportedOperationException("fuck");
    //}
    public Map<String, Object> process(USMFStatus twitterStatus) {
        try {
            Map<String, List<String>> a = TwitterPreprocessingMode.results(twitterStatus, tokMode);
            if(a == null) return null;
            List<String> strings = a.get(TokeniseMode.TOKENS_ALL);
            if(strings == null) return null;
			Map<String, Object> sentiment = this.mpqaTokenList.extract(strings);
			twitterStatus.addAnalysis(getAnalysisKey(), sentiment);
			return sentiment;
            
        } catch (Exception ex) {
            Logger.getLogger(SentimentExtractionMode.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public String getAnalysisKey() {
        return "sentiment";
    }
}
