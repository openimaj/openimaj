package org.openimaj.image.typography.hershey;

import java.io.IOException;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.typography.Font;
import org.openimaj.image.typography.FontRenderer;
import org.openimaj.image.typography.FontStyle;

/**
 * The set of Hershey's vector fonts.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public enum HersheyFont implements Font<HersheyFont> {
	ASTROLOGY("astrology.jhf", "Astrology"),
	CURSIVE("cursive.jhf", "Cursive"),
	CYRILLIC_1("cyrilc_1.jhf", "Cyrillic 1"),
	CYRILLIC("cyrillic.jhf", "Cyrillic"),
	FUTUTA_LIGHT("futural.jhf", "Futura Light"),
	FUTURA_MEDIUM("futuram.jhf", "Futura Medium"),
	GOTHIC_ENGLISH_TRIPLEX("gothgbt.jhf", "Gothic English Triplex"),
	GOTHIC_GERMAN_TRIPLEX("gothgrt.jhf", "Gothic German Triplex"),
	GOTHIC_ENGLISH("gothiceng.jhf", "Gothic English"),
	GOTHIC_GERMAN("gothicger.jhf", "Gothic German"),
	GOTHIC_ITALIAN("gothicita.jhf", "Gothic Italian"),
	GOTHIC_ITALIAN_TRIPLEX("gothitt.jhf", "Gothic Italian Triplex"),
	GREEK("greek.jhf", "Greek"),
	GREEK_COMPLEX("greekc.jhf", "Greek Complex"),
	GREEK_SIMPLEX("greeks.jhf", "Greeks Simplex"),
	JAPANESE("japanese.jhf", "Japanese"),
	MARKERS("markers.jhf", "Markers"),
	MATH_LOWER("mathlow.jhf", "Math Lower"),
	MATH_UPPER("mathupp.jhf", "Math Upper"),
	METEOROLOGY("meteorology.jhf", "Meteorology"),
	MUSIC("music.jhf", "Music"),
	ROMAN_DUPLEX("rowmand.jhf", "Roman Duplex"),
	ROMAN_SIMPLEX("rowmans.jhf", "Roman Simplex"),
	ROMAM_TRIPLEX("rowmant.jhf", "Roman Triplex"),
	SCRIPT_COMPLEX("scriptc.jhf", "Script Complex"),
	SCRIPT_SIMPLEX("scripts.jhf", "Script Simplex"),
	SYMBOLIC("symbolic.jhf", "Symbolic"),
	TIMES_GREEK("timesg.jhf", "Times Greek"),
	TIMES_MEDIUM_ITALIC("timesi.jhf", "Times Medium Italic"),
	TIMES_BOLD_ITALIC("timesib.jhf", "Times Bold Italic"),
	TIMES_MEDIUM("timesr.jhf", "Times Medium"),
	TIMES_BOLD("timesrb.jhf", "Times Bold"),
	;

	protected HersheyFontData data;
	protected String name;
	
	private HersheyFont(String fntName, String name) {
		try {
			this.data = new HersheyFontData(fntName);
			this.name = name;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T, Q extends FontStyle<?, T>> FontRenderer<T, Q> getRenderer(Image<T,?> image) {
		return (FontRenderer<T, Q>) HersheyFontRenderer.INSTANCE;
	}

	@Override
	public <T> HersheyFontStyle<T> createStyle(Image<T, ?> image) {
		return new HersheyFontStyle<T>(this, image);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * Demonstrate the font engine
	 * @param args not used
	 */
	public static void main(String[] args) {
		FImage image = new FImage(500, HersheyFont.values().length * 30 + 30);
		float i = 1.5f;
		for (HersheyFont f : HersheyFont.values()) { 
			FontRenderer<Float,HersheyFontStyle<Float>> renderer = f.getRenderer(image);
			renderer.renderText(image, f.getName(), 30, (int)(i*30), f.createStyle(image));
			i++;
		}
		DisplayUtilities.display(image);
		
		MBFImage mbfimage = new MBFImage(500,500, ColourSpace.RGB);
		Map<Attribute, Object> redText = new HashMap<Attribute, Object>();
		redText.put(FontStyle.FONT, HersheyFont.TIMES_BOLD);
		redText.put(FontStyle.COLOUR, RGBColour.RED);
		
		Map<Attribute, Object> blackText = new HashMap<Attribute, Object>();
		blackText.put(FontStyle.FONT, HersheyFont.TIMES_BOLD);
		
		AttributedString str = new AttributedString("hello world!");
//		str.addAttributes(blackText, 0, 4);
		str.addAttributes(redText, 4, 8);
//		str.addAttributes(blackText, 8, 12);
		
		mbfimage.drawText(str, 30, 300);
		
		DisplayUtilities.display(mbfimage);
	}
}
