package org.openimaj.utils.threads;

/**
 * CharSequence that noticed thread interrupts -- as might be necessary 
 * to recover from a loose regex on unexpected challenging input. 
 * 
 * @author gojomo
 */
public class InterruptibleCharSequence implements CharSequence {
    CharSequence inner;
    // public long counter = 0; 

    public InterruptibleCharSequence(CharSequence inner) {
        super();
        this.inner = inner;
    }

    public char charAt(int index) {
        if (Thread.interrupted()) { // clears flag if set
            throw new RuntimeException(new InterruptedException());
        }
        // counter++;
        return inner.charAt(index);
    }

    public int length() {
        return inner.length();
    }

    public CharSequence subSequence(int start, int end) {
        return new InterruptibleCharSequence(inner.subSequence(start, end));
    }

    @Override
    public String toString() {
        return inner.toString();
    }
}