package org.openimaj.rdf.storm.bolt;

import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.graph.compose.Polyadic;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.openimaj.rdf.storm.topology.bolt.FlexibleReteBolt;
import org.openimaj.rdf.storm.utils.CircularPriorityWindow;

/**
 * Represents one input left of a join node. The queue points to 
 * a sibling queue representing the other leg which should be joined
 * against.
 * 
 * @author David Monks <dm11g08@ecs.soton.ac.uk>, based largely on the RETEQueue implementation by <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 */
public class RETEStormQueue implements RETEStormSinkNode, RETEStormSourceNode {
    
    /** A time-prioritised and size limited sliding window of Tuples */
    private final CircularPriorityWindow<Tuple> window;
    
    /** A set of {@link Fields} which should match between the two inputs */
    protected final Fields matchFields;
    
    /** A set of {@link Fields} which should be produced by joins between the two inputs */
    protected final Fields outputFields;
    
    /** The sibling queue which forms the other half of the join node */
    protected RETEStormQueue sibling;
    
    /** The node that results should be passed on to */
    protected RETEStormSinkNode continuation;
    
    /** 
     * Constructor. The window is not usable until it has been bound
     * to a sibling and a continuation node.
     * @param matchFields 
     * @param outputFields 
     * @param size 
     * @param delay 
     * @param unit 
     */
    public RETEStormQueue(Fields matchFields,
    					  Fields outputFields,
    					  int size,
    					  long delay,
    					  TimeUnit unit) {
        this.matchFields = matchFields;
        this.outputFields = outputFields;
        this.window = new CircularPriorityWindow<Tuple>(size,delay,unit);
    }
    
    /**
     * Constructor including sibling to bind to. The window is not usable until it has 
     * also been bound to a continuation node.
     * @param matchFields
     * @param outputFields 
     * @param size 
     * @param delay 
     * @param unit 
     * @param sib
     */
    public RETEStormQueue(Fields matchFields,
    					  Fields outputFields,
    					  int size,
    					  long delay,
    					  TimeUnit unit,
    					  RETEStormQueue sib) {
        this(matchFields,outputFields,size,delay,unit);
        this.setSibling(sib);
        sib.setSibling(this);
    }
    
    /**
     * Constructor including sibling to bind to. The window is not usable until it has 
     * also been bound to a continuation node.
     * @param matchFields
     * @param outputFields 
     * @param size 
     * @param delay 
     * @param unit 
     * @param sib
     * @param sink 
     */
    public RETEStormQueue(Fields matchFields,
    					  Fields outputFields,
    					  int size,
    					  long delay,
    					  TimeUnit unit,
    					  RETEStormQueue sib,
    					  RETEStormSinkNode sink) {
        this(matchFields,outputFields,size,delay,unit,sib);
        this.setContinuation(sink);
    }
  
    
    /**
     * Set the sibling for this node.
     * @param sibling 
     */
    public void setSibling(RETEStormQueue sibling) {
        this.sibling = sibling;
    }
    
    /**
     * Set the continuation node for this node (and any sibling)
     */
    public void setContinuation(RETEStormSinkNode continuation) {
        this.continuation = continuation;
        if (sibling != null) sibling.continuation = continuation;
    }

    /** 
     * Propagate a token to this node.
     * @param env a set of variable bindings for the rule being processed. 
     * @param isAdd distinguishes between add and remove operations.
     */
    public void fire(Tuple env, boolean isAdd) {
        // Cross match new token against the entries in the sibling queue
        for (Iterator<Tuple> i = sibling.window.iterator(); i.hasNext(); ) {
            Tuple candidate = i.next();
            boolean matchOK = true;
            for (String field : matchFields) {
                if ( ! ((Node)candidate.getValueByField(field)).sameValueAs((Node)env.getValueByField(field))) {
                    matchOK = false;
                    break;
                }
            }
            if (matchOK) {
                // Instantiate a new extended environment
                Values newVals = new Values();
                for (String field : outputFields) {
                	if (Arrays.asList(FlexibleReteBolt.BASE_FIELDS).contains(field)){
                		if (field.equals(FlexibleReteBolt.BASE_FIELDS[0])){
                			Polyadic newG = new MultiUnion();
                			newG.addGraph((Graph)env.getValueByField(FlexibleReteBolt.BASE_FIELDS[0]));
                			newG.addGraph((Graph)candidate.getValueByField(FlexibleReteBolt.BASE_FIELDS[0]));
                			newVals.add((Graph)newG);
                		}
                	} else {
	                	Object o = candidate.getValueByField(field);
	                    newVals.add(o != null ? o : env.getValueByField(field));
                	}
                }
                // Fire the successor processing
                continuation.fire(newVals, isAdd);
            }
        }
        
        if (isAdd)
        	// Store the new token in this store
        	window.offer(env);
        else
        	// Remove any existing instances of the token from this store
        	window.remove(env);
    }
    
    /**
     * Clone this node in the network.
     * @param netCopy 
     * @param context the new context to which the network is being ported
     * @return RETEStormNode
     */
    public RETEStormNode clone(Map<RETEStormNode, RETEStormNode> netCopy, RETERuleContext context) {
        RETEStormQueue clone = (RETEStormQueue)netCopy.get(this);
        if (clone == null) {
            clone = new RETEStormQueue(matchFields,outputFields,window.getCapacity(),window.getDelay(),TimeUnit.MILLISECONDS);
            netCopy.put(this, clone);
            clone.setSibling((RETEStormQueue)sibling.clone(netCopy, context));
            clone.setContinuation((RETEStormSinkNode)continuation.clone(netCopy, context));
            clone.window.addAll(window);
        }
        return clone;
    }

	@Override
	public void fire(Values output, boolean isAdd) {
		if (this == this.continuation){
			
			return;
		}
		this.continuation.fire(output, isAdd);
	}

	@Override
	public boolean isActive() {
		if (this == this.continuation){
			return this.isActive();
		}
		return this.continuation.isActive();
	}

}