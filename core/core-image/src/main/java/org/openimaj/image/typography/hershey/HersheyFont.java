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
package org.openimaj.image.typography.hershey;

import java.io.IOException;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.renderer.FImageRenderer;
import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.image.typography.Font;
import org.openimaj.image.typography.FontRenderer;
import org.openimaj.image.typography.FontStyle;
import org.openimaj.image.typography.FontStyle.VerticalAlignment;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * The set of Hershey's vector fonts.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public enum HersheyFont implements Font<HersheyFont> {
	/**
	 * Hershey Astrology
	 */
	ASTROLOGY("astrology.jhf", "Astrology"),
	/**
	 * Hershey Cursive
	 */
	CURSIVE("cursive.jhf", "Cursive"),
	/**
	 * Hershey Cyrillic 1
	 */
	CYRILLIC_1("cyrilc_1.jhf", "Cyrillic 1"),
	/**
	 * Hershey Cyrillic
	 */
	CYRILLIC("cyrillic.jhf", "Cyrillic"),
	/**
	 * Hershey Futura Light
	 */
	FUTURA_LIGHT("futural.jhf", "Futura Light"),
	/**
	 * Hershey Futura Medium
	 */
	FUTURA_MEDIUM("futuram.jhf", "Futura Medium"),
	/**
	 * Hershey Gothic English Triplex
	 */
	GOTHIC_ENGLISH_TRIPLEX("gothgbt.jhf", "Gothic English Triplex"),
	/**
	 * Hershey Gothic German Triplex
	 */
	GOTHIC_GERMAN_TRIPLEX("gothgrt.jhf", "Gothic German Triplex"),
	/**
	 * Hershey Gothic English
	 */
	GOTHIC_ENGLISH("gothiceng.jhf", "Gothic English"),
	/**
	 * Hershey Gothic German
	 */
	GOTHIC_GERMAN("gothicger.jhf", "Gothic German"),
	/**
	 * Hershey Gothic Italian
	 */
	GOTHIC_ITALIAN("gothicita.jhf", "Gothic Italian"),
	/**
	 * Hershey Gothic Italian Triplex
	 */
	GOTHIC_ITALIAN_TRIPLEX("gothitt.jhf", "Gothic Italian Triplex"),
	/**
	 * Hershey Greek
	 */
	GREEK("greek.jhf", "Greek"),
	/**
	 * Hershey Greek Complex
	 */
	GREEK_COMPLEX("greekc.jhf", "Greek Complex"),
	/**
	 * Hershey Greek Simplex
	 */
	GREEK_SIMPLEX("greeks.jhf", "Greeks Simplex"),
	/**
	 * Hershey Japanese
	 */
	JAPANESE("japanese.jhf", "Japanese"),
	/**
	 * Hershey Markers
	 */
	MARKERS("markers.jhf", "Markers"),
	/**
	 * Hershey Math Lower
	 */
	MATH_LOWER("mathlow.jhf", "Math Lower"),
	/**
	 * Hershey Math Upper
	 */
	MATH_UPPER("mathupp.jhf", "Math Upper"),
	/**
	 * Hershey Meteorology
	 */
	METEOROLOGY("meteorology.jhf", "Meteorology"),
	/**
	 * Hershey Music
	 */
	MUSIC("music.jhf", "Music"),
	/**
	 * Hershey Roman Duplex
	 */
	ROMAN_DUPLEX("rowmand.jhf", "Roman Duplex"),
	/**
	 * Hershey Roman Simplex
	 */
	ROMAN_SIMPLEX("rowmans.jhf", "Roman Simplex"),
	/**
	 * Hershey Roman Triplex
	 */
	ROMAN_TRIPLEX("rowmant.jhf", "Roman Triplex"),
	/**
	 * Hershey Script Complex
	 */
	SCRIPT_COMPLEX("scriptc.jhf", "Script Complex"),
	/**
	 * Hershey Script Simplex
	 */
	SCRIPT_SIMPLEX("scripts.jhf", "Script Simplex"),
	/**
	 * Hershey Symbolic
	 */
	SYMBOLIC("symbolic.jhf", "Symbolic"),
	/**
	 * Hershey Greek
	 */
	TIMES_GREEK("timesg.jhf", "Times Greek"),
	/**
	 * Hershey Times Medium Italic
	 */
	TIMES_MEDIUM_ITALIC("timesi.jhf", "Times Medium Italic"),
	/**
	 * Hershey Times Bold Italic
	 */
	TIMES_BOLD_ITALIC("timesib.jhf", "Times Bold Italic"),
	/**
	 * Hershey Times Medium
	 */
	TIMES_MEDIUM("timesr.jhf", "Times Medium"),
	/**
	 * Hershey Times Bold
	 */
	TIMES_BOLD("timesrb.jhf", "Times Bold"),
	;

	protected HersheyFontData data;
	protected String name;

	private HersheyFont(final String fntName, final String name) {
		try {
			this.data = new HersheyFontData(fntName);
			this.name = name;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, Q extends FontStyle<T>> FontRenderer<T, Q> getRenderer(final ImageRenderer<T,?> renderer) {
		return (FontRenderer<T, Q>) ((Object) HersheyFontRenderer.INSTANCE);
	}

	@Override
	public <T> HersheyFontStyle<T> createStyle(final ImageRenderer<T, ?> renderer) {
		return new HersheyFontStyle<T>(this, renderer);
	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Demonstrate the font engine
	 * @param args not used
	 */
	public static void main(final String[] args) {
		final FImage image = new FImage(500, HersheyFont.values().length * 30 + 30);
		final FImageRenderer imRenderer = image.createRenderer();

		float i = 1.5f;
		for (final HersheyFont f : HersheyFont.values()) {
			final FontRenderer<Float,HersheyFontStyle<Float>> renderer = f.getRenderer(imRenderer);
			renderer.renderText(imRenderer, f.getName(), 30, (int)(i*30), f.createStyle(imRenderer));
			final Rectangle bounds = renderer.getSize( f.getName(), f.createStyle(imRenderer) );
			System.out.println( bounds );
			image.drawShape( bounds, 1f );
			i++;
		}
		DisplayUtilities.display(image);

		final MBFImage mbfimage = new MBFImage(500,500, ColourSpace.RGB);
		final Map<Attribute, Object> redText = new HashMap<Attribute, Object>();
		redText.put(FontStyle.FONT, HersheyFont.TIMES_BOLD);
		redText.put(FontStyle.COLOUR, RGBColour.RED);

		final Map<Attribute, Object> cursiveText = new HashMap<Attribute, Object>();
		cursiveText.put(FontStyle.FONT, HersheyFont.CURSIVE);
		cursiveText.put(FontStyle.COLOUR, RGBColour.YELLOW);
		cursiveText.put(HersheyFontStyle.HEIGHT_SCALE, 3f);
		cursiveText.put(FontStyle.VERTICAL_ALIGNMENT, VerticalAlignment.VERTICAL_HALF);
		cursiveText.put(HersheyFontStyle.STROKE_WIDTH, 2);

		final AttributedString str = new AttributedString("hello world!");
		str.addAttributes(redText, 4, 8);
		str.addAttributes(cursiveText, 8, 12);

		mbfimage.createRenderer().drawText(str, 150, 150);

		DisplayUtilities.display(mbfimage);


		final FImage image2 = new FImage(500,500);
		final FImageRenderer imRenderer2 = image2.createRenderer();
		for (i=1; i<40; i+=2) {
			final FontRenderer<Float,HersheyFontStyle<Float>> renderer = ROMAN_TRIPLEX.getRenderer(imRenderer2);
			final HersheyFontStyle<Float> sty = ROMAN_TRIPLEX.createStyle(imRenderer2);
			sty.setFontSize((int)i);
			renderer.renderText(imRenderer2, ROMAN_TRIPLEX.getName(), 30, (int)(i*30), sty);
		}
		DisplayUtilities.display(image2);
	}
}
