package com.rmb122.minijs.vm;

import com.rmb122.minijs.vm.eval.*;
import com.rmb122.minijs.vm.object.JBaseObject;
import com.rmb122.minijs.vm.object.JUndefine;

import java.util.EmptyStackException;
import java.util.Stack;

public class VM {
    private enum StmtType {
        PLAIN,
        LOOP,
    }

    private static class VMStatus {
        private final StmtList stmts;
        private int pc;
        private final StmtType type;
        private final LoopStmt loopStmt;

        public VMStatus(StmtList stmts) {
            this.stmts = stmts;
            this.pc = 0;
            this.type = StmtType.PLAIN;
            this.loopStmt = null;
        }

        public VMStatus(LoopStmt loopStmt) {
            this.stmts = loopStmt.getBodyStmts();
            this.pc = 0;
            this.type = StmtType.LOOP;
            this.loopStmt = loopStmt;
        }

        public boolean isStart() {
            return this.pc == 0;
        }

        public boolean isEnd() {
            return this.pc == this.stmts.size();
        }

        public void gotoEnd() {
            this.pc = this.stmts.size();
        }

        public Stmt nextStmt() {
            return stmts.getStmt(this.pc++);
        }

        public void restart() {
            this.pc = 0;
        }

        public boolean isLoop() {
            return type == StmtType.LOOP;
        }

        public LoopStmt getLoopStmt() {
            return loopStmt;
        }
    }

    private static VMStatus popUntilLoop(Stack<VMStatus> vmStack) {
        while (!vmStack.peek().isLoop()) {
            vmStack.pop();
        }
        return vmStack.peek();
    }

    public static JBaseObject run(Context context, StmtList stmts) throws JException {
        Stack<VMStatus> vmStack = new Stack<>();
        vmStack.push(new VMStatus(stmts));

        while (!vmStack.isEmpty()) {
            VMStatus currStatus = vmStack.peek();

            if (currStatus.isStart() && currStatus.isLoop() && currStatus.getLoopStmt().stopped(context)) {
                vmStack.pop();
                continue;
            }

            if (currStatus.isEnd()) {
                if (currStatus.isLoop()) {
                    currStatus.getLoopStmt().restart(context);
                    currStatus.restart();
                } else {
                    vmStack.pop();
                }
                continue;
            }

            Stmt currStmt = currStatus.nextStmt();
            if (currStmt instanceof IfStmt ifStmt) {
                vmStack.push(new VMStatus(ifStmt.getBodyStmts(context)));
            } else if (currStmt instanceof LoopStmt loopStmt) {
                loopStmt.init(context);
                vmStack.push(new VMStatus(loopStmt));
            } else if (currStmt instanceof BreakStmt) {
                try {
                    popUntilLoop(vmStack);
                } catch (EmptyStackException e) {
                    throw new JException("Illegal break statement");
                }
                vmStack.pop();
            } else if (currStmt instanceof ContinueStmt) {
                try {
                    currStatus = popUntilLoop(vmStack);
                } catch (EmptyStackException e) {
                    throw new JException("Illegal continue statement");
                }
                currStatus.gotoEnd();
            } else if (currStmt instanceof ReturnStmt returnStmt) {
                return returnStmt.getExpr().eval(context);
            } else if (currStmt instanceof ExprStmt exprStmt) {
                exprStmt.eval(context);
            } else if (currStmt instanceof VariableDefinitionStmt variableDefinitionStmt) {
                variableDefinitionStmt.eval(context);
            } else {
                throw new JException("Invalid statement");
            }
        }

        return JUndefine.UNDEFINE;
    }
}
