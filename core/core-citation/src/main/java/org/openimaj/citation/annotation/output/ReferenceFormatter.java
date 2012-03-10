package org.openimaj.citation.annotation.output;

import java.util.Collection;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.References;

/**
 * The {@link ReferenceFormatter} defines an interface for
 * objects capable of converting {@link Reference} and {@link References}
 * objects into {@link String}s.
 * 
 * @see StandardFormatters for standard implementations 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public interface ReferenceFormatter {
	/**
	 * Format a single reference
	 * @param ref the {@link Reference} to format
	 * @return formatted {@link Reference}
	 */
	public String formatReference(Reference ref);
	
	/**
	 * Format a multiples references
	 * @param refs the {@link References} to format
	 * @return formatted {@link References}
	 */
	public String formatReferences(Collection<Reference> refs);
}
