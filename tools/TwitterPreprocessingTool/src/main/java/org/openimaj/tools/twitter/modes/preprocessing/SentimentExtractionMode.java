/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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

import org.openimaj.text.nlp.sentiment.BillMPQASentiment;
import org.openimaj.text.nlp.sentiment.SentimentExtractor;
import org.openimaj.twitter.USMFStatus;

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
