package org.openimaj.tests;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;

/**
 * Useful utility methods to get things such as the current OpenIMAJ version.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Jonathan Hare", "Sina Samangooei", "David Dupplaw" },
		title = "Test::Class",
		year = "2012"
)
public class App {
	@Reference(
			type = ReferenceType.Inproceedings,
			author = { "Jonathan Hare", "Sina Samangooei", "David Dupplaw" },
			title = "Test::Method",
			year = "2012"
	)	
	public void method() {
		//do nothing
	}
	
	@Reference(
			type = ReferenceType.Inproceedings,
			author = { "Jonathan Hare", "Sina Samangooei", "David Dupplaw" },
			title = "Test::StaticMethod",
			year = "2012"
	)
    public static void main( String[] args ) {
		new App().method();
    }
}
