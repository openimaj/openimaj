package org.openimaj.vis.ternary;

import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourMap;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.typography.FontStyle;
import org.openimaj.image.typography.general.GeneralFont;
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
		final String[] fileroots = new String[] {
				"/Users/ss/Experiments/sed2013/trainweights_title_tag_desc",
				"/Users/ss/Experiments/sed2013/trainweights_taken_desc_tag",
				"/Users/ss/Experiments/sed2013/trainweights_taken_posted_geo"
		};
		for (final String fileroot : fileroots) {
			ternaryPlotJSONF(new File(fileroot + ".json"), new File(fileroot + ".png"));
		}

	}

	private static void ternaryPlotJSONF(File json, File outfile)
			throws FileNotFoundException, IOException
	{
		final Gson gson = new GsonBuilder().create();
		final Map<String, Object> obj = gson.fromJson(new FileReader(json), Map.class);
		final TernaryParams params = new TernaryParams();
		final List<TernaryData> data = prepareData(params, obj);
		final TernaryPlot plot = preparePlot(params, data);
		final MBFImage draw = plot.draw(params);
		ImageUtilities.write(draw, outfile);
		DisplayUtilities.display(draw);
	}

	private static TernaryPlot preparePlot(TernaryParams params, List<TernaryData> data) {
		final TernaryPlot plot = new TernaryPlot(500, data);

		params.put(TernaryParams.COLOUR_MAP, ColourMap.Greys9);
		params.put(TernaryParams.PADDING, 60);
		params.put(TernaryParams.TRIANGLE_BORDER, true);
		params.put(TernaryParams.TRIANGLE_BORDER_THICKNESS, 3);
		params.put(TernaryParams.TRIANGLE_BORDER_TICKS, true);
		final Map<Attribute, Object> fontAttrs = params.getTyped(TernaryParams.LABEL_FONT);
		fontAttrs.put(FontStyle.COLOUR, RGBColour.BLACK);
		fontAttrs.put(FontStyle.FONT_SIZE, 20);
		fontAttrs.put(FontStyle.FONT, new GeneralFont("Arial", Font.PLAIN));
		params.put(TernaryParams.BG_COLOUR, RGBColour.WHITE);

		params.put(TernaryParams.DRAW_SCALE, true);
		return plot;
	}

	private static List<TernaryData> prepareData(TernaryParams params, Map<String, Object> obj) {
		final List<String> exps = new ArrayList<String>(obj.keySet());
		final Set<String> weightedFeatures = new HashSet<String>();

		double f1min = 1;
		double f1max = 0;
		for (final String exp : exps) {
			final Map<String, Object> expVars = (Map<String, Object>) obj.get(exp);
			final Map<String, Double> expFeatureWeights = (Map<String, Double>) expVars.get("weights");
			for (final Entry<String, Double> fw : expFeatureWeights.entrySet()) {
				if (fw.getValue() != 0) {
					weightedFeatures.add(fw.getKey());
				}
			}
			final double avgf1 = (Double) expVars.get("averageF1");
			f1min = Math.min(avgf1, f1min);
			f1max = Math.max(avgf1, f1max);
		}
		final List<String> weightedFeaturesL = new ArrayList<String>(weightedFeatures);
		final List<TernaryData> data = new ArrayList<TernaryData>();
		final double f1rng = f1max - f1min;
		final List<IndependentPair<TernaryData, String>> labels = new ArrayList<IndependentPair<TernaryData, String>>();
		labels.add(IndependentPair.pair(new TernaryData(1f, 0, 0, 0), correctLabel(weightedFeaturesL.get(0))));
		labels.add(IndependentPair.pair(new TernaryData(0, 1, 0, 0), correctLabel(weightedFeaturesL.get(1))));
		labels.add(IndependentPair.pair(new TernaryData(0, 0, 1, 0), correctLabel(weightedFeaturesL.get(2))));
		for (final String exp : exps) {
			final Map<String, Object> expmap = (Map<String, Object>) obj.get(exp);
			final Map<String, Double> weights = (Map<String, Double>) ((Map<String, Object>) obj.get(exp)).get("weights");
			final double a = weights.get(weightedFeaturesL.get(0));
			final double b = weights.get(weightedFeaturesL.get(1));
			final double c = weights.get(weightedFeaturesL.get(2));
			final double af1 = (Double) expmap.get("averageF1");
			data.add(new TernaryData((float) a, (float) b, (float) c, ((float) ((af1 - f1min) / f1rng))));
		}
		params.put(TernaryParams.LABELS, labels);
		params.put(TernaryParams.SCALE_MIN, String.format("f1(%2.2f)", f1min));
		params.put(TernaryParams.SCALE_MAX, String.format("f1(%2.2f)", f1max));
		return data;
	}

	private static String correctLabel(String string) {
		if (string.contains("Geo"))
			return "Geo";
		if (string.contains("TAKEN"))
			return "TimeTaken";
		if (string.contains("POSTED"))
			return "TimePosted";
		if (string.contains("Title"))
			return "Title";
		if (string.contains("Tag"))
			return "Tags";
		if (string.contains("Descrip"))
			return "Desc";
		return string;
	}

}
