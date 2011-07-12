/**
 * 
 */
package org.openimaj.hardware.gps;

import java.util.HashMap;
import org.joda.time.DateTime;
import org.openimaj.hardware.gps.NMEASentenceType;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 12 Jul 2011
 */
public class NMEAMessage extends HashMap<String,Object>
{
    private static final long serialVersionUID = 1L;
    
	public String string = null;
	public DateTime timestamp = null;
	public String deviceIdentifier = null;
	public NMEASentenceType sentenceType = null;
	public String checksum = null;
}
