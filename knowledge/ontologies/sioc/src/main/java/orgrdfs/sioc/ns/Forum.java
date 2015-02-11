package orgrdfs.sioc.ns;

import java.util.List;

/**
 * A discussion area on which Posts or entries are made.
 */
@SuppressWarnings("javadoc")
public interface Forum
		extends Container
{
	public List<orgrdfs.sioc.ns.UserAccount> getHas_moderator();

	public void setHas_moderator(final List<orgrdfs.sioc.ns.UserAccount> has_moderator);

	public List<orgrdfs.sioc.ns.Site> getHas_host();

	public void setHas_host(final List<orgrdfs.sioc.ns.Site> has_host);

	public List<java.lang.Integer> getNum_threads();

	public void setNum_threads(final List<java.lang.Integer> num_threads);

	@Override
	public String getURI();

}
