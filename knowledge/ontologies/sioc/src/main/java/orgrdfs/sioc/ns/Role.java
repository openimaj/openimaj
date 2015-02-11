package orgrdfs.sioc.ns;

import java.util.List;

/**
 * A Role is a function of a UserAccount within a scope of a particular Forum,
 * Site, etc.
 */
@SuppressWarnings("javadoc")
public interface Role
{
	public List<String> getFunction_of();

	public void setFunction_of(final List<String> function_of);

	public List<String> getHas_scope();

	public void setHas_scope(final List<String> has_scope);

	public String getURI();

}
