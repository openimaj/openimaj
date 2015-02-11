package orgrdfs.sioc.ns;

import java.util.List;

import comxmlns.foaf._0.OnlineAccount;

/**
 * A user account in an online community site.
 */
@SuppressWarnings("javadoc")
public interface UserAccount
		extends OnlineAccount
{
	public List<java.lang.String> getFirst_name();

	public void setFirst_name(final List<java.lang.String> first_name);

	public List<String> getCreator_of();

	public void setCreator_of(final List<String> creator_of);

	public List<orgrdfs.sioc.ns.Item> getModifier_of();

	public void setModifier_of(final List<orgrdfs.sioc.ns.Item> modifier_of);

	public List<java.lang.String> getLast_name();

	public void setLast_name(final List<java.lang.String> last_name);

	public List<orgrdfs.sioc.ns.Site> getAdministrator_of();

	public void setAdministrator_of(final List<orgrdfs.sioc.ns.Site> administrator_of);

	public List<java.lang.String> getEmail_sha1();

	public void setEmail_sha1(final List<java.lang.String> email_sha1);

	public List<comxmlns.foaf._0.Agent> getAccount_of();

	public void setAccount_of(final List<comxmlns.foaf._0.Agent> account_of);

	public List<orgrdfs.sioc.ns.UserAccount> getFollows();

	public void setFollows(final List<orgrdfs.sioc.ns.UserAccount> follows);

	public List<String> getOwner_of();

	public void setOwner_of(final List<String> owner_of);

	public List<orgrdfs.sioc.ns.Container> getSubscriber_of();

	public void setSubscriber_of(final List<orgrdfs.sioc.ns.Container> subscriber_of);

	public List<orgrdfs.sioc.ns.Usergroup> getMember_of();

	public void setMember_of(final List<orgrdfs.sioc.ns.Usergroup> member_of);

	public List<orgrdfs.sioc.ns.Forum> getModerator_of();

	public void setModerator_of(final List<orgrdfs.sioc.ns.Forum> moderator_of);

	public List<String> getEmail();

	public void setEmail(final List<String> email);

	public List<String> getAvatar();

	public void setAvatar(final List<String> avatar);

	@Override
	public String getURI();

}
