/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.demos.sandbox.flickr.geo;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openimaj.feature.FloatFV;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.io.FileUtils;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadableMapBinary;
import org.openimaj.io.wrappers.WriteableMapBinary;
import org.openimaj.tools.FileToolsUtil;

public class GlobalFlickrColour {
	private static final int COUNT_PER_WRITE = 5000000;
	private static final String WRITE_FILE_NAME = "binary_long_floatfv_%d";
	protected static final String INSERT_COLOUR = "insert into colour values (?, ?, ?, ?)";
	protected static final String INSERT_LATLON = "insert into latlong values (?, ?, ?, ?, ?)";
	final static String CVS_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))";
	static {
		Logger.getRootLogger().setLevel(Level.ERROR);
	}

	public static void main(String[] args) throws Exception {
		// saveSEQFileVersion();
		loadBinaryMapVersion();

	}

	private static void loadBinaryMapVersion() throws IOException, SQLException, ClassNotFoundException,
			InstantiationException, IllegalAccessException
	{
		final String source = "/Users/ss/Development/data/flickr-all-geo-16-46M-images-maxhistogram.binary";
		final String geocsv = "/Volumes/Raid/FlickrCrawls/AllGeo16/images.csv";
		// String geocsv = "/Users/ss/Development/data/flickrcsv.csv";

		// Prepare the sqlite connection
		// final Connection connection = prepareDBSQLite(source + ".sqlite");
		final Connection connection = prepareDBmysql();
		connection.setAutoCommit(false);
		prepareTables(connection);
		insertGeo(geocsv, connection);
		insertColours(source, connection);
		connection.commit();
		connection.close();
	}

	private static Connection prepareDBmysql() throws SQLException, InstantiationException, IllegalAccessException,
			ClassNotFoundException
	{
		Connection conn = null;
		final String url = "jdbc:mysql://leto/";
		final String driver = "com.mysql.jdbc.Driver";
		final String userName = "root";
		final String password = "";
		Class.forName(driver).newInstance();
		conn = DriverManager.getConnection(url, userName, password);
		return conn;
	}

	private static void insertGeo(String source, Connection connection) throws IOException, SQLException {
		final File f = new File(source);
		final PreparedStatement statement = connection.prepareStatement(INSERT_LATLON);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		String line = null;
		int done = 0;
		while ((line = reader.readLine()) != null) {
			final String[] linesplit = line.split(CVS_REGEX);
			try {

				statement.setLong(1, Long.parseLong(linesplit[2].trim()));
				statement.setFloat(2, Float.parseFloat(linesplit[15].trim()));
				statement.setFloat(3, Float.parseFloat(linesplit[16].trim()));

				statement.setTimestamp(4, asDate(linesplit[10].trim()));
				statement.setTimestamp(5, asDate(linesplit[11].trim()));
				statement.executeUpdate();
				done++;
				if (done % 50000 == 0) {
					System.out.println("commiting geo: " + done);
					connection.commit();
				}
			} catch (final Exception e) {
				System.out.println("Failed writing: \n" + line + "\n to database");
			}
		}
		reader.close();
		return;

	}

	private static Timestamp asDate(String trim) throws ParseException {
		final SimpleDateFormat format = new SimpleDateFormat("EE MMM dd HH:mm:ss zz yyyy", Locale.US);
		final java.util.Date t = format.parse(trim);
		return new java.sql.Timestamp(t.getTime());
	}

	private static void insertColours(String source, Connection connection) throws SQLException, IOException {
		final PreparedStatement statement = connection.prepareStatement(INSERT_COLOUR);
		final File[] files = new File(source).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File f, String name) {
				return name.startsWith("binary");
			}

		});
		for (final File file : files) {
			System.out.println("Reading from " + file);
			final ReadableMapBinary<Long, FloatFV> readableMap = new ReadableMapBinary<Long, FloatFV>(
					new HashMap<Long, FloatFV>())
			{

				@Override
				protected Long readKey(DataInput in) throws IOException {

					return in.readLong();
				}

				@Override
				protected FloatFV readValue(DataInput in) throws IOException {
					final FloatFV f = new FloatFV();
					f.readBinary(in);
					return f;
				}

				@Override
				public void readBinary(DataInput in) throws IOException {
					final int sz = in.readInt();

					for (int i = 0; i < sz; i++) {
						final Long key = readKey(in);
						final FloatFV val = readValue(in);
						try {
							statement.setLong(1, key);
							statement.setFloat(2, val.values[0]);
							statement.setFloat(3, val.values[1]);
							statement.setFloat(4, val.values[2]);
							statement.executeUpdate();
						} catch (final SQLException e) {
							throw new IOException("Couldn't");
						}
					}
				}
			};
			IOUtils.read(file, readableMap);
			connection.commit();
		}
	}

	private static void prepareTables(Connection connection) throws SQLException, IOException {
		// Read the table def file
		final String sql = FileUtils.readall(GlobalFlickrColour.class
				.getResourceAsStream("/org/openimaj/demos/sandbox/flickr/geo/geoflickrcolour.sql"));
		final Statement statement = connection.createStatement();
		final String[] vals = sql.split(";");
		for (String str : vals) {
			str = str.trim();
			if (str.length() == 0)
				continue;
			statement.executeUpdate(str.trim());
		}
	}

	private static Connection prepareDBSQLite(String location) throws SQLException, ClassNotFoundException, IOException {
		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");
		FileToolsUtil.validateLocalOutput(location, true, false);
		Connection connection = null;
		connection = DriverManager.getConnection("jdbc:sqlite:" + location);

		return connection;
	}

	private static void saveSEQFileVersion() throws Exception {
		final String seqFileSource = "/Users/ss/Development/data/flickr-all-geo-16-46M-images-maxhistogram.seq";
		final String output = "/Users/ss/Development/data/flickr-all-geo-16-46M-images-maxhistogram.binary";
		final File ofile = FileToolsUtil.validateLocalOutput(output, true, false);
		ofile.mkdirs();

		final Path[] sequenceFiles = SequenceFileUtility.getFilePaths(seqFileSource, "part");
		final Configuration config = new Configuration();
		config.setQuietMode(true);
		final Map<Long, FloatFV> flickrMaxHist = new HashMap<Long, FloatFV>();
		int total = 0;
		int writeCount = 0;
		for (final Path path : sequenceFiles) {
			// System.out.println("Extracting from " + path.getName());
			System.out.print(".");
			total++;
			if (total % 40 == 0)
				System.out.println(flickrMaxHist.size());
			final FileSystem fs = HadoopToolsUtil.getFileSystem(path);
			final Reader reader = new Reader(config,
					Reader.file(path.makeQualified(fs.getUri(), fs.getWorkingDirectory())));
			final Text key = org.apache.hadoop.util.ReflectionUtils.newInstance(Text.class, config);
			final BytesWritable val = org.apache.hadoop.util.ReflectionUtils.newInstance(BytesWritable.class, config);
			while (reader.next(key, val)) {
				final FloatFV fv = IOUtils.deserialize(val.getBytes(), FloatFV.class);
				// System.out.println(key + ": " + fv);
				flickrMaxHist.put(Long.parseLong(key.toString().trim()), fv);
			}
			reader.close();
			if (flickrMaxHist.size() > COUNT_PER_WRITE) {
				System.out.println();
				System.out.println("Writing values:" + flickrMaxHist.size());
				final WriteableMapBinary<Long, FloatFV> writeMap = new WriteableMapBinary<Long, FloatFV>(flickrMaxHist) {
					@Override
					protected void writeKey(Long key, DataOutput out) throws IOException {
						out.writeLong(key);
					}

					@Override
					protected void writeValue(FloatFV value, DataOutput out) throws IOException {
						value.writeBinary(out);
					}

				};
				final File writeName = new File(ofile, String.format(WRITE_FILE_NAME, writeCount));
				System.out.println("writing to: " + writeName);
				IOUtils.writeBinary(writeName, writeMap);
				flickrMaxHist.clear();
				writeCount++;
			}
		}
	}
}
