package com.rmb122.minijs.vm.object;

import com.rmb122.minijs.vm.Context;

import java.util.List;
import java.util.function.BiFunction;

public class JNativeFunction extends JObject {
    String name;
    BiFunction<Context, List<JBaseObject>, JBaseObject> function;

    public JNativeFunction(String name, BiFunction<Context, List<JBaseObject>, JBaseObject> function) {
        this.name = name;
        this.function = function;
    }

    @Override
    public JBaseObject call(Context context, JBaseObject thisObject, List<JBaseObject> args) {
        context.pushFrame(thisObject);
        JBaseObject returnObject = this.function.apply(context, args);
        context.popFrame();
        return returnObject;
    }

    @Override
    public JString toJString() {
        return new JString(String.format("function %s() { [native code] }", this.name));
    }
}
