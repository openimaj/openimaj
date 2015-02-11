package orgrdfs.sioc.ns;

import java.util.List;

/**
 * A set of UserAccounts whose owners have a common purpose or interest. Can be
 * used for access control purposes.
 */
@SuppressWarnings("javadoc")
public interface Usergroup
{
	public List<orgrdfs.sioc.ns.UserAccount> getHas_member();

	public void setHas_member(final List<orgrdfs.sioc.ns.UserAccount> has_member);

	public List<orgrdfs.sioc.ns.Space> getUsergroup_of();

	public void setUsergroup_of(final List<orgrdfs.sioc.ns.Space> usergroup_of);

	public String getURI();

}
