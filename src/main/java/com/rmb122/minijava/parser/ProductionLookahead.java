package com.rmb122.minijava.parser;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ProductionLookahead {
    Production production;
    // index = 0 等于开始, = production.body.size 为结束
    int index;
    HashSet<Symbol> lookaheadSymbols;

    public ProductionLookahead(Production production, int index) {
        this.production = production;
        this.index = index;
        this.lookaheadSymbols = new HashSet<>();
    }

    public ProductionLookahead copy() {
        return new ProductionLookahead(this.production, this.index);
    }

    @Override
    public String toString() {
        return production.toString() + "@" + index + ":" + lookaheadSymbols.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(production, index, lookaheadSymbols);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ProductionLookahead p
                && this.production.equals(p.production)
                && this.index == p.index
                && this.lookaheadSymbols.equals(p.lookaheadSymbols);
    }

    public static Set<ProductionLookahead> findFinishedProduction(Set<ProductionLookahead> productionLookaheads) {
        HashSet<ProductionLookahead> finishedProduction = new HashSet<>();
        for (ProductionLookahead productionLookahead : productionLookaheads) {
            if (productionLookahead.index == productionLookahead.production.body.size()) {
                finishedProduction.add(productionLookahead);
            }
        }
        return finishedProduction;
    }

    public static Set<ProductionLookahead> findStartProduction(Set<ProductionLookahead> productionLookaheads) {
        HashSet<ProductionLookahead> startedProduction = new HashSet<>();
        for (ProductionLookahead productionLookahead : productionLookaheads) {
            if (productionLookahead.index == 0) {
                startedProduction.add(productionLookahead);
            }
        }
        return startedProduction;
    }

    public static Set<ProductionLookahead> findProduction(Set<ProductionLookahead> productionLookaheads, Production production, int index) {
        HashSet<ProductionLookahead> targetProduction = new HashSet<>();
        for (ProductionLookahead productionLookahead : productionLookaheads) {
            if (productionLookahead.production.equals(production) && productionLookahead.index == index) {
                targetProduction.add(productionLookahead);
            }
        }
        return targetProduction;
    }
}
