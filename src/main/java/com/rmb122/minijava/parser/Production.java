package com.rmb122.minijava.parser;

import java.util.Arrays;
import java.util.List;

public class Production {
    Symbol head;
    List<Symbol> body;

    public Production(Symbolize head, List<Symbolize> body) {
        this.head = head.asSymbol();
        this.body = body.stream().map(Symbolize::asSymbol).toList();
    }

    public Production(Symbolize head, Symbolize... body) {
        this.head = head.asSymbol();
        this.body = Arrays.stream(body).map(Symbolize::asSymbol).toList();
    }

    @Override
    public int hashCode() {
        return this.head.hashCode() + this.body.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Production p && this.head.equals(p.head) && this.body.equals(p.body);
    }

    @Override
    public String toString() {
        return this.head.toString() + " -> " + body.toString();
    }
}
