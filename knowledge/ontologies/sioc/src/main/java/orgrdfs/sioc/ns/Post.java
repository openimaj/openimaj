package orgrdfs.sioc.ns;

import comxmlns.foaf._0.*;
import java.util.List;

/**
  * An article or message that can be posted to a Forum.
 */
public interface Post 
	extends Item, Document
{
	public List<String> getReference();
	
	public void setReference( final List<String> reference );

	public List<java.lang.String> getCreated_at();
	
	public void setCreated_at( final List<java.lang.String> created_at );

	public List<java.lang.String> getDescription();
	
	public void setDescription( final List<java.lang.String> description );

	public List<java.lang.String> getContent_encoded();
	
	public void setContent_encoded( final List<java.lang.String> content_encoded );

	public List<java.lang.String> getTitle();
	
	public void setTitle( final List<java.lang.String> title );

	public List<java.lang.String> getSubject();
	
	public void setSubject( final List<java.lang.String> subject );

	public List<java.lang.String> getModified_at();
	
	public void setModified_at( final List<java.lang.String> modified_at );

	public String getURI();

}

