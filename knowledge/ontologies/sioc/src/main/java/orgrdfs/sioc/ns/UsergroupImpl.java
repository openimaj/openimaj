package orgrdfs.sioc.ns;

import org.openimaj.rdf.owl2java.Something;
import org.openimaj.rdf.serialize.Predicate;

import org.openimaj.rdf.serialize.RDFType;

import java.util.ArrayList;
import java.util.List;

/**
 * A set of UserAccounts whose owners have a common purpose or interest. Can be
 * used for access control purposes.
 */
@RDFType("http://rdfs.org/sioc/ns#Usergroup")
public class UsergroupImpl extends Something implements Usergroup
{

	/** A UserAccount that is a member of this Usergroup. */
	@Predicate("http://rdfs.org/sioc/ns#has_member")
	public List<orgrdfs.sioc.ns.UserAccount> has_member = new ArrayList<orgrdfs.sioc.ns.UserAccount>();


	/** A Space that the Usergroup has access to. */
	@Predicate("http://rdfs.org/sioc/ns#usergroup_of")
	public List<orgrdfs.sioc.ns.Space> usergroup_of = new ArrayList<orgrdfs.sioc.ns.Space>();



	// From class this


	@Override
	public List<orgrdfs.sioc.ns.UserAccount> getHas_member()
	{
		return this.has_member;
	}
	
	@Override
	public void setHas_member( final List<orgrdfs.sioc.ns.UserAccount> has_member )
	{
		this.has_member = has_member;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Space> getUsergroup_of()
	{
		return this.usergroup_of;
	}
	
	@Override
	public void setUsergroup_of( final List<orgrdfs.sioc.ns.Space> usergroup_of )
	{
		this.usergroup_of = usergroup_of;
	}

}

