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
package org.openimaj.text.nlp.textpipe.annotations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.text.nlp.textpipe.annotations.POSAnnotation.PartOfSpeech;

/**
 * Currently a collection of static functions required by various parts of the TextPipe.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 *
 */
public class AnnotationUtils {
	
	/**
	 * Get the string tokens from a list of {@link TokenAnnotation}s
	 * @param tokens
	 * @return List of string tokens
	 */
	public static List<String> getStringTokensFromTokenAnnotationList(List<TokenAnnotation> tokens){
		ArrayList<String> result = new ArrayList<String>();
		for (Iterator<TokenAnnotation> iterator = tokens.iterator(); iterator.hasNext();) { 
			TokenAnnotation tokenAnnotation = (TokenAnnotation) iterator.next();
			result.add(tokenAnnotation.stringToken);
		}
		return result;
	}
	
	/**
	 * Returns a list of String represented Parts of Speech given a list of {@link TokenAnnotation} (These should have {@link POSAnnotation})
	 * @param tokens
	 * @return list of POS strings
	 */
	public static List<String> getStringPOSsFromTokenAnnotationList(List<TokenAnnotation> tokens){
		ArrayList<String> result = new ArrayList<String>();
		for (Iterator<TokenAnnotation> iterator = tokens.iterator(); iterator.hasNext();) {
			TokenAnnotation tokenAnnotation = (TokenAnnotation) iterator.next();
			POSAnnotation pos = tokenAnnotation.getAnnotationsFor(POSAnnotation.class).get(0);
			if(pos.equals(PartOfSpeech.UK)){
				result.add(tokenAnnotation.stringToken);
			}
			else result.add(tokenAnnotation.getAnnotationsFor(POSAnnotation.class).get(0).toString());
		}
		return result;
	}
	
	/**
	 * Converst a list of strings to an array of strings
	 * @param convert
	 * @return array of strings
	 */
	public  static String[] ListToArray(List<String> convert){
		String[] result = new String[convert.size()];
		for (int i = 0; i < convert.size(); i++) {
			result[i]=convert.get(i);
		}
		return result;
	}
	
	/**
	 * Check if a {@link TextPipeAnnotation} has been added to all members in a list of {@link TextPipeAnnotation}.
	 * @param toBeChecked
	 * @param check
	 * @return false if not all members have check annotation.
	 */
	public static boolean allHaveAnnotation(List<? extends TextPipeAnnotation> toBeChecked,Class<? extends TextPipeAnnotation> check){		
		for(TextPipeAnnotation anno: toBeChecked){
			if(!anno.getAnnotationKeyList().contains(check))return false;
		}
		return true;	
	}
}
