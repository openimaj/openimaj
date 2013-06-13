package orgrdfs.sioc.ns;

import org.openimaj.rdf.owl2java.Something;
import org.openimaj.rdf.serialize.Predicate;

import org.openimaj.rdf.serialize.RDFType;

import java.util.ArrayList;
import java.util.List;

/**
  * An Item is something which can be in a Container.
 */
@RDFType("http://rdfs.org/sioc/ns#Item")
public class ItemImpl extends Something implements Item
{

	/** Refers to who (e.g. a UserAccount, e-mail address, etc.) a particular
	    Item is addressed to. */
	@Predicate("http://rdfs.org/sioc/ns#addressed_to")
	public List<String> addressed_to = new ArrayList<String>();


	/** The URI of a file attached to an Item. */
	@Predicate("http://rdfs.org/sioc/ns#attachment")
	public List<String> attachment = new ArrayList<String>();


	/** Links to the next revision of this Item or Post. */
	@Predicate("http://rdfs.org/sioc/ns#next_version")
	public List<orgrdfs.sioc.ns.Item> next_version = new ArrayList<orgrdfs.sioc.ns.Item>();


	/** Specifies that this Item is about a particular resource, e.g. a Post
	    describing a book, hotel, etc. */
	@Predicate("http://rdfs.org/sioc/ns#about")
	public List<String> about = new ArrayList<String>();


	/** A UserAccount that modified this Item. */
	@Predicate("http://rdfs.org/sioc/ns#has_modifier")
	public List<orgrdfs.sioc.ns.UserAccount> has_modifier = new ArrayList<orgrdfs.sioc.ns.UserAccount>();


	/** The IP address used when creating this Item. This can be associated
	    with a creator. Some wiki articles list the IP addresses for the
	    creator or modifiers when the usernames are absent. */
	@Predicate("http://rdfs.org/sioc/ns#ip_address")
	public List<java.lang.String> ip_address = new ArrayList<java.lang.String>();


	/** The Container to which this Item belongs. */
	@Predicate("http://rdfs.org/sioc/ns#has_container")
	public List<orgrdfs.sioc.ns.Container> has_container = new ArrayList<orgrdfs.sioc.ns.Container>();


	/** Next Item or Post in a given Container sorted by date. */
	@Predicate("http://rdfs.org/sioc/ns#next_by_date")
	public List<orgrdfs.sioc.ns.Item> next_by_date = new ArrayList<orgrdfs.sioc.ns.Item>();


	/** Links to an Item or Post which this Item or Post is a reply to. */
	@Predicate("http://rdfs.org/sioc/ns#reply_of")
	public List<orgrdfs.sioc.ns.Item> reply_of = new ArrayList<orgrdfs.sioc.ns.Item>();


	/** Previous Item or Post in a given Container sorted by date. */
	@Predicate("http://rdfs.org/sioc/ns#previous_by_date")
	public List<orgrdfs.sioc.ns.Item> previous_by_date = new ArrayList<orgrdfs.sioc.ns.Item>();


	/** This links Items to embedded statements, facts and structured content. */
	@Predicate("http://rdfs.org/sioc/ns#embeds_knowledge")
	public List<org.w3._2004._03.trix.rdfg_1.Graph> embeds_knowledge = new ArrayList<org.w3._2004._03.trix.rdfg_1.Graph>();


	/** Points to an Item or Post that is a reply or response to this Item or Post. */
	@Predicate("http://rdfs.org/sioc/ns#has_reply")
	public List<orgrdfs.sioc.ns.Item> has_reply = new ArrayList<orgrdfs.sioc.ns.Item>();


	/** Links to the latest revision of this Item or Post. */
	@Predicate("http://rdfs.org/sioc/ns#latest_version")
	public List<orgrdfs.sioc.ns.Item> latest_version = new ArrayList<orgrdfs.sioc.ns.Item>();


	/** The content of the Item in plain text format. */
	@Predicate("http://rdfs.org/sioc/ns#content")
	public List<java.lang.String> content = new ArrayList<java.lang.String>();


	/** The discussion that is related to this Item. */
	@Predicate("http://rdfs.org/sioc/ns#has_discussion")
	public List<String> has_discussion = new ArrayList<String>();


	/** Links to the previous revision of this Item or Post. */
	@Predicate("http://rdfs.org/sioc/ns#previous_version")
	public List<orgrdfs.sioc.ns.Item> previous_version = new ArrayList<orgrdfs.sioc.ns.Item>();



	// From class this


	@Override
	public List<String> getAddressed_to()
	{
		return this.addressed_to;
	}
	
	@Override
	public void setAddressed_to( final List<String> addressed_to )
	{
		this.addressed_to = addressed_to;
	}


	// From class this


	@Override
	public List<String> getAttachment()
	{
		return this.attachment;
	}
	
	@Override
	public void setAttachment( final List<String> attachment )
	{
		this.attachment = attachment;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Item> getNext_version()
	{
		return this.next_version;
	}
	
	@Override
	public void setNext_version( final List<orgrdfs.sioc.ns.Item> next_version )
	{
		this.next_version = next_version;
	}


	// From class this


	@Override
	public List<String> getAbout()
	{
		return this.about;
	}
	
	@Override
	public void setAbout( final List<String> about )
	{
		this.about = about;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.UserAccount> getHas_modifier()
	{
		return this.has_modifier;
	}
	
	@Override
	public void setHas_modifier( final List<orgrdfs.sioc.ns.UserAccount> has_modifier )
	{
		this.has_modifier = has_modifier;
	}


	// From class this


	@Override
	public List<java.lang.String> getIp_address()
	{
		return this.ip_address;
	}
	
	@Override
	public void setIp_address( final List<java.lang.String> ip_address )
	{
		this.ip_address = ip_address;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Container> getHas_container()
	{
		return this.has_container;
	}
	
	@Override
	public void setHas_container( final List<orgrdfs.sioc.ns.Container> has_container )
	{
		this.has_container = has_container;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Item> getNext_by_date()
	{
		return this.next_by_date;
	}
	
	@Override
	public void setNext_by_date( final List<orgrdfs.sioc.ns.Item> next_by_date )
	{
		this.next_by_date = next_by_date;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Item> getReply_of()
	{
		return this.reply_of;
	}
	
	@Override
	public void setReply_of( final List<orgrdfs.sioc.ns.Item> reply_of )
	{
		this.reply_of = reply_of;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Item> getPrevious_by_date()
	{
		return this.previous_by_date;
	}
	
	@Override
	public void setPrevious_by_date( final List<orgrdfs.sioc.ns.Item> previous_by_date )
	{
		this.previous_by_date = previous_by_date;
	}


	// From class this


	@Override
	public List<org.w3._2004._03.trix.rdfg_1.Graph> getEmbeds_knowledge()
	{
		return this.embeds_knowledge;
	}
	
	@Override
	public void setEmbeds_knowledge( final List<org.w3._2004._03.trix.rdfg_1.Graph> embeds_knowledge )
	{
		this.embeds_knowledge = embeds_knowledge;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Item> getHas_reply()
	{
		return this.has_reply;
	}
	
	@Override
	public void setHas_reply( final List<orgrdfs.sioc.ns.Item> has_reply )
	{
		this.has_reply = has_reply;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Item> getLatest_version()
	{
		return this.latest_version;
	}
	
	@Override
	public void setLatest_version( final List<orgrdfs.sioc.ns.Item> latest_version )
	{
		this.latest_version = latest_version;
	}


	// From class this


	@Override
	public List<java.lang.String> getContent()
	{
		return this.content;
	}
	
	@Override
	public void setContent( final List<java.lang.String> content )
	{
		this.content = content;
	}


	// From class this


	@Override
	public List<String> getHas_discussion()
	{
		return this.has_discussion;
	}
	
	@Override
	public void setHas_discussion( final List<String> has_discussion )
	{
		this.has_discussion = has_discussion;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Item> getPrevious_version()
	{
		return this.previous_version;
	}
	
	@Override
	public void setPrevious_version( final List<orgrdfs.sioc.ns.Item> previous_version )
	{
		this.previous_version = previous_version;
	}

}

