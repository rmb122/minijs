package com.rmb122.minijava.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class DFAState {
    int id;
    List<LR1Production> lr1Productions = new ArrayList<>();
    private final HashMap<Symbol, DFAState> edges = new HashMap<>();

    public DFAState(int id) {
        this.id = id;
    }

    public void addLR1Production(LR1Production lr1Production) {
        // 找相同的产生式进行合并
        List<LR1Production> sameProduction = LR1Production.findProduction(lr1Productions, lr1Production.production, lr1Production.index);
        if (sameProduction.size() == 1) {
            sameProduction.get(0).lookaheadSymbols.addAll(lr1Production.lookaheadSymbols);
        } else if (sameProduction.size() == 0) {
            this.lr1Productions.add(lr1Production);
        } else {
            throw new RuntimeException("duplicate lr1Production found, this should not happen");
        }
    }

    public void addNextState(Symbol symbol, DFAState state) throws RuntimeException {
        if (edges.get(symbol) != null) {
            throw new RuntimeException("when transforming NFA to DFA, duplicate edge found");
        }
        edges.put(symbol, state);
    }

    public DFAState getNextState(Symbol s) {
        return edges.get(s);
    }

    public Set<Symbol> getEdgeKeys() {
        return this.edges.keySet();
    }

    public HashMap<Symbol, DFAState> getEdges() {
        return this.edges;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DFAState dfaState && this.id == dfaState.id;
    }
}