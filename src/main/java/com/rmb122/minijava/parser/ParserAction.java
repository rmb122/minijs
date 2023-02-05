package com.rmb122.minijava.parser;

public class ParserAction {
    public enum ParserActionType {
        REDUCE,
        SHIFT,
        ACCEPT,
    }

    public static ParserAction ACCEPT_PARSER_ACTION = new ParserAction(ParserActionType.ACCEPT);

    ParserActionType type;
    Production reduceProduction;
    DFA.State gotoState;

    public ParserAction(Production reduceProduction) {
        this.type = ParserActionType.REDUCE;
        this.reduceProduction = reduceProduction;
    }

    public ParserAction(DFA.State gotoState) {
        this.type = ParserActionType.SHIFT;
        this.gotoState = gotoState;
    }

    private ParserAction(ParserActionType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        switch (this.type) {
            case REDUCE -> {
                return this.reduceProduction.toString();
            }
            case SHIFT -> {
                return "S" + this.gotoState.id;
            }
            case ACCEPT -> {
                return "ACC";
            }
        }

        throw new RuntimeException("unexpected parser action type");
    }
}
