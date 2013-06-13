package orgrdfs.sioc.ns;

import org.openimaj.rdf.owl2java.Something;
import org.openimaj.rdf.serialize.Predicate;

import org.openimaj.rdf.serialize.RDFType;

import java.util.ArrayList;
import java.util.List;

/**
  * An area in which content Items are contained.
 */
@RDFType("http://rdfs.org/sioc/ns#Container")
public class ContainerImpl extends Something implements Container
{

	/** A UserAccount that is subscribed to this Container. */
	@Predicate("http://rdfs.org/sioc/ns#has_subscriber")
	public List<orgrdfs.sioc.ns.UserAccount> has_subscriber = new ArrayList<orgrdfs.sioc.ns.UserAccount>();


	/** A child Container or Forum that this Container or Forum is a parent of. */
	@Predicate("http://rdfs.org/sioc/ns#parent_of")
	public List<orgrdfs.sioc.ns.Container> parent_of = new ArrayList<orgrdfs.sioc.ns.Container>();


	/** The number of Posts (or Items) in a Forum (or a Container). */
	@Predicate("http://rdfs.org/sioc/ns#num_items")
	public List<java.lang.Integer> num_items = new ArrayList<java.lang.Integer>();


	/** The date and time of the last Post (or Item) in a Forum (or a
	    Container), in ISO 8601 format. */
	@Predicate("http://rdfs.org/sioc/ns#last_item_date")
	public List<java.lang.String> last_item_date = new ArrayList<java.lang.String>();


	/** An Item that this Container contains. */
	@Predicate("http://rdfs.org/sioc/ns#container_of")
	public List<orgrdfs.sioc.ns.Item> container_of = new ArrayList<orgrdfs.sioc.ns.Item>();


	/** A Container or Forum that this Container or Forum is a child of. */
	@Predicate("http://rdfs.org/sioc/ns#has_parent")
	public List<orgrdfs.sioc.ns.Container> has_parent = new ArrayList<orgrdfs.sioc.ns.Container>();



	// From class this


	@Override
	public List<orgrdfs.sioc.ns.UserAccount> getHas_subscriber()
	{
		return this.has_subscriber;
	}
	
	@Override
	public void setHas_subscriber( final List<orgrdfs.sioc.ns.UserAccount> has_subscriber )
	{
		this.has_subscriber = has_subscriber;
	}


	// From class this


	@Override
	public List<java.lang.Integer> getNum_items()
	{
		return this.num_items;
	}
	
	@Override
	public void setNum_items( final List<java.lang.Integer> num_items )
	{
		this.num_items = num_items;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Container> getParent_of()
	{
		return this.parent_of;
	}
	
	@Override
	public void setParent_of( final List<orgrdfs.sioc.ns.Container> parent_of )
	{
		this.parent_of = parent_of;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Item> getContainer_of()
	{
		return this.container_of;
	}
	
	@Override
	public void setContainer_of( final List<orgrdfs.sioc.ns.Item> container_of )
	{
		this.container_of = container_of;
	}


	// From class this


	@Override
	public List<java.lang.String> getLast_item_date()
	{
		return this.last_item_date;
	}
	
	@Override
	public void setLast_item_date( final List<java.lang.String> last_item_date )
	{
		this.last_item_date = last_item_date;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Container> getHas_parent()
	{
		return this.has_parent;
	}
	
	@Override
	public void setHas_parent( final List<orgrdfs.sioc.ns.Container> has_parent )
	{
		this.has_parent = has_parent;
	}

}

