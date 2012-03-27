package org.openimaj.citation.annotation.input;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXParser;
import org.jbibtex.ParseException;
import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.mock.MockReference;
import org.openimaj.citation.annotation.output.StandardFormatters;

/**
 * Helper tool to convert bibtex to {@link Reference} annotations.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class BibtexToReference {
	/**
	 * Helper tool to convert bibtex to {@link Reference} annotations.
	 * Reads from stdin, writes to stdout.
	 * 
	 * @param args not used
	 * @throws IOException 
	 * @throws ParseException
	 */
	public static void main(String[] args) throws IOException, ParseException {
		System.out.println("Enter bibtex record(s), followed by ctrl-d: ");
		
		Reader reader = new InputStreamReader(System.in);
		
		BibTeXParser parser = new BibTeXParser();
		BibTeXDatabase database = parser.parse(reader);
		
		System.out.println();
		
		for (BibTeXEntry entry : database.getEntries().values()) {
			Reference r = MockReference.makeReference(entry);
			System.out.println(StandardFormatters.REFERENCE_ANNOTATION.formatReference(r));
		}
	}
}
