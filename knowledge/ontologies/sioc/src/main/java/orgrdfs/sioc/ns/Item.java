package orgrdfs.sioc.ns;

import java.util.List;

/**
 * An Item is something which can be in a Container.
 */
@SuppressWarnings("javadoc")
public interface Item
{
	public List<String> getAddressed_to();

	public void setAddressed_to(final List<String> addressed_to);

	public List<String> getAttachment();

	public void setAttachment(final List<String> attachment);

	public List<orgrdfs.sioc.ns.Item> getNext_version();

	public void setNext_version(final List<orgrdfs.sioc.ns.Item> next_version);

	public List<String> getAbout();

	public void setAbout(final List<String> about);

	public List<orgrdfs.sioc.ns.UserAccount> getHas_modifier();

	public void setHas_modifier(final List<orgrdfs.sioc.ns.UserAccount> has_modifier);

	public List<java.lang.String> getIp_address();

	public void setIp_address(final List<java.lang.String> ip_address);

	public List<orgrdfs.sioc.ns.Container> getHas_container();

	public void setHas_container(final List<orgrdfs.sioc.ns.Container> has_container);

	public List<orgrdfs.sioc.ns.Item> getNext_by_date();

	public void setNext_by_date(final List<orgrdfs.sioc.ns.Item> next_by_date);

	public List<orgrdfs.sioc.ns.Item> getReply_of();

	public void setReply_of(final List<orgrdfs.sioc.ns.Item> reply_of);

	public List<orgrdfs.sioc.ns.Item> getPrevious_by_date();

	public void setPrevious_by_date(final List<orgrdfs.sioc.ns.Item> previous_by_date);

	public List<org.w3._2004._03.trix.rdfg_1.Graph> getEmbeds_knowledge();

	public void setEmbeds_knowledge(final List<org.w3._2004._03.trix.rdfg_1.Graph> embeds_knowledge);

	public List<orgrdfs.sioc.ns.Item> getHas_reply();

	public void setHas_reply(final List<orgrdfs.sioc.ns.Item> has_reply);

	public List<orgrdfs.sioc.ns.Item> getLatest_version();

	public void setLatest_version(final List<orgrdfs.sioc.ns.Item> latest_version);

	public List<java.lang.String> getContent();

	public void setContent(final List<java.lang.String> content);

	public List<String> getHas_discussion();

	public void setHas_discussion(final List<String> has_discussion);

	public List<orgrdfs.sioc.ns.Item> getPrevious_version();

	public void setPrevious_version(final List<orgrdfs.sioc.ns.Item> previous_version);

	public String getURI();

}
