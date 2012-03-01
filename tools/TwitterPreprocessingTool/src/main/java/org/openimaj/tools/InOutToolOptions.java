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
package org.openimaj.tools;

import org.kohsuke.args4j.Option;

/**
 * A file tool reads and writes files and knows whether existing outputs should be deleted
 * 
 * @author ss
 *
 */
public abstract class InOutToolOptions {
	
	@Option(name="--input", aliases="-i", required=true, usage="Input tweets", metaVar="STRING")
	String input = null;
	
	@Option(name="--output", aliases="-o", required=false, usage="Tweet output location", metaVar="STRING")
	String output = null;
	
	@Option(name="--remove-existing-output", aliases="-rm", required=false, usage="If existing output exists, remove it")
	boolean force = false;
	
	/**
	 * @return the input string option
	 */
	public String getInput(){
		return this.input;
	}
	/**
	 * @return the input string option
	 */
	public String getOutput(){
		return this.output;
	}
	
	/**
	 * @return the force option, whether the output should be overwritten if it exists
	 */
	public boolean overwriteOutput(){
		return this.force;
	}
}
