parser grammar BEEppParser;

options {tokenVocab = BEEppLexer;}

@header {
    import beepp.util.*;
    import beepp.util.Pair;
    import beepp.expression.*;
    import beepp.StaticStorage;
    import java.util.Map;
    import java.util.HashMap;
}

@members {
    List<BooleanExpression> constraints = new ArrayList<>();
    List<String> constraintsText = new ArrayList<>();
}

file returns [Pair<Map<String, Variable>, List<BooleanExpression>> model, List<String> text]
    :   (variableDefinition | boolExpr {
            constraints.add($boolExpr.expr);
            constraintsText.add($boolExpr.text);
        })*
        {
            $model = new Pair<>(StaticStorage.vars, constraints);
            $text = constraintsText;
        }
    ;

variableDefinition
    :   'int' ID ':' domain {StaticStorage.vars.put($ID.text, new IntegerVariable($ID.text, $domain.dom));}
    |   'dual_int' ID ':' domain {StaticStorage.vars.put($ID.text, new IntegerVariable($ID.text, $domain.dom, true));}
    |   'bool' ID {StaticStorage.vars.put($ID.text, new BooleanVariable($ID.text));}
    ;

domain returns [RangeUnion dom]
    :   r1=range {$dom = new RangeUnion($r1.r.a, $r1.r.b);}
        (',' rc=range {$dom.addRange($rc.r.a, $rc.r.b);})*
    ;

range returns [Pair<Integer, Integer> r]
    :   left=INT_CONST '..' right=INT_CONST
        {$r = new Pair<>(Integer.valueOf($left.text), Integer.valueOf($right.text));}
    ;

boolExpr returns [BooleanExpression expr] locals [String op]
    :   boolPrimary {$expr = $boolPrimary.expr;}
    |   ('AMO' {$op = "AMO";}) '(' boolExprList? ')'
        {$expr = new AtMostOneOperation($boolExprList.list);} // TODO check if boolExprList present
    |   i1=intExpr
        (  '<=' {$op = "leq";}
        |  '>=' {$op = "geq";}
        |  '>'  {$op = "gt";}
        |  '<'  {$op = "lt";}
        |  '='  {$op = "eq";}
        |  '!=' {$op = "neq";}
        ) i2=intExpr {$expr = new BinaryIntBooleanOperation($op, $i1.expr, $i2.expr);}
    |   e1=boolExpr '&' e2=boolExpr   {$expr = $e1.expr.and($e2.expr);}
    |   e1=boolExpr '^' e2=boolExpr   {$expr = $e1.expr.xor($e2.expr);}
    |   e1=boolExpr '|' e2=boolExpr   {$expr = $e1.expr.or($e2.expr);}
    |   e1=boolExpr '<=>' e2=boolExpr {$expr = $e1.expr.iff($e2.expr);}
    |   e1=boolExpr '->' e2=boolExpr  {$expr = $e1.expr.then($e2.expr);}
    // |   <assoc=right> e1=expr '?' e2=expr ':' e3=expr // TODO implement
    ;

boolPrimary returns [BooleanExpression expr]
    :   '(' boolExpr ')' {$expr = $boolExpr.expr;}
    |   BOOL_CONST {$expr = BooleanConstant.valueOf($BOOL_CONST.text.toUpperCase());}
    |   ID {
              $expr = (BooleanExpression) StaticStorage.vars.get($ID.text);
              if ($expr == null)
                  throw new IllegalArgumentException("Variable was not declared: " + $ID.text);
           }
    |   '!' boolPrimary { $expr = new NegateBooleanExpression($boolPrimary.expr); }
    ;

boolExprList returns [List<BooleanExpression> list]
    :   {$list = new ArrayList<>();} b1=boolExpr {$list.add($b1.expr);}
        (',' bc=boolExpr {$list.add($bc.expr);})*
    ;

intExpr returns [IntegerExpression expr] locals [String op]
    :   intPrimary {$expr = $intPrimary.expr;}
    // |   ('min' {$op = "min";} | 'max' {$op = "max";}) '(' intExprList? ')' TODO implement
    |   e1=intExpr
        (   '*' {$op = "times";}
        |   '/' {$op = "div";}
        |   '%' {$op = "mod";}
        ) e2=intExpr {$expr = new BinaryIntegerOperation($op, $e1.expr, $e2.expr);}
    |   e1=intExpr
        (   '+' {$op = "plus";}
        |   '-' {$op = "minus";}
        ) e2=intExpr {
            switch ($op) {
                case "plus":
                    $expr = $e1.expr.plus($e2.expr);
                    break;
                case "minus":
                    $expr = $e1.expr.plus(new NegateExpression($e2.expr));
                    break;
                default:
                    assert false;
                    $expr = null;
            }
        }
    ;

intPrimary returns [IntegerExpression expr]
    :   '(' intExpr ')' {$expr = $intExpr.expr;}
    |   INT_CONST {$expr = new IntegerConstant(Integer.parseInt($INT_CONST.text));}
    |   ID {
               $expr = (IntegerExpression) StaticStorage.vars.get($ID.text);
               if ($expr == null)
                   throw new IllegalArgumentException("Variable was not declared: " + $ID.text);
           }
    |   '-' intPrimary {$expr = new NegateExpression($intPrimary.expr);}
    ;

intExprList returns [List<IntegerExpression> list]
    :   {$list = new ArrayList<>();} i1=intExpr {$list.add($i1.expr);}
        (',' ic=intExpr {$list.add($ic.expr);})*
    ;