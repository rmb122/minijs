package com.rmb122.minijs.vm.eval;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;
import com.rmb122.minijs.vm.object.JBaseObject;

public class MemberOperator extends LeftHandOperator {
    private final Expr member;

    public MemberOperator(Expr member) {
        this.member = member;
    }

    public JBaseObject eval(Context context) throws JException {
        return member.eval(context);
    }
}
