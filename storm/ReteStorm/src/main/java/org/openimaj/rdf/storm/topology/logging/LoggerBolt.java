package org.openimaj.rdf.storm.topology.logging;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class LoggerBolt extends BaseRichBolt {

	private static final long serialVersionUID = -5055859216495263711L;

	public static enum LogType {

		/**
		 * 
		 */
		EXCEPTION
		,
		/**
		 * 
		 */
		EVENT
		,
		/**
		 * 
		 */
		STATISTIC
		,
		/**
		 * 
		 */
		OTHER;
	}
	public static final String STREAM_ID = "logging";
	
	private PrintStream out;
	private final String logFileName;
	
	public LoggerBolt(){
		Calendar c = Calendar.getInstance();
		this.logFileName = String.format("/TopologyLog_%d-%d-%d_%d-%d-%d_%d.log",
										 c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
										 c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), c.get(Calendar.MILLISECOND));
	}
	
	public LoggerBolt(String fileName){
		this.logFileName = fileName;
	}
	
	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context,
			OutputCollector collector) {
		if (this.logFileName != null)
			try {
				File logFile = new File(this.logFileName);
				logFile.createNewFile();
				this.out = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile, true)), true);
			} catch (IOException e) {
				this.out = System.out;
				e.printStackTrace(out);
			}
		else
			this.out = System.out;
	}

	@Override
	public void execute(Tuple input) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis((Long) input.getValue(0));
		LogType type = (LogType) input.getValue(1);
		String message = (String) input.getValue(2);
		out.format("%s/%s/%s %s:%s:%s.%s | %s -> %s: ",
					c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
					c.get(Calendar.HOUR), c.get(Calendar.MINUTE), c.get(Calendar.SECOND),
					c.get(Calendar.MILLISECOND),
					input.getSourceComponent(), type.toString());
		switch (type){
			case EXCEPTION:
				out.println(message);
				for (String stackTraceElement : (String[]) input.getValue(3))
					out.format("\t%s%s", stackTraceElement,
								System.getProperty("line.separator"));
				break;
			case EVENT:
				LoggedEvent event = (LoggedEvent) input.getValue(3);
				out.format("%s%s",event.getName(),
							System.getProperty("line.separator"));
				out.println(message);
				break;
			case STATISTIC:
				LoggedStatistics stats = (LoggedStatistics) input.getValue(3);
//				out.format("%s",
//						System.getProperty("line.separator"));
				out.println(message);
				break;
			case OTHER:
			default:
				out.println(message);
				out.println(input.getValue(3));
				break;
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields());
	}
	
	/**
	 * Takes a bolt declarer and an array of component IDs to subscribe to.
	 * Connects the bolt declarer to the logging stream produced by each of the
	 * listed component IDs' respective bolts/spouts.
	 * @param declarer
	 * @param componentID
	 * @return declarer
	 */
	public static BoltDeclarer connectToSource(BoltDeclarer declarer, String[] componentID){
		for (String id : componentID)
			declarer = declarer.shuffleGrouping(id, STREAM_ID);
		return declarer;
	}
	
	// ********* Log Emitter Class *********

	public static class LogEmitter implements Serializable {
		
		private static final long serialVersionUID = 8216443056429401253L;
		
		private final OutputCollector collector;
		
		public LogEmitter(OutputCollector c){
			this.collector = c;
		}
		
		// ********* Overloaded Emitting Methods
		
		public void emit(Exception exception){
			StackTraceElement[] st = exception.getStackTrace();
			String[] stackTrace = new String[st.length];
			for (int i = 0; i < stackTrace.length; i++)
				stackTrace[i] = st[i].toString();
			this.emit(LogType.EXCEPTION, exception.getMessage(), stackTrace);
		}
		
		public void emit(LoggedEvent event){
			this.emit(LogType.EVENT, event.getName(), event);
		}
		
		public void emit(LoggedEvent event, String message){
			this.emit(LogType.EVENT, message, event);
		}
		
		public void emit(LoggedStatistics stats){
			this.emit(LogType.STATISTIC, "Statistics emitted.", stats);
		}
		
		public void emit(LoggedStatistics stats, String message){
			this.emit(LogType.STATISTIC, message, stats);
		}
		
		public void emit(String message){
			this.emit(LogType.OTHER, message, "No data.");
		}
		
		public void emit(String message, Object data){
			this.emit(LogType.OTHER, message, data);
		}
		
		// ********* private emitter *********
		
		private void emit(LogType type, String message, Object data){
			Values log = new Values();
			log.add(type);
			log.add(new Date().getTime());
			log.add(message);
			log.add(data);
			this.collector.emit(STREAM_ID, log);
		}

		// ********* public declarer *********
		
		public static void declareFields(OutputFieldsDeclarer declarer){
			declarer.declareStream(STREAM_ID, new Fields("type","timestamp","message","data"));
		}

		public OutputCollector getOutputCollector() {
			return this.collector;
		}
		
	}
	
	// ********* Logged Data Classes ********* 
	
	public static class LoggedEvent implements Serializable {

		private static final long serialVersionUID = -872266540293532L;

		public static enum EventType {
			TUPLE_FIRED {
				@Override
				public String descriptionFormat() {
					return "Message %s with bindings %s emitted on stream %s.";
				}
				public String toString() {
					return "Tuple fired.";
				}
			},
			TUPLE_DROPPED {
				@Override
				public String descriptionFormat() {
					return "Message %s with bindings %s dropped due to %s limit exceeded.";
				}
				public String toString() {
					return "Tuple dropped.";
				}
			};
			
			public abstract String descriptionFormat();
		}
		
		private EventType type;
		private Tuple cause;
		private String additionalInfo;
		private String description;
		
		public LoggedEvent(EventType type, Tuple cause, String additionalInfo){
			this.type = type;
			this.cause = cause;
			this.additionalInfo = additionalInfo;
			StringBuilder bindings = new StringBuilder();
			Node n = (Node) cause.getValue(0);
			String value = n.isLiteral() 
								? n.getLiteralValue().toString()
								: n.isURI()
									? n.getURI()
									: n.isBlank()
										? n.getBlankNodeLabel()
										: n.getName();
			bindings.append(cause.getFields().get(0)).append(" => ").append(value);
			for (int i = 1; i < cause.getFields().size() - 3; i++){
				n = (Node) cause.getValue(i);
				value = n.isLiteral() 
									? n.getLiteralValue().toString()
									: n.isURI()
										? n.getURI()
										: n.isBlank()
											? n.getBlankNodeLabel()
											: n.getName();
				bindings.append(", ").append(cause.getFields().get(i)).append(" => ").append(value);
			}
			this.description = String.format(type.descriptionFormat(), cause.getMessageId().toString(), bindings.toString(), additionalInfo);
		}
		
		public String getName() {
			return this.type.toString();
		}
		
		public String getDescription(){
			return this.description;
		}
		
		public Tuple getCause(){
			return this.cause;
		}
		
		public String getStream(){
			if (this.type == EventType.TUPLE_FIRED){
				return this.additionalInfo;
			}
			return null;
		}
		
		public String getLimit(){
			if (this.type == EventType.TUPLE_DROPPED){
				return this.additionalInfo;
			}
			return null;
		}
		
	}
	
	public static class LoggedStatistics implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	}
	
}
