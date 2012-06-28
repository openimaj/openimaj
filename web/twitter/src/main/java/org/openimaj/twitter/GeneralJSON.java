package org.openimaj.twitter;



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
public interface GeneralJSON {

	/**
	 * This is the method that will be called by USMFStatus to fill itself with
	 * the matching values from the extending class.
	 * It is up to the extending programmer to carry this out as they see fit.
	 * See GeneralJSONTwitter for a Twitter example.
	 * 
	 * @param status = USMFStatus to be filled
	 */
	public void fillUSMF(USMFStatus status);

}
