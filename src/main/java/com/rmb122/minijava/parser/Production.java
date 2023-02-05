package com.rmb122.minijava.parser;

import java.util.List;

public class Production {
    Symbol head;
    List<Symbol> body;

    public Production(Symbol head, List<Symbol> body) {
        this.head = head;
        this.body = body;
    }

    public Production(Symbol head, Symbol... body) {
        this.head = head;
        this.body = List.of(body);
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
