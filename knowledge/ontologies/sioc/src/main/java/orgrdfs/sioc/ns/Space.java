package orgrdfs.sioc.ns;

import java.util.List;

/**
 * A Space is a place where data resides, e.g. on a website, desktop, fileshare,
 * etc.
 */
@SuppressWarnings("javadoc")
public interface Space
{
	public List<String> getSpace_of();

	public void setSpace_of(final List<String> space_of);

	public List<orgrdfs.sioc.ns.Usergroup> getHas_usergroup();

	public void setHas_usergroup(final List<orgrdfs.sioc.ns.Usergroup> has_usergroup);

	public String getURI();

}
