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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openimaj.tools.twitter;

import java.io.*;
import java.util.Arrays;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.text.nlp.sentiment.BillMPQASentiment;

/**
 *
 * @author bill
 */
public class SentimentExtractionTest {
    
    /**
        * the output folder
        */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    private File fileFromStream(InputStream stream) throws IOException {
            File f = folder.newFile("tweet" + stream.hashCode() + ".txt");
            PrintWriter writer = new PrintWriter(f,"UTF-8");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String line = null;
            while((line = reader.readLine()) != null){
                    writer.println(line);
            }
            writer.flush(); writer.close();
            return f;
    }
    
    
    @Test
    public void testSentences() throws IOException, Exception{
        String[][] sentences = new String[][]
        {
            new String[]{"happy","happy"},
            new String[]{"sadness","sadness"},
            new String[]{"happy","sadness"}
        };
        
        int[] sentimentScores = new int[]{
            2,-2,0
        };
        
        int[] sentimentPosScores = new int[]{
            2,0,1
        };
        
        for (int i = 0; i < sentences.length; i++){
            BillMPQASentiment mockSenti = new BillMPQASentiment();
            Map<String, Object> mockSentiOut = mockSenti.extract(Arrays.asList(sentences[i]));
            //System.out.println("Sentiment = " + mockSentiOut.get("sentiment") + 
            //        "--- Expected Sentiment = " + sentimentScores[i]);
            //System.out.println("Positive Sentiment = " + mockSentiOut.get("sentiment_positive") + 
            //        "--- Expected Positive Sentiment = " + sentimentPosScores[i]);
            Assert.assertTrue((Integer)mockSentiOut.get("sentiment")==sentimentScores[i]);
            Assert.assertTrue((Integer)mockSentiOut.get("sentiment_positive")==sentimentPosScores[i]);
        }
        
        //File unanalysed = fileFromStream(SentimentExtractionTest.class.getResourceAsStream("/org/openimaj/twitter/json_tweets.txt"));
        //TwitterStatusList<USMFStatus> tweets = FileTwitterStatusList.readUSMF(unanalysed,"UTF-8",GeneralJSONTwitter.class);
        //USMFStatus tweet = tweets.get(0);
        //TwitterPreprocessingMode.results(tweet, new SentimentExtractionMode());
    }
}
