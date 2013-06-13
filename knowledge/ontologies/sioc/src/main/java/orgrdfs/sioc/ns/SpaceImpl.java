package orgrdfs.sioc.ns;

import org.openimaj.rdf.owl2java.Something;
import org.openimaj.rdf.serialize.Predicate;

import org.openimaj.rdf.serialize.RDFType;

import java.util.ArrayList;
import java.util.List;

/**
 * A Space is a place where data resides, e.g. on a website, desktop, fileshare,
 * etc.
 */
@RDFType("http://rdfs.org/sioc/ns#Space")
public class SpaceImpl extends Something implements Space
{

	/** A resource which belongs to this data Space. */
	@Predicate("http://rdfs.org/sioc/ns#space_of")
	public List<String> space_of = new ArrayList<String>();


	/** Points to a Usergroup that has certain access to this Space. */
	@Predicate("http://rdfs.org/sioc/ns#has_usergroup")
	public List<orgrdfs.sioc.ns.Usergroup> has_usergroup = new ArrayList<orgrdfs.sioc.ns.Usergroup>();



	// From class this


	@Override
	public List<String> getSpace_of()
	{
		return this.space_of;
	}
	
	@Override
	public void setSpace_of( final List<String> space_of )
	{
		this.space_of = space_of;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Usergroup> getHas_usergroup()
	{
		return this.has_usergroup;
	}
	
	@Override
	public void setHas_usergroup( final List<orgrdfs.sioc.ns.Usergroup> has_usergroup )
	{
		this.has_usergroup = has_usergroup;
	}

}

