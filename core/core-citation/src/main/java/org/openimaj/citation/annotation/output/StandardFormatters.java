package org.openimaj.citation.annotation.output;

import java.util.Collection;

import org.openimaj.citation.annotation.Reference;

/**
 * Standard reference formatters.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public enum StandardFormatters implements ReferenceFormatter {
	/**
	 * Format references as BibTeX.
	 * 
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 */
	BIBTEX {
		@Override
		protected String formatRefs(Iterable<Reference> refs) {
			StringBuilder builder = new StringBuilder();
			
			for (Reference r : refs)
				builder.append(formatReference(r));
			
			return builder.toString();
		}

		void appendNames(StringBuilder builder, String key, String [] authors) {
			if (authors == null || authors.length == 0 || (authors.length == 1 && authors[0].length() == 0))
				return;
			
			builder.append(" " + key + " = {" );
			for (int i=0; i<authors.length-1; i++) {
				builder.append("{" + authors[i] + "} and ");
			}
			builder.append("{" + authors[authors.length-1] + "}");
			builder.append("}\n");
		}
		
		@Override
		public String formatReference(Reference ref) {
			StringBuilder builder = new StringBuilder();
			
			String key = ref.hashCode()+"";
			builder.append("@" + ref.type() + "{" + key + "\n");
			appendNames(builder, "author", ref.author());

			builder.append(" title = {" + ref.title() + "}\n");
			builder.append(" year = {" + ref.year() + "}\n");

			if (ref.journal().length() > 0) builder.append(" journal = {" + ref.journal() + "}\n");
			if (ref.booktitle().length() > 0) builder.append(" booktitle = {" + ref.booktitle() + "}\n");
			if (ref.pages().length > 0) { 
				if (ref.pages().length == 1) builder.append(" pages = {" + ref.pages()[0] + "}\n");
				else if (ref.pages().length == 2) builder.append(" pages = {" + ref.pages()[0] + "--" + ref.pages()[1] + "}\n");
				else {
					builder.append(" pages = {");
					for (int i=0; i<ref.pages().length-1; i++) builder.append(ref.pages()[i] + ", ");
					builder.append(ref.pages()[ref.pages().length - 1] + "}\n");
				}
			}

			if (ref.chapter().length() > 0) builder.append(" chapter = {" + ref.chapter() + "}\n");
			if (ref.edition().length() > 0) builder.append(" edition = {" + ref.edition() + "}\n");
		    if (ref.url().length() > 0) builder.append(" url = {" + ref.url() + "}\n");
		    if (ref.note().length() > 0) builder.append(" note = {" + ref.note() + "}\n");
		    
		    appendNames(builder, "editor", ref.editor());
		    
		    if (ref.institution().length() > 0) builder.append(" institution = {" + ref.institution() + "}\n");
		    if (ref.month().length() > 0) builder.append(" month = {" + ref.month() + "}\n");
		    if (ref.number() != "") builder.append(" number = {" + ref.number() + "}\n");
		    if (ref.organization().length() > 0) builder.append(" organization = {" + ref.organization() + "}\n");
		    if (ref.publisher().length() > 0) builder.append(" publisher = {" + ref.publisher() + "}\n");
		    if (ref.school().length() > 0) builder.append(" school = {" + ref.school() + "}\n");
		    if (ref.series().length() > 0) builder.append(" series = {" + ref.series() + "}\n");
		    if (ref.volume() != "") builder.append(" volume = {" + ref.volume() + "}\n");
		    
		    if (ref.customData().length > 1) {
		    	for (int i=0; i<ref.customData().length; i+=2) {
		    		builder.append(" " + ref.customData()[i] + " = {" + ref.customData()[i+1] + "}\n");
		    	}
		    }
		    
			builder.append("}\n");
			
			return builder.toString();
		}		
	},
	/**
	 * Format as a {@link Reference} annotation for inclusion in code
	 * 
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 *
	 */
	REFERENCE_ANNOTATION {
		@Override
		protected String formatRefs(Iterable<Reference> refs) {
			StringBuilder builder = new StringBuilder();
			
			for (Reference r : refs)
				builder.append(formatReference(r));
			
			return builder.toString();
		}

		private String formatArray(String[] arr) {
			StringBuilder builder = new StringBuilder();
			builder.append("{ ");
			for (int i=0; i<arr.length - 1; i++) {
				builder.append(formatString(arr[i]) + ", ");
			}
			builder.append(formatString(arr[arr.length-1]) + " }");
			
			return builder.toString();
		}
		
		private String formatString(String str) {
			return  "\"" + str + "\"";
		}
		
		@Override
		public String formatReference(Reference ref) {
			StringBuilder builder = new StringBuilder();
			
			builder.append("@Reference(\n");
			builder.append("\ttype = ReferenceType." + ref.type() + ",\n");
			builder.append("\tauthor = " + formatArray(ref.author()) + ",\n");
			builder.append("\ttitle = " + formatString(ref.title()) + ",\n");
			builder.append("\tyear = " + formatString(ref.year()) + ",\n");

			if (ref.journal().length() > 0) builder.append("\tjournal = " + formatString(ref.journal()) + ",\n");
			if (ref.booktitle().length() > 0) builder.append("\tbooktitle = " + formatString(ref.booktitle()) + ",\n");
			if (ref.pages().length > 0) { 
				builder.append("\tpages = " + formatArray(ref.pages()) + ",\n");
			}

			if (ref.chapter().length() > 0) builder.append("\tchapter = " + formatString(ref.chapter()) + ",\n");
			if (ref.edition().length() > 0) builder.append("\tedition = " + formatString(ref.edition()) + ",\n");
		    if (ref.url().length() > 0) builder.append("\turl = " + formatString(ref.url()) + ",\n");
		    if (ref.note().length() > 0) builder.append("\tnote = " + formatString(ref.note()) + ",\n");
		    if (ref.editor().length > 0) builder.append("\teditor = " + formatArray(ref.editor()) + ",\n");
		    if (ref.institution().length() > 0) builder.append("\tinstitution = " + formatString(ref.institution()) + ",\n");
		    if (ref.month().length() > 0) builder.append("\tmonth = " + formatString(ref.month()) + ",\n");
		    if (ref.number() != "") builder.append("\tnumber = " + formatString(ref.number()) + ",\n");
		    if (ref.organization().length() > 0) builder.append("\torganization = " + formatString(ref.organization()) + ",\n");
		    if (ref.publisher().length() > 0) builder.append("\tpublisher = " + formatString(ref.publisher()) + ",\n");
		    if (ref.school().length() > 0) builder.append("\tschool = " + formatString(ref.school()) + ",\n");
		    if (ref.series().length() > 0) builder.append("\tseries = " + formatString(ref.series()) + ",\n");
		    if (ref.volume() != "") builder.append("\tvolume = " + formatString(ref.volume()) + ",\n");
		    
		    if (ref.customData().length > 1) {
		    	builder.append("\tcustomData = " + formatArray(ref.customData()) + ",\n");
		    }
		    
		    if (builder.charAt(builder.length() - 2) == ',') {
		    	builder.deleteCharAt(builder.length() - 2);
		    }
			builder.append(")\n");
			
			return builder.toString();
		}
		
	}
	;

	protected abstract String formatRefs(Iterable<Reference> refs);
	
	@Override
	public abstract String formatReference(Reference ref);

	@Override
	public String formatReferences(Collection<Reference> refs) {
		return formatRefs(refs);
	}
}
