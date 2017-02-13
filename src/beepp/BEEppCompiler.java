package beepp;

import beepp.expression.BinaryIntegerOperation;
import beepp.expression.BooleanExpression;
import beepp.expression.Variable;
import beepp.parser.BEEppLexer;
import beepp.parser.BEEppParser;
import beepp.util.Pair;
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Moklev Vyacheslav
 */
public class BEEppCompiler {
//    public static Pair<List<BooleanExpression>, List<String>> compile(InputStream source, OutputStream destination) throws IOException {
//        StaticStorage.resetVarCounter();
//        StaticStorage.vars = new HashMap<>();
//        ANTLRInputStream inputStream = new ANTLRInputStream(source);
//        BEEppLexer lexer = new BEEppLexer(inputStream);
//        CommonTokenStream tokens = new CommonTokenStream(lexer);
//        BEEppParser parser = new BEEppParser(tokens);
//        BEEppParser.FileContext file = parser.file();
//        Pair<Map<String, Variable>, List<BooleanExpression>> model = file.model;
//
//        PrintWriter pw = new PrintWriter(destination);
//        model.a.values().forEach(variable -> pw.println(variable.getDeclaration()));
//        model.b.forEach(booleanExpression -> pw.println(booleanExpression.holds()));
//        pw.println("solve satisfy");
//        pw.flush();
//        return new Pair<>(model.b, file.text);
//    }
    
    public static void fastCompile(InputStream source, OutputStream destination) throws IOException {
        StaticStorage.resetVarCounter();
        StaticStorage.vars = new HashMap<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(source));
        PrintWriter pw = new PrintWriter(destination);
        br.lines().forEach(line -> {
            if (line.trim().startsWith("//"))
                return;
            ANTLRInputStream inputStream = new ANTLRInputStream(line);
            BEEppLexer lexer = new BEEppLexer(inputStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            BEEppParser parser = new BEEppParser(tokens);

            BEEppParser.LineContext ctx = parser.line();
            if (ctx.variable != null) {
                pw.println(ctx.variable.getDeclaration());
                StaticStorage.vars.put(ctx.variable.getName(), ctx.variable);
            } else {
                pw.println(ctx.expr.holds());
            }
        });
        pw.println("solve satisfy");
        pw.flush();
    }

    public static void main(String[] args) throws IOException {
        fastCompile(new FileInputStream("bigsample.beepp"), new FileOutputStream("out.bee"));
    }
}
