/**
 *
 */
package org.openimaj.picslurper;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

import com.mysql.jdbc.Connection;

/**
 *	This class is a status feeder for picslurper that takes statuses from a database.
 *	The default database driver is the MySQL driver and expects a mysql URL in the constructor;
 *	e.g.
 *		jdbc:mysql://localhost/database
 *	<p>
 *	The class connects to the database lazily (that is, only when the feed is started), so the
 *	driver can be changed after construction using {@link #setDriver(String)}.
 *	<p>
 *	The schema of the table must include at least two columns: one that contains the tweet text
 *	and one that contains the created time.  The default column names are expected to be
 *	<code>text</code> and <code>created_at</code> respectively. These can be changed by passing
 *	in a map, where the key is the expected column name and the value is the actual column name;
 *	e.g. <code>text -> tweet_text</code>
 *	<p>
 *	The results are paged through in steps of 25 by default, so the database must support the
 *	<code>LIMIT</code> command.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 18 Sep 2013
 */
public class DatabaseStatusFeeder implements StatusFeeder
{
	protected static class DatabaseFeederStatus implements Status
	{
		private final XMLGregorianCalendar cal;
		private final String text;

		public DatabaseFeederStatus( final String text, final XMLGregorianCalendar cal )
		{
			this.text = text;
			this.cal = cal;
		}

		/** */
		private static final long serialVersionUID = 1L;

		@Override
		public int compareTo( final Status o ) { return 0; }

		@Override
		public RateLimitStatus getRateLimitStatus() { return null; }

		@Override
		public int getAccessLevel() { return 0;	}

		@Override
		public UserMentionEntity[] getUserMentionEntities() { return null; }

		@Override
		public URLEntity[] getURLEntities()	{ return null; }

		@Override
		public HashtagEntity[] getHashtagEntities() { return null; }

		@Override
		public MediaEntity[] getMediaEntities() { return null; }

		@Override
		public Date getCreatedAt() { return this.cal.toGregorianCalendar().getTime(); }

		@Override
		public long getId() { return 0;	}

		@Override
		public String getText() { return this.text; }

		@Override
		public String getSource() {	return null; }

		@Override
		public boolean isTruncated() { return false; }

		@Override
		public long getInReplyToStatusId() { return 0; }

		@Override
		public long getInReplyToUserId() { return 0; }

		@Override
		public String getInReplyToScreenName() { return null; }

		@Override
		public GeoLocation getGeoLocation()	{ return null; }

		@Override
		public Place getPlace() { return null; }

		@Override
		public boolean isFavorited() { return false; }

		@Override
		public User getUser() { return null; }

		@Override
		public boolean isRetweet() { return false; }

		@Override
		public Status getRetweetedStatus() { return null; }

		@Override
		public long[] getContributors() { return null; }

		@Override
		public long getRetweetCount() { return 0; }

		@Override
		public boolean isRetweetedByMe() { return false; }

		@Override
		public long getCurrentUserRetweetId() { return 0; }

		@Override
		public boolean isPossiblySensitive() { return false; }

	}

	/** The map of column names */
	private Map<String,String> columnNames = new HashMap<String,String>();

	/** The username to connect to the database */
	private final String username;

	/** The password to connect to the database */
	private final String password;

	/** The database table where the tweets are stored */
	private final String table;

	/** The database connection that is created */
	private Connection connection;

	/** The URL to the database */
	private final String url;

	/** The database driver to use */
	private String databaseDriver = "com.mysql.jdbc.Driver";

	/** The size of each page of results to retrieve from the database */
	private final int pageSize = 25;

	/**
	 * 	Create a database status feeder using the database, table,
	 * 	username and password provided.
	 *
	 *	@param databaseURL The URL to the database
	 *	@param table The table to use
	 *	@param username The user name
	 *	@param password The password
	 */
	public DatabaseStatusFeeder( final String databaseURL, final String table,
			final String username, final String password )
	{
		this.url = databaseURL;
		this.table = table;
		this.username = username;
		this.password = password;
	}

	/**
	 * 	Create a database status feeder using the database, table,
	 * 	username and password provided with the given column mapping
	 *
	 *	@param databaseURL The URL to the database
	 *	@param table The table to use
	 *	@param username The user name
	 *	@param password The password
	 *	@param columnNames The column mapping
	 */
	public DatabaseStatusFeeder( final String databaseURL, final String table,
			final String username, final String password, final Map<String,String> columnNames )
	{
		this( databaseURL, table, username, password );
		this.columnNames = columnNames;
	}

	/**
	 * 	Create a connection using the given driver for the given database.
	 *
	 *	@param driver The driver
	 *	@param url The URL to the database
	 *	@param username The username
	 *	@param password The password
	 *	@return The database connection
	 *	@throws ClassNotFoundException If the driver cannot be found
	 *	@throws SQLException If the connection could not be created
	 */
    private static Connection createConnection( final String driver, final String url,
    		final String username, final String password )
    				throws ClassNotFoundException, SQLException
    {
        Class.forName(driver);
        if( username == null || password == null ||
        	username.trim().length() == 0 || password.trim().length() == 0 )
            	return (Connection) DriverManager.getConnection( url );
        else	return (Connection) DriverManager.getConnection( url, username, password );
    }

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.picslurper.StatusFeeder#feedStatus(org.openimaj.picslurper.PicSlurper)
	 */
	@Override
	public void feedStatus( final PicSlurper slurper ) throws IOException
	{
		try
		{
			// Create the connection to the database
			this.connection = DatabaseStatusFeeder.createConnection(
					this.databaseDriver, this.url, this.username, this.password );

			// Create a scrolling result set type
			final Statement s = this.connection.createStatement(
					ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY  );
			s.setFetchSize( this.pageSize  );
			s.setMaxRows( this.pageSize );

			// First count how many rows there are so we know how many
			// pages there will be.
			String sql = "SELECT COUNT(*) FROM "+this.table;
			ResultSet r = s.executeQuery( sql );
			r.next(); final int rows = r.getInt( 1 );
			final int pages = (int)Math.ceil(rows/(double)this.pageSize);

			for( int pageNumber = 1; pageNumber <= pages; pageNumber++ )
			{
				sql = "SELECT "+
						this.getColumn("created_at",true)+", "+
						this.getColumn("text",true)+
						" from "+this.table+
						" LIMIT "+(pageNumber*this.pageSize) +","+this.pageSize;

				r = s.executeQuery( sql );

				// Read the results sets
				while( r.next() )
				{
					try
					{
						final String createdAt = r.getString(1);
						final XMLGregorianCalendar cal = DatatypeFactory.newInstance()
									.newXMLGregorianCalendar(createdAt);
						final String text = r.getString(2);

						final DatabaseFeederStatus status = new DatabaseFeederStatus( text, cal );
						slurper.handleStatus( status );
					}
					catch( final DatatypeConfigurationException e )
					{
						e.printStackTrace();
					}
				}
			}
		}
		catch( final ClassNotFoundException e )
		{
			e.printStackTrace();
		}
		catch( final SQLException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * 	Get a column name through the column name mapping, if one exists.
	 *	@param defaultName The name to look up
	 *	@param tf Whether to enclose in back-ticks
	 *	@return The column name to use
	 */
	private String getColumn( final String defaultName, final boolean tf )
	{
		if( this.columnNames.get(defaultName) != null )
			return tf ? "`"+this.columnNames.get(defaultName)+"`" :
				this.columnNames.get(defaultName);
		return tf ? "`"+defaultName+"`" : defaultName;
	}

	/**
	 * 	Set the database driver to use.
	 *	@param driverName The driver name.
	 */
	public void setDriver( final String driverName )
	{
		this.databaseDriver = driverName;
	}

	/**
	 *	@param args
	 */
	public static void main( final String[] args )
	{
		try
		{
			// Connect to local database
			final DatabaseStatusFeeder dbsf = new DatabaseStatusFeeder(
					"jdbc:mysql://localhost:3306/swr", "tweets", "swr", "swr" );

			final PicSlurper ps = new PicSlurper();
			dbsf.feedStatus( ps );
		}
		catch( final IOException e )
		{
			e.printStackTrace();
		}
	}
}
