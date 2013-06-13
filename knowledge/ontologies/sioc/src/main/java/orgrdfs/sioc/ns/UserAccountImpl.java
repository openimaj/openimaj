package orgrdfs.sioc.ns;

import org.openimaj.rdf.owl2java.Something;
import org.openimaj.rdf.serialize.Predicate;

import org.openimaj.rdf.serialize.RDFType;

import java.util.ArrayList;
import java.util.List;

/**
  * A user account in an online community site.
 */
@RDFType("http://rdfs.org/sioc/ns#UserAccount")
public class UserAccountImpl extends Something implements UserAccount
{

	/** First (real) name of this User. Synonyms include given name or christian name. */
	@Predicate("http://rdfs.org/sioc/ns#first_name")
	public List<java.lang.String> first_name = new ArrayList<java.lang.String>();


	/** A resource that the UserAccount is a creator of. */
	@Predicate("http://rdfs.org/sioc/ns#creator_of")
	public List<String> creator_of = new ArrayList<String>();


	/** An Item that this UserAccount has modified. */
	@Predicate("http://rdfs.org/sioc/ns#modifier_of")
	public List<orgrdfs.sioc.ns.Item> modifier_of = new ArrayList<orgrdfs.sioc.ns.Item>();


	/** Last (real) name of this user. Synonyms include surname or family name. */
	@Predicate("http://rdfs.org/sioc/ns#last_name")
	public List<java.lang.String> last_name = new ArrayList<java.lang.String>();


	/** A Site that the UserAccount is an administrator of. */
	@Predicate("http://rdfs.org/sioc/ns#administrator_of")
	public List<orgrdfs.sioc.ns.Site> administrator_of = new ArrayList<orgrdfs.sioc.ns.Site>();


	/** An electronic mail address of the UserAccount, encoded using SHA1. */
	@Predicate("http://rdfs.org/sioc/ns#email_sha1")
	public List<java.lang.String> email_sha1 = new ArrayList<java.lang.String>();


	/** Refers to the foaf:Agent or foaf:Person who owns this sioc:UserAccount. */
	@Predicate("http://rdfs.org/sioc/ns#account_of")
	public List<comxmlns.foaf._0.Agent> account_of = new ArrayList<comxmlns.foaf._0.Agent>();


	/** Indicates that one UserAccount follows another UserAccount (e.g. for
	    microblog posts or other content item updates). */
	@Predicate("http://rdfs.org/sioc/ns#follows")
	public List<orgrdfs.sioc.ns.UserAccount> follows = new ArrayList<orgrdfs.sioc.ns.UserAccount>();


	/** A resource owned by a particular UserAccount, for example, a weblog or
	    image gallery. */
	@Predicate("http://rdfs.org/sioc/ns#owner_of")
	public List<String> owner_of = new ArrayList<String>();


	/** A Container that a UserAccount is subscribed to. */
	@Predicate("http://rdfs.org/sioc/ns#subscriber_of")
	public List<orgrdfs.sioc.ns.Container> subscriber_of = new ArrayList<orgrdfs.sioc.ns.Container>();


	/** A Usergroup that this UserAccount is a member of. */
	@Predicate("http://rdfs.org/sioc/ns#member_of")
	public List<orgrdfs.sioc.ns.Usergroup> member_of = new ArrayList<orgrdfs.sioc.ns.Usergroup>();


	/** A Forum that a UserAccount is a moderator of. */
	@Predicate("http://rdfs.org/sioc/ns#moderator_of")
	public List<orgrdfs.sioc.ns.Forum> moderator_of = new ArrayList<orgrdfs.sioc.ns.Forum>();


	/** An electronic mail address of the UserAccount. */
	@Predicate("http://rdfs.org/sioc/ns#email")
	public List<String> email = new ArrayList<String>();


	/** An image or depiction used to represent this UserAccount. */
	@Predicate("http://rdfs.org/sioc/ns#avatar")
	public List<String> avatar = new ArrayList<String>();



	// From class this


	@Override
	public List<java.lang.String> getFirst_name()
	{
		return this.first_name;
	}
	
	@Override
	public void setFirst_name( final List<java.lang.String> first_name )
	{
		this.first_name = first_name;
	}


	// From class this


	@Override
	public List<String> getCreator_of()
	{
		return this.creator_of;
	}
	
	@Override
	public void setCreator_of( final List<String> creator_of )
	{
		this.creator_of = creator_of;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Item> getModifier_of()
	{
		return this.modifier_of;
	}
	
	@Override
	public void setModifier_of( final List<orgrdfs.sioc.ns.Item> modifier_of )
	{
		this.modifier_of = modifier_of;
	}


	// From class this


	@Override
	public List<java.lang.String> getLast_name()
	{
		return this.last_name;
	}
	
	@Override
	public void setLast_name( final List<java.lang.String> last_name )
	{
		this.last_name = last_name;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Site> getAdministrator_of()
	{
		return this.administrator_of;
	}
	
	@Override
	public void setAdministrator_of( final List<orgrdfs.sioc.ns.Site> administrator_of )
	{
		this.administrator_of = administrator_of;
	}


	// From class this


	@Override
	public List<java.lang.String> getEmail_sha1()
	{
		return this.email_sha1;
	}
	
	@Override
	public void setEmail_sha1( final List<java.lang.String> email_sha1 )
	{
		this.email_sha1 = email_sha1;
	}


	// From class this


	@Override
	public List<comxmlns.foaf._0.Agent> getAccount_of()
	{
		return this.account_of;
	}
	
	@Override
	public void setAccount_of( final List<comxmlns.foaf._0.Agent> account_of )
	{
		this.account_of = account_of;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.UserAccount> getFollows()
	{
		return this.follows;
	}
	
	@Override
	public void setFollows( final List<orgrdfs.sioc.ns.UserAccount> follows )
	{
		this.follows = follows;
	}


	// From class this


	@Override
	public List<String> getOwner_of()
	{
		return this.owner_of;
	}
	
	@Override
	public void setOwner_of( final List<String> owner_of )
	{
		this.owner_of = owner_of;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Container> getSubscriber_of()
	{
		return this.subscriber_of;
	}
	
	@Override
	public void setSubscriber_of( final List<orgrdfs.sioc.ns.Container> subscriber_of )
	{
		this.subscriber_of = subscriber_of;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Usergroup> getMember_of()
	{
		return this.member_of;
	}
	
	@Override
	public void setMember_of( final List<orgrdfs.sioc.ns.Usergroup> member_of )
	{
		this.member_of = member_of;
	}


	// From class this


	@Override
	public List<orgrdfs.sioc.ns.Forum> getModerator_of()
	{
		return this.moderator_of;
	}
	
	@Override
	public void setModerator_of( final List<orgrdfs.sioc.ns.Forum> moderator_of )
	{
		this.moderator_of = moderator_of;
	}


	// From class this


	@Override
	public List<String> getEmail()
	{
		return this.email;
	}
	
	@Override
	public void setEmail( final List<String> email )
	{
		this.email = email;
	}


	// From class this


	@Override
	public List<String> getAvatar()
	{
		return this.avatar;
	}
	
	@Override
	public void setAvatar( final List<String> avatar )
	{
		this.avatar = avatar;
	}

}

