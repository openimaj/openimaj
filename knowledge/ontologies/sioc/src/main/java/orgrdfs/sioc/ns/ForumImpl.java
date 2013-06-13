package orgrdfs.sioc.ns;

import org.openimaj.rdf.owl2java.Something;
import org.openimaj.rdf.serialize.Predicate;

import org.openimaj.rdf.serialize.RDFType;

import java.util.ArrayList;
import java.util.List;

/**
  * A discussion area on which Posts or entries are made.
 */
@RDFType("http://rdfs.org/sioc/ns#Forum")
public class ForumImpl extends Something implements Forum
{

	/** A UserAccount that is a moderator of this Forum. */
	@Predicate("http://rdfs.org/sioc/ns#has_moderator")
	public List<orgrdfs.sioc.ns.UserAccount> has_moderator = new ArrayList<orgrdfs.sioc.ns.UserAccount>();


	/** The Site that hosts this Forum. */
	@Predicate("http://rdfs.org/sioc/ns#has_host")
	public List<orgrdfs.sioc.ns.Site> has_host = new ArrayList<orgrdfs.sioc.ns.Site>();


	/** The number of Threads (AKA discussion topics) in a Forum. */
	@Predicate("http://rdfs.org/sioc/ns#num_threads")
	public List<java.lang.Integer> num_threads = new ArrayList<java.lang.Integer>();


	/** orgrdfs.sioc.ns.Container superclass instance */
	private orgrdfs.sioc.ns.Container container;


	// From class container


	@Override
	public List<orgrdfs.sioc.ns.UserAccount> getHas_subscriber()
	{
		return container.getHas_subscriber();
	}
	
	@Override
	public void setHas_subscriber( final List<orgrdfs.sioc.ns.UserAccount> has_subscriber )
	{
		container.setHas_subscriber( has_subscriber );
	}


	// From class container


	@Override
	public List<java.lang.Integer> getNum_items()
	{
		return container.getNum_items();
	}
	
	@Override
	public void setNum_items( final List<java.lang.Integer> num_items )
	{
		container.setNum_items( num_items );
	}


	// From class container


	@Override
	public List<orgrdfs.sioc.ns.Container> getParent_of()
	{
		return container.getParent_of();
	}
	
	@Override
	public void setParent_of( final List<orgrdfs.sioc.ns.Container> parent_of )
	{
		container.setParent_of( parent_of );
	}


	// From class container


	@Override
	public List<orgrdfs.sioc.ns.Item> getContainer_of()
	{
		return container.getContainer_of();
	}
	
	@Override
	public void setContainer_of( final List<orgrdfs.sioc.ns.Item> container_of )
	{
		container.setContainer_of( container_of );
	}


	// From class container


	@Override
	public List<java.lang.String> getLast_item_date()
	{
		return container.getLast_item_date();
	}
	
	@Override
	public void setLast_item_date( final List<java.lang.String> last_item_date )
	{
		container.setLast_item_date( last_item_date );
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Site> getHas_host()
	{
		return this.has_host;
	}
	
	@Override
	public void setHas_host( final List<orgrdfs.sioc.ns.Site> has_host )
	{
		this.has_host = has_host;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.UserAccount> getHas_moderator()
	{
		return this.has_moderator;
	}
	
	@Override
	public void setHas_moderator( final List<orgrdfs.sioc.ns.UserAccount> has_moderator )
	{
		this.has_moderator = has_moderator;
	}


	// From class container


	@Override
	public List<orgrdfs.sioc.ns.Container> getHas_parent()
	{
		return container.getHas_parent();
	}
	
	@Override
	public void setHas_parent( final List<orgrdfs.sioc.ns.Container> has_parent )
	{
		container.setHas_parent( has_parent );
	}


	// From class this


	@Override
	public List<java.lang.Integer> getNum_threads()
	{
		return this.num_threads;
	}
	
	@Override
	public void setNum_threads( final List<java.lang.Integer> num_threads )
	{
		this.num_threads = num_threads;
	}

}

