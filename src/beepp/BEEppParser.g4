parser grammar BEEppParser;

options {tokenVocab = BEEppLexer;}

variableDefinition
    :   'int' ID ':' domain
    |   'dual_int' ID ':' domain
    |   'bool' ID
    ;

domain
    :   range (',' range)*
    ;

range
    :   INT_CONST '..' INT_CONST
    ;

intExpr
    :   intPrimary
    |   ('min' | 'max') '(' intExprList? ')'
    |   '-' intExpr
    |   intExpr
        (   '*'
        |   '/'
        |   '%'
        ) intExpr
    |   intExpr
        (   '+'
        |   '-'
        ) intExpr
    ;

boolExpr
    :   boolPrimary
    |   intExpr
        (  '<='
        |  '>='
        |  '>'
        |  '<'
        |  '='
        |  '!='
        ) intExpr
        |   boolExpr '&' boolExpr
    |   boolExpr '^' boolExpr
    |   boolExpr '|' boolExpr
    |   boolExpr '->' boolExpr
    // |   <assoc=right> e1=expr '?' e2=expr ':' e3=expr // TODO implement
    ;

intPrimary
    :   '(' intExpr ')'
    |   INT_CONST
    |   ID
    ;

boolPrimary
    :   '(' boolExpr ')'
    |   BOOL_CONST
    |   ID
    ;

intExprList
    :   intExpr (',' intExpr)*
    ;