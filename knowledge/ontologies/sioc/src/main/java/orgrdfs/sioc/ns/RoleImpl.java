package orgrdfs.sioc.ns;

import org.openimaj.rdf.owl2java.Something;
import org.openimaj.rdf.serialize.Predicate;

import org.openimaj.rdf.serialize.RDFType;

import java.util.ArrayList;
import java.util.List;

/**
 * A Role is a function of a UserAccount within a scope of a particular Forum,
 * Site, etc.
 */
@RDFType("http://rdfs.org/sioc/ns#Role")
public class RoleImpl extends Something implements Role
{

	/** A UserAccount that has this Role. */
	@Predicate("http://rdfs.org/sioc/ns#function_of")
	public List<String> function_of = new ArrayList<String>();


	/** A resource that this Role applies to. */
	@Predicate("http://rdfs.org/sioc/ns#has_scope")
	public List<String> has_scope = new ArrayList<String>();



	// From class this


	@Override
	public List<String> getFunction_of()
	{
		return this.function_of;
	}
	
	@Override
	public void setFunction_of( final List<String> function_of )
	{
		this.function_of = function_of;
	}


	// From class this


	@Override
	public List<String> getHas_scope()
	{
		return this.has_scope;
	}
	
	@Override
	public void setHas_scope( final List<String> has_scope )
	{
		this.has_scope = has_scope;
	}

}

