package com.rmb122.minijs.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class LR1Production {
    Production production;
    // index = 0 等于开始, = production.body.size 为结束
    int index;
    HashSet<Symbol> lookaheadSymbols;

    public LR1Production(Production production, int index, HashSet<Symbol> lookaheadSymbols) {
        this.production = production;
        this.index = index;
        this.lookaheadSymbols = lookaheadSymbols;
    }

    public LR1Production copy() {
        return new LR1Production(production, index, new HashSet<>(lookaheadSymbols));
    }

    public boolean isStart() {
        return this.index == 0;
    }

    public boolean isFinished() {
        return this.index == this.production.body.size();
    }

    public Symbol currSymbol() {
        return this.production.body.get(this.index);
    }

    public Symbol getSymbol(int pos) {
        return this.production.body.get(pos);
    }

    public List<Symbol> body() {
        return this.production.body;
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
        return obj instanceof LR1Production p
                && this.production.equals(p.production)
                && this.index == p.index
                && this.lookaheadSymbols.equals(p.lookaheadSymbols);
    }

    public static List<LR1Production> findFinishedProduction(List<LR1Production> LR1Productions) {
        ArrayList<LR1Production> finishedProduction = new ArrayList<>();
        for (LR1Production lr1Production : LR1Productions) {
            if (lr1Production.isFinished()) {
                finishedProduction.add(lr1Production);
            }
        }
        return finishedProduction;
    }

    public static List<LR1Production> findStartProduction(List<LR1Production> LR1Productions) {
        ArrayList<LR1Production> startedProduction = new ArrayList<>();
        for (LR1Production lr1Production : LR1Productions) {
            if (lr1Production.isStart()) {
                startedProduction.add(lr1Production);
            }
        }
        return startedProduction;
    }

    public static List<LR1Production> findProduction(List<LR1Production> LR1Productions, Production production, int index) {
        ArrayList<LR1Production> targetProduction = new ArrayList<>();
        for (LR1Production lr1Production : LR1Productions) {
            if (lr1Production.production.equals(production) && lr1Production.index == index) {
                targetProduction.add(lr1Production);
            }
        }
        return targetProduction;
    }
}
