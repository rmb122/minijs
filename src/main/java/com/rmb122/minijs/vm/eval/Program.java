package com.rmb122.minijs.vm.eval;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.object.JBaseObject;
import com.rmb122.minijs.vm.object.JFunction;

import java.util.HashMap;

public class Program {
    private final HashMap<String, JFunction> functions = new HashMap<>();

    public void eval(Context context) {

    }

    public void addFunction(String name, JFunction function) {
        functions.put(name, function);
    }
}
