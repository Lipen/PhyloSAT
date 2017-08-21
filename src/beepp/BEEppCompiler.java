package beepp;

import beepp.parser.BEEppLexer;
import beepp.parser.BEEppParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Moklev Vyacheslav
 */
public class BEEppCompiler {
    public static synchronized boolean fastCompile(List<String> clauses, String filename, int numberOfSolutions) {
        StaticStorage.resetVarCounter();
        StaticStorage.vars = new HashMap<>();
        try (PrintWriter pw = new PrintWriter(filename)
        ) {
            for (String clause : clauses) {
                CharStream inputStream = CharStreams.fromString(clause);
                BEEppLexer lexer = new BEEppLexer(inputStream);
                TokenStream tokens = new CommonTokenStream(lexer);
                BEEppParser parser = new BEEppParser(tokens);

                BEEppParser.LineContext ctx = parser.line();
                if (ctx.variable != null) {
                    pw.println(ctx.variable.getDeclaration());
                    StaticStorage.vars.put(ctx.variable.getName(), ctx.variable);
                } else if (ctx.expr != null) {
                    pw.println(ctx.expr.holds());
                } else if (ctx.comment != null) {
                    String comment = "% " + ctx.comment.replaceFirst("^//\\s*(.*)", "$1");
                    pw.println(comment);
                    // System.out.println("Adding comment <" + comment + ">");
                } else if (ctx.explicit != null) {
                    String explicit = ctx.explicit.replaceFirst("^@\\s*(.*)", "$1");
                    pw.println(explicit);
                } else {
                    System.err.printf("[!] Couldn't parse: %s%n", clause);
                }
            }

            if (numberOfSolutions == 1)
                pw.println("solve satisfy");
            else
                pw.printf("solve satisfy(%d)\n", numberOfSolutions);
            pw.flush();
        } catch (FileNotFoundException e) {
            System.err.println("[!] Couldn't open <" + filename + ">: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static void main(String... argv) {
        if (argv.length < 1)
            throw new RuntimeException("Please, pass path to file with bee++ formula");
        String filename = argv[0];
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            System.out.printf("[*] Reading formula from <%s>...%n", filename);
            List<String> clauses = br.lines().collect(Collectors.toList());
            System.out.printf("[*] Compiling formula (%d clauses)...%n", clauses.size());
            fastCompile(clauses, "out.bee", 1);
            System.out.println("[+] OK");
        } catch (FileNotFoundException e) {
            System.err.println("[!] No such file: " + filename);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("[!] So sad: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
