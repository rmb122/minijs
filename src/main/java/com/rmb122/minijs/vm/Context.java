package com.rmb122.minijs.vm;

import com.rmb122.minijs.vm.object.JBaseObject;

import java.util.HashMap;
import java.util.Stack;

public class Context {
    public HashMap<String, JBaseObject> globalThis = new HashMap<>();
    public Stack<Frame> callStack = new Stack<>();
    public Frame currFrame;
}
