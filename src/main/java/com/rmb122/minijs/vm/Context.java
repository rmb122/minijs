package com.rmb122.minijs.vm;

import com.rmb122.minijs.vm.object.JBaseObject;
import com.rmb122.minijs.vm.object.JObject;
import com.rmb122.minijs.vm.object.JString;

import java.util.Stack;

public class Context {
    private final JObject globalThis;
    private final Stack<Frame> callStack = new Stack<>();
    private Frame currFrame;

    public Context() {
        this.globalThis = new JObject();
        this.currFrame = new Frame(this.globalThis);
    }

    public JObject getGlobalThis() {
        return this.globalThis;
    }

    public JBaseObject getThis() {
        return this.currFrame.thisObject;
    }

    public void pushFrame(JBaseObject thisObject) {
        this.callStack.push(this.currFrame);
        this.currFrame = new Frame(thisObject);
    }

    public void popFrame() {
        this.currFrame = this.callStack.pop();
    }

    public boolean variableExistsInLocal(String name) {
        return this.currFrame.variables.containsKey(name);
    }

    public void setGlobalVariable(String name, JBaseObject value) {
        try {
            this.globalThis.set(new JString(name), value);
        } catch (JException e) {
            throw new RuntimeException("this error should not happened");
        }
    }

    public void removeGlobalVariable(String name) {
        this.globalThis.remove(new JString(name));
    }

    public void setLocalVariable(String name, JBaseObject value) {
        this.currFrame.variables.put(name, value);
    }

    public void removeLocalVariable(String name) {
        this.currFrame.variables.remove(name);
    }

    // 如果变量存在在 local, 在 local scope 赋值, 否则在 global 赋值
    public void setVariable(String name, JBaseObject value) {
        if (variableExistsInLocal(name)) {
            this.setLocalVariable(name, value);
        } else {
            this.setGlobalVariable(name, value);
        }
    }

    public JBaseObject getVariable(String name) {
        if (currFrame.variables.containsKey(name)) {
            return currFrame.variables.get(name);
        } else {
            return globalThis.get(new JString(name));
        }
    }
}
