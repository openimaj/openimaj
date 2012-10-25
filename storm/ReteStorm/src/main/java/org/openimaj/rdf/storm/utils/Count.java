package org.openimaj.rdf.storm.utils;

import com.hp.hpl.jena.reasoner.rulesys.impl.RETEQueue;

/**
 * @author Jena Team, originally part of {@link RETEQueue}
 *
 */
public class Count {
    /** the count */
    int count;
    
    /** Constructor 
     * @param count */
    public Count(int count) {
        this.count = count;
    }
    
    /** Access count value 
     * @return int */
    public int getCount() {
        return count;
    }
    
    /** Increment the count value 
     * @return count before increment */
    public int inc() {
        return count++;
    }
    
    /** Decrement the count value 
     * @return count before decrement */
    public int dec() {
        return count--;
    }
    
    /** Set the count value 
     * @param count */
    public void setCount(int count) {
        this.count = count;
    }
    
}