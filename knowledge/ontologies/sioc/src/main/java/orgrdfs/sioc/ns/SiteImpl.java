package orgrdfs.sioc.ns;

import org.openimaj.rdf.owl2java.Something;
import org.openimaj.rdf.serialize.Predicate;

import org.openimaj.rdf.serialize.RDFType;

import java.util.ArrayList;
import java.util.List;

/**
 * A Site can be the location of an online community or set of communities, with
 * UserAccounts and Usergroups creating Items in a set of Containers. It can be
 * thought of as a web-accessible data Space.
 */
@RDFType("http://rdfs.org/sioc/ns#Site")
public class SiteImpl extends Something implements Site
{

	/** A Forum that is hosted on this Site. */
	@Predicate("http://rdfs.org/sioc/ns#host_of")
	public List<orgrdfs.sioc.ns.Forum> host_of = new ArrayList<orgrdfs.sioc.ns.Forum>();


	/** A UserAccount that is an administrator of this Site. */
	@Predicate("http://rdfs.org/sioc/ns#has_administrator")
	public List<orgrdfs.sioc.ns.UserAccount> has_administrator = new ArrayList<orgrdfs.sioc.ns.UserAccount>();


	/** orgrdfs.sioc.ns.Space superclass instance */
	private orgrdfs.sioc.ns.Space space;


	// From class space


	@Override
	public List<String> getSpace_of()
	{
		return space.getSpace_of();
	}
	
	@Override
	public void setSpace_of( final List<String> space_of )
	{
		space.setSpace_of( space_of );
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Forum> getHost_of()
	{
		return this.host_of;
	}
	
	@Override
	public void setHost_of( final List<orgrdfs.sioc.ns.Forum> host_of )
	{
		this.host_of = host_of;
	}


	// From class space


	@Override
	public List<orgrdfs.sioc.ns.Usergroup> getHas_usergroup()
	{
		return space.getHas_usergroup();
	}
	
	@Override
	public void setHas_usergroup( final List<orgrdfs.sioc.ns.Usergroup> has_usergroup )
	{
		space.setHas_usergroup( has_usergroup );
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.UserAccount> getHas_administrator()
	{
		return this.has_administrator;
	}
	
	@Override
	public void setHas_administrator( final List<orgrdfs.sioc.ns.UserAccount> has_administrator )
	{
		this.has_administrator = has_administrator;
	}

}

