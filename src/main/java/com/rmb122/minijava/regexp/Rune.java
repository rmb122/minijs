package com.rmb122.minijava.regexp;

public class Rune {
    public static Rune ANY_CHAR = new Rune("ANY");
    public static Rune EMPTY_CHAR = new Rune("EMPTY");

    char c;
    boolean unique;
    String comment;

    private Rune(String comment) {
        this.unique = true;
        this.comment = comment;
    }

    public Rune(char c) {
        this.unique = false;
        this.c = c;
    }

    @Override
    public int hashCode() {
        if (this.unique) {
            return super.hashCode();
        } else {
            return this.c;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Rune r && ((!this.unique && r.c == this.c) || (this == obj));
    }

    @Override
    public String toString() {
        if (!this.unique) {
            return String.valueOf(this.c);
        } else {
            return this.comment;
        }
    }
}
