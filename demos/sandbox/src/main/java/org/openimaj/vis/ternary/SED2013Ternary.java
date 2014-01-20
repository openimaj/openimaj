package org.openimaj.vis.ternary;

import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourMap;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.typography.FontStyle;
import org.openimaj.image.typography.general.GeneralFont;
import org.openimaj.image.typography.mathml.MathMLFont;
import org.openimaj.image.typography.mathml.MathMLFontStyle;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.vis.ternary.TernaryPlot.TernaryData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class SED2013Ternary {
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, IOException {
		String[] fileroots = new String[]{
				"/Users/ss/Experiments/sed2013/long_regen_weights_taken_posted_geo.div",
//				"/Users/ss/Experiments/sed2013/long_regen_weights_taken_posted_tag.div",
				"/Users/ss/Experiments/sed2013/long_regen_weights_desc_tags_title.div",
				"/Users/ss/Experiments/sed2013/long_regen_weights_taken_desc_tag.div",
//				"/Users/ss/Experiments/sed2013/long_regen_weights_geo_desc_tag.div"
		};
		boolean[] correctRange = new boolean[]{
				true,
//				false,
				true,
				true,
//				true
		};
		for (int i = 0; i < correctRange.length; i++) {
			String fileroot = fileroots[i];
			
			ternaryPlotJSONF(new File(fileroot + ".json"), new File(fileroot.replace(".", "_") + ".png"), correctRange[i]);
		}
		
		
	}

	private static void ternaryPlotJSONF(File json, File outfile, boolean correctRange)
			throws FileNotFoundException, IOException {
		Gson gson = new GsonBuilder().create();		
		Map<String,Object> obj = gson.fromJson(new FileReader(json), Map.class);
		TernaryParams params = new TernaryParams();
		List<TernaryData> data = prepareData(params,obj,correctRange);
		TernaryPlot plot = preparePlot(params, data);
		MBFImage draw = plot.draw(params);
		ImageUtilities.write(draw, outfile);
		DisplayUtilities.display(draw);
	}

	private static TernaryPlot preparePlot(TernaryParams params, List<TernaryData> data) {
		TernaryPlot plot = new TernaryPlot(500, data);
		
		params.put(TernaryParams.COLOUR_MAP, ColourMap.Greys9);
		params.put(TernaryParams.PADDING, 60);
		
		params.put(TernaryParams.TRIANGLE_BORDER, true);
		params.put(TernaryParams.TRIANGLE_BORDER_THICKNESS, 3);
		params.put(TernaryParams.TRIANGLE_BORDER_TICKS, true);
		Map<Attribute, Object> fontAttrs = params.getTyped(TernaryParams.LABEL_FONT);
		fontAttrs.put(FontStyle.COLOUR, RGBColour.BLACK);
		fontAttrs.put(FontStyle.FONT_SIZE, 20);
		fontAttrs.put(FontStyle.FONT, new MathMLFont());
		fontAttrs.put(MathMLFontStyle.TEXT_MODE, true);
		
		fontAttrs = params.getTyped(TernaryParams.SCALE_FONT);
		fontAttrs.put(FontStyle.COLOUR, RGBColour.BLACK);
		fontAttrs.put(FontStyle.FONT_SIZE, 16);
		fontAttrs.put(FontStyle.FONT, new MathMLFont());
		
		params.put(TernaryParams.BG_COLOUR, RGBColour.WHITE);
		
		params.put(TernaryParams.DRAW_SCALE, true);
		return plot;
	}

	private static List<TernaryData> prepareData(TernaryParams params,Map<String, Object> obj, boolean correctRange) {
		List<String> exps = new ArrayList<String>(obj.keySet());
		Set<String> weightedFeatures = new HashSet<String>();
		
		
		double f1min = 1;double f1max = 0;
		for (String exp : exps) {
			Map<String, Object> expVars = (Map<String, Object>) obj.get(exp);
			Map<String,Double> expFeatureWeights = (Map<String, Double>) expVars.get("weights");
			for (Entry<String, Double> fw : expFeatureWeights.entrySet()) {
				if(fw.getValue()!=0){
					weightedFeatures.add(fw.getKey());
				}
			}
			double avgf1 = ((Double) expVars.get("averageF1"));
			f1min = Math.min(avgf1, f1min);
			f1max = Math.max(avgf1, f1max);
		}
		if(correctRange)
			f1min = f1min + (((f1max - f1min)/8) * 7);
		List<String> weightedFeaturesL = new ArrayList<String>(weightedFeatures);
		List<TernaryData> data = new ArrayList<TernaryData>();
		double f1rng = f1max - f1min;
		List<IndependentPair<TernaryData, String>> labels = new ArrayList<IndependentPair<TernaryData,String>>();
		labels.add(IndependentPair.pair(new TernaryData(1f,0,0,0),correctLabel(weightedFeaturesL.get(0))));
		labels.add(IndependentPair.pair(new TernaryData(0,1,0,0),correctLabel(weightedFeaturesL.get(1))));
		labels.add(IndependentPair.pair(new TernaryData(0,0,1,0),correctLabel(weightedFeaturesL.get(2))));
		float maxa = 0, maxb = 0, maxc = 0;
		double maxv = 0;
		for (String exp : exps) {
			Map<String, Object> expmap = (Map<String, Object>) obj.get(exp);
			Map<String,Double> weights = (Map<String, Double>) ((Map<String, Object>) obj.get(exp)).get("weights");
			double a = weights.get(weightedFeaturesL .get(0));
			double b = weights.get(weightedFeaturesL .get(1));
			double c = weights.get(weightedFeaturesL .get(2));
			double af1 = (Double) expmap.get("averageF1");
			float value = (float)(( af1 - f1min)/f1rng);
			value = Math.max(value, 0);
			if(value >= af1){
				maxv = af1;
				maxa = (float) a;
				maxb = (float) b;
				maxc = (float) c;
			}
			data.add(new TernaryData((float)a,(float)b,(float)c,value));
		}
		labels.add(IndependentPair.pair(new TernaryData(maxa,maxb,maxc,5),String.format("max_{F_1}(%2.2f)",f1max)));
		params.put(TernaryParams.LABELS,labels);
		params.put(TernaryParams.SCALE_MIN,String.format("\\leq div_{F_1}(%2.2f)",f1min));
		params.put(TernaryParams.SCALE_MAX,String.format("div_{F_1}(%2.2f)",f1max));
		return data;
	}

	private static String correctLabel(String string) {
		if(string.contains("Geo")) return "Geo";
		if(string.contains("TAKEN")) return "TimeTaken";
		if(string.contains("POSTED")) return "TimePosted";
		if(string.contains("Title")) return "Title";
		if(string.contains("Tag")) return "Tags";
		if(string.contains("Descrip")) return "Desc";
		return string;
	}

}
