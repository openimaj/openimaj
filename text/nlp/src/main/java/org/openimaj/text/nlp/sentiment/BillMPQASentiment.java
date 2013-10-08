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
package org.openimaj.text.nlp.sentiment;

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
public class BillMPQASentiment extends SentimentExtractor {
    private String mpqaN = "/org/openimaj/text/nlp/sentiment/mpqan.txt";
    private String mpqaP = "/org/openimaj/text/nlp/sentiment/mpqap.txt";

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