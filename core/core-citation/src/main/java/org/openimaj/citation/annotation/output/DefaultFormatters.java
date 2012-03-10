package org.openimaj.citation.annotation.output;

import java.util.Arrays;
import java.util.Collection;

import org.openimaj.citation.annotation.Reference;

public enum DefaultFormatters implements ReferenceFormatter {
	BIBTEX {
		@Override
		protected String formatRefs(Iterable<Reference> refs) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String formatReference(Reference ref) {
			// TODO Auto-generated method stub
			return null;
		}		
	}
	;

	protected abstract String formatRefs(Iterable<Reference> refs);
	public abstract String formatReference(Reference ref);

	@Override
	public String formatReferences(Reference... refs) {
		return formatRefs(Arrays.asList(refs));
	}

	@Override
	public String formatReferences(Collection<Reference> refs) {
		return formatRefs(refs);
	}

}
