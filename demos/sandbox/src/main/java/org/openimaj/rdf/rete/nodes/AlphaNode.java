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
package org.openimaj.rdf.rete.nodes;

import com.hp.hpl.jena.reasoner.TriplePattern;

public class AlphaNode {
	private TriplePattern pattern;

	//	def __init__(self, pattern):        
//        self.pattern = pattern
//        #print "pattern", pattern
//        self.ind = Memory()     ###ind=memory, or ind=index
//        self.betaNodes = list()
//        self.vars = [v for v in pattern if isinstance(v, Variable)]
//        
//        #hack vlatko 10/24/2006t: 
//        #special case for log:includes: get all variables in RHS and add them
//        if self.pattern[1]== INCLUDES:                
//                self.vars.extend(list(self.pattern[2].getVars()))
//        #end hack
//                
//        sortVars(self.vars)
//        self.svars = list()
//        self.dependents = list()
//        self.dependsOn = list()
	public AlphaNode(TriplePattern pattern) {
		this.pattern = pattern;
//		this.memory = new HashMap<>();
	}
//
//            
//    def clear(self):
//        self.ind = Memory()
//
//    #####Make this a Pattern method -- multiple arity patterns
//    def getvar(self, var, fact):
//        """I return the value of var in fact according to our pattern."""
//        if var in self.pattern:
//            pos = self.pattern.index(var)
//            return fact[pos]
//        else:
//            raise exception.UnboundRuleVariable(var, self.pattern)
//
//    def getbindings(self, row, useBuiltin=None):
//        """I return a set of bindings for row, where row is a set of values from
//        my AlphaMemory (note that rows that do not have constants, only variable values.)"""
//        bindings = dict()
//        key = removedups(self.svars + self.vars)
//        for i, val in enumerate(key):
//            bindings[val] = row[i]
//        #print "key alpha:",key
//        #print "row alpha:",row
//
//        return bindings
//
//    def  getkey(self):
//        return removedups(self.svars + self.vars)
//    
//    def getrest(self, fact, sharedVars):
//        """I return the unshared variable values for a given fact 
//        according to our pattern. Note that right now we need to 
//        pass the *shared* variable list as a parameter, but in our 
//        Rete network we would just make that an attribute of every 
//        ANode in compilation."""
//        vals = list()
//        for i, v in enumerate(self.pattern):
//            if isinstance(v, Variable) and v not in sharedVars:
//                vals.append(fact[i])
//        return vals
//
//    def setupIndex(self):
//        if not self.svars:
//            self.svars = [self.vars[0]] # need to account for 0 var case
//        # len(shared) <= len(self.vars)
//        # We need to remove dups.
//        # unshared and shared are *disjoint*, so only need to remove
//        # dups in each
//        self.unshared = list(removedups([v for v in self.vars if v not in self.svars]))
//
//    ####Make this a Pattern method
//    def match(self, fact):
//        """Determines whether the fact matches the node's pattern"""
//        bindings = dict()
//        for p, f in zip(self.pattern, fact):
//            if not isinstance(p, Variable):
//                if p != f:
//                    return False
//            elif p not in bindings:
//                bindings[p] = f
//            elif bindings[p] != f:
//                return False
//        return bindings                
//
//    def addAll(self, facts):
//        for f in facts:
//            self.add(f)
//            
//    def add(self, fact):
//        bindings = self.match(fact)        
//        if bindings:
//            #make sure key has shared vars first
//            key = removedups(self.svars + self.vars)
//            return self.index(self.ind, bindings, key, fact)
//        else:
//            return False
//        
//    def getJustification(fact):
//        """I take a fact and return its justification set (another set of facts)."""
//        if fact.s in self.ind:
//            if fact.p in self.ind[fact.s]:
//                val = self.ind[fact.s][fact.p]
//                if fact.o in val[fact.o]:
//                    return val[fact.o]
//        return None
//
//    def exists(self, fact, bindings):
//        """Check the index for the presence of the fact
//        (as expressed as a set of bindings returned by match)"""
//        key = self.svars + self.unshared
//        return self.__exists(bindings, self.ind, key)
//
//    def __exists(self, bindings, ind, key):
//        # vars are in reverse shared/unshared sorted
//        if key: # Still comparison work to be done
//            cur = key.pop(0)
//            if bindings[cur] in ind:
//                return self.existshelp(bindings, ind[bindings[cur]], key)
//            else:
//                return False
//        else: # We succeded in getting to the bottom of the index
//            return True
//
//    def clear(self):
//        """I clear the memory of this AlphaNode.  This is only called
//        from unit tests."""
//        self.ind = Memory()
//        
//    def index(self, ind, bindings, key, factAdded=None):
//        if key: # Still work to be done
//            cur = bindings[key.pop(0)]  #pop(0) pops the first item off
//            if cur not in ind:
//                # So we know the fact doesn't exist
//                if key:
//                    ind[cur] = Memory()  # just a dictionary -- intended for sorted join 
//                    return self.index(ind[cur], bindings, key, factAdded)
//                else: # At the bottom, and the fact still doesn't exist
//                    # Create justification set, used for proof tracing, and stick it
//                    # as the inner most value in the memory
//                    pt = ProofTrace()
//                    pt.addPremise(factAdded)
//                    ind[cur] = tuple(pt)   
//                    return True #it was added
//            else:
//                if key: # Perhaps the fact does exist
//                    return self.index(ind[cur], bindings, key, factAdded)
//                else:
//                    # It definitely exists.
//                    return False
//
//    def __repr__(self):
//        return """AlphaNode(%s)(Mem: %s)""" %(str(self.pattern), str(self.ind))
}
