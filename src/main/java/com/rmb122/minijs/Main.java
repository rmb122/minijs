package com.rmb122.minijs;

import com.rmb122.minijs.lexer.LexerError;
import com.rmb122.minijs.parser.AST;
import com.rmb122.minijs.parser.ParserError;
import com.rmb122.minijs.syntax.MiniJS;
import com.rmb122.minijs.vm.JException;
import com.rmb122.minijs.vm.Program;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws ParserError, LexerError, JException {
        if (args.length == 0) {
            System.out.print("""
                    Usage: java -jar minijs.jar script.js
                    """);
            System.exit(-1);
        }

        try {
            String scriptContent = new String(Files.readAllBytes(Path.of(args[0])));
            AST ast = MiniJS.parse(scriptContent);
            ((Program) ast.getCustom()).eval();
        } catch (IOException e) {
            System.out.printf("Script file %s, read error\n", args[0]);
            e.printStackTrace();
        }
    }
}
