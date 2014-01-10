package org.openimaj.image.typography.general.latex;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.image.typography.Font;
import org.openimaj.image.typography.FontRenderer;
import org.openimaj.image.typography.FontStyle;
import org.openimaj.image.typography.FontStyle.HorizontalAlignment;
import org.openimaj.image.typography.FontStyle.VerticalAlignment;
import org.openimaj.image.typography.general.GeneralFontStyle;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import Jama.Matrix;

public class LatexFontRenderer<T> extends FontRenderer<T,LatexFontStyle<T>> {

	@Override
	public void renderText(ImageRenderer<T, ?> renderer, String math, int x, int y, LatexFontStyle<T> sty) {
		MBFImage createMBFImage = renderToMBFImage(math, sty);
		MBFImageRenderer r = (MBFImageRenderer) renderer;
		
		// if we have a non-standard horizontal alignment
		if ((sty.getHorizontalAlignment() != HorizontalAlignment.HORIZONTAL_LEFT)) {
			// find the length of the string in pixels ...
			float len = createMBFImage.getWidth();
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
				y -= createMBFImage.getHeight();
				break;
			case VERTICAL_HALF:
				y -= createMBFImage.getHeight()/2f;
				break;
			default:
				break;
			}
		}
		r.drawImage(createMBFImage, x, y);
	}

	private MBFImage renderToMBFImage(String math, LatexFontStyle<T> style) {
		if(style.textMode){
			math = String.format("\\text{%s}",math);
		}
		TeXFormula fomule = new TeXFormula(math);
		TeXIcon ti = fomule.createTeXIcon(TeXConstants.STYLE_DISPLAY, style.getFontSize());
		Float[] col = (Float[]) style.getColour();
		ti.setForeground(new Color(col[0], col[1], col[2]));
		BufferedImage b = new BufferedImage(ti.getIconWidth(), ti.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		ti.paintIcon(new JLabel(), b.getGraphics(), 0, 0);
		MBFImage createMBFImage = ImageUtilities.createMBFImage(b, true);
		return createMBFImage;
	}

	@Override
	public Rectangle getBounds(String string, LatexFontStyle<T> style) {
		MBFImage img = renderToMBFImage(string,style);
		return img.getBounds();
	}
	public static void main(String[] args) {
		LatexFontRenderer<Float[]> fr = new LatexFontRenderer<>();
		MBFImage img = new MBFImage(300,300,3);
		MBFImageRenderer createRenderer = img.createRenderer();
		LatexFontStyle<Float[]> f = new LatexFont().createStyle(createRenderer);
		f.setFontSize(20);
		f.setTextMode(true);
		f.setColour(RGBColour.WHITE);
		img.drawText("Normal Text", 0, 0, f );
		
		DisplayUtilities.display(img);
	}
}
