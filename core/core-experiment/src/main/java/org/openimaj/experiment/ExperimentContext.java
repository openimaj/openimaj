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
package org.openimaj.experiment;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;
import org.openimaj.citation.ReferenceListener;
import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.output.StandardFormatters;
import org.openimaj.experiment.agent.TimeTracker;
import org.openimaj.experiment.annotations.DatasetDescription;
import org.openimaj.experiment.annotations.DependentVariable;
import org.openimaj.experiment.annotations.Experiment;
import org.openimaj.experiment.annotations.IndependentVariable;
import org.openimaj.experiment.evaluation.AnalysisResult;
import org.openimaj.util.array.ArrayUtils;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;

/**
 * The recorded context of an experiment.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ExperimentContext {
	/**
	 * Representation of a recorded variable in an experiment (i.e. a field
	 * annotated with {@link IndependentVariable} or {@link DependentVariable}).
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class Variable {
		/**
		 * The variables identifier taken from the annotation, or the variables
		 * name if the annotation has the default identifier.
		 */
		public String identifier;

		Variable(String identifier) {
			this.identifier = identifier;
		}
	}

	private static final Logger logger = Logger.getLogger(ExperimentContext.class);

	private boolean isLocked;

	private RunnableExperiment experiment;
	private Date dateCompleted;
	private Class<?> exptClass;
	private Experiment experimentDetails;
	private Set<Reference> bibliography;
	private Map<String, SummaryStatistics> timingInfo;
	private Map<Variable, Field> independentVariables = new HashMap<Variable, Field>();
	private Map<Variable, Field> dependentVariables = new HashMap<Variable, Field>();

	protected ExperimentContext(RunnableExperiment experiment) {
		this.experiment = experiment;
		experimentDetails = getExperiment(experiment);
		exptClass = experiment.getClass();

		readVariables(experiment);
	}

	private Experiment getExperiment(RunnableExperiment experiment) {
		Class<?> exptClass = experiment.getClass();

		while (exptClass != null) {
			final Experiment ann = exptClass.getAnnotation(Experiment.class);

			if (ann != null)
				return ann;

			exptClass = exptClass.getSuperclass();
		}

		return null;
	}

	private void readVariables(RunnableExperiment expt) {
		Class<?> exptClass = expt.getClass();

		while (exptClass != null) {

			for (final Field field : exptClass.getDeclaredFields()) {
				final IndependentVariable iv = field.getAnnotation(IndependentVariable.class);
				final DependentVariable dv = field.getAnnotation(DependentVariable.class);

				if (iv != null && dv != null)
					throw new RuntimeException("Invalid experiment! The field " + field
							+ " cannot be both a dependent and independent variable.");

				if (iv != null) {
					String id = iv.identifier();
					if (id == null || id.length() == 0)
						id = field.getName();

					this.independentVariables.put(new Variable(id), field);
				}

				if (dv != null) {
					String id = dv.identifier();
					if (id == null || id.length() == 0)
						id = field.getName();

					this.dependentVariables.put(new Variable(id), field);
				}
			}

			exptClass = exptClass.getSuperclass();
		}
	}

	protected void lock() {
		isLocked = true;
		this.bibliography = ReferenceListener.getReferences();
		this.timingInfo = TimeTracker.getTimes();
		this.dateCompleted = new Date();
	}

	/**
	 * Get the bibliography for the experiment.
	 * 
	 * @return the bibliography
	 */
	public Set<Reference> getBibliography() {
		if (!isLocked)
			this.bibliography = ReferenceListener.getReferences();

		return bibliography;
	}

	/**
	 * Get the timing information for the experiment.
	 * 
	 * @return the timing information
	 */
	public Map<String, SummaryStatistics> getTimingInfo() {
		if (!isLocked)
			this.timingInfo = TimeTracker.getTimes();

		return timingInfo;
	}

	/**
	 * Get the independent variables of the experiment and their values at the
	 * time this method is called.
	 * 
	 * @return the independent variables
	 */
	public Map<Variable, Object> getIndependentVariables() {
		final Map<Variable, Object> vars = new HashMap<Variable, Object>();

		for (final Entry<Variable, Field> e : this.independentVariables.entrySet()) {
			final Field field = e.getValue();
			field.setAccessible(true);
			Object value;
			try {
				value = field.get(this.experiment);
				vars.put(e.getKey(), value);
			} catch (final Exception ex) {
				logger.warn(ex);
				vars.put(e.getKey(), null);
			}
		}

		return vars;
	}

	/**
	 * Get the dependent variables of the experiment and their values at the
	 * time this method is called.
	 * 
	 * @return the dependent variables
	 */
	public Map<Variable, Object> getDependentVariables() {
		final Map<Variable, Object> vars = new HashMap<Variable, Object>();

		for (final Entry<Variable, Field> e : this.dependentVariables.entrySet()) {
			final Field field = e.getValue();
			field.setAccessible(true);
			Object value;
			try {
				value = field.get(this.experiment);
				vars.put(e.getKey(), value);
			} catch (final Exception ex) {
				logger.warn(ex);
				vars.put(e.getKey(), null);
			}
		}

		return vars;
	}

	private String getExptInfoTable() {
		final Date dc = dateCompleted == null ? new Date() : dateCompleted;

		final List<String[]> data = new ArrayList<String[]>();
		data.add(new String[] { "Class", exptClass.getName() });
		data.add(new String[] { "Report compiled", new SimpleDateFormat().format(dc) });

		if (experimentDetails != null) {
			data.add(new String[] { "Author", WordUtils.wrap(experimentDetails.author(), exptClass.getName().length()) });
			data.add(new String[] { "Created on", experimentDetails.dateCreated() });
			data.add(new String[] { "Description", WordUtils.wrap(experimentDetails.description(), exptClass.getName()
					.length()) });
		}

		final ASCIITableHeader[] header = {
				new ASCIITableHeader("", ASCIITable.ALIGN_RIGHT),
				new ASCIITableHeader("", ASCIITable.ALIGN_LEFT)
		};

		String table = ASCIITable.getInstance().getTable(header, data.toArray(new String[data.size()][]));

		final int width = table.indexOf("\n") + 1;
		table = table.substring(2 * width);

		return table;
	}

	private String getTimingTable() {
		final ASCIITableHeader[] header = { new ASCIITableHeader("Experimental Timing", ASCIITable.ALIGN_CENTER) };
		final String[][] data = formatAsTable(TimeTracker.format(timingInfo));
		return ASCIITable.getInstance().getTable(header, data);
	}

	private String getBibliographyTable() {
		final ASCIITableHeader[] header = { new ASCIITableHeader("Bibliography", ASCIITable.ALIGN_LEFT) };
		String refs = StandardFormatters.STRING.format(bibliography);

		refs = WordUtils.wrap(refs, Math.max(exptClass.getName().length() + 10, 72), SystemUtils.LINE_SEPARATOR + "  ",
				true);

		final String[][] data = formatAsTable(refs);
		return ASCIITable.getInstance().getTable(header, data);
	}

	private String[][] formatAsTable(String data) {
		final String[] splits = data.trim().split("\\r?\\n");

		final String[][] out = new String[splits.length][];
		for (int i = 0; i < splits.length; i++) {
			out[i] = new String[] { splits[i] };
		}

		return out;
	}

	private String getIndependentVariablesTable() {
		final ASCIITableHeader[] header = { new ASCIITableHeader("Independent Variables", ASCIITable.ALIGN_CENTER) };
		final String[][] data = formatAsTable(formatVariables(getIndependentVariables()));
		return ASCIITable.getInstance().getTable(header, data);
	}

	private String formatVariables(Map<Variable, Object> vars) {
		final List<String[]> data = new ArrayList<String[]>();

		for (final Entry<Variable, Object> e : vars.entrySet()) {
			final String id = e.getKey().identifier;
			final String[] val = formatValue(e.getValue());

			data.add(new String[] { id, val[0] });
			for (int i = 1; i < val.length; i++)
				data.add(new String[] { "", val[i] });
		}

		final ASCIITableHeader[] header = {
				new ASCIITableHeader("", ASCIITable.ALIGN_RIGHT),
				new ASCIITableHeader("", ASCIITable.ALIGN_LEFT)
		};

		String table = ASCIITable.getInstance().getTable(header, data.toArray(new String[data.size()][]));

		final int width = table.indexOf("\n") + 1;
		table = table.substring(2 * width);

		return table;
	}

	private String getDependentVariablesTable() {
		final ASCIITableHeader[] header = { new ASCIITableHeader("Dependent Variables", ASCIITable.ALIGN_CENTER) };
		final String[][] data = formatAsTable(formatVariables(getDependentVariables()));
		return ASCIITable.getInstance().getTable(header, data);
	}

	private String[] formatValue(Object value) {
		// is it a dataset?
		if (value.getClass().getAnnotation(DatasetDescription.class) != null) {
			final DatasetDescription d = value.getClass().getAnnotation(DatasetDescription.class);

			final List<String[]> data = new ArrayList<String[]>();
			data.add(new String[] { "Name", d.name() });

			final String[] description = WordUtils.wrap(d.description(), exptClass.getName().length() - 20).split(
					"\\r?\\n");
			data.add(new String[] { "Description", description[0] });
			for (int i = 1; i < description.length; i++)
				data.add(new String[] { "", description[i] });

			final ASCIITableHeader[] header = {
					new ASCIITableHeader("", ASCIITable.ALIGN_RIGHT),
					new ASCIITableHeader("", ASCIITable.ALIGN_LEFT)
			};

			String table = ASCIITable.getInstance().getTable(header, data.toArray(new String[data.size()][]));

			final int width = table.indexOf("\n") + 1;
			table = table.substring(2 * width);

			return table.split("\\r?\\n");
		}

		// is it an analysis result?
		if (value instanceof AnalysisResult) {
			// return
			// ((AnalysisResult)value).getDetailReport().split("\\r?\\n");
			return ((AnalysisResult) value).getSummaryReport().split("\\r?\\n");
		}

		if (value.getClass().isArray()) {
			final int length = Array.getLength(value);
			if (length == 0)
				return new String[] { "" };

			String str = Array.get(value, 0).toString();
			for (int i = 1; i < length; i++) {
				str += ", " + Array.get(value, i);
			}
			return WordUtils.wrap(str, exptClass.getName().length() - 20).split("\\r?\\n");
		}

		// otherwise use toString
		return value.toString().split("\\r?\\n");
	}

	@Override
	public String toString() {
		final String[][] exptinfo = formatAsTable(getExptInfoTable());
		final String[][] timeInfo = formatAsTable(getTimingTable());
		final String[][] ivInfo = formatAsTable(getIndependentVariablesTable());
		final String[][] dvInfo = formatAsTable(getDependentVariablesTable());
		final String[][] biblInfo = formatAsTable(getBibliographyTable());

		final String[][] data = ArrayUtils.concatenate(exptinfo, timeInfo, ivInfo, dvInfo, biblInfo);
		final ASCIITableHeader[] header = { new ASCIITableHeader("Experiment Context", ASCIITable.ALIGN_LEFT) };

		return ASCIITable.getInstance().getTable(header, data);
	}
}
