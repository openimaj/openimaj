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
package org.openimaj.math.util;

/**
 * Data structure for a complex number with real and imaginary parts.
 */
public class Complex {
    /**
     * The real part
     */
    public double re=0;
    
    /**
     * The imaginary part 
     */
    public double im=0;

    /**
     * Create a complex number with real and imaginary parts set to 0.
     */
    public Complex(){}

    /**
     * Create a complex number with the specified real and
     * imaginary values.
     * @param re the real part
     * @param im the imaginary part
     */
    public Complex(double re, double im) {
        this.re=re;
        this.im=im;
    }
    
    /** 
     * Set the real part. 
     * @param re the real part 
     */
    public void setRE(double re) {
    	this.re = re;
    }
    
    /** 
     * Set the imaginary part. 
     * @param im the imaginary part;
     */
    public void setIM(double im) {
    	this.im = im;
    }

    /** 
     * Return the real part. 
     * @return the real part 
     */
    public double getRE() {
        return this.re;
    }

    /** 
     * Return the imaginary part. 
     * @return the imaginary part
     */
    public double getIM() {
        return this.im;
    }

    /** 
     * Return the magnitude of this complex number. 
     * @return the magnitude
     */
    public double getMagnitude() {
        double sum = this.re*this.re+this.im*this.im;
        return Math.sqrt(sum);
    }

    /** Text representation of this complex number. */
    @Override
	public String toString() {
        return im<0 ? String.format("%4.3f%4.3fj", re, im) : String.format("%4.3f+%4.3fj", re, im);
    }
}
