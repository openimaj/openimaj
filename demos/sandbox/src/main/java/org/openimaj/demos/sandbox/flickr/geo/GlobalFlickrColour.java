package org.openimaj.demos.sandbox.flickr.geo;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.io.compress.CodecPool;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.FloatFV;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.hadoop.sequencefile.TextBytesSequenceFileUtility;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.io.FileUtils;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadableMapBinary;
import org.openimaj.io.wrappers.WriteableMapBinary;
import org.openimaj.tools.FileToolsUtil;
import org.openimaj.util.reflection.ReflectionUtils;

import cern.colt.Arrays;


public class GlobalFlickrColour {
	private static final int COUNT_PER_WRITE = 5000000;
	private static final String WRITE_FILE_NAME = "binary_long_floatfv_%d";
	protected static final String INSERT_COLOUR = "insert into colour values (?, ?, ?, ?)";
	protected static final String INSERT_LATLON = "insert into latlong values (?, ?, ?)";
	final static String CVS_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))";
	static{
		Logger.getRootLogger().setLevel(Level.ERROR);
	}
	public static void main(String[] args) throws Exception {
//		saveSEQFileVersion();
		loadBinaryMapVersion();

	}
	private static void loadBinaryMapVersion() throws IOException, SQLException, ClassNotFoundException {
		String source = "/Users/ss/Development/data/flickr-all-geo-16-46M-images-maxhistogram.binary";
		String geocsv = "/Volumes/Raid/FlickrCrawls/AllGeo16/images.csv";
		
		// Prepare the sqlite connection
		final Connection connection = prepareDB(source + ".sqlite");
		connection.setAutoCommit(false);
		prepareTables(connection);
		insertGeo(geocsv,connection);
		insertColours(source,connection);
		connection.commit();
		connection.close();
	}
	private static void insertGeo(String source, Connection connection) throws IOException, SQLException {
		File f = new File(source);
		final PreparedStatement statement = connection.prepareStatement(INSERT_LATLON);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		String line = null;
		int done = 0;
		while((line = reader.readLine())!=null){
			String[] linesplit = line.split(CVS_REGEX);
			try{
				
				statement.setLong(1, Long.parseLong(linesplit[2].trim()));
				statement.setFloat(2, Float.parseFloat(linesplit[15].trim()));
				statement.setFloat(3, Float.parseFloat(linesplit[16].trim()));
				statement.executeUpdate();
				done++;
				if(done%50000 == 0){
					System.out.println("commiting geo!");
					connection.commit();
				}
			}
			catch(Exception e){
				System.out.println("Failed writing: \n" + line + "\n to database");
			}
		}
		return;
		
	}
	private static void insertColours(String source, Connection connection) throws SQLException, IOException {
		final PreparedStatement statement = connection.prepareStatement(INSERT_COLOUR);
		File[] files = new File(source).listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File f, String name) {
				return name.startsWith("binary");
			}
			
		});
		for (File file : files) {			
			System.out.println("Reading from " + file);
			ReadableMapBinary<Long, FloatFV> readableMap = new ReadableMapBinary<Long, FloatFV>(new HashMap<Long,FloatFV>()) {
				
				@Override
				protected Long readKey(DataInput in) throws IOException {
					
					return in.readLong();
				}
				
				@Override
				protected FloatFV readValue(DataInput in) throws IOException {
					FloatFV f = new FloatFV();
					f.readBinary(in);
					return f;
				}
				@Override
				public void readBinary(DataInput in) throws IOException {
					int sz = in.readInt();
					
					for (int i=0; i<sz; i++) {
						Long key = readKey(in);
						FloatFV val = readValue(in);
						try {
							statement.setLong(1, key);
							statement.setFloat(2, val.values[0]);
							statement.setFloat(3, val.values[1]);
							statement.setFloat(4, val.values[2]);
							statement.executeUpdate();
						} catch (SQLException e) {
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
		String sql = FileUtils.readall(GlobalFlickrColour.class.getResourceAsStream("/org/openimaj/demos/sandbox/flickr/geo/geoflickrcolour.sql"));
		Statement statement = connection.createStatement();
		String[] vals = sql.split(";");
		for (String str : vals) {
			str = str.trim();
			if(str.length()==0)continue;
			statement.executeUpdate(str.trim());
		}
	}
	private static Connection prepareDB(String location) throws SQLException, ClassNotFoundException, IOException {
		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");
		FileToolsUtil.validateLocalOutput(location, true, false);
	    Connection connection = null;
	    connection = DriverManager.getConnection("jdbc:sqlite:" + location);
	     
		return connection;
	}
	private static void saveSEQFileVersion() throws Exception {
		String seqFileSource = "/Users/ss/Development/data/flickr-all-geo-16-46M-images-maxhistogram.seq";
		String output = "/Users/ss/Development/data/flickr-all-geo-16-46M-images-maxhistogram.binary";
		File ofile = FileToolsUtil.validateLocalOutput(output, true, false);
		ofile.mkdirs();
		
		Path[] sequenceFiles = SequenceFileUtility.getFilePaths(seqFileSource, "part");
		Configuration config = new Configuration();
		config.setQuietMode(true);
		Map<Long,FloatFV> flickrMaxHist = new HashMap<Long,FloatFV>();
		int total = 0;
		int writeCount = 0;
		for (Path path : sequenceFiles) {
//			System.out.println("Extracting from " + path.getName());
			System.out.print(".");
			total++;
			if(total % 40 == 0) System.out.println(flickrMaxHist.size());
			Reader reader = new Reader(HadoopToolsUtil.getFileSystem(path), path, config); 
			Text key = org.apache.hadoop.util.ReflectionUtils.newInstance(Text.class, config);
			BytesWritable val = org.apache.hadoop.util.ReflectionUtils.newInstance(BytesWritable.class, config);
			while(reader.next(key, val)){
				FloatFV fv = IOUtils.deserialize(val.getBytes(), FloatFV.class);
//				System.out.println(key + ": " + fv);
				flickrMaxHist.put(Long.parseLong(key.toString().trim()),fv);
			}
			if(flickrMaxHist.size() > COUNT_PER_WRITE){
				System.out.println();
				System.out.println("Writing values:" + flickrMaxHist.size());
				WriteableMapBinary<Long, FloatFV> writeMap = new WriteableMapBinary<Long,FloatFV>(flickrMaxHist){
					@Override
					protected void writeKey(Long key, DataOutput out)throws IOException {
						out.writeLong(key);
					}

					@Override
					protected void writeValue(FloatFV value, DataOutput out)throws IOException {
						value.writeBinary(out);
					}
					
				};
				File writeName = new File(ofile,String.format(WRITE_FILE_NAME,writeCount));
				System.out.println("writing to: " + writeName);
				IOUtils.writeBinary(writeName, writeMap);
				flickrMaxHist.clear();
				writeCount++;
			}
		}
	}
}
