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
package org.openimaj.vis.ternary;

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

	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, IOException {
		final String[] fileroots = new String[] {
				"/Users/ss/Experiments/sed2013/long_regen_weights_taken_posted_geo.div",
				// "/Users/ss/Experiments/sed2013/long_regen_weights_taken_posted_tag.div",
				"/Users/ss/Experiments/sed2013/long_regen_weights_desc_tags_title.div",
				"/Users/ss/Experiments/sed2013/long_regen_weights_taken_desc_tag.div",
				// "/Users/ss/Experiments/sed2013/long_regen_weights_geo_desc_tag.div"
		};
		final boolean[] correctRange = new boolean[] {
				true,
				// false,
				true,
				true,
				// true
		};
		for (int i = 0; i < correctRange.length; i++) {
			final String fileroot = fileroots[i];

			ternaryPlotJSONF(new File(fileroot + ".json"), new File(fileroot.replace(".", "_") + ".png"), correctRange[i]);
		}

		ternaryPlotSearchGrid(new File("/Users/ss/Experiments/sed2013/searchgrid.png"));

	}

	private static void ternaryPlotSearchGrid(File file) throws IOException {
		final List<IndependentPair<TernaryData, String>> labels = new ArrayList<IndependentPair<TernaryData, String>>();
		final TernaryParams params = new TernaryParams();
		final List<TernaryData> data = new ArrayList<TernaryData>();
		data.add(new TernaryData(0.3f, 0.3f, 0.3f, 1f));
		final TernaryPlot plot = preparePlot(params, data);
		params.put(TernaryParams.DRAW_SCALE, false);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				for (int k = 0; k < 3; k++) {
					final float sum = i + j + k;
					if (sum == 0)
						continue;
					final float ip = i / sum;
					final float jp = j / sum;
					final float kp = k / sum;
					final TernaryData point = new TernaryData(ip, jp, kp, 10);
					final String label = String.format("(%2.1f,%2.1f,%2.1f)", ip, jp, kp);
					final IndependentPair<TernaryData, String> pair = IndependentPair.pair(point, label);
					labels.add(pair);
				}
			}
		}

		// labels.add(IndependentPair.pair(new TernaryData(1f,0,0,0),"f_1"));
		// labels.add(IndependentPair.pair(new TernaryData(0,1,0,0),"f_2"));
		// labels.add(IndependentPair.pair(new TernaryData(0,0,1,0),"f_3"));
		params.put(TernaryParams.LABELS, labels);
		final Map<Attribute, Object> fontAttrs = params.getTyped(TernaryParams.LABEL_FONT);
		fontAttrs.put(MathMLFontStyle.TEXT_MODE, false);
		fontAttrs.put(MathMLFontStyle.FONT_SIZE, 12);
		final MBFImage draw = plot.draw(params);
		ImageUtilities.write(draw, file);
		DisplayUtilities.display(draw);
	}

	private static void ternaryPlotJSONF(File json, File outfile, boolean correctRange)
			throws FileNotFoundException, IOException
	{
		final Gson gson = new GsonBuilder().create();
		@SuppressWarnings("unchecked")
		final Map<String, Object> obj = gson.fromJson(new FileReader(json), Map.class);
		final TernaryParams params = new TernaryParams();
		final List<TernaryData> data = prepareData(params, obj, correctRange);
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
		params.put(TernaryParams.TRIANGLE_BORDER_COLOUR, RGBColour.WHITE);
		Map<Attribute, Object> fontAttrs = params.getTyped(TernaryParams.LABEL_FONT);
		fontAttrs.put(FontStyle.COLOUR, RGBColour.WHITE);
		fontAttrs.put(FontStyle.FONT_SIZE, 20);
		fontAttrs.put(FontStyle.FONT, new MathMLFont());
		fontAttrs.put(MathMLFontStyle.TEXT_MODE, true);

		fontAttrs = params.getTyped(TernaryParams.SCALE_FONT);
		fontAttrs.put(FontStyle.COLOUR, RGBColour.WHITE);
		fontAttrs.put(FontStyle.FONT_SIZE, 16);
		fontAttrs.put(FontStyle.FONT, new MathMLFont());

		// params.put(TernaryParams.BG_COLOUR, RGBColour.WHITE);
		params.put(TernaryParams.BG_COLOUR, RGBColour.BLACK);

		params.put(TernaryParams.DRAW_SCALE, true);
		// params.put(TernaryParams.LABEL_BACKGROUND, RGBColour.WHITE);
		// params.put(TernaryParams.LABEL_BORDER, RGBColour.BLACK);
		params.put(TernaryParams.LABEL_BACKGROUND, RGBColour.BLACK);
		params.put(TernaryParams.LABEL_BORDER, RGBColour.WHITE);
		params.put(TernaryParams.LABEL_PADDING, 3);
		return plot;
	}

	@SuppressWarnings("unchecked")
	private static List<TernaryData> prepareData(TernaryParams params, Map<String, Object> obj, boolean correctRange) {
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
			final double avgf1 = ((Double) expVars.get("averageF1"));
			f1min = Math.min(avgf1, f1min);
			f1max = Math.max(avgf1, f1max);
		}
		if (correctRange)
			f1min = f1min + (((f1max - f1min) / 8) * 7);
		final List<String> weightedFeaturesL = new ArrayList<String>(weightedFeatures);
		final List<TernaryData> data = new ArrayList<TernaryData>();
		final double f1rng = f1max - f1min;
		final List<IndependentPair<TernaryData, String>> labels = new ArrayList<IndependentPair<TernaryData, String>>();
		labels.add(IndependentPair.pair(new TernaryData(1f, 0, 0, 0), correctLabel(weightedFeaturesL.get(0))));
		labels.add(IndependentPair.pair(new TernaryData(0, 1, 0, 0), correctLabel(weightedFeaturesL.get(1))));
		labels.add(IndependentPair.pair(new TernaryData(0, 0, 1, 0), correctLabel(weightedFeaturesL.get(2))));
		float maxa = 0, maxb = 0, maxc = 0;
		// double maxv = 0;
		for (final String exp : exps) {
			final Map<String, Object> expmap = (Map<String, Object>) obj.get(exp);
			final Map<String, Double> weights = (Map<String, Double>) ((Map<String, Object>) obj.get(exp)).get("weights");
			final double a = weights.get(weightedFeaturesL.get(0));
			final double b = weights.get(weightedFeaturesL.get(1));
			final double c = weights.get(weightedFeaturesL.get(2));
			final double af1 = (Double) expmap.get("averageF1");
			float value = (float) ((af1 - f1min) / f1rng);
			value = Math.max(value, 0);
			if (value >= af1) {
				// maxv = af1;
				maxa = (float) a;
				maxb = (float) b;
				maxc = (float) c;
			}
			data.add(new TernaryData((float) a, (float) b, (float) c, value));
		}
		labels.add(IndependentPair.pair(new TernaryData(maxa, maxb, maxc, 5), String.format("max_{F_1}(%2.2f)", f1max)));
		params.put(TernaryParams.LABELS, labels);
		params.put(TernaryParams.SCALE_MIN, String.format("\\leq div_{F_1}(%2.2f)", f1min));
		params.put(TernaryParams.SCALE_MAX, String.format("div_{F_1}(%2.2f)", f1max));
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
