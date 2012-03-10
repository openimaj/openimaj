package org.openimaj.citation.annotation.output;

import java.util.Collection;

import org.openimaj.citation.annotation.Reference;

public interface ReferenceFormatter {
	public String formatReference(Reference ref);
	public String formatReferences(Reference... refs);
	public String formatReferences(Collection<Reference> refs);
}
