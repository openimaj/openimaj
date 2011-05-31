/**
 * 
 */
package org.openimaj.tools.faces;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 30 May 2011
 */
public class FaceToolOptions
{
	@Option(name="-d",aliases="--display",metaVar="Display results")
	public boolean displayResults = false;
	
	/** The input files that are to be input into the face tools */
	@Argument( required=true, metaVar="Image Files" )
	public List<File> inputFiles = new ArrayList<File>();
}
