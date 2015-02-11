package orgrdfs.sioc.ns;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.rdf.owl2java.Something;
import org.openimaj.rdf.serialize.Predicate;
import org.openimaj.rdf.serialize.RDFType;

/**
 * An article or message that can be posted to a Forum.
 */
@RDFType("http://rdfs.org/sioc/ns#Post")
public class PostImpl extends Something implements Post
{

	/**
	 * Links either created explicitly or extracted implicitly on the HTML level
	 * from the Post.
	 */
	@Predicate("http://rdfs.org/sioc/ns#reference")
	public List<String> reference = new ArrayList<String>();

	/** When this was created, in ISO 8601 format. */
	@Predicate("http://rdfs.org/sioc/ns#created_at")
	public List<java.lang.String> created_at = new ArrayList<java.lang.String>();

	/** The content of the Post. */
	@Predicate("http://rdfs.org/sioc/ns#description")
	public List<java.lang.String> description = new ArrayList<java.lang.String>();

	/** The encoded content of the Post, contained in CDATA areas. */
	@Predicate("http://rdfs.org/sioc/ns#content_encoded")
	public List<java.lang.String> content_encoded = new ArrayList<java.lang.String>();

	/**
	 * This is the title (subject line) of the Post. Note that for a Post within
	 * a threaded discussion that has no parents, it would detail the topic
	 * thread.
	 */
	@Predicate("http://rdfs.org/sioc/ns#title")
	public List<java.lang.String> title = new ArrayList<java.lang.String>();

	/** Keyword(s) describing subject of the Post. */
	@Predicate("http://rdfs.org/sioc/ns#subject")
	public List<java.lang.String> subject = new ArrayList<java.lang.String>();

	/** When this was modified, in ISO 8601 format. */
	@Predicate("http://rdfs.org/sioc/ns#modified_at")
	public List<java.lang.String> modified_at = new ArrayList<java.lang.String>();

	/** comxmlns.foaf._0.Document superclass instance */
	// private comxmlns.foaf._0.Document document;

	/** orgrdfs.sioc.ns.Item superclass instance */
	private orgrdfs.sioc.ns.Item item;

	// From class item

	@Override
	public List<String> getAddressed_to()
	{
		return item.getAddressed_to();
	}

	@Override
	public void setAddressed_to(final List<String> addressed_to)
	{
		item.setAddressed_to(addressed_to);
	}

	// From class item

	@Override
	public List<String> getAttachment()
	{
		return item.getAttachment();
	}

	@Override
	public void setAttachment(final List<String> attachment)
	{
		item.setAttachment(attachment);
	}

	// From class item

	@Override
	public List<orgrdfs.sioc.ns.Item> getNext_version()
	{
		return item.getNext_version();
	}

	@Override
	public void setNext_version(final List<orgrdfs.sioc.ns.Item> next_version)
	{
		item.setNext_version(next_version);
	}

	// From class this

	@Override
	public List<java.lang.String> getCreated_at()
	{
		return this.created_at;
	}

	@Override
	public void setCreated_at(final List<java.lang.String> created_at)
	{
		this.created_at = created_at;
	}

	// From class item

	@Override
	public List<String> getAbout()
	{
		return item.getAbout();
	}

	@Override
	public void setAbout(final List<String> about)
	{
		item.setAbout(about);
	}

	// From class item

	@Override
	public List<orgrdfs.sioc.ns.UserAccount> getHas_modifier()
	{
		return item.getHas_modifier();
	}

	@Override
	public void setHas_modifier(final List<orgrdfs.sioc.ns.UserAccount> has_modifier)
	{
		item.setHas_modifier(has_modifier);
	}

	// From class item

	@Override
	public List<java.lang.String> getIp_address()
	{
		return item.getIp_address();
	}

	@Override
	public void setIp_address(final List<java.lang.String> ip_address)
	{
		item.setIp_address(ip_address);
	}

	// From class item

	@Override
	public List<orgrdfs.sioc.ns.Container> getHas_container()
	{
		return item.getHas_container();
	}

	@Override
	public void setHas_container(final List<orgrdfs.sioc.ns.Container> has_container)
	{
		item.setHas_container(has_container);
	}

	// From class this

	@Override
	public List<java.lang.String> getDescription()
	{
		return this.description;
	}

	@Override
	public void setDescription(final List<java.lang.String> description)
	{
		this.description = description;
	}

	// From class this

	@Override
	public List<java.lang.String> getContent_encoded()
	{
		return this.content_encoded;
	}

	@Override
	public void setContent_encoded(final List<java.lang.String> content_encoded)
	{
		this.content_encoded = content_encoded;
	}

	// From class this

	@Override
	public List<java.lang.String> getTitle()
	{
		return this.title;
	}

	@Override
	public void setTitle(final List<java.lang.String> title)
	{
		this.title = title;
	}

	// From class item

	@Override
	public List<orgrdfs.sioc.ns.Item> getNext_by_date()
	{
		return item.getNext_by_date();
	}

	@Override
	public void setNext_by_date(final List<orgrdfs.sioc.ns.Item> next_by_date)
	{
		item.setNext_by_date(next_by_date);
	}

	// From class this

	@Override
	public List<java.lang.String> getSubject()
	{
		return this.subject;
	}

	@Override
	public void setSubject(final List<java.lang.String> subject)
	{
		this.subject = subject;
	}

	// From class item

	@Override
	public List<orgrdfs.sioc.ns.Item> getPrevious_by_date()
	{
		return item.getPrevious_by_date();
	}

	@Override
	public void setPrevious_by_date(final List<orgrdfs.sioc.ns.Item> previous_by_date)
	{
		item.setPrevious_by_date(previous_by_date);
	}

	// From class item

	@Override
	public List<orgrdfs.sioc.ns.Item> getReply_of()
	{
		return item.getReply_of();
	}

	@Override
	public void setReply_of(final List<orgrdfs.sioc.ns.Item> reply_of)
	{
		item.setReply_of(reply_of);
	}

	// From class item

	@Override
	public List<orgrdfs.sioc.ns.Item> getLatest_version()
	{
		return item.getLatest_version();
	}

	@Override
	public void setLatest_version(final List<orgrdfs.sioc.ns.Item> latest_version)
	{
		item.setLatest_version(latest_version);
	}

	// From class item

	@Override
	public List<orgrdfs.sioc.ns.Item> getHas_reply()
	{
		return item.getHas_reply();
	}

	@Override
	public void setHas_reply(final List<orgrdfs.sioc.ns.Item> has_reply)
	{
		item.setHas_reply(has_reply);
	}

	// From class item

	@Override
	public List<org.w3._2004._03.trix.rdfg_1.Graph> getEmbeds_knowledge()
	{
		return item.getEmbeds_knowledge();
	}

	@Override
	public void setEmbeds_knowledge(final List<org.w3._2004._03.trix.rdfg_1.Graph> embeds_knowledge)
	{
		item.setEmbeds_knowledge(embeds_knowledge);
	}

	// From class this

	@Override
	public List<String> getReference()
	{
		return this.reference;
	}

	@Override
	public void setReference(final List<String> reference)
	{
		this.reference = reference;
	}

	// From class item

	@Override
	public List<java.lang.String> getContent()
	{
		return item.getContent();
	}

	@Override
	public void setContent(final List<java.lang.String> content)
	{
		item.setContent(content);
	}

	// From class item

	@Override
	public List<String> getHas_discussion()
	{
		return item.getHas_discussion();
	}

	@Override
	public void setHas_discussion(final List<String> has_discussion)
	{
		item.setHas_discussion(has_discussion);
	}

	// From class this

	@Override
	public List<java.lang.String> getModified_at()
	{
		return this.modified_at;
	}

	@Override
	public void setModified_at(final List<java.lang.String> modified_at)
	{
		this.modified_at = modified_at;
	}

	// From class item

	@Override
	public List<orgrdfs.sioc.ns.Item> getPrevious_version()
	{
		return item.getPrevious_version();
	}

	@Override
	public void setPrevious_version(final List<orgrdfs.sioc.ns.Item> previous_version)
	{
		item.setPrevious_version(previous_version);
	}

}
