package beepp;

import beepp.expression.BinaryIntegerOperation;
import beepp.expression.BooleanExpression;
import beepp.expression.Variable;
import beepp.parser.BEEppLexer;
import beepp.parser.BEEppParser;
import beepp.util.Pair;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @author Moklev Vyacheslav
 */
public class BEEppCompiler {
    public static List<BooleanExpression> compile(InputStream source, OutputStream destination) throws IOException {
        StaticStorage.lastTempVar = 0;
        ANTLRInputStream inputStream = new ANTLRInputStream(source);
        BEEppLexer lexer = new BEEppLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BEEppParser parser = new BEEppParser(tokens);
        Pair<Map<String, Variable>, List<BooleanExpression>> model = parser.file().model;

        PrintWriter pw = new PrintWriter(destination);
        model.a.values().forEach(variable -> pw.println(variable.getDeclaration()));
        model.b.forEach(booleanExpression -> pw.println(booleanExpression.holds()));
        pw.println("solve satisfy");
        pw.flush();
        return model.b;
    }
}
