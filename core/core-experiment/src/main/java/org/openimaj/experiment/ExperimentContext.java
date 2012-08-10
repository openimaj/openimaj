package org.openimaj.experiment;

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
import org.openimaj.citation.agent.ReferenceListener;
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
	 * Representation of a recorded variable in
	 * an experiment (i.e. a field annotated with
	 * {@link IndependentVariable} or {@link DependentVariable}).
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class Variable {
		/**
		 * The variables identifier taken from the annotation, or the
		 * variables name if the annotation has the default identifier.
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
			Experiment ann = exptClass.getAnnotation(Experiment.class);
			
			if (ann != null)
				return ann;
			
			exptClass = exptClass.getSuperclass();
		}
		
		return null;
	}
	
	private void readVariables(RunnableExperiment expt) {
		Class<?> exptClass = expt.getClass();
		
		while (exptClass != null) {
			
			for (Field field : exptClass.getDeclaredFields()) {
				IndependentVariable iv = field.getAnnotation(IndependentVariable.class);
				DependentVariable dv = field.getAnnotation(DependentVariable.class);
				
				if (iv != null && dv != null)
					throw new RuntimeException("Invalid experiment! The field " + field + " cannot be both a dependent and independent variable.");
				
				if (iv != null) {
					String id =  iv.identifier();
					if (id == null || id.length() == 0) id = field.getName();
					
					this.independentVariables.put(new Variable(id), field);
				}
				
				if (dv != null) {
					String id =  dv.identifier();
					if (id == null || id.length() == 0) id = field.getName();
					
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
	 * @return the bibliography
	 */
	public Set<Reference> getBibliography() {
		if (!isLocked)
			this.bibliography = ReferenceListener.getReferences();
		
		return bibliography;
	}
	
	/**
	 * Get the timing information for the experiment.
	 * @return the timing information
	 */
	public Map<String, SummaryStatistics> getTimingInfo() {
		if (!isLocked)
			this.timingInfo = TimeTracker.getTimes();
		
		return timingInfo;
	}
	
	/**
	 * Get the independent variables of the experiment and their values
	 * at the time this method is called.
	 * 
	 * @return the independent variables
	 */
	public Map<Variable, Object> getIndependentVariables() {
		Map<Variable, Object> vars = new HashMap<Variable, Object>();
		
		for (Entry<Variable, Field> e : this.independentVariables.entrySet()) {
			Field field = e.getValue();
			field.setAccessible(true);
			Object value;
			try {
				value = field.get(this.experiment);
				vars.put(e.getKey(), value);
			} catch (Exception ex) {
				logger.warn(ex);
				vars.put(e.getKey(), null);
			}
		}
		
		return vars;
	}
	
	/**
	 * Get the dependent variables of the experiment and their values
	 * at the time this method is called.
	 * 
	 * @return the dependent variables
	 */
	public Map<Variable, Object> getDependentVariables() {
		Map<Variable, Object> vars = new HashMap<Variable, Object>();
		
		for (Entry<Variable, Field> e : this.dependentVariables.entrySet()) {
			Field field = e.getValue();
			field.setAccessible(true);
			Object value;
			try {
				value = field.get(this.experiment);
				vars.put(e.getKey(), value);
			} catch (Exception ex) {
				logger.warn(ex);
				vars.put(e.getKey(), null);
			}
		}
		
		return vars;
	}
	
	private String getExptInfoTable() {
		Date dc = dateCompleted == null ? new Date() : dateCompleted;
		
		List<String[]> data = new ArrayList<String[]>();
		data.add(new String[] {"Class", exptClass.getName() });
		data.add(new String[] {"Report compiled", new SimpleDateFormat().format(dc)});
		
		if (experimentDetails != null) {
			data.add(new String[] { "Author", WordUtils.wrap(experimentDetails.author(), exptClass.getName().length()) });
			data.add(new String[] { "Created on", experimentDetails.dateCreated() });
			data.add(new String[] { "Description", WordUtils.wrap(experimentDetails.description(), exptClass.getName().length()) });
		}
		
		ASCIITableHeader [] header = {
				new ASCIITableHeader("", ASCIITable.ALIGN_RIGHT),
				new ASCIITableHeader("", ASCIITable.ALIGN_LEFT)
		};
		
		String table = ASCIITable.getInstance().getTable(header, data.toArray(new String[data.size()][]));
		
		int width = table.indexOf("\n") + 1;
		table = table.substring(2 * width);
		
		return table;
	}
	
	private String getTimingTable() {
		ASCIITableHeader [] header = { new ASCIITableHeader("Experimental Timing", ASCIITable.ALIGN_CENTER) };
		String [][] data = formatAsTable(TimeTracker.format(timingInfo));
		return ASCIITable.getInstance().getTable(header, data);
	}
	
	private String getBibliographyTable() {
		ASCIITableHeader [] header = {new ASCIITableHeader("Bibliography", ASCIITable.ALIGN_LEFT)};
		String refs = StandardFormatters.STRING.formatReferences(bibliography);
		
		refs = WordUtils.wrap(refs, Math.max(exptClass.getName().length() + 10, 72), SystemUtils.LINE_SEPARATOR + "  ", true);
		
		String [][] data = formatAsTable(refs);
		return ASCIITable.getInstance().getTable(header, data);
	}
	
	private String[][] formatAsTable(String data) {
		String[] splits = data.trim().split("\\r?\\n");
		
		String [][] out = new String[splits.length][];
		for (int i=0; i<splits.length; i++) {
			out[i] = new String[] { splits[i] };
		}
		
		return out;
	}
	
	private String getIndependentVariablesTable() {
		ASCIITableHeader [] header = { new ASCIITableHeader("Independent Variables", ASCIITable.ALIGN_CENTER) };
		String [][] data = formatAsTable(formatVariables(getIndependentVariables()));
		return ASCIITable.getInstance().getTable(header, data);
	}
	
	private String formatVariables(Map<Variable, Object> vars) {
		List<String[]> data = new ArrayList<String[]>();
		
		for (Entry<Variable, Object> e : vars.entrySet()) {
			String id = e.getKey().identifier;
			String [] val = formatValue(e.getValue());
			
			data.add(new String[] {id, val[0]});
			for (int i=1; i<val.length; i++)
				data.add(new String[] {"", val[i]});
		}
		
		ASCIITableHeader [] header = {
				new ASCIITableHeader("", ASCIITable.ALIGN_RIGHT),
				new ASCIITableHeader("", ASCIITable.ALIGN_LEFT)
		};
		
		String table = ASCIITable.getInstance().getTable(header, data.toArray(new String[data.size()][]));
		
		int width = table.indexOf("\n") + 1;
		table = table.substring(2 * width);
		
		return table;
	}
	
	private String getDependentVariablesTable() {
		ASCIITableHeader [] header = { new ASCIITableHeader("Dependent Variables", ASCIITable.ALIGN_CENTER) };
		String [][] data = formatAsTable(formatVariables(getDependentVariables()));
		return ASCIITable.getInstance().getTable(header, data);
	}
	
	private String[] formatValue(Object value) {
		//is it a dataset?
		if (value.getClass().getAnnotation(DatasetDescription.class) != null) {
			DatasetDescription d = value.getClass().getAnnotation(DatasetDescription.class);
			
			List<String[]> data = new ArrayList<String[]>();
			data.add(new String[] {"Name", d.name()});

			String[] description = WordUtils.wrap(d.description(), exptClass.getName().length()-20).split("\\r?\\n");
			data.add(new String[] {"Description", description[0]});
			for (int i=1; i<description.length; i++)
				data.add(new String[] {"", description[i]});
						
			ASCIITableHeader [] header = {
					new ASCIITableHeader("", ASCIITable.ALIGN_RIGHT),
					new ASCIITableHeader("", ASCIITable.ALIGN_LEFT)
			};
			
			String table = ASCIITable.getInstance().getTable(header, data.toArray(new String[data.size()][]));
			
			int width = table.indexOf("\n") + 1;
			table = table.substring(2 * width);
			
			return table.split("\\r?\\n");
		}
		
		//is it an analysis result?
		if (value instanceof AnalysisResult) {
			//return ((AnalysisResult)value).getDetailReport().split("\\r?\\n");
			return ((AnalysisResult)value).getSummaryReport().split("\\r?\\n");
		}
		
		//otherwise use toString
		return value.toString().split("\\r?\\n");
	}

	@Override
	public String toString() {
		String[][] exptinfo = formatAsTable( getExptInfoTable() );
		String[][] timeInfo = formatAsTable( getTimingTable() );
		String[][] ivInfo = formatAsTable( getIndependentVariablesTable() );
		String[][] dvInfo = formatAsTable( getDependentVariablesTable() );
		String[][] biblInfo = formatAsTable( getBibliographyTable() );
		
		String[][] data = ArrayUtils.concatenate(exptinfo, timeInfo, ivInfo, dvInfo, biblInfo);
		ASCIITableHeader [] header = { new ASCIITableHeader("Experiment Context", ASCIITable.ALIGN_LEFT) };
		
		return ASCIITable.getInstance().getTable(header, data);
	}
}
