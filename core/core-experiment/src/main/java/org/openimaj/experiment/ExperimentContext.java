package org.openimaj.experiment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.openimaj.citation.agent.ReferenceListener;
import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.output.StandardFormatters;
import org.openimaj.experiment.agent.TimeTracker;
import org.openimaj.experiment.annotations.DatasetDescription;
import org.openimaj.experiment.annotations.DependentVariable;
import org.openimaj.experiment.annotations.Experiment;
import org.openimaj.experiment.annotations.IndependentVariable;
import org.openimaj.util.array.ArrayUtils;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.ASCIITableHeader;

/**
 * The recorded context of an experiment.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ExperimentContext {
	private boolean isLocked;

	private Date dateCompleted;
	private Class<?> exptClass;
	private Experiment experimentDetails;
	private Set<Reference> bibliography;
	private Map<String, SummaryStatistics> timingInfo;
	private Map<IndependentVariable, Object> independentVariables;
	private Map<DependentVariable, Object> dependentVariables;
	private List<DatasetDescription> datasets;
	
	protected ExperimentContext(RunnableExperiment expt) {
		experimentDetails = getExperiment(expt);
		exptClass = expt.getClass();
	}
	
	private Experiment getExperiment(RunnableExperiment expt) {
		Class<?> exptClass = expt.getClass();
		
		while (exptClass != null) {
			Experiment ann = exptClass.getAnnotation(Experiment.class);
			
			if (ann != null)
				return ann;
			
			exptClass = exptClass.getSuperclass();
		}
		
		return null;
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
		
		return ASCIITable.getInstance().getTable((String[])null, data.toArray(new String[data.size()][]));
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
	
	@Override
	public String toString() {
		String[][] exptinfo = formatAsTable( getExptInfoTable() );
		String[][] timeInfo = formatAsTable( getTimingTable() );
		String[][] biblInfo = formatAsTable( getBibliographyTable() );
		
		String[][] data = ArrayUtils.concatenate(exptinfo, timeInfo, biblInfo);
		ASCIITableHeader [] header = { new ASCIITableHeader("Experiment Context", ASCIITable.ALIGN_CENTER) };
		
		return ASCIITable.getInstance().getTable(header, data);
	}
}
