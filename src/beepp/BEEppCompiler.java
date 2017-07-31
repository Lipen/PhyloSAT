package beepp;

import beepp.parser.BEEppLexer;
import beepp.parser.BEEppParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.io.*;
import java.util.HashMap;

/**
 * @author Moklev Vyacheslav
 */
public class BEEppCompiler {

    public static void fastCompile(String formula, OutputStream destination) throws IOException {
        StaticStorage.resetVarCounter();
        StaticStorage.vars = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new StringReader(formula));
             PrintWriter pw = new PrintWriter(destination)
        ) {
            br.lines().forEach(line -> {
                // if (line.trim().startsWith("//"))
                //     return;
                CharStream inputStream = CharStreams.fromString(line);
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
                }
            });

            pw.println("solve satisfy");
            pw.flush();
        }
    }
}
