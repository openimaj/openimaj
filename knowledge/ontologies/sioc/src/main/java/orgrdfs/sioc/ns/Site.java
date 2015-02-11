package orgrdfs.sioc.ns;

import java.util.List;

/**
 * A Site can be the location of an online community or set of communities, with
 * UserAccounts and Usergroups creating Items in a set of Containers. It can be
 * thought of as a web-accessible data Space.
 */
@SuppressWarnings("javadoc")
public interface Site
		extends Space
{
	public List<orgrdfs.sioc.ns.Forum> getHost_of();

	public void setHost_of(final List<orgrdfs.sioc.ns.Forum> host_of);

	public List<orgrdfs.sioc.ns.UserAccount> getHas_administrator();

	public void setHas_administrator(final List<orgrdfs.sioc.ns.UserAccount> has_administrator);

	@Override
	public String getURI();

}
