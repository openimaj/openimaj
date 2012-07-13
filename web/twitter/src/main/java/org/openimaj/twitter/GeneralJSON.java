package org.openimaj.twitter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.io.ReadWriteable;

import com.google.gson.Gson;



/**
 * This interface should be implemented by classes that will define a new json
 * input format for the USMFStatus object. This object will be populated by GSon
 * which fills the fields of the object with the matching named values in a json
 * string structure. It is up to the extending programmer to implement their
 * object so that it is accurately filled by GSon. See more here
 * http://code.google.com/p/google-gson/
 * 
 * @author Laurence Willmore <lgw1e10@ecs.soton.ac.uk>
 * 
 */
public abstract class GeneralJSON implements ReadWriteable{
	
	protected transient Gson gson = new Gson();

	/**
	 * This is the method that will be called by USMFStatus to fill itself with
	 * the matching values from the extending class.
	 * It is up to the extending programmer to carry this out as they see fit.
	 * See GeneralJSONTwitter for a Twitter example.
	 * 
	 * @param status = USMFStatus to be filled
	 */
	public abstract void fillUSMF(USMFStatus status);
	
	/**
	 * This is the method that will be called to allow this object
	 * to fill itself from a USMF object. Implementations must guarantee
	 * that they hold on and copy internal ANALYSIS. we recommend the
	 * helper function
	 * 
	 * @param status = USMFStatus to be filled
	 */
	public abstract void fromUSMF(USMFStatus status);
	
	@Override
	public void readBinary(DataInput in) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] binaryHeader() {
		return "BINARYHEADER".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		throw new UnsupportedOperationException();

	}
	
	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		gson.toJson(this, out);
	}
	
	/**
	 * analysos held in the object
	 */
	public Map<String, Object> analysis = new HashMap<String, Object>();
	
	/**
	 * Convenience to allow writing of just the analysis to a writer
	 * 
	 * @param outputWriter
	 * @param selectiveAnalysis
	 */
	public void writeASCIIAnalysis(PrintWriter outputWriter,List<String> selectiveAnalysis) {
		writeASCIIAnalysis(outputWriter, selectiveAnalysis,
				new ArrayList<String>());
	}

	/**
	 * Convenience to allow writing of just the analysis and some status
	 * information to a writer
	 * 
	 * @param outputWriter
	 * @param selectiveAnalysis
	 * @param selectiveStatus
	 */
	public void writeASCIIAnalysis(PrintWriter outputWriter,List<String> selectiveAnalysis, List<String> selectiveStatus) {
		Map<String, Object> toOutput = new HashMap<String, Object>();
		Map<String, Object> analysisBit = new HashMap<String, Object>();
		toOutput.put("analysis", analysisBit);
		for (String analysisKey : selectiveAnalysis) {
			analysisBit.put(analysisKey, getAnalysis(analysisKey));
		}
		for (String status : selectiveStatus) {
			try {

				Field f = this.getClass().getField(status);
				toOutput.put(status, f.get(this));
			} catch (SecurityException e) {
				System.err.println("Invalid field: " + status);
			} catch (NoSuchFieldException e) {
				System.err.println("Invalid field: " + status);
			} catch (IllegalArgumentException e) {
				System.err.println("Invalid field: " + status);
			} catch (IllegalAccessException e) {
				System.err.println("Invalid field: " + status);
			}
		}
		gson.toJson(toOutput, outputWriter);
	}
	
	/**
	 * Add analysis to the analysis object. This is where all non twitter stuff
	 * should go
	 * 
	 * @param <T>
	 *            The type of data being saved
	 * @param annKey
	 *            the key
	 * @param annVal
	 *            the value
	 */
	public <T> void addAnalysis(String annKey, T annVal) {
		if (annVal instanceof Number)
			this.analysis.put(annKey, ((Number) annVal).doubleValue());
		else
			this.analysis.put(annKey, annVal);
	}

	/**
	 * @param <T>
	 * @param name
	 * @return the analysis under the name
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAnalysis(String name) {
		return (T) this.analysis.get(name);
	}
	
	/**
	 * Get all the Analysis in JSON format.
	 * @return String of JSON.
	 */
	public String analysisToJSON(){
		return gson.toJson(analysis, Map.class);
	}
	
	/**
	 * @param other
	 */
	public void fillAnalysis(GeneralJSON other){
		other.analysis = this.analysis;
	}
	

}
