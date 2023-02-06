package com.rmb122.minijs.regexp;

import java.util.Set;

public class RegexpMatchResult<C> {
    public Set<C> containerSet;
    public int length;

    public RegexpMatchResult(Set<C> containerSet, int length) {
        this.containerSet = containerSet;
        this.length = length;
    }
}
