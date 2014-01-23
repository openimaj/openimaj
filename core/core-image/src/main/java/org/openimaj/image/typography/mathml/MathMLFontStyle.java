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
/**
 * 
 */
package org.openimaj.image.typography.mathml;

import java.awt.Color;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Map;

import net.sourceforge.jeuclid.LayoutContext;
import net.sourceforge.jeuclid.MutableLayoutContext;
import net.sourceforge.jeuclid.context.LayoutContextImpl;
import net.sourceforge.jeuclid.context.Parameter;

import org.openimaj.image.typography.FontStyle;

/**
 * @param <T> 
 *	
 */
public class MathMLFontStyle<T> extends FontStyle<T>
{
	enum MathInput{
		MATHML,LATEX;
	}

	/**
	 * 
	 */
	public static final Attribute TEXT_MODE = null;

	private MathInput mathInput = MathInput.LATEX;

	private boolean textMode;
	protected MathMLFontStyle(MathMLFont font, T col) {
		super(font, col);
	}

	/**
	 * @return the {@link LayoutContext} encoding of this {@link MathMLFontStyle}
	 */
	public LayoutContext getLayoutContext() {
		MutableLayoutContext mlc = new LayoutContextImpl(
                LayoutContextImpl.getDefaultLayoutContext());
        T col = this.colour;
        if(col instanceof Float[]){
        	Float[] mcol = (Float[]) col;
        	mlc.setParameter(Parameter.MATHCOLOR, new Color(mcol[0], mcol[1], mcol[2]));
        }
        else if(col instanceof Float){
        	mlc.setParameter(Parameter.MATHCOLOR, new Color((Float)col,(Float)col,(Float)col));
        }
        mlc.setParameter(Parameter.MATHSIZE, this.getFontSize());
		return mlc;
	}

	/**
	 * @return how to interruprate math text
	 */
	public MathInput getMathInput() {
		return this.mathInput ;
	}
	
	/**
	 * @param input
	 */
	public void setMathInput(MathInput input){
		this.mathInput = input;
	}

	/**
	 * @return whether text
	 */
	public boolean isTextMode() {
		return this.textMode;
	}
	
	@Override
	public void parseAttributes(Map<? extends Attribute, Object> attrs) {
		super.parseAttributes(attrs);
		if (attrs.containsKey(MathMLFontStyle.TEXT_MODE)) this.textMode = ((Boolean) attrs.get(TEXT_MODE)).booleanValue();
		
	}
	
}
