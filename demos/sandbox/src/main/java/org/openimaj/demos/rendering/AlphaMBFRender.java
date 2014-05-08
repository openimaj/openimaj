package org.openimaj.demos.rendering;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.typography.FontStyle;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.image.typography.hershey.HersheyFontStyle;
import org.openimaj.math.util.RunningStat;
import org.openimaj.time.Timer;

public class AlphaMBFRender {
	public static void main(String[] args) {
		int wh = 800;
		MBFImage img = new MBFImage(wh,wh,ColourSpace.RGB);
		MBFImage imgAlpha = new MBFImage(wh,wh,ColourSpace.RGBA);
		Timer t = Timer.timer();
		RunningStat stat = new RunningStat();  
		while(true){
			img.drawImage(imgAlpha, 0, 0);
			stat.push(t.duration()/1000f);
			FontStyle<Float[]> f = HersheyFont.TIMES_BOLD.createStyle(img.createRenderer());
			img.drawText(String.format("FPS: %2.2f", 1f/(stat.mean())), 30,30, f );
			DisplayUtilities.displayName(img, "wang");
			t.start();
			img.fill(RGBColour.BLACK);
		}
	}
}
