/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.citation.annotation.output;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import org.jbibtex.LaTeXObject;
import org.jbibtex.LaTeXParser;
import org.jbibtex.LaTeXPrinter;
import org.jbibtex.ParseException;
import org.openimaj.citation.annotation.Reference;

/**
 * Standard reference formatters.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public enum StandardFormatters implements ReferenceFormatter {
	/**
	 * Format references as BibTeX.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	BIBTEX {
		@Override
		protected String formatRefs(Iterable<Reference> refs) {
			final StringBuilder builder = new StringBuilder();

			for (final Reference r : refs)
				builder.append(format(r));

			return builder.toString();
		}

		void appendNames(StringBuilder builder, String key, String[] authors) {
			if (authors == null || authors.length == 0 || (authors.length == 1 && authors[0].length() == 0))
				return;

			builder.append(" " + key + " = {");
			for (int i = 0; i < authors.length - 1; i++) {
				builder.append("{" + authors[i] + "} and ");
			}
			builder.append("{" + authors[authors.length - 1] + "}");
			builder.append("}\n");
		}

		@Override
		public String format(Reference ref) {
			final StringBuilder builder = new StringBuilder();

			final String key = ref.hashCode() + "";
			builder.append("@" + ref.type() + "{" + key + "\n");
			appendNames(builder, "author", ref.author());

			builder.append(" title = {" + ref.title() + "}\n");
			builder.append(" year = {" + ref.year() + "}\n");

			if (ref.journal().length() > 0)
				builder.append(" journal = {" + ref.journal() + "}\n");
			if (ref.booktitle().length() > 0)
				builder.append(" booktitle = {" + ref.booktitle() + "}\n");
			if (ref.pages().length > 0) {
				if (ref.pages().length == 1)
					builder.append(" pages = {" + ref.pages()[0] + "}\n");
				else if (ref.pages().length == 2)
					builder.append(" pages = {" + ref.pages()[0] + "--" + ref.pages()[1] + "}\n");
				else {
					builder.append(" pages = {");
					for (int i = 0; i < ref.pages().length - 1; i++)
						builder.append(ref.pages()[i] + ", ");
					builder.append(ref.pages()[ref.pages().length - 1] + "}\n");
				}
			}

			if (ref.chapter().length() > 0)
				builder.append(" chapter = {" + ref.chapter() + "}\n");
			if (ref.edition().length() > 0)
				builder.append(" edition = {" + ref.edition() + "}\n");
			if (ref.url().length() > 0)
				builder.append(" url = {" + ref.url() + "}\n");
			if (ref.note().length() > 0)
				builder.append(" note = {" + ref.note() + "}\n");

			appendNames(builder, "editor", ref.editor());

			if (ref.institution().length() > 0)
				builder.append(" institution = {" + ref.institution() + "}\n");
			if (ref.month().length() > 0)
				builder.append(" month = {" + ref.month() + "}\n");
			if (ref.number() != "")
				builder.append(" number = {" + ref.number() + "}\n");
			if (ref.organization().length() > 0)
				builder.append(" organization = {" + ref.organization() + "}\n");
			if (ref.publisher().length() > 0)
				builder.append(" publisher = {" + ref.publisher() + "}\n");
			if (ref.school().length() > 0)
				builder.append(" school = {" + ref.school() + "}\n");
			if (ref.series().length() > 0)
				builder.append(" series = {" + ref.series() + "}\n");
			if (ref.volume() != "")
				builder.append(" volume = {" + ref.volume() + "}\n");

			if (ref.customData().length > 1) {
				for (int i = 0; i < ref.customData().length; i += 2) {
					builder.append(" " + ref.customData()[i] + " = {" + ref.customData()[i + 1] + "}\n");
				}
			}

			builder.append("}\n");

			return builder.toString();
		}
	},
	/**
	 * Format as a {@link Reference} annotation for inclusion in code
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	REFERENCE_ANNOTATION {
		@Override
		protected String formatRefs(Iterable<Reference> refs) {
			final StringBuilder builder = new StringBuilder();

			for (final Reference r : refs)
				builder.append(format(r));

			return builder.toString();
		}

		private String formatArray(String[] arr) {
			final StringBuilder builder = new StringBuilder();
			builder.append("{ ");
			for (int i = 0; i < arr.length - 1; i++) {
				builder.append(formatString(arr[i]) + ", ");
			}
			builder.append(formatString(arr[arr.length - 1]) + " }");

			return builder.toString();
		}

		private String formatString(String str) {
			return "\"" + str + "\"";
		}

		@Override
		public String format(Reference ref) {
			final StringBuilder builder = new StringBuilder();

			builder.append("@Reference(\n");
			builder.append("\ttype = ReferenceType." + ref.type() + ",\n");
			builder.append("\tauthor = " + formatArray(ref.author()) + ",\n");
			builder.append("\ttitle = " + formatString(ref.title()) + ",\n");
			builder.append("\tyear = " + formatString(ref.year()) + ",\n");

			if (ref.journal().length() > 0)
				builder.append("\tjournal = " + formatString(ref.journal()) + ",\n");
			if (ref.booktitle().length() > 0)
				builder.append("\tbooktitle = " + formatString(ref.booktitle()) + ",\n");
			if (ref.pages().length > 0) {
				builder.append("\tpages = " + formatArray(ref.pages()) + ",\n");
			}

			if (ref.chapter().length() > 0)
				builder.append("\tchapter = " + formatString(ref.chapter()) + ",\n");
			if (ref.edition().length() > 0)
				builder.append("\tedition = " + formatString(ref.edition()) + ",\n");
			if (ref.url().length() > 0)
				builder.append("\turl = " + formatString(ref.url()) + ",\n");
			if (ref.note().length() > 0)
				builder.append("\tnote = " + formatString(ref.note()) + ",\n");
			if (ref.editor().length > 0)
				builder.append("\teditor = " + formatArray(ref.editor()) + ",\n");
			if (ref.institution().length() > 0)
				builder.append("\tinstitution = " + formatString(ref.institution()) + ",\n");
			if (ref.month().length() > 0)
				builder.append("\tmonth = " + formatString(ref.month()) + ",\n");
			if (ref.number() != "")
				builder.append("\tnumber = " + formatString(ref.number()) + ",\n");
			if (ref.organization().length() > 0)
				builder.append("\torganization = " + formatString(ref.organization()) + ",\n");
			if (ref.publisher().length() > 0)
				builder.append("\tpublisher = " + formatString(ref.publisher()) + ",\n");
			if (ref.school().length() > 0)
				builder.append("\tschool = " + formatString(ref.school()) + ",\n");
			if (ref.series().length() > 0)
				builder.append("\tseries = " + formatString(ref.series()) + ",\n");
			if (ref.volume() != "")
				builder.append("\tvolume = " + formatString(ref.volume()) + ",\n");

			if (ref.customData().length > 1) {
				builder.append("\tcustomData = {\n");
				for (int i = 0; i < ref.customData().length; i += 2) {
					builder.append("\t\t" + formatString(ref.customData()[i]) + ", "
							+ formatString(ref.customData()[i + 1]));
					if (i < ref.customData().length - 2)
						builder.append(",");
					builder.append("\n");
				}
				builder.append("\t}\n");
			}

			if (builder.charAt(builder.length() - 2) == ',') {
				builder.deleteCharAt(builder.length() - 2);
			}
			builder.append(")\n");

			return builder.toString();
		}

	},
	/**
	 * Format as a pretty string
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	STRING {
		@Override
		protected String formatRefs(Iterable<Reference> refs) {
			final StringBuilder builder = new StringBuilder();

			for (final Reference r : refs)
				builder.append(format(r) + "\n");

			return builder.toString();
		}

		void appendNames(StringBuilder builder, String[] authors) {
			if (authors == null || authors.length == 0 || (authors.length == 1 && authors[0].length() == 0))
				return;

			if (authors.length == 1) {
				builder.append(formatName(authors[0]) + ".");
				return;
			}

			for (int i = 0; i < authors.length - 2; i++) {
				builder.append(formatName(authors[i]) + ", ");
			}
			builder.append(formatName(authors[authors.length - 2]) + " and " + formatName(authors[authors.length - 1])
					+ ". ");
		}

		String formatName(String name) {
			name = format(name);

			if (name.contains(",")) {
				final String lastName = name.substring(0, name.indexOf(","));
				final String[] firstNames = name.substring(name.indexOf(",") + 1).split(" ");

				String formatted = "";
				for (String f : firstNames) {
					f = f.trim();
					if (f.length() > 0)
						formatted += f.charAt(0) + ". ";
				}

				return formatted + lastName;
			} else {
				final String[] parts = name.split(" ");
				String formatted = "";

				for (int i = 0; i < parts.length - 1; i++) {
					formatted += parts[i].charAt(0) + ". ";
				}

				return formatted + parts[parts.length - 1];
			}
		}

		@Override
		public String format(Reference ref) {
			final StringBuilder builder = new StringBuilder();

			appendNames(builder, ref.author());

			builder.append(format(ref.title()) + ". ");

			if (ref.journal().length() > 0)
				builder.append(format(ref.journal()) + ". ");
			if (ref.booktitle().length() > 0)
				builder.append(format(ref.booktitle()) + ". ");
			if (ref.institution().length() > 0)
				builder.append(format(ref.institution()) + ". ");
			if (ref.school().length() > 0)
				builder.append(format(ref.school()) + ". ");
			if (ref.publisher().length() > 0)
				builder.append(format(ref.publisher()) + ". ");
			if (ref.organization().length() > 0)
				builder.append(format(ref.organization()) + ". ");

			if (ref.pages().length > 0) {
				if (ref.pages().length == 1)
					builder.append("p" + ref.pages()[0] + ". ");
				else if (ref.pages().length == 2)
					builder.append("pp" + ref.pages()[0] + "-" + ref.pages()[1] + ". ");
				else {
					builder.append("pp");
					for (int i = 0; i < ref.pages().length - 1; i++)
						builder.append(ref.pages()[i] + ", ");
					builder.append(ref.pages()[ref.pages().length - 1] + ". ");
				}
			}

			if (ref.month().length() > 0)
				builder.append(format(ref.month()) + ", ");
			builder.append(format(ref.year()) + ". ");

			if (ref.url().length() > 0)
				builder.append(format(ref.url()));

			return builder.toString();
		}
	},
	/**
	 * Format as an HTML fragment
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	HTML {
		@Override
		protected String formatRefs(Iterable<Reference> refs) {
			final StringBuilder builder = new StringBuilder();

			for (final Reference r : refs) {
				builder.append("<p class=\"ref\">");
				builder.append(format(r));
				builder.append("</p>\n");
			}

			return builder.toString();
		}

		void appendNames(StringBuilder builder, String[] authors) {
			if (authors == null || authors.length == 0 || (authors.length == 1 && authors[0].length() == 0))
				return;

			if (authors.length == 1) {
				builder.append(formatName(authors[0]) + ". ");
				return;
			}

			for (int i = 0; i < authors.length - 2; i++) {
				builder.append(formatName(authors[i]) + ", ");
			}
			builder.append(formatName(authors[authors.length - 2]) + " and " + formatName(authors[authors.length - 1])
					+ ". ");
		}

		String formatName(String name) {
			name = format(name);

			if (name.contains(",")) {
				final String lastName = name.substring(0, name.indexOf(","));
				final String[] firstNames = name.substring(name.indexOf(",") + 1).split(" ");

				String formatted = "";
				for (String f : firstNames) {
					f = f.trim();
					if (f.length() > 0)
						formatted += f.charAt(0) + ". ";
				}

				return formatted + lastName;
			} else {
				final String[] parts = name.split(" ");
				String formatted = "";

				for (int i = 0; i < parts.length - 1; i++) {
					formatted += parts[i].charAt(0) + ". ";
				}

				return formatted + parts[parts.length - 1];
			}
		}

		@Override
		public String format(Reference ref) {
			final StringBuilder builder = new StringBuilder();

			builder.append("<span class='authors'>");
			appendNames(builder, ref.author());
			builder.append("</span>");

			builder.append("<span class='title'>");
			builder.append(format(ref.title()) + ". ");
			builder.append("</span>");

			if (ref.journal().length() > 0)
				builder.append("<span class='journal'>" + format(ref.journal()) + ". </span>");
			if (ref.booktitle().length() > 0)
				builder.append("<span class='booktitle'>" + format(ref.booktitle()) + ". </span>");
			if (ref.institution().length() > 0)
				builder.append("<span class='institution'>" + format(ref.institution()) + ". </span>");
			if (ref.school().length() > 0)
				builder.append("<span class='school'>" + format(ref.school()) + ". </span>");
			if (ref.publisher().length() > 0)
				builder.append("<span class='publisher'>" + format(ref.publisher()) + ". </span>");
			if (ref.organization().length() > 0)
				builder.append("<span class='organization'>" + format(ref.organization()) + ". </span>");

			if (ref.pages().length > 0) {
				if (ref.pages().length == 1)
					builder.append("<span class='pages'>" + "p" + ref.pages()[0] + ". </span>");
				else if (ref.pages().length == 2)
					builder.append("<span class='pages'>" + "pp" + ref.pages()[0] + "-" + ref.pages()[1] + ". </span>");
				else {
					builder.append("<span class='pages'>" + "pp");
					for (int i = 0; i < ref.pages().length - 1; i++)
						builder.append(ref.pages()[i] + ", ");
					builder.append(ref.pages()[ref.pages().length - 1] + ". </span>");
				}
			}

			if (ref.month().length() > 0)
				builder.append("<span class='month'>" + format(ref.month()) + ", </span>");
			builder.append("<span class='year'>" + format(ref.year()) + ". </span>");

			if (ref.url().length() > 0)
				builder.append("<a class='url' href='" + format(ref.url()) + "'>" + format(ref.url()) + "</a>");

			return builder.toString();
		}
	};

	protected abstract String formatRefs(Iterable<Reference> refs);

	@Override
	public abstract String format(Reference ref);

	@Override
	public String format(Collection<Reference> refs) {
		return formatRefs(refs);
	}

	static List<LaTeXObject> parseLaTeX(String string) throws IOException, ParseException {
		final Reader reader = new StringReader(string);

		try {
			final LaTeXParser parser = new LaTeXParser();

			return parser.parse(reader);
		} finally {
			reader.close();
		}
	}

	static String printLaTeX(List<LaTeXObject> objects) {
		final LaTeXPrinter printer = new LaTeXPrinter();

		return printer.print(objects);
	}

	static String format(String latex) {
		try {
			return printLaTeX(parseLaTeX(latex));
		} catch (final Exception e) {
			return latex;
		}
	}
}
