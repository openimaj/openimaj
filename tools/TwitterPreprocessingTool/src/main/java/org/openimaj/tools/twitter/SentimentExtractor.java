/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openimaj.tools.twitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bill
 */
public abstract class SentimentExtractor {

    public abstract Map<String, Object> extract(List<String> strings);

    public static class MockSentiment extends SentimentExtractor {
        private String mpqaN = "/mpqan.txt";
        private String mpqaP = "/mpqap.txt";

        @Override
        public Map<String, Object> extract(List<String> strings) {

            HashSet<String> mpqaPSet = readSentiSet(mpqaP);
            HashSet<String> mpqaNSet = readSentiSet(mpqaN);
            HashMap<String, Object> output = new HashMap<String, Object>();
            HashSet<String> positiveWords = new HashSet<String>();
            HashSet<String> negativeWords = new HashSet<String>();

            int countP = 0;
            int countN = 0;

            for (String string : strings){

                if (mpqaPSet.contains(string)){
                    countP++;
                    positiveWords.add(string);
                }
                else if (mpqaNSet.contains(string)){
                    countN++;
                    negativeWords.add(string);
                }
            }

            output.put("sentiment", countP - countN);
            output.put("sentiment_positive", countP);
            output.put("sentiment_negative", countN);
            output.put("positive_words",positiveWords);
            output.put("negative_words",negativeWords);

            return output;
        }

        /**
         *
         * @param filepath
         * @return
         */
        public HashSet<String> readSentiSet (String filepath){
            HashSet<String> sentiSet = new HashSet<String>();

            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(SentimentExtractor.class.getResourceAsStream(filepath)));
            } catch (Exception ex) {
                Logger.getLogger(SentimentExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
            try{
                String line = br.readLine();

                while (line != null) {
                    //System.out.println(line);
                    sentiSet.add(line.trim());
                    line = br.readLine();
                }
            }
            catch (IOException ex) {
                Logger.getLogger(SentimentExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
            finally {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(SentimentExtractor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            return sentiSet;
        }
    }
}
