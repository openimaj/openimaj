/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openimaj.text.nlp.sentiment;

import java.util.List;
import java.util.Map;

/**
 *
 * @author bill
 */
public abstract class SentimentExtractor {

    /**
     * @param strings
     * @return extract sentiments
     */
    public abstract Map<String, Object> extract(List<String> strings);
}
