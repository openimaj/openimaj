package backtype.storm.spout;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import net.lag.kestrel.thrift.*;
import java.util.List;
import java.util.Set;
import org.apache.thrift7.TException;
import org.apache.thrift7.protocol.TBinaryProtocol;
import org.apache.thrift7.protocol.TProtocol;
import org.apache.thrift7.transport.TFramedTransport;
import org.apache.thrift7.transport.TSocket;
import org.apache.thrift7.transport.TTransport;

/* Thin wrapper around Thrift Client for Kestrel */
public class KestrelThriftClient implements Kestrel.Iface {
    Kestrel.Client _client = null;
    TTransport _transport = null;

    public KestrelThriftClient(String hostname, int port)
        throws TException {

        _transport = new TFramedTransport(new TSocket(hostname, port));
        TProtocol proto = new TBinaryProtocol(_transport);
        _client = new Kestrel.Client(proto);
        _transport.open();
    }

    public void close() {
        _transport.close();
        _transport = null;
        _client = null;
    }

    public QueueInfo peek(String queue_name) throws TException {
        return _client.peek(queue_name);
    }

    public void delete_queue(String queue_name) throws TException {
        _client.delete_queue(queue_name);
    }

    public String get_version() throws TException {
        return _client.get_version();
    }

    @Override
    public int put(String queue_name, List<ByteBuffer> items, int expiration_msec) throws TException {
        return _client.put(queue_name, items, expiration_msec);
    }
    
    public void put(String queue_name, String item, int expiration_msec) throws TException {
        List<ByteBuffer> toPut = new ArrayList<ByteBuffer>();
        try {
            toPut.add(ByteBuffer.wrap(item.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        put(queue_name, toPut, expiration_msec);
    }

    @Override
    public List<Item> get(String queue_name, int max_items, int timeout_msec, int auto_abort_msec) throws TException {
        return _client.get(queue_name, max_items, timeout_msec, auto_abort_msec);
    }

    @Override
    public int confirm(String queue_name, Set<Long> ids) throws TException {
        return _client.confirm(queue_name, ids);
    }

    @Override
    public int abort(String queue_name, Set<Long> ids) throws TException {
        return _client.abort(queue_name, ids);
    }

    @Override
    public void flush_queue(String queue_name) throws TException {
        _client.flush_queue(queue_name);
    }

    @Override
    public void flush_all_queues() throws TException {
        _client.flush_all_queues();
    }

}