/**
 * 
 */
package org.openimaj.tools.faces;

import org.kohsuke.args4j.Option;

/**
 * 	Options specific to the face detector tool.
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 30 May 2011
 */
public class FaceDetectorToolOptions extends FaceToolOptions
{
	@Option(name="-m",aliases="--minSize",metaVar="VAL",usage="Minimum Face Size allowable")
	public int minSize = 80;
}
