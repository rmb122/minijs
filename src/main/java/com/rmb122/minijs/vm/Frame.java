package com.rmb122.minijs.vm;

import com.rmb122.minijs.vm.object.JBaseObject;

import java.util.HashMap;

public class Frame {
    public JBaseObject thisObject;
    public HashMap<String, JBaseObject> variables = new HashMap<>();
}
