package orgrdfs.sioc.ns;

import java.util.List;

/**
 * An area in which content Items are contained.
 */
@SuppressWarnings("javadoc")
public interface Container
{
	public List<orgrdfs.sioc.ns.UserAccount> getHas_subscriber();

	public void setHas_subscriber(final List<orgrdfs.sioc.ns.UserAccount> has_subscriber);

	public List<orgrdfs.sioc.ns.Container> getParent_of();

	public void setParent_of(final List<orgrdfs.sioc.ns.Container> parent_of);

	public List<java.lang.Integer> getNum_items();

	public void setNum_items(final List<java.lang.Integer> num_items);

	public List<java.lang.String> getLast_item_date();

	public void setLast_item_date(final List<java.lang.String> last_item_date);

	public List<orgrdfs.sioc.ns.Item> getContainer_of();

	public void setContainer_of(final List<orgrdfs.sioc.ns.Item> container_of);

	public List<orgrdfs.sioc.ns.Container> getHas_parent();

	public void setHas_parent(final List<orgrdfs.sioc.ns.Container> has_parent);

	public String getURI();

}
