package com.rmb122.minijs.parser;

public class ParserAction {
    public enum ParserActionType {
        REDUCE,
        SHIFT,
        ACCEPT,
    }

    public static ParserAction PARSER_ACTION_ACCEPT = new ParserAction(ParserActionType.ACCEPT);

    ParserActionType type;
    Production reduceProduction;
    DFAState gotoState;

    public ParserAction(Production reduceProduction) {
        this.type = ParserActionType.REDUCE;
        this.reduceProduction = reduceProduction;
    }

    public ParserAction(DFAState gotoState) {
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
