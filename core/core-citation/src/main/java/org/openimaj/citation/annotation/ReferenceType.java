package org.openimaj.citation.annotation;

/**
 * Standard BibTeX types.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public enum ReferenceType {
	/**
	 * An article from a journal or magazine.
	 */
	Article,
	
	/**
	 * A book with an explicit publisher. 
	 */
	Book,
	
	/**
	 * A work that is printed and bound, but without a named publisher or sponsoring institution.
	 */
	Booklet,
	
	/**
	 * A part of a book, usually untitled. May be a chapter (or section or whatever) and/or a range of pages.
	 */
	Inbook,
	
	/**
	 * A part of a book having its own title. 
	 */
	Incollection,
	
	/**
	 * An article in a conference proceedings. 
	 */
	Inproceedings,
	
	/**
	 * Technical documentation.
	 */
	Manual,
	
	/**
	 * A Master's thesis.
	 */
	Mastersthesis,
	
	/**
	 * For use when nothing else fits.
	 */
	Misc,
	
	/**
	 * A Ph.D. thesis. 
	 */
	Phdthesis,
	
	/**
	 * The proceedings of a conference.
	 */
	Proceedings,
	
	/**
	 * A report published by a school or other institution, usually numbered within a series.
	 */
	Techreport,
	
	/**
	 * A document having an author and title, but not formally published.
	 */
	Unpublished
}
