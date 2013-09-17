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
