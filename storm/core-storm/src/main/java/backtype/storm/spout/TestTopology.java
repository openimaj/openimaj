package backtype.storm.spout;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.scheme.StringScheme;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import java.util.Map;


public class TestTopology {
    public static class FailEveryOther extends BaseRichBolt {
        
        OutputCollector _collector;
        int i=0;
        
        @Override
        public void prepare(Map map, TopologyContext tc, OutputCollector collector) {
            _collector = collector;
        }

        @Override
        public void execute(Tuple tuple) {
            i++;
            if(i%2==0) {
                _collector.fail(tuple);
            } else {
                _collector.ack(tuple);
            }
        }
        
        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
        }
    }
    
    public static void main(String[] args) throws Exception {
        TopologyBuilder builder = new TopologyBuilder();
        KestrelThriftSpout spout = new KestrelThriftSpout("localhost", 2229, "test", new StringScheme());
        builder.setSpout("spout", spout).setDebug(true);
        builder.setBolt("bolt", new FailEveryOther())
                .shuffleGrouping("spout");
        
        LocalCluster cluster = new LocalCluster();
        Config conf = new Config();
        cluster.submitTopology("test", conf, builder.createTopology());
        
        Thread.sleep(600000);
    }
}
