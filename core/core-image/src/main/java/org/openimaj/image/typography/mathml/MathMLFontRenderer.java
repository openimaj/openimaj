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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.jeuclid.converter.Converter;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.renderer.FImageRenderer;
import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.image.typography.FontRenderer;
import org.openimaj.image.typography.FontStyle.HorizontalAlignment;
import org.openimaj.image.typography.FontStyle.VerticalAlignment;
import org.openimaj.math.geometry.shape.Rectangle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;

/**
 * @param <T> 
 */
public class MathMLFontRenderer<T> extends FontRenderer<T, MathMLFontStyle<T>>
{

	@Override
	public void renderText(ImageRenderer<T, ?> renderer, String text, int x,int y, MathMLFontStyle<T> sty) {
		MBFImage img = renderToMBFImage(text, sty);
		
		// if we have a non-standard horizontal alignment
		if ((sty.getHorizontalAlignment() != HorizontalAlignment.HORIZONTAL_LEFT)) {
			// find the length of the string in pixels ...
			float len = img.getWidth();
			// if we are center aligned
			if (sty.getHorizontalAlignment() == HorizontalAlignment.HORIZONTAL_CENTER) {
				x -= len/2;
			} else {
				x -= len;
			}
			
		}
		
		if(sty.getVerticalAlignment() != VerticalAlignment.VERTICAL_TOP){
			switch (sty.getVerticalAlignment()) {
			case VERTICAL_BOTTOM:
				y -= img.getHeight();
				break;
			case VERTICAL_HALF:
				y -= img.getHeight()/2f;
				break;
			default:
				break;
			}
		}
		
		if(renderer instanceof MBFImageRenderer){
			MBFImageRenderer render = (MBFImageRenderer) renderer;
			render.drawImage(img, x, y);			
		}
		else if (renderer instanceof FImageRenderer){
			FImageRenderer render = (FImageRenderer) renderer;
			render.drawImage(img.flatten(), x, y);
		}
	}

	private MBFImage renderToMBFImage(String text, MathMLFontStyle<T> style) {
		Converter c = Converter.getInstance();
		Node node = null;
		switch (style.getMathInput()){
		case LATEX:
			if(style.isTextMode()){
				text = String.format("$\\mathrm{%s}$",text);
			} else {
				text = String.format("$%s$",text);
			}
			node = latexToNode(text);
			break;
		case MATHML:
			node = mathmlToNode(text);
			break;
		}
		
		BufferedImage rend = null;
		try {
			rend = c.render(node, style.getLayoutContext());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		MBFImage img = ImageUtilities.createMBFImage(rend, true);
		return img;
	}

	@Override
	public Rectangle getSize(String string, MathMLFontStyle<T> style) {
		MBFImage mbf = renderToMBFImage(string,style);
		return mbf.getBounds();
	}
	
	private Node latexToNode(String latex){
		SnuggleEngine engine = new SnuggleEngine();
		try {
			SnuggleSession createSession = engine.createSession();
			createSession.parseInput(new SnuggleInput(latex));
			NodeList dst = createSession.buildDOMSubtree();
			return dst.item(0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	private Element mathmlToNode(String string) {
		try {
			return DocumentBuilderFactory
			.newInstance()
			.newDocumentBuilder()
			.parse(new ByteArrayInputStream(string.getBytes()))
			.getDocumentElement();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MathMLFontRenderer<Float[]> rend = new MathMLFontRenderer<Float[]>();
		
		String mathML = "x = 2\\mathrm{wang}wang" ;
		
		MBFImage img = new MBFImage(300, 300, ColourSpace.RGB);
		img.fill(RGBColour.WHITE);
		MBFImageRenderer renderer = img.createRenderer();
		MathMLFontStyle<Float[]> style = new MathMLFontStyle<Float[]>(new MathMLFont(), RGBColour.WHITE);
		style.setColour(RGBColour.RED);
		style.setFontSize(30);
		rend.renderText(renderer, mathML, 0, 100, style);
		DisplayUtilities.display(img);
		
		MathMLFontRenderer<Float> rendf = new MathMLFontRenderer<Float>();
		
		FImage imgf = new FImage(300, 300);
		imgf.fill(0f);
		FImageRenderer rendererf = imgf.createRenderer();
		MathMLFontStyle<Float> stylef = new MathMLFontStyle<Float>(new MathMLFont(), 0.5f);
		stylef.setFontSize(30);
		rendf.renderText(rendererf, mathML, 0, 100, stylef);
		DisplayUtilities.display(imgf);
	}
	
}
