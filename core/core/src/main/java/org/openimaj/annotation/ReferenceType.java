package org.openimaj.annotation;

/**
 * Standard BibTeX types.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public enum ReferenceType {
	/**
	 * An article from a journal or magazine.
	 */
	article,
	
	/**
	 * A book with an explicit publisher. 
	 */
	book,
	
	/**
	 * A work that is printed and bound, but without a named publisher or sponsoring institution.
	 */
	booklet,
	
	/**
	 * A part of a book, usually untitled. May be a chapter (or section or whatever) and/or a range of pages.
	 */
	inbook,
	
	/**
	 * A part of a book having its own title. 
	 */
	incollection,
	
	/**
	 * An article in a conference proceedings. 
	 */
	inproceedings,
	
	/**
	 * Technical documentation.
	 */
	manual,
	
	/**
	 * A Master's thesis.
	 */
	mastersthesis,
	
	/**
	 * For use when nothing else fits.
	 */
	misc,
	
	/**
	 * A Ph.D. thesis. 
	 */
	phdthesis,
	
	/**
	 * The proceedings of a conference.
	 */
	proceedings,
	
	/**
	 * A report published by a school or other institution, usually numbered within a series.
	 */
	techreport,
	
	/**
	 * A document having an author and title, but not formally published.
	 */
	unpublished
}
